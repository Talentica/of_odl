/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.utils;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetDlSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetTpSrcActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.dst.action._case.SetDlDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.dl.src.action._case.SetDlSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.src.action._case.SetNwSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.dst.action._case.SetTpDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.tp.src.action._case.SetTpSrcActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;

/**
 * @author narenderk
 *
 */
public class FlowUtils {
	
	private FlowUtils(){
		//utility class, do not instantiate
	}
	
	/**
	 * 
	 * @param instructions
	 * @param matchBuilder
	 * @param flowId
	 * @param priority
	 * @return
	 */
	public static FlowBuilder createFlowBuilder(List<Instruction> instructions, MatchBuilder matchBuilder, String flowId, int priority) {
		FlowBuilder flowBuilder = new FlowBuilder();
		flowBuilder.setMatch(matchBuilder.build());
		flowBuilder.setId(new FlowId(flowId));
		FlowKey key = new FlowKey(new FlowId(flowId));
		flowBuilder.setBarrier(true);
		flowBuilder.setTableId((short) 0);
		flowBuilder.setKey(key);
		flowBuilder.setPriority(priority);
		flowBuilder.setFlowName(flowId);
		flowBuilder.setHardTimeout(0);
		flowBuilder.setIdleTimeout(0);
		InstructionsBuilder isb = new InstructionsBuilder();
		flowBuilder.setInstructions(isb.setInstruction(instructions).build());
		return flowBuilder;
	}
	
	/**
	 * 
	 * @param actionList
	 * @param instructions
	 */
	public static void createApplyActionInstructions(List<Action> actionList, List<Instruction> instructions) {
		ApplyActionsBuilder aab = new ApplyActionsBuilder();		
		aab.setAction(actionList);
		
		InstructionBuilder ib = new InstructionBuilder();		
		ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
		ib.setOrder(0);
		ib.setKey(new InstructionKey(0));
		instructions.add(ib.build());
	}
	
	/**
	 * 
	 * @param actionList
	 * @param outputPort
	 * @param order
	 */
	public static void createOutputAction(List<Action> actionList, Uri outputPort, int order) {
		ActionBuilder ab = new ActionBuilder();
		OutputActionBuilder output = new OutputActionBuilder();
	    output.setOutputNodeConnector(outputPort);
		output.setMaxLength(65535);
		ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
		ab.setOrder(order);
		actionList.add(ab.build());	
	}
	
	/**
	 * 
	 * @param actionList
	 * @param mac
	 * @param order
	 */
	public static void creteDlSrcAction(List<Action> actionList, String mac, int order) {
		ActionBuilder dlSrcActionBuilder = new ActionBuilder();
		SetDlSrcActionBuilder dl = new SetDlSrcActionBuilder();
		dl.setAddress(new MacAddress(mac));
		dlSrcActionBuilder.setAction(new SetDlSrcActionCaseBuilder().setSetDlSrcAction(dl.build()).build());
		dlSrcActionBuilder.setOrder(order);
		actionList.add(dlSrcActionBuilder.build());		
	}
	
	/**
	 * 
	 * @param actionList
	 * @param ip
	 * @param order
	 */
	public static void createNewSrcAction(List<Action> actionList, String ip, int order) {
		ActionBuilder nwSrcActionBuiulder = new ActionBuilder();
		Ipv4Builder ipSrc = new Ipv4Builder();
		Ipv4Address ipAddress = new Ipv4Address(ip);
		Ipv4Prefix prefixdst = new Ipv4Prefix(new Ipv4Prefix(ipAddress.getValue() + "/32"));
		ipSrc.setIpv4Address(prefixdst);
		SetNwSrcActionBuilder setNwSrcActionBuilder = new SetNwSrcActionBuilder();
		setNwSrcActionBuilder.setAddress(ipSrc.build());
		nwSrcActionBuiulder.setAction(new SetNwSrcActionCaseBuilder().setSetNwSrcAction(setNwSrcActionBuilder.build()).build());
		nwSrcActionBuiulder.setOrder(order);
		actionList.add(nwSrcActionBuiulder.build());
		
	}
	
