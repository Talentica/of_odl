/**
 * 
 */
package com.talentica.sdn.odlswitch.impl;

import java.util.Collections;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.talentica.sdn.odlswitch.impl.utils.CommonUtils;

/**
 * @author narenderk
 *
 */
public class MeterEngine {
	
	private MeterBuilder meterBuilder = new MeterBuilder();
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @throws Exception
	 */
	public void createGuestMeter(DataBroker dataBroker, NodeId nodeId) throws Exception {
		meterBuilder.setMeterId(new MeterId(1L));
		meterBuilder.setKey(new MeterKey(new MeterId(1L)));
		meterBuilder.setContainerName("guestMeterContainer");
		meterBuilder.setMeterName("guestMeter");
		meterBuilder.setFlags(new MeterFlags(false, true, false, false));
		
        meterBuilder.setMeterBandHeaders(new MeterBandHeadersBuilder()
                .setMeterBandHeader(Collections.singletonList(new MeterBandHeaderBuilder()
                        .setBandId(new BandId(0L))
                        .setBandRate(50000L)
                        .setMeterBandTypes(new MeterBandTypesBuilder()
                        		.setFlags(new MeterBandType(true, false, false))
                        		.build())
                        .setBandBurstSize(0L)
                        .setBandType(new DropBuilder()
                                .setDropRate(500L)
                                .setDropBurstSize(0L)
                                .build())
                        .build()))
                .build());
        Meter meter = meterBuilder.build();
        
        InstanceIdentifier<Meter> meterIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        
		CommonUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, meterIID, meter,true);
	}
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @throws Exception
	 */
	public void createUserMeter(DataBroker dataBroker, NodeId nodeId) throws Exception {
		meterBuilder.setMeterId(new MeterId(2L));
		meterBuilder.setKey(new MeterKey(new MeterId(2L)));
		meterBuilder.setContainerName("userMeterContainer");
		meterBuilder.setMeterName("userMeter");
		meterBuilder.setFlags(new MeterFlags(false, true, false, false));
		
        meterBuilder.setMeterBandHeaders(new MeterBandHeadersBuilder()
                .setMeterBandHeader(Collections.singletonList(new MeterBandHeaderBuilder()
                        .setBandId(new BandId(0L))
                        .setBandRate(50000L)
                        .setMeterBandTypes(new MeterBandTypesBuilder()
                        		.setFlags(new MeterBandType(true, false, false))
                        		.build())
                        .setBandBurstSize(0L)
                        .setBandType(new DropBuilder()
                                .setDropRate(10000L)
                                .setDropBurstSize(0L)
                                .build())
                        .build()))
                .build());
        Meter meter = meterBuilder.build();
        
        InstanceIdentifier<Meter> meterIID = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class).child(Meter.class, new MeterKey(meter.getMeterId()));
        
		CommonUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION, meterIID, meter,true);
	}
	

}
