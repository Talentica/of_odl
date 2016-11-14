/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.engine;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import com.google.common.collect.Lists;
import com.talentica.sdn.odlcommon.odlutils.exception.AuthServerRestFailedException;
import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;
import com.talentica.sdn.odlcommon.odlutils.to.CapFluxPacket;
import com.talentica.sdn.odlcommon.odlutils.to.User;
import com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.FlowUtils;

/**
 * @author narenderk
 *
 */
public class FlowEngine {
	private FlowEngine(){
		//utility class, do not instantiate
	}
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @param outputPort
	 * @throws OdlDataStoreException
	 */
	public static void programFloodARPFlow(DataBroker dataBroker, NodeId nodeId, Uri outputPort) throws OdlDataStoreException {
		MatchBuilder matchBuilder = new MatchBuilder();
		CommonUtils.createEthTypeARPMatch(matchBuilder);		
		List<Action> actionList = Lists.newArrayList();	
		
		// Set output action
		FlowUtils.createOutputAction(actionList, outputPort, 0);				
		
		List<Instruction> instructions = Lists.newArrayList();
		// Create Apply Actions Instruction
		FlowUtils.createApplyActionInstructions(actionList, instructions);
		
		// Create Flow
		String flowId = "FLOOD_ARP_" + nodeId.getValue();		
		FlowBuilder flowBuilder = FlowUtils.createFlowBuilder(instructions, matchBuilder,flowId, Constants.ORDER_ARP_FLOOD);
		FlowUtils.writeFlowToDataStore(dataBroker, flowBuilder, nodeId);
		
	}
	
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @param outputPort
	 * @param srcMac
	 * @param dstMac
	 * @param srcIp
	 * @param dstIp
	 * @param dstPort
	 * @throws OdlDataStoreException
	 */
	public static void addforwardflow(DataBroker dataBroker, NodeId nodeId, Uri outputPort, CapFluxPacket packet) throws OdlDataStoreException {
		String srcMac = packet.getSrcMacAddress();
		String dstMac = packet.getDestMacAddress();
		String dstIP = packet.getDestIpAddress();
		String srcIP = packet.getSrcIpAddress();
		int dstPort = packet.getDestTcpPort();		
		MatchBuilder matchBuilder = new MatchBuilder();
		CommonUtils.createEthMatch(matchBuilder, new MacAddress(srcMac), new MacAddress(dstMac), null);
		Ipv4Prefix srcip = new Ipv4Prefix(srcIP + "/32");
		Ipv4Prefix dstip = new Ipv4Prefix(dstIP + "/32");
		CommonUtils.createL3IPv4Match(matchBuilder, srcip, dstip);
		CommonUtils.createSetTcpDstMatch(matchBuilder, new PortNumber(dstPort));
		
		List<Action> actionList = new ArrayList<>();
		
		// Set dl dst action
		FlowUtils.createDlDstAction(actionList, Constants.CAPTIVE_PORTAL_MAC, 0);
		
		// Set new dst action
		FlowUtils.createNewDstAction(actionList, Constants.CAPTIVE_PORTAL_IP, 1);
		
		// Set tcp dst action
		FlowUtils.createTcpDstAction(actionList, Constants.CAPTIVE_PORTAL_SERVER_PORT, 2);
		
		// Set output action
		FlowUtils.createOutputAction(actionList, outputPort, 3);

		List<Instruction> instructions = Lists.newArrayList();
		// Create Apply Actions Instruction
		FlowUtils.createApplyActionInstructions(actionList, instructions);

		// Create Flow
		String flowId = "ForwardFlow_"+srcMac;
		FlowBuilder flowBuilder = FlowUtils.createFlowBuilder(instructions, matchBuilder,flowId, Constants.ORDER_FORWARDING_RULE);
		FlowUtils.writeFlowToDataStore(dataBroker, flowBuilder, nodeId);
	}
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @param outputPort
	 * @param srcMac
	 * @param dstMac
	 * @param srcIp
	 * @param dstIp
	 * @param dstPort
	 * @throws OdlDataStoreException
	 */
	public static void addReverseflow(DataBroker dataBroker,NodeId nodeId, Uri outputPort, CapFluxPacket packet) throws OdlDataStoreException {
		String srcMac = packet.getSrcMacAddress();
		String dstMac = packet.getDestMacAddress();
		String dstIP = packet.getDestIpAddress();
		String srcIP = packet.getSrcIpAddress();
		int dstPort = packet.getDestTcpPort();
		MatchBuilder matchBuilder = new MatchBuilder();
		CommonUtils.createEthMatch(matchBuilder, new MacAddress(Constants.CAPTIVE_PORTAL_MAC),new MacAddress(srcMac), null);
		Ipv4Prefix srcip = new Ipv4Prefix(Constants.CAPTIVE_PORTAL_IP + "/32");
		Ipv4Prefix dstip = new Ipv4Prefix(srcIP + "/32");
		CommonUtils.createL3IPv4Match(matchBuilder, srcip, dstip);
		CommonUtils.createSetTcpSrcMatch(matchBuilder, new PortNumber(Constants.CAPTIVE_PORTAL_SERVER_PORT));
		List<Action> actionList = new ArrayList<>();
		
		// Set dl src action
		FlowUtils.creteDlSrcAction(actionList, dstMac, 0);
		
		// Set new src action
		FlowUtils.createNewSrcAction(actionList,dstIP, 1);

		// Set tcp src action
		FlowUtils.createTcpSrcAction(actionList,dstPort, 2);
		
		// Set output action
		FlowUtils.createOutputAction(actionList, outputPort, 3);

		List<Instruction> instructions = Lists.newArrayList();
		// Create Apply Actions Instruction
		FlowUtils.createApplyActionInstructions(actionList, instructions);

		// Create Flow
		String flowId = "ReverseFlow_"+srcMac;
		FlowBuilder flowBuilder = FlowUtils.createFlowBuilder(instructions, matchBuilder,flowId, Constants.ORDER_FORWARDING_RULE);
		FlowUtils.writeFlowToDataStore(dataBroker, flowBuilder, nodeId);
	}
	
}
