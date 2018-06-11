/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   LocalRelay
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
package de.taleteller.relay.local;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.taleteller.relay.Relay;
import de.taleteller.relay.data.ErrorCode;
import de.taleteller.relay.data.InMessage;
import de.taleteller.relay.data.OutMessage;

/**
 * The local relay basically works like 
 * the online relay, but instead of contacting
 * taleteller.de for communication, messages
 * are send, stored, and received locally.
 * For this a local path can be specified.
 * 
 * Unlike in the online relay, no syncing and
 * passwords are required.
 */
public class LocalRelay implements Relay {

	/** name of this relays owner */
	String name;

	/** path to folder containing the messages */
	String path;

	/** Queue of unread messages */
	private ConcurrentLinkedQueue<InMessage> incoming_messages;
	/** Queue of to be sent messages */
	private ConcurrentLinkedQueue<OutMessage> outgoing_messages;

	/** ErrorCode of last performed server contacting action */
	private ErrorCode lasterror;
	
	//////////////////////////////////////////

	public LocalRelay(String path, String name) {
		this.path = path;
		this.name = name;
		incoming_messages = new ConcurrentLinkedQueue<>();
		outgoing_messages = new ConcurrentLinkedQueue<>();
		
		lasterror = ErrorCode.NONE;
	}
	
	//////////////////////////////////////////

	@Override
	public boolean pullMessages(String tag) {

		// look for the folder with the desired tag
		if (!Files.exists(Paths.get(path + "/" + tag)))
			return false;

		// look for the folder with the correct name
		if (!Files.exists(Paths.get(path + "/" + tag + "/" + name)))
			return false;

		try {

			// look for new messages, and add them to the in queue
			File folder = new File(path + "/" + tag + "/" + name);
			File[] files = folder.listFiles();
			for (File file : files) {
				// check if it is a local relay message type
				if (file.getName().endsWith(".lrm")) {
					String filename = file.getName().replace(".lrm", "");
					// read message
					String author = filename.split("_", 2)[0];
					long datetime = Long.parseLong(filename.split("_", 2)[1]);
					String content = new String(
							Files.readAllBytes(Paths.get(
									path + "/" + tag + "/" + name + "/" + file.getName())));
					// create it and add it to the in queue
					incoming_messages.add(new InMessage(content, tag, author, datetime));
					
					// delete read message
					Files.delete(Paths.get(path + "/" + tag + "/" + name + "/" + file.getName()));
				}
			}

		} catch (IOException e) {
			lasterror = ErrorCode.UNEXPECTED_RESPONSE;
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean hasNext() {
		if (incoming_messages.size() > 0)
			return true;
		return false;
	}

	@Override
	public InMessage getNext() {
		if (incoming_messages.size() > 0)
			return incoming_messages.remove();
		return null;
	}

	@Override
	public void newOutMessage(String message, String tag, String... targets) {
		outgoing_messages.add(new OutMessage(message, tag, name, targets));
	}

	@Override
	public void clearOutQueue() {
		outgoing_messages.clear();
	}

	@Override
	public boolean readyToSend() {
		if (outgoing_messages.size() > 0)
			return true;
		return false;
	}

	@Override
	public boolean sendNext() {

		if (outgoing_messages.size() <= 0)
			return false;

		try {
			// Get next message
			OutMessage next = outgoing_messages.poll();

			// Set up path
			Path path_to_tag = Paths.get(path + "/" + next.getTag());

			// Find or create folder for tag
			if (!Files.exists(path_to_tag))
				Files.createDirectories(path_to_tag);

			// Find or create folders for all targets
			String[] targets = next.getTargetsAsString().split("_");
			for (String target : targets) {
				// set up path
				Path path_to_target = Paths.get(path + "/" + next.getTag() + "/" + target);
				Path path_to_targetfile = Paths.get(path + "/" + next.getTag() + "/" + target + "/" + next.getAuthor()
						+ "_" + System.currentTimeMillis() + ".lrm");

				// Find or create folder for target
				if (!Files.exists(path_to_target))
					Files.createDirectories(path_to_target);

				// create message file with current timestamp
				Files.createFile(path_to_targetfile);
				Files.write(path_to_targetfile, next.getMessage().getBytes());
			}

		} catch (IOException e) {
			lasterror = ErrorCode.UNEXPECTED_RESPONSE;
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public int sendAllMessages() {
		int k = 0;
		while (outgoing_messages.size() > 0) {
			sendNext();
			k++;
		}
		return k;
	}

	@Override
	public ErrorCode getLastError() {
		return lasterror;
	}

	@Override
	public int numberUnreadMessages() {
		return incoming_messages.size();
	}

	@Override
	public int numberQueuedOutgoingMessages() {
		return outgoing_messages.size();
	}

	/**
	 * Sets the path to folder with the relayed message data.
	 * 
	 * @param baseurl
	 */
	public void setLocalPath(String path) {
		this.path = path;
	}

}
