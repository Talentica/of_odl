/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.talentica.sdn.odlcommon.odlutils.exception.RequestFailedException;
import com.talentica.sdn.odlcommon.odlutils.utils.Constants;

/**
 * * @author narenderK
 *
 */
public class AuthenticationEngine {
	/**
	 * 
	 * @param srcMac
	 * @return
	 * @throws Exception
	 */
	public boolean isMacRegistered(String srcMac) throws Exception {
		String output = null;
		boolean exist = false;
		try {
			URL url = new URL("http://localhost:9090/active" + "?srcMac=" + srcMac);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty(Constants.HTTP_ACCEPT, Constants.HTTP_ACCEPT_TYPE);
			if (conn.getResponseCode() != 200) {
				throw new RequestFailedException(conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((output = br.readLine()) != null) {
				if (output.contains(srcMac)) {
					exist = true;
				}
			}
			conn.disconnect();
		} catch (Exception e) {
			throw e;
		} 
		return exist;
	}
	
	/**
	 * 
	 * @param srcIp
	 * @param srcMac
	 * @return
	 * @throws Exception
	 */
	public boolean saveUnauthUser(String srcIp, String srcMac) throws Exception {
		String output = null;
		boolean isSaved = false;
		try {
			String requestUrl = "http://localhost:9090/newUser" + "?srcIp=" + srcIp + "&srcMac=" + srcMac;
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty(Constants.HTTP_ACCEPT, Constants.HTTP_ACCEPT_TYPE);
			if (conn.getResponseCode() != 200) {
				throw new RequestFailedException(conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((output = br.readLine()) != null) {
				if (output.contains(srcMac)) {
					isSaved = true;
				}
			}
			conn.disconnect();
		} catch (Exception e) {
			throw e;
		} 
		return isSaved;
	}
	
	/**
	 * 
	 * @param srcMac
	 * @return
	 * @throws Exception
	 */
	public String getSrcMacRole(String srcMac) throws Exception {
		String output = null;
		String role = "";
		try {
			String requestUrl = "http://localhost:9090/getRole" + "?srcMac=" + srcMac;
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty(Constants.HTTP_ACCEPT, Constants.HTTP_ACCEPT_TYPE);
			if (conn.getResponseCode() != 200) {
				throw new RequestFailedException(conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((output = br.readLine()) != null) {
					role = output;
			}
			conn.disconnect();
		} catch (Exception e) {
			throw e;
		}
		return role;
	}	

}
