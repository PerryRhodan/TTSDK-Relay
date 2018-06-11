/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   KeyValuePair
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
public class KeyValuePair {

	private String key;
	private String value;
	
	//////////////////////////////////////////
	
	public KeyValuePair(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	//////////////////////////////////////////
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	
}
