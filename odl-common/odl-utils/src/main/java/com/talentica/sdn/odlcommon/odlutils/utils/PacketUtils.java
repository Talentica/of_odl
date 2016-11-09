/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.utils;

import com.talentica.sdn.odlcommon.odlutils.to.CapFluxPacket;
import com.talentica.sdn.odlcommon.odlutils.to.User;

/**
 * @author narenderk
 *
 */
public class PacketUtils {
	
	private PacketUtils(){
		//utility class, do not instantiate
	}
	
	/**
	 * 
	 * @param payload
	 * @return
	 */
	public static CapFluxPacket parsePacketFromPayload(byte[] payload){
		CapFluxPacket packet = new CapFluxPacket();
		// Parse packet
		byte[] rawDstMac = CommonUtils.extractDstMac(payload);
		byte[] rawSrcMac = CommonUtils.extractSrcMac(payload);
		String srcMac = CommonUtils.rawMacToString(rawSrcMac);
		String dstMac = CommonUtils.rawMacToString(rawDstMac);
		byte[] rawDstIP = CommonUtils.extractDstIP(payload);
		byte[] rawSrcIP = CommonUtils.extractSrcIP(payload);
		String dstIP = CommonUtils.rawIPToString(rawDstIP);
		String srcIP = CommonUtils.rawIPToString(rawSrcIP);
		byte[] rawDstPort = CommonUtils.extractDstPort(payload);
		int dstPort = CommonUtils.rawPortToInteger(rawDstPort);
		packet.setSrcMacAddress(srcMac);
		packet.setDestMacAddress(dstMac);
		packet.setSrcIpAddress(srcIP);
		packet.setDestIpAddress(dstIP);
		packet.setDestTcpPort(dstPort);		
		return packet;
	}
	
	/**
	 * 
	 * @param dstMac
	 * @return
	 */
	public static boolean isDestCaptivePortal(String dstMac){
		return dstMac.equalsIgnoreCase(Constants.CAPTIVE_PORTAL_MAC);
			
	}
	
	/**
	 * 
	 * @param srcUser
	 * @param dstUser
	 * @return
	 */
	public static boolean isSrcDstActivated(User srcUser, User dstUser) {
		return srcUser.isExist() && dstUser.isExist() && srcUser.isActivated() && dstUser.isActivated();
	}


}
