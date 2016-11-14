/**
 * 
 */
package com.talentica.sdn.odlofsoftswitch.impl.engine;

import java.util.Collections;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.meter.band.header.MeterBandTypesBuilder;

import com.talentica.sdn.odlcommon.odlutils.exception.OdlDataStoreException;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;
import com.talentica.sdn.odlcommon.odlutils.utils.FlowUtils;



/**
 * @author narenderk
 *
 */
public class MeterEngine {
	
	private static MeterBuilder meterBuilder = new MeterBuilder();
	
	private MeterEngine(){
		//utility class, do not instantiate
	}
	
	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @throws OdlDataStoreException
	 */
	public static void createGuestMeter(DataBroker dataBroker, NodeId nodeId) throws OdlDataStoreException {
		meterBuilder.setMeterId(new MeterId(Constants.METER_ID_GUEST));
		meterBuilder.setKey(new MeterKey(new MeterId(Constants.METER_ID_GUEST)));
		meterBuilder.setContainerName("guestMeterContainer");
		meterBuilder.setMeterName("guestMeter");
		meterBuilder.setFlags(new MeterFlags(false, true, false, false));
		createDropMeterband(meterBuilder, Constants.BAND_RATE_KB, Constants.DROP_RATE_KB_GUEST);
        Meter meter = meterBuilder.build();
        FlowUtils.writeMeterToDataStore(dataBroker, meter, nodeId);
	}
	

	/**
	 * 
	 * @param dataBroker
	 * @param nodeId
	 * @throws OdlDataStoreException
	 */
	public static void createUserMeter(DataBroker dataBroker, NodeId nodeId) throws OdlDataStoreException {
		meterBuilder.setMeterId(new MeterId(Constants.METER_ID_EMPLOYEE));
		meterBuilder.setKey(new MeterKey(new MeterId(Constants.METER_ID_EMPLOYEE)));
		meterBuilder.setContainerName("userMeterContainer");
		meterBuilder.setMeterName("userMeter");
		meterBuilder.setFlags(new MeterFlags(false, true, false, false));
		createDropMeterband(meterBuilder, Constants.BAND_RATE_KB, Constants.DROP_RATE_KB_EMPLOYEE);
        Meter meter = meterBuilder.build();
        FlowUtils.writeMeterToDataStore(dataBroker, meter, nodeId);
	}
	
	private static void createDropMeterband(MeterBuilder meterBuilder, long bandRateKb, long dropRateKb) {
		meterBuilder.setMeterBandHeaders(new MeterBandHeadersBuilder()
                .setMeterBandHeader(Collections.singletonList(new MeterBandHeaderBuilder()
                        .setBandId(new BandId(0L))
                        .setBandRate(bandRateKb)
                        .setMeterBandTypes(new MeterBandTypesBuilder()
                        		.setFlags(new MeterBandType(true, false, false))
                        		.build())
                        .setBandBurstSize(0L)
                        .setBandType(new DropBuilder()
                                .setDropRate(dropRateKb)
                                .setDropBurstSize(0L)
                                .build())
                        .build()))
                .build());		
	}
	

}
