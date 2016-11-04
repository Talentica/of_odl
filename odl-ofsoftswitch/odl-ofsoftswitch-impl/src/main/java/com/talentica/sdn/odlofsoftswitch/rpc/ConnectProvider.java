/**
 * 
 */
package com.talentica.sdn.odlofsoftswitch.rpc;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.connect.rev150105.ConnectService;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author naren
 *
 */
public class ConnectProvider implements AutoCloseable, BindingAwareProvider{
	
	private static final Logger LOG = LoggerFactory.getLogger(ConnectProvider.class);
	private RpcRegistration<ConnectService> connectService;

	@Override
	public void onSessionInitiated(ProviderContext session) {
		LOG.info("ConnectProvider session initialted");
		this.connectService = session.addRpcImplementation(ConnectService.class, new ConnectionImpl());
		
	}

	@Override
	public void close() throws Exception {
		if(this.connectService != null){
			connectService.close();
		}
		
	}

}
