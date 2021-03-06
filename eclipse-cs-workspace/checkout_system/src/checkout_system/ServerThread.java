package checkout_system;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import javafx.scene.control.*;

class ServerThread extends Thread {

    private Socket socket = null;
    public String _name = null;
    public TextArea display = null;
    public int relativeId;
    public Queue<String> queue = new LinkedList<>();
    public Queue<String> resp = new LinkedList<>();
    private boolean stop;
    private Server server = null;
    public Client container = null;

    public ServerThread(Socket socket, TextArea display, Server server, Client container) {
        this.socket = socket;
        this.display = display;
        this.server = server;
        this.container = container;
    }

    @Override
    public void run() {

        try{
            stop = false;
            
            DataInputStream in = new DataInputStream( socket.getInputStream() );
            DataOutputStream out = new DataOutputStream( socket.getOutputStream() );
            out.writeUTF("app-name");
            
            System.out.println("ServerThread: " + "app-name");
            
            new Thread(()->queueListener()).start();
            
            String fromClient = null;
            
            while(!stop){
            	String _resp = getClientMessage();
            	
            	if (_resp != null) {
            		System.out.println("Sending msg to " + container.name + ": " + _resp);
            		out.writeUTF(_resp);
            	}
            	
            	if (in.available() != 0) {
            		fromClient = in.readUTF();
            	}
            	
                if (fromClient != null) {
                	System.out.println("msg from client: " + fromClient);
                	
                    if (this._name != null) {
                    	writeToDisplay(this._name + ": sent message - " + fromClient);
                    } else {
                    	writeToDisplay("CLIENT: sent message - " + fromClient);
                    }
                    
                    this.queue.add(fromClient);
                    
                    fromClient = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void queueListener() {
		Boolean remove = false;
		
		while (!remove) {
			String value = null;
			
			try {
				value = ServerThread.this.queue.remove();
	    		
	    		String[] result = value.split(",");
	    		
	    		ServerThread.this.onMessage(result);
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}
    
    public void onMessage(String[] values) {
		String type = values[0];
		
		if (type.equals("app-name")) {
			this._name = values[1];
			this.container.name = values[1];
		} else {
			String msg = "";
			
			for (String str : values) {
				msg += str + ",";
			}
			
			msg += this._name;
			
			this.server.queue.add(msg);
		}
	}
    
    public void writeToDisplay(String msg) {
    	String text = this.display.getText();
    	
    	text += "\n" + msg;
    	
    	this.display.setText(text);
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

    void stopServerTread(){
        stop = true;
    }
}
