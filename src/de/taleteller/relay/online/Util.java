/**
 * www.taleteller.de
 * 
 * TaletellerRelay
 *   Util
 * 
 * Summary:
 *   
 * 
 * History:
 *   
 *   
 * 
 * Ideas:
 *   TODO Implement HttpIO/HttpsIO in TaletellerIO, 
 *        and use that then instead.
 * 
 * Stephan Hogrefe, Edinburgh, 2017
 */
package de.taleteller.relay.online;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import de.taleteller.relay.data.InMessage;
import de.taleteller.relay.data.KeyValuePair;


class Util {

	/**
	 * Handles post request to server.
	 * @param type
	 * @param pairs
	 * @return
	 */
	static String send(String serverurl, KeyValuePair... pairs) {
		HttpsURLConnection con = null;
		StringBuilder answer = new StringBuilder();
		URL url = null;
		try {
			url = new URL(serverurl);

			con = (HttpsURLConnection)url.openConnection();
			con.setReadTimeout(10000);
			con.setConnectTimeout(15000);
			con.setRequestMethod("POST");
			
			con.setDoInput(true);
			con.setDoOutput(true);

			OutputStream os = con.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
			        new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(pairs));
			writer.flush();
			writer.close();
			os.close();
			
			con.connect();

			// execute HTTPS request
			int returnCode = con.getResponseCode();
			//System.out.println("response code: " + returnCode);
			
			InputStream connectionIn = null;
			if (returnCode==200)
				connectionIn = con.getInputStream();
			else
				connectionIn = con.getErrorStream();

			// print resulting stream
			BufferedReader buffer = new BufferedReader(new InputStreamReader(connectionIn));
			String inputLine;
			while ((inputLine = buffer.readLine()) != null) {
				inputLine = inputLine.replace("\\n", "\n");//set the newline char
				answer.append(inputLine + "\n");
			}
			buffer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(con != null)
				con.disconnect();
		}

		//System.out.println(answer.toString());
		return answer.toString();
	}

	static private String getQuery(KeyValuePair... params) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    for (KeyValuePair pair : params)
	    {
	        if (first)
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
	        result.append("=");
	        result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
	
	
	/**
	 * 
	 * @param achs
	 * @return
	 */
	static List<InMessage> parseMessages(String jsonstring) {
		//System.out.println(jsonstring);
        List<InMessage> result = new ArrayList<>();
        
        String author = "";
        long time = 0;
        String tag = "";
        String message = "";
       
        // manually deconstructs json sting here
        for (String messageobject : jsonstring.split("\\}\\{")) {
        	messageobject = messageobject.replace("}", "");
        	messageobject = messageobject.replace("{", "");
			for (String field : messageobject.split(",")) {
				String[] keyvaluepair = field.split(":", 2);
				
				if(keyvaluepair[0].equals("\"r_author\""))
					author = keyvaluepair[1].replace("\"", "");
				else if(keyvaluepair[0].equals("\"r_time\""))
					time = Long.parseLong(keyvaluepair[1].replace("\"", ""));
				else if(keyvaluepair[0].equals("\"r_tag\""))
					tag = keyvaluepair[1].replace("\"", "");
				else if(keyvaluepair[0].equals("\"r_message\""))
					message = keyvaluepair[1].replace("\"", "");
			}
			/* only add new one, if there was actually data retrieved from the server */
			if(time > 0 && (author.length() > 0 || message.length() > 0))
				result.add(new InMessage(message.trim(), tag.trim(), author.trim(), time));
		}
        
        return result;
    }
	
}
