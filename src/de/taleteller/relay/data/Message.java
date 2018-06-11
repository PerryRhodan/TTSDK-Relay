/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   Message
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
abstract class Message {

	/** the author of the message, ie
	 *  the taleteller.de username.  */ 
	protected String author;
	/** the message itself */
	protected String message;
	/** tag of the message to allow 
	 *  sending and receiving messages 
	 *  specific to one topic/tag */
	protected String tag;
	
	//////////////////////////////////////////

	protected Message(String message, String tag, String author) {
		this.message = message;
		this.tag = tag;
		this.author = author;
	}
	
	//////////////////////////////////////////

	public String getMessage() {
		return message;
	}
	
	public String getTag() {
		return tag;
	}

	public String getAuthor() {
		return author;
	}
	
}
