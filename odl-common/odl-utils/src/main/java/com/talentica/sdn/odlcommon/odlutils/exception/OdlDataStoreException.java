/**
 * 
 */
package com.talentica.sdn.odlcommon.odlutils.exception;

/**
 * @author narenderk
 *
 */
public class OdlDataStoreException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param msg
	 * @param cause
	 */
	public OdlDataStoreException(String msg, Exception cause) {
		super(msg, cause);
	}

}
