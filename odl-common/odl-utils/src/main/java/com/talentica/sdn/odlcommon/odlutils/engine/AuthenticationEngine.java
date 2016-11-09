/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentica.sdn.odlcommon.odlutils.exception.AuthServerRestFailedException;
import com.talentica.sdn.odlcommon.odlutils.exception.RequestFailedException;
import com.talentica.sdn.odlcommon.odlutils.to.User;
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
	 * @throws AuthServerRestFailedException
	 */
	public static boolean isMacRegistered(String srcMac) throws AuthServerRestFailedException{
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
			throw new AuthServerRestFailedException("Unable to find mac registered", e);
		} 
		return exist;
	}
	
	
	/**
	 * 
	 * @param srcIp
	 * @param srcMac
	 * @return
	 * @throws AuthServerRestFailedException
	 */
	public static boolean saveUnauthUser(String srcIp, String srcMac) throws AuthServerRestFailedException {
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
			throw new AuthServerRestFailedException("Unable to save unauth user", e);
		} 
		return isSaved;
	}
	
	/**
	 * 
	 * @param srcMac
	 * @return
	 * @throws AuthServerRestFailedException
	 */
	public static String getSrcMacRole(String srcMac) throws AuthServerRestFailedException{
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
			throw new AuthServerRestFailedException("Unable to get user role", e);
		}
		return role;
	}	
	
	/**
	 * 
	 * @param srcMac
	 * @return
	 * @throws AuthServerRestFailedException
	 */
	public static User getUserDetailsFromDB(String srcMac) throws AuthServerRestFailedException {
		String output = null;
		StringBuilder sb = new StringBuilder();
		User user = null;
		try {
			String requestUrl = "http://localhost:9090/getUserDetials" + "?srcMac=" + srcMac;
			URL url = new URL(requestUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty(Constants.HTTP_ACCEPT, Constants.HTTP_ACCEPT_TYPE);
			if (conn.getResponseCode() != 200) {
				throw new RequestFailedException(conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((output = br.readLine()) != null) {
				sb.append(output + "\n");
			}
			ObjectMapper mapper = new ObjectMapper();
			user = mapper.readValue(sb.toString(), User.class);
			conn.disconnect();
		} catch (Exception e) {
			throw new AuthServerRestFailedException("Unable to get user detail", e);
		}
		return user;
	}

}
