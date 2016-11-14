/**
 * 
 */
package com.talentica.sdn.odlofsoftswitch.impl.engine;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.MeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.meter._case.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

import com.google.common.collect.Lists;
import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;
import com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.FlowUtils;

/**
 * * @author narenderK
 *
 */
public class OfFlowEngine {
	
	private OfFlowEngine(){
		//utility class, do not instantiate
	}
	
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @param outputPort
	 * @param srcMac
	 * @param dstMac
	 * @param role
	 * @throws OdlDataStoreException
	 */
	public static void programL2Flow(DataBroker dataBroker, NodeId nodeId, Uri outputPort, String srcMac, String dstMac, String role) throws OdlDataStoreException {
		MatchBuilder matchBuilder = new MatchBuilder();
		CommonUtils.createEthMatch(matchBuilder, new MacAddress(srcMac), new MacAddress(dstMac), null);
		
		List<Action> actionList = new ArrayList<>();

		// Set output action
		FlowUtils.createOutputAction(actionList, outputPort, 1);
		
		List<Instruction> instructions = Lists.newArrayList();
		// Create Apply Actions Instruction
		FlowUtils.createApplyActionInstructions(actionList, instructions);

		MeterCaseBuilder meterCaseBuilder = new MeterCaseBuilder();
		MeterBuilder meterBuilder = new MeterBuilder();
		if (role.equalsIgnoreCase(Constants.ROLE_EMPLOYEE)) {
			meterBuilder.setMeterId(new MeterId(Constants.METER_ID_EMPLOYEE));
		} else {
			meterBuilder.setMeterId(new MeterId(Constants.METER_ID_GUEST));
		}
		meterCaseBuilder.setMeter(meterBuilder.build());

		InstructionBuilder meterInstructionBuilder = new InstructionBuilder();
		meterInstructionBuilder.setInstruction(meterCaseBuilder.build());
		meterInstructionBuilder.setOrder(1);
		meterInstructionBuilder.setKey(new InstructionKey(1));

		instructions.add(meterInstructionBuilder.build());

		// Create Flow
		String flowId = "L2_Rule_" + srcMac +"_to_" +dstMac;
		FlowBuilder flowBuilder = FlowUtils.createFlowBuilder(instructions, matchBuilder,flowId, Constants.ORDER_L2_RULE);
		FlowUtils.writeFlowToDataStore(dataBroker, flowBuilder, nodeId);
	}
			
}
