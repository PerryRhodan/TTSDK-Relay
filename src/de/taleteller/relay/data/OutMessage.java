/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   OutMessage
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
public class OutMessage extends Message {

	/** the usernames of the indended
	 *  receivers of the message.*/
	private String[] targets;
	
	//////////////////////////////////////////
	
	public OutMessage(String content, String tag, String author, String[] targets) {
		super(content, tag, author);
		this.targets = targets;
	}
	
	//////////////////////////////////////////
	
	public String getTargetsAsString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < targets.length; i++) {
			result.append(targets[i]);
			if(i < targets.length-1)
				result.append("_");
		}
		return result.toString();
	}
	
}
