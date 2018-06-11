/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   OnlineRelay
 * 
 * Summary:
 *   
 * 
 * History:
 *   25.11.2017 - Cleaning of code
 *   
 * 
 * Ideas:
 *   -  TODO keep password hashed in memory
 * 
 * Stephan Hogrefe, Edinburgh, 2017
 */
package de.taleteller.relay.online;


import java.util.concurrent.ConcurrentLinkedQueue;

import de.taleteller.relay.Relay;
import de.taleteller.relay.data.ErrorCode;
import de.taleteller.relay.data.InMessage;
import de.taleteller.relay.data.KeyValuePair;
import de.taleteller.relay.data.OutMessage;
import de.taleteller.relay.data.TaletellerRelayException;

/**
 * The online relay requires an accound on www.taleteller.de.
 * 
 */
public class OnlineRelay implements Relay {
	
	/** Version code */
	private final int version = 1000;
	
	/** Server url */
	//private String server_base = "https://localhost/taleteller/";
	private String server_base = "https://www.taleteller.de/";
	private String server_relayapi = "api.php?type=relay&subtype=";

	/* account details */
	private String email;					//
	private String name;					//gets pulled on sync
	private String password;				// TODO keep hashed in memory
	private long mintimebetweenmessages;	//gets pulled on sync, time required to 
											//wait between sending messages in s
	
	/** current sync value */
	private String sync;
	
	/** flag to determine sync status */
	private boolean isSynced;
	
	/** Queue of unread messages */
	private ConcurrentLinkedQueue<InMessage> incoming_messages;
	/** Queue of to be sent messages */
	private ConcurrentLinkedQueue<OutMessage> outgoing_messages;
	
	/** timestamp of when the last messages was sent */
	private long times_lastmessagesent;

	/** ErrorCode of last performed server contacting action */
	private ErrorCode lasterror;
	
	///////////////////////////////////////////////////
	
	public OnlineRelay(String email, String password) {
		this.email = email;
		this.password = password;
		incoming_messages = new ConcurrentLinkedQueue<>();
		outgoing_messages = new ConcurrentLinkedQueue<>();
		
		sync = "";
	}
	
	///////////////////////////////////////////////////
	
	private ErrorCode action_sync() {
		/* build message and contact server */
		KeyValuePair[] data = {
				new KeyValuePair("email", email),
				new KeyValuePair("pw", password),
				new KeyValuePair("version", "" + version)};

		String[] answer = Util.send(server_base + server_relayapi + "sync", data).split("_");

		
		
		/* check for unexpected result */
		if(answer.length < 3)
			return ErrorCode.UNEXPECTED_RESPONSE;
		
		/* check return code for error */
		ErrorCode errorcode = ErrorCode.getErrorCode(answer[0]);
		
		/* No error */
		if(errorcode.equals(ErrorCode.NONE)) {
			/* set new sync */
			sync = answer[1].trim();
			/* set name */
			name = answer[2].trim();
			/* set status */
			mintimebetweenmessages = Long.parseLong(answer[3].trim());
			/* set sync flag */
			isSynced = true;
		}

		return errorcode;
	}
	
	private ErrorCode action_writeMessage(OutMessage message) {

		/* build message and contact server */
		String targets = message.getTargetsAsString();
		KeyValuePair[] data = {
				new KeyValuePair("sync", sync),
				new KeyValuePair("targets", "" + targets),
				new KeyValuePair("tag", message.getTag()),
				new KeyValuePair("msg", message.getMessage()),
				new KeyValuePair("version", "" + version)};

		/* send message */
		String[] answer = Util.send(server_base + server_relayapi + "send",data).split("_");
		/* remember time */
		times_lastmessagesent = System.currentTimeMillis();
		
		/* check for unexpected result */
		if(answer.length < 2)
			return ErrorCode.UNEXPECTED_RESPONSE;

		/* check return code for error */
		ErrorCode errorcode = ErrorCode.getErrorCode(answer[0]);
		
		/* No error */
		if(errorcode.equals(ErrorCode.NONE)) {
			/* set new sync */
			sync = answer[1].trim();
		}

		return errorcode;
	}
	
	/**
	 * Contacts server and adds potential new messages to the incoming messages queue.
	 * @return
	 */
	private ErrorCode action_pullMessages(String tag) {

		/* build message and contact server */
		KeyValuePair[] data = {
				new KeyValuePair("sync", sync),
				new KeyValuePair("tag", tag),
				new KeyValuePair("version", "" + version)};

		String[] answer = Util.send(server_base + server_relayapi + "pull",data).split("_",3);
		
		/* check for unexpected result */
		if(answer.length < 2)
			return ErrorCode.UNEXPECTED_RESPONSE;

		/* check return code for error */
		ErrorCode errorcode = ErrorCode.getErrorCode(answer[0]);
		
		/* check if answer contains no message part */
		if(answer.length < 3)
			return errorcode;

		/* No error, and at least one message */
		if(errorcode.equals(ErrorCode.NONE)) {
			
			/* add messages in answer to inmessages list */
			for (InMessage msg : Util.parseMessages(answer[2])) {
				incoming_messages.add(msg);
			}

			/* set new sync */
			sync = answer[1].trim();
		}
		
		return errorcode;
	}
	
