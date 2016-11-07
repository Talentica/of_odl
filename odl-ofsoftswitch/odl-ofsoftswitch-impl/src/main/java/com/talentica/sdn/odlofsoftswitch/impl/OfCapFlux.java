package com.talentica.sdn.odlofsoftswitch.impl;

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
import com.talentica.sdn.odlcommon.odlutils.engine.AuthenticationEngine;
import com.talentica.sdn.odlcommon.odlutils.to.CapFluxPacket;
import com.talentica.sdn.odlcommon.odlutils.to.User;
import com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.PacketUtils;
import com.talentica.sdn.odlofsoftswitch.impl.engine.FlowEngine;
import com.talentica.sdn.odlofsoftswitch.impl.engine.MeterEngine;
import com.talentica.sdn.odlofsoftswitch.impl.rpc.ConnectionImpl;

/**
 * @author narenderK
 *
 */
public class OfCapFlux implements AutoCloseable, PacketProcessingListener{
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private List<Registration> registrations;
	private DataBroker dataBroker;
	private PacketProcessingService packetProcessingService;
	private RpcRegistration<ConnectService> connectService;	
	private static Map<String, String> macToIpMap = new HashMap<>();
    private static Map<String, Boolean> edgeRuleMacFlags = new HashMap<>();
    private static Map<NodeId,Map<String, NodeConnectorRef>> rules = new HashMap<>();
	
    /**
     * 
     * @param dataBroker
     * @param notificationProviderService
     * @param rpcProviderRegistry
     */
	public OfCapFlux(DataBroker dataBroker, NotificationProviderService notificationProviderService, RpcProviderRegistry rpcProviderRegistry){
		this.dataBroker = dataBroker;
		this.packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);
		this.connectService = rpcProviderRegistry.addRpcImplementation(ConnectService.class, new ConnectionImpl());
		this.registrations = Lists.newArrayList();
		registrations.add(notificationProviderService.registerNotificationListener(this));
	}
	
	public static Map<String, String> getMacToIpMap() {
		return macToIpMap;
	}

	public static void setMacToIpMap(Map<String, String> macToIpMap) {
		OfCapFlux.macToIpMap = macToIpMap;
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
		NodeConnectorId ingressNodeConnectorId = com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils.getNodeConnectorRef(ingressNodeConnectorRef);
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
				FlowEngine.programFloodARPFlow(this.dataBroker, ingressNodeId);
				return;
			}

			// IGNORE NON-IP TRAFFIC
			if (etherType != 0x0800) {
				return;
			}
			
			// Parse packet
			CapFluxPacket packet = PacketUtils.parsePacketFromPayload(payload);
			String srcMac = packet.getSrcMacAddress();
			String dstMac = packet.getDestMacAddress();
			String dstIP = packet.getDestIpAddress();
			String srcIP = packet.getSrcIpAddress();
			int dstPort = packet.getDestTcpPort();
			
			//Map mac to IP for validating the new user request by connectService
			if (!macToIpMap.containsKey(srcIP)) {
				macToIpMap.put(srcIP, srcMac);
			}
			
			//Create Mac Table for the node
			if(!rules.containsKey(ingressNodeId)){
				Map<String, NodeConnectorRef> macTable = new HashMap<>();
				rules.put(ingressNodeId, macTable);
			}
			
			if(!rules.get(ingressNodeId).containsKey(srcMac)){
				rules.get(ingressNodeId).put(srcMac, ingressNodeConnectorRef);
			}
			
			NodeConnectorRef egressNodeConnectorRef = rules.get(ingressNodeId).get(dstMac);
			Uri ingressOutputPort = Constants.FLOOD_OUTPUT_PORT;
		    Uri egressOutputPort =  Constants.FLOOD_OUTPUT_PORT;
			if(egressNodeConnectorRef!=null){
				ingressOutputPort = new Uri(CommonUtils.getNodeConnectorRef(ingressNodeConnectorRef));
				egressOutputPort = new Uri(CommonUtils.getNodeConnectorRef(egressNodeConnectorRef));
			}
						
			User srcUser = AuthenticationEngine.getUserDetailsFromDB(srcMac);
			User dstUser = AuthenticationEngine.getUserDetailsFromDB(dstMac);
			
			programMeters(ingressNodeId);
			
			if (PacketUtils.isDestCaptivePortal(dstMac)) {
				programL2Flows(ingressNodeId, ingressOutputPort, egressOutputPort, srcUser, dstUser);

			} else if (PacketUtils.isSrcDstActivated(srcUser,dstUser)){
				programL2Flows(ingressNodeId, ingressOutputPort, egressOutputPort, srcUser, dstUser);
			}

			else {
				// adds the user to the database and the user’s state is unauthenticated
				if (!srcUser.isExist()) {
					AuthenticationEngine.saveUnauthUser(srcIP, srcMac);
				}
				// redirection rules must be installed on edge switches only
				edgeRuleMacFlags.put(srcMac, false);
				// add redirection flows
				if (dstPort == 80 && !edgeRuleMacFlags.get(srcMac)) {
					FlowEngine.addReverseflow(this.dataBroker, ingressNodeId, ingressOutputPort, srcMac, dstMac,srcIP, dstIP, dstPort);
					FlowEngine.addforwardflow(this.dataBroker, ingressNodeId, egressOutputPort, srcMac, dstMac,srcIP, dstIP, dstPort);
					edgeRuleMacFlags.put(srcMac, true);
				}
			}
		}catch(Exception e){
			log.error("Exception occured:: ", e);
		}
	}

	private void programL2Flows(NodeId ingressNodeId, Uri ingressOutputPort, Uri egressOutputPort, User srcUser, User dstUser) throws Exception {
		FlowEngine.programL2Flow(this.dataBroker, ingressNodeId, egressOutputPort, srcUser.getMacAddress(), dstUser.getMacAddress(), srcUser.getUserRole());
		FlowEngine.programL2Flow(this.dataBroker, ingressNodeId, ingressOutputPort, dstUser.getMacAddress(), srcUser.getMacAddress(), dstUser.getUserRole());
	}
	
	private void programMeters(NodeId ingressNodeId) throws Exception {
		MeterEngine.createGuestMeter(this.dataBroker, ingressNodeId);
		MeterEngine.createUserMeter(this.dataBroker, ingressNodeId);
	}
	
}