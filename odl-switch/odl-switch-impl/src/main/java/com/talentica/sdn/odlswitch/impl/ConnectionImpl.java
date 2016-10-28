/**
 * 
 */
package com.talentica.sdn.odlswitch.impl;

import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectionOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectionOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * @author narenderK
 *
 */
public class ConnectionImpl implements ConnectService{

	@Override
	public Future<RpcResult<ConnectionOutput>> connection(ConnectionInput input) {
		ConnectionOutputBuilder helloBuilder = new ConnectionOutputBuilder();
		String mac = input.getMac();
		String ipAddress = input.getIpAddress();
		String macCompare = CapFlux.getMacToIpMap().get(ipAddress);
		if(mac.equals(macCompare)){
			helloBuilder.setExist("true "+mac+" equals "+macCompare);
		}else{
			helloBuilder.setExist("false "+mac+" equals "+macCompare);
		}
		return RpcResultBuilder.success(helloBuilder.build()).buildFuture();
	}

}