	/**
	 * Makes sure client is synced.
	 */
	private void handleSync() throws TaletellerRelayException {
		if(isSynced)
			return;
		
		lasterror = action_sync();
		if(!lasterror.equals(ErrorCode.NONE))
			throw new TaletellerRelayException(lasterror.getDescription());
	}
	
	///////////////////////////////////////////////////

	/**
	 * Contacts server and adds potential new messages to the incoming messages queue.
	 * Returns true on success, ie no error occurred. This does not mean a message was 
	 * received, as there might just be none at this moment in time.
	 * @return
	 */
	@Override
	public boolean pullMessages(String tag) {
		try {
			handleSync();
			/* pull messages */
			lasterror = action_pullMessages(tag);
			
			/* return on success */
			if(lasterror.equals(ErrorCode.NONE))
				return true;
			
			/* handle resync */
			if(lasterror.equals(ErrorCode.RESYNC_REQUIRED)) {
				/* set flag to false and call method again TODO have a infinite loop preventing counter */
				isSynced = false;
				pullMessages(tag);
			}

		} catch (TaletellerRelayException e) {
			e.printStackTrace();
			return false;
		}
		
		return false;
	}
	
	
	/**
	 * Returns true if the relay has an unread message ready.
	 * @return
	 */
	@Override
	public boolean hasNext() {
		if(incoming_messages.size() > 0)
			return true;
		return false;
	}
	
	/**
	 * Returns next available unread message.
	 * @return
	 */
	@Override
	public InMessage getNext() {
		if(incoming_messages.size() > 0)
			return incoming_messages.remove();
		return null;
	}
	
	/**
	 * Add a new message to the outgoing message queue.
	 * @param message
	 */
	@Override
	public void newOutMessage(String message, String tag, String... targets) {
		outgoing_messages.add(new OutMessage(message, tag, name, targets));
	}
	
	/**
	 * Clears all queued out messages, discarding any potential message in it, 
	 * which are then therefore not sent
	 */
	@Override
	public void clearOutQueue() {
		outgoing_messages.clear();
	}
	
	/**
	 * Returns true if the next message in the outgoing message queue 
	 * can be send. Ie enough time since the last message has passed.
	 * Also returns false if the queue is empty.
	 * @return
	 */
	@Override
	public boolean readyToSend() {
		if(outgoing_messages.size() == 0)
			return false;
		if(System.currentTimeMillis() - times_lastmessagesent < mintimebetweenmessages * 1000l)
			return false;
		return true;
	}
	
	/**
	 * Sends a message from the outgoing message queue IF POSSIBLE.
	 * Returns true when a message has been sent without error.
	 * Also returns true when the queue is empty.
	 * (Only tries to send when it thinks enough time has passed locally)
	 * 
	 * @return
	 */
	@Override
	public boolean sendNext() {
		if(outgoing_messages.size() == 0)
			return true;
		if(!readyToSend())
			return false;
		
		try {
			handleSync();
			
			/* get new message */
			OutMessage msg = outgoing_messages.poll();
			/* send message */
			lasterror = action_writeMessage(msg);
			
			/* return on success */
			if(lasterror.equals(ErrorCode.NONE))
				return true;
			
			/* handle resync */
			if(lasterror.equals(ErrorCode.RESYNC_REQUIRED)) {
				/* set flag to false and call method again.
				 * This will make it attempt to re-sync */
				isSynced = false;
				action_writeMessage(msg);
			}
				
		} catch (TaletellerRelayException e) {
			e.printStackTrace();
			return false;
		}

		return false;
	}
	
	/**
	 * Sends out all messages currently in the out queue.
	 * BLOCKS THREAD! until all messages are sent, which 
	 * depends on the number of messages queued.
	 * Returns number of messages sent.
	 */
	@Override
	public int sendAllMessages() {
		int count = 0;
		while (outgoing_messages.size() > 0) {
			if(readyToSend()) {
				sendNext();
				count ++;
			}
			try {
				Thread.sleep(mintimebetweenmessages * 500);	//check two, ie 500ms = 1000ms/2, times during the min wait period
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	
	/**
	 * Returns current error code of last performed action.
	 * @return
	 */
	@Override
	public ErrorCode getLastError() {
		return lasterror;
	}
	
	/**
	 * Returns the current size of the incoming messages queue, 
	 * ie the number of unhandled received messages.
	 * @return
	 */
	@Override
	public int numberUnreadMessages() {
		return incoming_messages.size();
	}
	
	/**
	 * Returns the current size of the outgoing messages queue, 
	 * ie the number of messages queued to be send out.
	 * @return
	 */
	@Override
	public int numberQueuedOutgoingMessages() {
		return outgoing_messages.size();
	}
	
	/**
	 * Expected format:
	 * https://localhost/taleteller/
	 * ie the location of the api.php file.
	 */
	public void setServerURL(String baseurl) {
		server_base = baseurl;
	}
}
