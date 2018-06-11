/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   ErrorCode
 * 
 * Summary:
 *   
 * 
 * History:
 *   25.11.2017 - Cleaning of code
 *   
 * 
 * Ideas:
 *   
 * 
 * Stephan Hogrefe, Edinburgh, 2017
 */
package de.taleteller.relay.data;

/**
 *
 */
public class ErrorCode {

	public static final ErrorCode NONE
				= new ErrorCode( 1, "No error.");
	public static final ErrorCode RESYNC_REQUIRED
				= new ErrorCode(0, "The used sync is valid, but is too old. Resync required.");
	public static final ErrorCode INVALID_SYNC
				= new ErrorCode(-1, "The used sync value did not match any value in the database.");
	public static final ErrorCode INVALID_FORMAT
				= new ErrorCode(-2, "The sent data is incomplete or inconsistent.");
	public static final ErrorCode INVALID_CREDENTIALS
				= new ErrorCode(-3, "The sent credentials are invalid - cannot sync.");
	public static final ErrorCode SENTTOOFREQUENT
				= new ErrorCode(-4, "Cannot send another message just yet. Between two messages there must be "
						+ "a pause of a few seconds.");
	public static final ErrorCode ACCOUNT_NOTAUTHORIZED
				= new ErrorCode(-5, "The account is not authorized. (Not yet activated or Banned)");
	public static final ErrorCode UNEXPECTED_RESPONSE
				= new ErrorCode(-10, "Unexpected/Invalid response from server.");

	public static ErrorCode getErrorCode(int code) {
		switch (code) {
		case 1:
			return NONE;
		case 0:
			return RESYNC_REQUIRED;
		case -1:
			return INVALID_SYNC;
		case -2:
			return INVALID_FORMAT;
		case -3:
			return INVALID_CREDENTIALS;
		case -4:
			return SENTTOOFREQUENT;
		case -10:
			return UNEXPECTED_RESPONSE;
			
		default:
			throw new TaletellerRelayException("Unkown error code: " + code);
		}
	}
	
	public static ErrorCode getErrorCode(String code) {
		return getErrorCode(Integer.parseInt(code));
	}
	
	final int code;
	final String description;
	
	private ErrorCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}

}
