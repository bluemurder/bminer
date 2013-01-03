/*
 *  bminer - the first bitcoin miner for Android OS 
 *  Copyright (C) 2013  Alessio Leoncini
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/
 */

package bluemurder.miner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Date;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Base64;

public class MinerTask extends AsyncTask<Object, Object, Object> {
	
	public BminerActivity activity;
	public boolean running = false;
	private MessageDigest h1;
	private String authPair;
	private JSONObject response;
	private JSONObject result;
	private Socket socket;
	private PrintWriter writer;
	private BufferedReader reader;
	private static final int BUFFER_SIZE = 2048;
	private String target;
	private String data;
    private boolean found;
	private long nonce;
	private byte[] bytes;
	private byte[] noncebin = new byte[4];
	private byte[] nonceok = new byte[4];
	private static final long MAX_LONG = 4294967290L;
	
	public MinerTask(BminerActivity a) {
		// Save reference to parent thread
		activity = a;
	    // Initialize hashing engines
	    try {
			h1 = MessageDigest.getInstance("SHA-256");
		} catch (Exception e) {
			activity.SetStatus("Stopped");
			activity.SetInfo("Error:"+e);
		}
	    
	    
	}
	
    // Miner process
    void Mine(){
    	try {
    		/* working code
    		data="00000001b0177d9afce82f60614c6423740558742f3de9a876f8d7a60000057d00000000e94d08a92cb44c918d1e8b9c86a52ac40d3715ba9faa433fca7296531a175b4450ddf6d61a05a16b00000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000";
    		String blockHeader="010000009a7d17b0602fe8fc23644c6174580574a8e93d2fa6d7f8767d05000000000000a9084de9914cb42c9c8b1e8dc42aa586ba15370d3f43aa9f539672ca445b171ad6f6dd506ba1051a";
    		String nonce="7b000000";
    		h1.reset();
    		h1.update(hexStringToByteArray(blockHeader));
    		h1.update(hexStringToByteArray(nonce));
    		byte [] hashed = h1.digest();
    		tv_info.setText("hashed:"+ByteArrayToHexString(hashed));
    		// result must be ed8e85093d375d6eff4fd0320137fee07c0b86487420a50fc63c96a3ef15da9d
    		 end of working code */
    		
    		activity.SetStatus("Mining...");
    		
    		//data="00000001b0177d9afce82f60614c6423740558742f3de9a876f8d7a60000057d00000000e94d08a92cb44c918d1e8b9c86a52ac40d3715ba9faa433fca7296531a175b4450ddf6d61a05a16b00000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000";
    		// Retrieve header (first 152 characters = first 76 bytes)
    		String blockHeader = data.substring(0, 152);
    		// Change block header endianness
    		byte [] reversedHeader = ChangeEndianness(hexStringToByteArray(blockHeader));
    		// Test nonces
    		found=false;
    		long t1 = new Date().getTime();
    		for(nonce=0; nonce < 1000; nonce++){
    			// Nonce converted to byte[] and swapped
    			bytes = ByteBuffer.allocate(8).putLong(nonce).array();
    			noncebin[0]=bytes[7];
    			noncebin[1]=bytes[6];
    			noncebin[2]=bytes[5];
    			noncebin[3]=bytes[4];
        		// Hash header first
    			h1.reset();
        		h1.update(reversedHeader);
    			h1.update(noncebin);
    			// hash of header+nonce
    			bytes = h1.digest();
    			// hash of hash of header+nonce
    			bytes = h1.digest(bytes);
    			// If hash starting with four bytes to zero
    			if(bytes[0]==0x00){
    				if(bytes[1]==0x00){
    					if(bytes[2]==0x00){
    						if(bytes[3]==0x00){
								found=true;
								return;
    						}
    					}
    				}
    			}
    		}	
    		long t2 = new Date().getTime();
    		float speed = (float)1000/(t2 - t1);
    		activity.SetSpeed(String.format("%.1f",speed)+" KH/s");
    		for(nonce=1000; nonce < MAX_LONG; nonce++){
    			// Nonce converted to byte[] and swapped
    			bytes = ByteBuffer.allocate(8).putLong(nonce).array();
    			noncebin[0]=bytes[7];
    			noncebin[1]=bytes[6];
    			noncebin[2]=bytes[5];
    			noncebin[3]=bytes[4];
        		// Hash header first
    			h1.reset();
        		h1.update(reversedHeader);
    			h1.update(noncebin);
    			// hash of header+nonce
    			bytes = h1.digest();
    			// hash of hash of header+nonce
    			bytes = h1.digest(bytes);
    			// If hash starting with four bytes to zero
    			if(bytes[0]==0x00){
    				if(bytes[1]==0x00){
    					if(bytes[2]==0x00){
    						if(bytes[3]==0x00){
								found=true;
								return;
    						}
    					}
    				}
    			}
    			if(running==false)
    				return;
    		}	
		} catch (Exception e) {
			running=false;
			activity.SetStatus("Stopped");
			activity.SetInfo("Error:"+e);
		}
    }
    
    // Takes a string of printed bytes and convert it to bytes
    byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    // Prints bytes array
    String ByteArrayToHexString(byte [] mdbytes){//convert the byte to hex format method 2
	    StringBuffer hexString = new StringBuffer();
	    String byteString;
		for (int i=0;i<mdbytes.length;i++) {
			byteString = Integer.toHexString(0xFF & mdbytes[i]);
			if(byteString.length()==1)
				hexString.append("0");
			hexString.append(byteString);
		}
		return hexString.toString();
    }
    
