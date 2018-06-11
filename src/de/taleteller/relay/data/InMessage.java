/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   InMessage
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
public class InMessage extends Message {

	/** timestamp of when the message 
	 *  was sent by the author, in s */
	private long datetime;
	
	//////////////////////////////////////////
	
	public InMessage(String content, String tag, String author, long datetime) {
		super(content, tag, author);
		this.datetime = datetime;
	}
	
	//////////////////////////////////////////

	public long getDatetime() {
		return datetime;
	}
	
	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("[" + tag + "] From " + author + " sent at " + datetime + ":\n");
		string.append(message);
		return string.toString();
	}
}
