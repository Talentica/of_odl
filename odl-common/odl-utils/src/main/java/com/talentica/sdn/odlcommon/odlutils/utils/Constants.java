/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.utils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;

/**
 * @author narenderK
 *
 */
public interface Constants {
	
	static final int ETHER_TYPE_START_POSITION = 12;
	static final int ETHER_TYPE_END_POSITION = 14;
	static final int DST_MAC_START_POSITION = 0;
	static final int DST_MAC_END_POSITION = 6;
	static final int SRC_MAC_START_POSITION = 6;
	static final int SRC_MAC_END_POSITION = 12;
	static final int IP_LENGTH = 4;
    static final int SRC_IP_START_POSITION = 26;
    static final int SRC_IP_END_POSITION = 30;
    static final int DST_IP_START_POSITION = 30;
    static final int DST_IP_END_POSITION = 34;
    static final int PORT_LENGTH = 2;
    static final int SRC_PORT_START_POSITION = 34;
    static final int SRC_PORT_END_POSITION = 36;
    static final int DST_PORT_START_POSITION = 36;
    static final int DST_PORT_END_POSITION = 38;  
    static final int POWER = 256;
    
    static final short ICMP_SHORT = 1;
    static final short TCP_SHORT = 6;
    static final short UDP_SHORT = 17;
    static final String TCP = "tcp";
    static final String UDP = "udp";
    static final int TCP_SYN = 0x0002;
    static final long IPV4_LONG = (long) 0x800;
    static final long LLDP_LONG = (long) 0x88CC;
    static final long ARP_LONG = (long) 0x0806;
    
    static final String OPENFLOW_NODE_PREFIX = "openflow:";
    static final Uri OPENFLOW_OUTPUT_PORT_NORMAL = new Uri("NORMAL");
    static final Uri OPENFLOW_OUTPUT_PORT_FLOOD = new Uri("FLOOD");
    
    static final String HTTP_ACCEPT = "Accept";
    static final String HTTP_ACCEPT_TYPE = "application/json";
    
    static final String CAPTIVE_PORTAL_MAC = "00:00:00:00:00:09";
	static final String CAPTIVE_PORTAL_IP = "172.29.3.3";
	static final Integer CAPTIVE_PORTAL_SERVER_PORT = 9090;
    
    static final String ROLE_GUEST = "GUEST";
    static final String ROLE_EMPLOYEE = "USER";
    
    static final int ORDER_ARP_FLOOD = 2;
    static final int ORDER_FORWARDING_RULE = 13;
    static final int ORDER_L2_RULE = 12;
    
    static final long BAND_RATE_KB = 50000L;
    static final long DROP_RATE_KB_GUEST = 500L;
    static final long DROP_RATE_KB_EMPLOYEE = 10000L;
    
    static final long QUEUE_ID_GUEST = 2L;
    static final long QUEUE_ID_EMPLOYEE = 1L;
    static final long METER_ID_GUEST = 2L;
    static final long METER_ID_EMPLOYEE = 1L;
    	
}