    // Change endianness of a byte array
    byte [] ChangeEndianness(byte [] bytes){
    	byte [] result = new byte[bytes.length];
    	int i;
    	// Swap every 4 bytes
    	for(i=0; i<(bytes.length);i+=4){
    		result[i]=bytes[i+3];
    		result[i+1]=bytes[i+2];
    		result[i+2]=bytes[i+1];
    		result[i+3]=bytes[i];
    	}
    	return result;
    }
    
    // RPC-JSON communication
    void RpcJSON(String method, String params, int id){
        // Initialize connection protocol
    	activity.SetStatus("Getting work...");
        Connect(); 
    	try {
    	    String content;
    	    String responseText = "";
    	    int charsRead;
    	    char[] buffer = new char[BUFFER_SIZE];
    		// Build request fields
    	    if(params==null){
    	    	content="{\"method\": \""+method+"\",\"params\": [], \"id\": "+id+"}";
    	    	//content = content.replaceAll("[\n\r]", "");
    	    }
    	    else{
    	    	content="{\"method\": \""+method+"\",\"params\": [ \""+params+"\" ], \"id\": "+id+"}";
    	    	//content = content.replaceAll("[\n\r]", "");
    	    }
    	    // Send content
    	    writer.println("Content-Length: "+content.length());
    	    writer.println("");
    		writer.println(content);
    		writer.println("");// end of communication!
    		writer.flush();
			// Read response
    		charsRead=reader.read(buffer);
//			while((charsRead=reader.read(buffer)) > 0){
				responseText += new String(buffer).substring(0, charsRead);
//			}
			charsRead=responseText.indexOf("{");
			response = new JSONObject(responseText.substring(charsRead));
//			activity.SetInfo(responseText);
		} catch (Exception e) {
			running=false;
			activity.SetStatus("Stopped");
			activity.SetInfo("Error:"+e);
		}
    	// Disconnect socket
		Disconnect();
    }
    
    void Disconnect(){
	    try{
	    	if(socket!=null){
	    		if(socket.isConnected()){
	    	    	if(reader!=null)
	    	    		reader.close();
	    			if(writer!=null)
	    				writer.close();
	    			socket.close();	    			
	    		}
	    	}
		} catch (Exception e) {
			running=false;
			activity.SetStatus("Stopped");
			activity.SetInfo("Error:"+e);
		}
    }
    
    // Initialize connection protocol
    // http://stackoverflow.com/questions/2307291/getting-raw-http-response-headers
    void Connect(){
    	try {
            // Encode authentication data
            EncodeAuth();
    		String hostname = activity.rpchost;
    		int port = Integer.parseInt(activity.rpcport);
    		// Create the socket and IO objects
    		socket = new Socket(hostname,port);
    		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		// Send headers
    		writer.println("POST / HTTP/1.1");
            writer.println("Host: " + hostname+":"+String.valueOf(port));
            writer.println("Accept: */*");
            writer.println("Content-type: application/json");
            writer.println("Authorization: "+authPair);
            writer.println("User-Agent: bminer for android");
		} catch (Exception e) {
			running=false;
			activity.SetStatus("Stopped");
			activity.SetInfo("Error:"+e);
		}
    }
    
    // Encode base64 authentication pair
    void EncodeAuth(){
    	byte [] enc;
    	// build authentication pair with base64("user:pass")
    	authPair = new String(activity.rpcuser+":"+activity.rpcpass);
    	enc = authPair.getBytes();
    	enc = Base64.encode(enc,0);
    	authPair = "Basic "+new String(enc);
    	// Remove newline
    	authPair = authPair.replaceAll("[\n\r]", "");
    }

	@Override
	protected Object doInBackground(Object... arg0) {
		activity.SetStatus("Mining...");
		while(running){
			// Request work
			RpcJSON("getwork",null,0);
			// Read response
			try {
				// Read "error" field, it must be "null"
				if(response.getString("error")!="null"){
					activity.SetInfo("Error on getwork response...");
					continue;
				}
				// Retrieve JSON object "result" with fields "data" and "target"
				result = response.getJSONObject("result");
				target = result.getString("target");
				data = result.getString("data");
//				activity.SetInfo("data:"+data+"\ntarget:"+target);
			} catch (Exception e) {
				running=false;
				activity.SetStatus("Stopped");
				activity.SetInfo("Error:"+e);
			}
			// Mine!
			Mine();
			if(running==false)
				return null;
			// If solution found, send it
			if(found){
				nonceok[0]=noncebin[3];
				nonceok[0]=noncebin[3];
				nonceok[0]=noncebin[3];
				nonceok[0]=noncebin[3];
				RpcJSON("getwork",data.substring(0, 152)+ByteArrayToHexString(nonceok)+data.substring(160,256),1);
				// Save response fields
				try {
					// Read "error" field, it must be "null"
					if(response.getString("error")!="null"){
						activity.SetInfo("Error on response after submission");
						continue;
					}
					// Retrieve boolean "result"
					if(response.getBoolean("result"))
						activity.SetInfo("Accepted "+ByteArrayToHexString(nonceok)+"!!");
					else
						activity.SetInfo("Rejected "+ByteArrayToHexString(nonceok));
				} catch (Exception e) {
					running=false;
					activity.SetStatus("Stopped");
					activity.SetInfo("Error:"+e);
				}
			}
		}
		return null;
	}
	
    protected void onPostExecute() {
    	running=false;
    }

}
