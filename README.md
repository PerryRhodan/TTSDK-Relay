# TTSDK-Relay
Provides relay classes that can be used for online communication through https://www.taleteller.de using http requests.

This requires you to have an account on https://www.taleteller.de/ (which is totally free and does not want any personal information).

The following example shows how to use the online relay. The local relay works similar.

```java
// You require an account with
// email and password
String email = "mytest@mail.com";
String password = "mypassword";

// Create the relay
OnlineRelay relay = new OnlineRelay(email, password);

// Decide on a tag to be used
String tag = "game_sunday";
String message = "A message regarding our sunday game session";

// The names of your targets - these are
// again the usernames on www.taleteller.de. 
// This could be your own name as well,
// if you want to send a message to yourself
String target1 = "Joe";
String target2 = "Jane";
String[] targets = {target1, target2};

// Queue the message in the relay
// You can queue several messages here
// before they are send out
relay.newOutMessage(message, tag, targets);

// when ready, you can send out a message.
// Keep in mind there is a delay of >10 seconds
// imposed in how often messages can be sent.
// So we can test that first:
if(relay.readyToSend())
	relay.sendNext();

// To check if you have received a message,
// call the pullMessages and then check
// if there is a new unread message to 
// be handled
relay.pullMessages(tag);
if(relay.hasNext()) {
	InMessage newmessage = relay.getNext();
	System.out.println(newmessage.getMessage());	// prints the contents of the message
}
```
