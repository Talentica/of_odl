package com.talentica.sdn.odlswitch.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
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
import com.talentica.sdn.odlcommon.odlutils.engine.FlowEngine;
import com.talentica.sdn.odlcommon.odlutils.exception.AuthServerRestFailedException;
import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;
import com.talentica.sdn.odlcommon.odlutils.to.CapFluxPacket;
import com.talentica.sdn.odlcommon.odlutils.to.User;
import com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.PacketUtils;
import com.talentica.sdn.odlswitch.impl.engine.OvsFlowEngine;
import com.talentica.sdn.odlswitch.impl.rpc.ConnectionImpl;

/**
 * @author narenderK
 *
 */
public class CapFlux implements AutoCloseable, PacketProcessingListener{
	
	private static final Logger LOG = LoggerFactory.getLogger(CapFlux.class);
	private List<Registration> registrations;
	private DataBroker dataBroker;
	private PacketProcessingService packetProcessingService;
	private RpcRegistration<ConnectService> connectService;	
	private static Map<String, String> macToIpMap = new HashMap<>();
    private static Map<String, Boolean> edgeRuleMacFlags = new HashMap<>();
	
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
		LOG.trace("notification receicved for ", notification.getMatch());
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
				FlowEngine.programFloodARPFlow(this.dataBroker, ingressNodeId, Constants.OPENFLOW_OUTPUT_PORT_FLOOD);
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
			String srcIP = packet.getSrcIpAddress();

			if (!macToIpMap.containsKey(srcIP)) {
				macToIpMap.put(srcIP, srcMac);
			}
			
			User srcUser = AuthenticationEngine.getUserDetailsFromDB(srcMac);
			User dstUser = AuthenticationEngine.getUserDetailsFromDB(dstMac);
			
			if (PacketUtils.isFlowValid(srcUser,dstUser)) {
				programL2Flows(ingressNodeId, srcUser, dstUser);

			} else {
				programRedirectionFlows(ingressNodeId, srcUser, packet);
			}

		}catch(OdlDataStoreException e){
			LOG.error("Exception occured while Odl data store update:: ", e);
		} catch (AuthServerRestFailedException e) {
			LOG.error("Exception occured while rest call to auth server:: ", e);
		} catch (Exception e) {
			LOG.error("Exception occured:: ", e);
		}
	}

	private void programL2Flows(NodeId ingressNodeId, User srcUser, User dstUser) throws OdlDataStoreException  {
		OvsFlowEngine.programL2Flow(this.dataBroker, ingressNodeId, srcUser.getMacAddress(), dstUser.getMacAddress(), srcUser.getUserRole());
		OvsFlowEngine.programL2Flow(this.dataBroker, ingressNodeId, dstUser.getMacAddress(), srcUser.getMacAddress(), srcUser.getUserRole());
	}
	
	private void programRedirectionFlows(NodeId ingressNodeId, User srcUser, CapFluxPacket packet)
			throws AuthServerRestFailedException, OdlDataStoreException {
		String srcMac = packet.getSrcMacAddress();
		String srcIP = packet.getSrcIpAddress();
		int dstPort = packet.getDestTcpPort();
		// adds the user to the database and the user’s state is unauthenticated
		if (!srcUser.isExist()) {
			AuthenticationEngine.saveUnauthUser(srcIP, srcMac);
		}
		// redirection rules must be installed on edge switches only
		edgeRuleMacFlags.put(packet.getSrcMacAddress(), false);
		// add redirection flows
		if (dstPort == Constants.HTTP_PORT && !edgeRuleMacFlags.get(srcMac)) {
			FlowEngine.addReverseflow(this.dataBroker, ingressNodeId, Constants.OPENFLOW_OUTPUT_PORT_NORMAL, packet);
			FlowEngine.addforwardflow(this.dataBroker, ingressNodeId, Constants.OPENFLOW_OUTPUT_PORT_NORMAL, packet);
			edgeRuleMacFlags.put(srcMac, true);
		}
	}
	
}