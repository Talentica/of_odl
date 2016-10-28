package com.talentica.sdn.odlswitch.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.talentica.sdn.odlswitch.impl.utils.CommonUtils;
import com.talentica.sdn.odlswitch.impl.utils.Constants;

/**
 * @author narenderK
 *
 */
public class CapFlux implements AutoCloseable, PacketProcessingListener{
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private List<Registration> registrations;
	private DataBroker dataBroker;
	private PacketProcessingService packetProcessingService;
	private RpcRegistration<ConnectService> connectService;	
	private static List<String> savedMacs = new ArrayList<>();
	private static Map<String, String> macToIpMap = new HashMap<>();
    private FlowEngine flowEngine = new FlowEngine();
    private MeterEngine meterEngine = new MeterEngine();
    private AuthenticationEngine authenticationEngine = new AuthenticationEngine();
    private static Map<String, Boolean> edgeRuleMacFlags = new HashMap<>();
    private static Map<NodeId,Map<String, NodeConnectorRef>> rules = new HashMap<>();
	
    /**
     * 
     * @param dataBroker
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
	public CapFlux(DataBroker dataBroker, NotificationProviderService notificationProviderService, RpcProviderRegistry rpcProviderRegistry){
		this.dataBroker = dataBroker;
		this.packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);
		this.connectService = rpcProviderRegistry.addRpcImplementation(ConnectService.class, new ConnectionImpl());
		this.registrations = Lists.newArrayList();
		registrations.add(notificationProviderService.registerNotificationListener(this));
		savedMacs.add("00:00:00:00:00:01");
		savedMacs.add("00:00:00:00:00:02");
		savedMacs.add("00:00:00:00:00:09");
	}
	
	public static Map<String, String> getMacToIpMap() {
		return macToIpMap;
	}

	public static void setMacToIpMap(Map<String, String> macToIpMap) {
		CapFlux.macToIpMap = macToIpMap;
	}	

	@Override
	public void close() throws Exception {
		for(Registration registration : registrations){
			registration.close();
		}
		registrations.clear();		
	}
	
	@Override
	public void onPacketReceived(PacketReceived notification) {
		log.trace("notification receicved for ", notification.getMatch());
	    byte[] payload = notification.getPayload();
		NodeConnectorRef ingressNodeConnectorRef = notification.getIngress();
		NodeConnectorId ingressNodeConnectorId = CommonUtils.getNodeConnectorRef(ingressNodeConnectorRef);
		NodeId ingressNodeId = CommonUtils.getNodeId(ingressNodeConnectorId);		
		byte[] etherTypeRaw = CommonUtils.extractEtherType(notification.getPayload());
		// Ignore LLDP packets
		int etherType = 0x0000ffff & ByteBuffer.wrap(etherTypeRaw).getShort();
		if (etherType == 0x88cc) {
			return;
		}
		try {
			// ARP packets lets them flood
			if (etherType == 0x0806) {
				this.flowEngine.programFloodARPFlow(this.dataBroker, ingressNodeId);
				return;
			}

			// IGNORE NON-IP TRAFFIC
			if (etherType != 0x0800) {
				return;
			}

			// Parse packet
			byte[] rawDstMac = CommonUtils.extractDstMac(payload);
			byte[] rawSrcMac = CommonUtils.extractSrcMac(payload);
			String srcMac = CommonUtils.rawMacToString(rawSrcMac);
			String dstMac = CommonUtils.rawMacToString(rawDstMac);
			byte[] rawDstIP = CommonUtils.extractDstIP(payload);
			byte[] rawSrcIP = CommonUtils.extractSrcIP(payload);
			String dstIP = CommonUtils.rawIPToString(rawDstIP);
			String srcIP = CommonUtils.rawIPToString(rawSrcIP);
			byte[] rawDstPort = CommonUtils.extractDstPort(payload);
			int dstPort = CommonUtils.rawPortToInteger(rawDstPort);
			
			if(!rules.containsKey(ingressNodeId)){
				Map<String, NodeConnectorRef> macTable = new HashMap<>();
				rules.put(ingressNodeId, macTable);
			}
			
			if(!rules.get(ingressNodeId).containsKey(srcMac)){
				rules.get(ingressNodeId).put(srcMac, ingressNodeConnectorRef);
			}

			if (!macToIpMap.containsKey(srcIP)) {
				macToIpMap.put(srcIP, srcMac);
			}

			String role = authenticationEngine.getSrcMacRole(srcMac);
			this.meterEngine.createGuestMeter(dataBroker, ingressNodeId);
			this.meterEngine.createUserMeter(dataBroker, ingressNodeId);
			
			NodeConnectorRef egressNodeConnectorRef = rules.get(ingressNodeId).get(dstMac);
			Uri ingressOutputPort = new Uri(Constants.OPENFLOW_FORWARDING_ACTION_FLOOD);
			Uri egressOutputPort = new Uri(Constants.OPENFLOW_FORWARDING_ACTION_FLOOD);
			if(egressNodeConnectorRef!=null){
				ingressOutputPort = new Uri(CommonUtils.getNodeConnectorRef(ingressNodeConnectorRef));
				egressOutputPort = new Uri(CommonUtils.getNodeConnectorRef(egressNodeConnectorRef));
			}
			
			if (dstMac.equalsIgnoreCase(Constants.CAPTIVE_PORTAL_MAC)) {
				programL2Flows(ingressNodeId, ingressOutputPort, egressOutputPort, srcMac, dstMac, role);

			} else if (authenticationEngine.isMacRegistered(srcMac) && authenticationEngine.isMacRegistered(dstMac)) {
				programL2Flows(ingressNodeId, ingressOutputPort, egressOutputPort, srcMac, dstMac, role);
			}

			else {
				// adds the user to the database and the user’s state is
				// unauthenticated
				if (!savedMacs.contains(srcMac)) {
					boolean isSaved = authenticationEngine.saveUnauthUser(srcIP, srcMac);
					if (isSaved) {
						savedMacs.add(srcMac);
					}
				}
				// redirection rules must be installed on edge switches only
				edgeRuleMacFlags.put(srcMac, false);
				// add redirection flows
				if (dstPort == 80 && !edgeRuleMacFlags.get(srcMac)) {
					this.flowEngine.addReverseflow(this.dataBroker, ingressNodeId, ingressOutputPort, srcMac, dstMac,srcIP, dstIP, dstPort);
					this.flowEngine.addforwardflow(this.dataBroker, ingressNodeId, egressOutputPort, srcMac, dstMac,srcIP, dstIP, dstPort);
					edgeRuleMacFlags.put(srcMac, true);
				}
			}
		}catch(Exception e){
			log.error("Exception occured:: ", e);
		}
	}

	private void programL2Flows(NodeId ingressNodeId, Uri ingressOutputPort, Uri egressOutputPort, String srcMac, String dstMac, String role) throws Exception {
		this.flowEngine.programL2Flow(this.dataBroker, ingressNodeId, egressOutputPort, srcMac, dstMac, role);
		this.flowEngine.programL2Flow(this.dataBroker, ingressNodeId, ingressOutputPort, dstMac, srcMac, role);
	}
	
}