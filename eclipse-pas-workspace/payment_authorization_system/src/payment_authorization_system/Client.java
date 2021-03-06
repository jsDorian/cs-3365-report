package payment_authorization_system;

import java.util.LinkedList;
import java.util.Queue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Client{
    private final int port;
    private final String host;
    private boolean stop;
    public Queue<String> queue = new LinkedList<>();
    public Queue<String> resp = new LinkedList<>();
    public boolean eventHandled = true;
    
    Client(String host, int port) {
        this.port = port;
        this.host = host;
    }

    private void runClient() {
        try {
            stop = false;
            Socket socket = new Socket(host, port);
            DataInputStream in = new DataInputStream( socket.getInputStream() );
            DataOutputStream out = new DataOutputStream( socket.getOutputStream() );

            while (!stop) {
                TimeUnit.SECONDS.sleep(3);
                
                String msg = getClientMessage();
                
                if (msg != null) {
                	System.out.println("sending msg to server: " + msg);
                	out.writeUTF(msg);
                }
                
                String _queue = null;
                
                if (in.available() != 0) {
                	_queue = in.readUTF();
                }
                
                if (_queue != "" && _queue != null) {
                	this.queue.add(_queue);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Client activate() {
        new Thread(()->runClient()).start();
        return this;
    }

    public void stop() {
        stop = true;
    }

    private String getClientMessage() {
    	if (resp.size() > 0) {
    		try {
	    		String value = resp.remove();
	    		System.out.println("getClientMessage: " + value);
	    		return value;
    		} catch (Exception e) {
    			
    		}
    	} else {
    		//System.out.println("Queue is empty!");
    	}
    	
    	return null;
    }
}