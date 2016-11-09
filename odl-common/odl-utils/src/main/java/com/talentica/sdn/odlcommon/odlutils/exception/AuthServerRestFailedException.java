package com.talentica.sdn.odlcommon.odlutils.exception;

/**
 * @author narenderK
 *
 */
public class AuthServerRestFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param code
	 */
	public AuthServerRestFailedException(String msg, Exception cause){  
		super(msg, cause);  
	}
}
