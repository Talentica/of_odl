/**
 * 
 */
package com.talentica.sdn.odlswitch.impl.engine;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetQueueActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.queue.action._case.SetQueueActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import com.google.common.collect.Lists;
import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;
import com.talentica.sdn.odlcommon.odlutils.utils.CommonUtils;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.FlowUtils;

/**
 * * @author narenderK
 *
 */
public class OvsFlowEngine {
	
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @param srcMac
	 * @param dstMac
	 * @param ingressNodeConnectorId
	 * @param role
	 * @param dstPort
	 * @throws OdlDataStoreException
	 */
	public static void programL2Flow(DataBroker dataBroker, NodeId nodeId, String srcMac, String dstMac, String role) throws OdlDataStoreException {
		MatchBuilder matchBuilder = new MatchBuilder();
		CommonUtils.createEthMatch(matchBuilder, new MacAddress(srcMac), new MacAddress(dstMac), null);
		
		List<Action> actionList = new ArrayList<>();

		// Set output action
		FlowUtils.createOutputAction(actionList, Constants.OPENFLOW_OUTPUT_PORT_NORMAL, 1);
		
		ActionBuilder actionBuilder = new ActionBuilder();
		SetQueueActionBuilder setQueueActionBuilder = new SetQueueActionBuilder();
		if(role.equalsIgnoreCase(Constants.ROLE_EMPLOYEE)){
			setQueueActionBuilder.setQueueId(Constants.QUEUE_ID_EMPLOYEE);
		}else{
			setQueueActionBuilder.setQueueId(Constants.QUEUE_ID_GUEST);
		}
        actionBuilder.setAction(new SetQueueActionCaseBuilder().setSetQueueAction(setQueueActionBuilder.build()).build());
        actionBuilder.setOrder(0);
        actionList.add(actionBuilder.build());

        List<Instruction> instructions = Lists.newArrayList();
        
        // Create Apply Actions Instruction
		FlowUtils.createApplyActionInstructions(actionList, instructions);
        
		// Create Flow
		String flowId = "L2_Rule_" + srcMac +"_to_" +dstMac;
		FlowBuilder flowBuilder = FlowUtils.createFlowBuilder(instructions, matchBuilder,flowId, Constants.ORDER_L2_RULE);
		FlowUtils.writeFlowToDataStore(dataBroker, flowBuilder, nodeId);			
	}
		
}
