package com.talentica.sdn.odlswitch.impl.exception;

/**
 * @author narenderK
 *
 */
public class RequestFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param code
	 */
	public RequestFailedException(int code){  
		super("Failed : HTTP error code : " +code);  
	}
}
