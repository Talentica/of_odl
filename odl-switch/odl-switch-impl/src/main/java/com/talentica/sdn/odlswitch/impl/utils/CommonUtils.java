/**
 * 
 */
package com.talentica.sdn.odlswitch.impl.utils;

import java.util.Arrays;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * @author narenderK
 *
 */
public class CommonUtils {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	private CommonUtils() {
		//utility class, do not instantiate 
	}
    
	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static byte[] extractEtherType(byte[] payload) {
		return Arrays.copyOfRange(payload, Constants.ETHER_TYPE_START_POSITION, Constants.ETHER_TYPE_END_POSITION);
	}
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static byte[] extractDstMac(byte[] payload) {
		return Arrays.copyOfRange(payload, Constants.DST_MAC_START_POSITION, Constants.DST_MAC_END_POSITION);
	}
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static byte[] extractSrcMac(byte[] payload) {
		return Arrays.copyOfRange(payload, Constants.SRC_MAC_START_POSITION, Constants.SRC_MAC_END_POSITION);
	}
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static byte[] extractDstIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, Constants.DST_IP_START_POSITION, Constants.DST_IP_END_POSITION);
    }
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
    public static byte[] extractSrcIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, Constants.SRC_IP_START_POSITION, Constants.SRC_IP_END_POSITION);
    }
    
    /**
     * 
     * @param payload
     * @return
     */
    public static byte[] extractSrcPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, Constants.SRC_PORT_START_POSITION, Constants.SRC_PORT_END_POSITION);
    }
    
    /**
     * 
     * @param payload
     * @return
     */
    public static byte[] extractDstPort(final byte[] payload) {
        return Arrays.copyOfRange(payload, Constants.DST_PORT_START_POSITION, Constants.DST_PORT_END_POSITION);
    }
    
    /**
     * 
     * @param rawMac
     * @return
     */
	public static String rawMacToString(byte[] rawMac) {
		if (rawMac != null && rawMac.length == 6) {
			StringBuilder sb = new StringBuilder();
			for (byte octet : rawMac) {
				sb.append(String.format(":%02X", octet));
			}
			return sb.substring(1);
		}
		return null;
	}
	
	/**
	 * 
	 * @param rawIP
	 * @return
	 */
    public static String rawIPToString(byte[] rawIP) {
        if (rawIP != null && rawIP.length == Constants.IP_LENGTH) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawIP) {
                sb.append(String.format(".%d", octet));
            }
            return sb.substring(1);
        }
        return null;
    }
    
    /**
     * 
     * @param rawPort
     * @return
     */
    public static int rawPortToInteger(byte[] rawPort) {
        int intOctet =0;
        int intOctetSum = 0;
        int iter = 1;
        if (rawPort != null && rawPort.length == Constants.PORT_LENGTH) {
            for (byte octet : rawPort) {
                intOctet = octet & 0xff;
                intOctetSum = (int) (intOctetSum + intOctet *  Math.pow(Constants.POWER,iter));
                iter--;
            }
            return intOctetSum;
        }
        return 0;
    }
    
    /**
     * 
     * @param ingressNodeConnectorRef
     * @return
     */
    public static NodeRef getNodeRef(NodeConnectorRef ingressNodeConnectorRef) {
		return new NodeRef(ingressNodeConnectorRef.getValue().firstIdentifierOf(Node.class));
	}
    
    /**
     * 
     * @param ingressNodeConnectorRef
     * @return
     */
	public static NodeConnectorId getNodeConnectorRef(NodeConnectorRef ingressNodeConnectorRef) {
		return ingressNodeConnectorRef.getValue().firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId();
	}
	
	/**
	 * 
	 * @param ingressNodeConnectorId
	 * @return
	 */
	public static NodeId getNodeId(NodeConnectorId ingressNodeConnectorId) {
		if(ingressNodeConnectorId == null){
			return null;
		}
		String[] splits = ingressNodeConnectorId.getValue().split(":");
		if (splits.length == 3)
    		return new NodeId(Constants.OPENFLOW_NODE_PREFIX + Long.parseLong(splits[1]));
    	else
    		return null;
	}
	
	/**
	 * 
	 * @param ingressNodeId
	 * @param floodPortNumber
	 * @return
	 */
	public static NodeConnectorId getNodeConnectorId(NodeId ingressNodeId, long floodPortNumber) {
		if (ingressNodeId == null)
    		return null;
    	String nodeConnectorIdStr = ingressNodeId.getValue() + ":" + floodPortNumber;
		return new NodeConnectorId(nodeConnectorIdStr);
	}
	
	/**
	 * 
	 * @param floodNodeConnectorId
	 * @return
	 */
	public static NodeConnectorRef getNodeConnectorRef(NodeConnectorId floodNodeConnectorId) {
		NodeId nodeId = getNodeId(floodNodeConnectorId);
        return new NodeConnectorRef(InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .child(NodeConnector.class, new NodeConnectorKey(floodNodeConnectorId))
                .build());
	}
	
	/**
	 * 
	 * @param matchBuilder
	 * @param macAddress
	 * @param mask
	 * @return
	 */
	public static MatchBuilder createEthDstMatch(MatchBuilder matchBuilder, MacAddress macAddress, MacAddress mask) {
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
		EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
		ethTypeBuilder.setType(new EtherType(Constants.IPV4_LONG));
		ethernetMatch.setEthernetType(ethTypeBuilder.build());
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(macAddress));
        if (mask != null) {
            ethDestinationBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());
        return matchBuilder;
	}
	
	/**
	 * 
	 * @param matchBuilder
	 * @param macAddress
	 * @return
	 */
	public static MatchBuilder createEthSrcMatch(MatchBuilder matchBuilder, MacAddress macAddress) {
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
		EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
		ethTypeBuilder.setType(new EtherType(Constants.IPV4_LONG));
		ethernetMatch.setEthernetType(ethTypeBuilder.build());
		EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
		ethSourceBuilder.setAddress(new MacAddress(macAddress));
		ethernetMatch.setEthernetSource(ethSourceBuilder.build());
		matchBuilder.setEthernetMatch(ethernetMatch.build());
    	return matchBuilder;
	}
	
	/**
	 * 
	 * @param dataBroker
	 * @param logicalDatastoreType
	 * @param iid
	 * @param dataObject
	 * @param isAdd
	 * @return
	 * @throws Exception
	 */
	public static <T extends DataObject> boolean writeData(DataBroker dataBroker, LogicalDatastoreType logicalDatastoreType, 
    		InstanceIdentifier<T> iid, T dataObject, boolean isAdd) throws Exception {
		Preconditions.checkNotNull(dataBroker);
		WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
		if (isAdd) {
			if (dataObject == null) {
				return false;
			}
			modification.merge(logicalDatastoreType, iid, dataObject, true);
		} else {
			modification.delete(LogicalDatastoreType.CONFIGURATION, iid);
		}
		CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
		try {
			commitFuture.checkedGet();
			return true;
		} catch (Exception e) {
			modification.cancel();
			throw e;
		}
	}
	
	/**
	 * 
	 * @param matchBuilder
	 * @param tcpSourcePort
	 * @return
	 */
	public static MatchBuilder createSetTcpSrcMatch(MatchBuilder matchBuilder, PortNumber tcpSourcePort) {
        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipmatch.build());
        
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        tcpmatch.setTcpSourcePort(tcpSourcePort);
        matchBuilder.setLayer4Match(tcpmatch.build());

        return matchBuilder;

    }
	
	/**
	 * 
	 * @param matchBuilder
	 * @param tcpDestPort
	 * @return
	 */
	public static MatchBuilder createSetTcpDstMatch(MatchBuilder matchBuilder, PortNumber tcpDestPort) {
        IpMatchBuilder ipmatch = new IpMatchBuilder();
        ipmatch.setIpProtocol((short) 6);
        matchBuilder.setIpMatch(ipmatch.build());
        
        TcpMatchBuilder tcpmatch = new TcpMatchBuilder();
        tcpmatch.setTcpDestinationPort(tcpDestPort);
        matchBuilder.setLayer4Match(tcpmatch.build());

        return matchBuilder;

    }
	
	/**
	 * 
	 * @param matchBuilder
	 * @param srcip
	 * @param dstip
	 * @return
	 */
	public static MatchBuilder createL3IPv4Match(MatchBuilder matchBuilder, Ipv4Prefix srcip, Ipv4Prefix dstip) {
        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        ipv4match.setIpv4Source(srcip);
        ipv4match.setIpv4Destination(dstip);
        matchBuilder.setLayer3Match(ipv4match.build());
        return matchBuilder;
    }
	
	/**
	 * 
	 * @param matchBuilder
	 * @param sMacAddr
	 * @param dMacAddr
	 * @param mask
	 * @return
	 */
	public static MatchBuilder createEthMatch(MatchBuilder matchBuilder, MacAddress sMacAddr, MacAddress dMacAddr, MacAddress mask) {

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Constants.IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());
        EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
        ethSourceBuilder.setAddress(new MacAddress(sMacAddr));
        EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
        ethDestinationBuilder.setAddress(new MacAddress(dMacAddr));
        if (mask != null) {
            ethDestinationBuilder.setMask(mask);
        }
        ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
    }
	
	/**
	 * 
	 * @param matchBuilder
	 * @return
	 */
	public static MatchBuilder createEthTypeARPMatch(MatchBuilder matchBuilder) {
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Constants.ARP_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
		
	}
	
	/**
	 * 
	 * @param matchBuilder
	 * @return
	 */
	public static MatchBuilder createEthTypeMatch(MatchBuilder matchBuilder) {
		EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Constants.IPV4_LONG));
        ethernetMatch.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        return matchBuilder;
		
	}

}
