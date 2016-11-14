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
	 * @param user
	 * @return
	 */
	public static boolean isUserCaptivePortal(User user){
		return Constants.CAPTIVE_PORTAL_MAC.equalsIgnoreCase(user.getMacAddress());
	}
	
	/**
	 * 
	 * @param user
	 * @return
	 */
	public static boolean isUserActivated(User user) {
		return user.isExist() && user.isActivated();
	}
	
	/**
	 * 
	 * @param srcUser
	 * @param dstUser
	 * @return
	 */
	public static boolean isCaptivePortal(User srcUser, User dstUser){
		return isUserCaptivePortal(srcUser) || isUserCaptivePortal(dstUser);
	}
	
	/**
	 * 
	 * @param srcUser
	 * @param dstUser
	 * @return
	 */
	public static boolean isSrcDstActivated(User srcUser, User dstUser) {
		return isUserActivated(srcUser) && isUserActivated(dstUser);
	}

	/**
	 * 
	 * @param srcUser
	 * @param dstUser
	 * @return
	 */
	public static boolean isFlowValid(User srcUser, User dstUser) {
		return isSrcDstActivated(srcUser, dstUser) || isCaptivePortal(srcUser, dstUser);
	}
	
}
