/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   Relay
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
package de.taleteller.relay;

import de.taleteller.relay.data.ErrorCode;
import de.taleteller.relay.data.InMessage;

/**
 * The relay interface providing the methods used in 
 * online or local relay.
 */
public interface Relay {

	/**
	 * Contacts server and adds potential new messages to the incoming messages queue.
	 * Returns true on success, ie no error occurred. This does not mean a message was 
	 * received, as there might just be none at this moment in time.
	 * @return
	 */
	public boolean pullMessages(String tag);
	
	
	/**
	 * Returns true if the relay has an unread message ready.
	 * @return
	 */
	public boolean hasNext();
	
	/**
	 * Returns next available unread message.
	 * @return
	 */
	public InMessage getNext();
	
	/**
	 * Add a new message to the outgoing message queue.
	 * @param message
	 */
	public void newOutMessage(String message, String tag, String... targets);
	
	/**
	 * Clears all queued out messages, discarding any potential message in it, 
	 * which are then therefore not sent
	 */
	public void clearOutQueue();
	
	/**
	 * Returns true if the next message in the outgoing message queue 
	 * can be send. Ie enough time since the last message has passed.
	 * Also returns false if the queue is empty.
	 * @return
	 */
	public boolean readyToSend();
	
	/**
	 * Sends a message from the outgoing message queue IF POSSIBLE.
	 * Returns true when a message has been sent without error.
	 * Also returns true when the queue is empty.
	 * (Only tries to send when it thinks enough time has passed locally)
	 * 
	 * @return
	 */
	public boolean sendNext();
	
	/**
	 * Sends out all messages currently in the out queue.
	 * BLOCKS THREAD! until all messages are sent, which 
	 * depends on the number of messages queued.
	 * Returns number of messages sent.
	 */
	public int sendAllMessages();
	
	/**
	 * Returns current error code of last performed action.
	 * @return
	 */
	public ErrorCode getLastError();
	
	/**
	 * Returns the current size of the incoming messages queue, 
	 * ie the number of unhandled received messages.
	 * @return
	 */
	public int numberUnreadMessages();
	
	/**
	 * Returns the current size of the outgoing messages queue, 
	 * ie the number of messages queued to be send out.
	 * @return
	 */
	public int numberQueuedOutgoingMessages();
	
}
