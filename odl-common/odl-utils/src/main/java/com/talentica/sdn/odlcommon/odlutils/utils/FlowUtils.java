/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

/**
 * @author narenderk
 *
 */
public class FlowUtils {
	
	private FlowUtils(){
		//utility class, do not instantiate
	}
	
	public static void createFlowBuilder(FlowBuilder flowBuilder, MatchBuilder matchBuilder, String flowId, int priority) {
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
	}

}