	/**
	 * 
	 * @param actionList
	 * @param port
	 * @param order
	 */
	public static void createTcpSrcAction(List<Action> actionList, int port, int order) {
		ActionBuilder tpSrcActionBuilder = new ActionBuilder();
		SetTpSrcActionBuilder tpSrcBuilder = new SetTpSrcActionBuilder();
		tpSrcBuilder.setPort(new PortNumber(port));
		tpSrcActionBuilder.setAction(new SetTpSrcActionCaseBuilder().setSetTpSrcAction(tpSrcBuilder.build()).build());
		tpSrcActionBuilder.setOrder(order);
		actionList.add(tpSrcActionBuilder.build());

		
	}

	/**
	 * 
	 * @param actionList
	 * @param mac
	 * @param order
	 */
	public static void createDlDstAction(List<Action> actionList, String mac, int order) {
		ActionBuilder dlDstActionBuilder = new ActionBuilder();
		SetDlDstActionBuilder dl = new SetDlDstActionBuilder();
        dl.setAddress(new MacAddress(mac));
		dlDstActionBuilder.setAction(new SetDlDstActionCaseBuilder().setSetDlDstAction(dl.build()).build());
		dlDstActionBuilder.setOrder(order);
		actionList.add(dlDstActionBuilder.build());
		
	}

	/**
	 * 
	 * @param actionList
	 * @param ip
	 * @param order
	 */
	public static void createNewDstAction(List<Action> actionList, String ip, int order) {
		ActionBuilder nwDstActionBuilder = new ActionBuilder();
		Ipv4Builder ipDest = new Ipv4Builder();
		Ipv4Address ipAddress = new Ipv4Address(ip);
		Ipv4Prefix prefixdst = new Ipv4Prefix(new Ipv4Prefix(ipAddress.getValue() + "/32"));
		ipDest.setIpv4Address(prefixdst);
		SetNwDstActionBuilder setNwDstActionBuilder = new SetNwDstActionBuilder();
		setNwDstActionBuilder.setAddress(ipDest.build());
		nwDstActionBuilder.setAction(new SetNwDstActionCaseBuilder().setSetNwDstAction(setNwDstActionBuilder.build()).build());
		nwDstActionBuilder.setOrder(order);
		actionList.add(nwDstActionBuilder.build());
	}

	/**
	 * 
	 * @param actionList
	 * @param port
	 * @param order
	 */
	public static void createTcpDstAction(List<Action> actionList, Integer port, int order) {
		ActionBuilder tpDstActionBuilder = new ActionBuilder();
		SetTpDstActionBuilder tpDstBuilder = new SetTpDstActionBuilder();
		tpDstBuilder.setPort(new PortNumber(port));
		tpDstActionBuilder.setAction(new SetTpDstActionCaseBuilder().setSetTpDstAction(tpDstBuilder.build()).build());
		tpDstActionBuilder.setOrder(order);
		actionList.add(tpDstActionBuilder.build());
		
	}

	/**
	 * 
	 * @param dataBroker
	 * @param meter
	 * @param nodeId
	 * @throws OdlDataStoreException
	 */
	public static void writeMeterToDataStore(DataBroker dataBroker, Meter meter, NodeId nodeId) throws OdlDataStoreException {
		 InstanceIdentifier<Meter> meterIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId))
	                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
			CommonUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, meterIID, meter,true);
		
	}
	
	/**
	 * 
	 * @param dataBroker
	 * @param flowBuilder
	 * @param nodeId
	 * @throws OdlDataStoreException
	 */
	public static void writeFlowToDataStore(DataBroker dataBroker, FlowBuilder flowBuilder, NodeId nodeId) throws OdlDataStoreException {
		InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
				.child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
				.child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
				.build();
		CommonUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(),
				true);		
	}

}
