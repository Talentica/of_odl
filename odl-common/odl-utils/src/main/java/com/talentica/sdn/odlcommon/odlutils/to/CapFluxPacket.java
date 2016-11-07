/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.to;

/**
 * @author narenderk
 *
 */
public class CapFluxPacket {
	
	private String srcMacAddress;
	private String destMacAddress;
	private String srcIpAddress;
	private String destIpAddress;
	private int srcTcpPort;
	private int destTcpPort;
	public String getSrcMacAddress() {
		return srcMacAddress;
	}
	public void setSrcMacAddress(String srcMacAddress) {
		this.srcMacAddress = srcMacAddress;
	}
	public String getDestMacAddress() {
		return destMacAddress;
	}
	public void setDestMacAddress(String destMacAddress) {
		this.destMacAddress = destMacAddress;
	}
	public String getSrcIpAddress() {
		return srcIpAddress;
	}
	public void setSrcIpAddress(String srcIpAddress) {
		this.srcIpAddress = srcIpAddress;
	}
	public String getDestIpAddress() {
		return destIpAddress;
	}
	public void setDestIpAddress(String destIpAddress) {
		this.destIpAddress = destIpAddress;
	}
	public int getSrcTcpPort() {
		return srcTcpPort;
	}
	public void setSrcTcpPort(int srcTcpPort) {
		this.srcTcpPort = srcTcpPort;
	}
	public int getDestTcpPort() {
		return destTcpPort;
	}
	public void setDestTcpPort(int destTcpPort) {
		this.destTcpPort = destTcpPort;
	}

}
