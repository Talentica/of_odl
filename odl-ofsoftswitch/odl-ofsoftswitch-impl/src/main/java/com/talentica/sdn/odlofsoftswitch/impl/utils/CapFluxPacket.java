/**
 * 
 */
package com.talentica.sdn.odlofsoftswitch.impl.utils;

/**
 * @author narenderk
 *
 */
public class CapFluxPacket {
	
	private String srcMacAddress;
	private String destMacAddress;
	private String srcIpAddress;
	private String destIpAddress;
	private String srcTcpPort;
	private String destTcpPort;
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
	public String getSrcTcpPort() {
		return srcTcpPort;
	}
	public void setSrcTcpPort(String srcTcpPort) {
		this.srcTcpPort = srcTcpPort;
	}
	public String getDestTcpPort() {
		return destTcpPort;
	}
	public void setDestTcpPort(String destTcpPort) {
		this.destTcpPort = destTcpPort;
	}

}
