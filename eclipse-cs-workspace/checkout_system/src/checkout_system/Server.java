package checkout_system;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.control.*;
import java.sql.*;

public class Server {

    private final ExecutorService pool;
    private final List<Client> clients;
    public Queue<String> queue = new LinkedList<>();
    private final int port;
    private boolean stop;
    private TextArea display;
    int itemId;
    int total = 0;
    int amount = 0;
    String phoneNumber = null;
    String pin = null;
    String cardNumber = null;
    String cardPin = null;
    boolean member = false;
    String receipt = "";

    Server(int port, TextArea display) {
        this.port = port;
        this.display = display;
        pool = Executors.newFixedThreadPool(3);
        clients = new ArrayList<>();
    }

    private void runServer() {

        writeToDisplay("Server enabled: Waiting for client...");
        
        try{
            ServerSocket serverSocket = new ServerSocket(port);
            
            new Thread(()->queueListener()).start();
            
            stop = false;

            while (!stop) {
                Socket clientSocket = serverSocket.accept();
                writeToDisplay("client connected!");
                Client _c = new Client();
                ServerThread thread = new ServerThread(clientSocket, this.display, this, _c);
                _c.thread = thread;
                pool.execute(thread);
                clients.add(_c);
            }
            
            if (stop) {
            	serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void writeToDisplay(String msg) {
    	String text = this.display.getText();
    	
    	text += "\n" + msg;
    	
    	this.display.setText(text);
    }

    public void stop(){
        for( Client client : clients) {
            client.thread.stopServerTread();
        }
        stop = true;
        pool.shutdown();
    }

    public void activate(){
        new Thread(()->runServer()).start();
    }
    
    public void queueListener() {
		Boolean remove = false;
		
		while (!remove) {
			String value = null;
			
			try {
				value = Server.this.queue.remove();
	    		
	    		String[] result = value.split(",");
	    		
	    		String msg = "";
				
				for (String str : result) {
					msg += str + ",";
				}
				
	    		Server.this.onMessage(result, msg);
			} catch (Exception e) {
				// do nothing
			}
		}
	}
    
    public void onMessage(String[] values, String msg) {
		String type = values[0];
		String name = values[values.length - 1];
		Client cd = getClient("customer_display");
		Client cr = getClient("cashier_register");
		Client pas = getClient("payment_authorization_system");
		
		System.out.println("onMessage from " + name + ": type - " + type);
		System.out.println("onMessage from " + name + ": msg - " + values[1]);
		
		if (name.equals("cashier_register")) {
			if (type.equals("scale")) {
				// save id and send name to cd
				itemId = Integer.parseInt(values[1]);
				sendMsg(cd, "scale, " + values[2]);
			} else if (type.equals("get-scale")) {
				// send saved id and amount to cr
				if (amount != 0) {
					sendMsg(cr, String.join(",", "scale", Integer.toString(itemId), Integer.toString(amount)));
				}
			} else if (type.equals("total")) {
				// add item to cart total
				total += Integer.parseInt(values[1]);
			} else if (type.equals("get-total")) {
				// send saved cart total
				sendMsg(cr, String.join(",", "total", Integer.toString(total)));
			} else if (type.equals("display")) {
				sendMsg(cd, msg);
			} else if (type.equals("payment")) {
				sendMsg(cd, msg);
			} else if (type.equals("member")) {
				sendMsg(cd, msg);
			} else if (type.equals("receipt")) {
				receipt += "\n" + values[1];
			} else if (type.equals("print-receipt")) {
				sendMsg(cd, String.join(",", "print-receipt", receipt));
			} else if (type.equals("resupply")) {
				Supplier supplier = new Supplier();
				supplier.makeOrder(Integer.parseInt(values[1]), 10);
			}
		} else if (name.equals("customer_display")) {
			if (type.equals("scale")) {
				// get amount from cd and save amount
				amount = Integer.parseInt(values[1]);
			} else if (type.equals("cart")) {
				sendMsg(cr, msg);
			} else if (type.equals("member")) {
				// verify phone number and send pin message to cd
				phoneNumber = values[1];
				boolean valid = verifyData("members", "members", "phone", values[1]);
				
				if (valid) {
					sendMsg(cd, "pin");
				}
			} else if (type.equals("pin")) {
				// check pin and if valid then save phone number and add earned credits to record
				pin = values[1];
				boolean valid = verifyData("members", "members", "pin", values[1]);
				
				if (valid) {
					member = true;
				}
			} else if (type.equals("payment")) {
				// authorize payment if valid process payment else send error to cd and cr
				cardNumber = values[1];
				cardPin = values[2];
				
				sendMsg(pas, String.join(",", "authorize", cardNumber, cardPin));
			} else if (type.equals("amount")) {
				amount = Integer.parseInt(values[1]);
			}
		} else if (name.equals("payment_authorization_system")) {
			if (type.equals("authorize")) {
				if (values[1].equals("card-authorized")) {
					sendMsg(pas, String.join(",", "process-payment", cardNumber, Integer.toString(total)));
				}
			} else if (type.equals("payment-processed")) {
				if (values[1].equals("success")) {
					sendMsg(cr, "payment," + receipt);
				} else {
					// send cr and cd error messages
				}
			} else if (type.equals("error")) {
				if (values[1].equals("card-number")) {
					sendMsg(cd, String.join(",", "error", "payment-error", "card-number"));
					sendMsg(cr, String.join(",", "error", "payment-error", "card-number"));
				} else {
					sendMsg(cd, String.join(",", "error", "payment-error", "pin-number"));
					sendMsg(cr, String.join(",", "error", "payment-error", "pin-number"));
				}
			}
		}
	}
    
    public boolean verifyData(String addr, String table, String column, String value) {
    	String dbAddr = "jdbc:sqlite:" + addr + ".db";
    	Connector db;
    	Connection conn;
    	
    	db = new Connector(dbAddr);
		conn = db.getConnection();
		
		try {
			String query = "SELECT * FROM " + table + " WHERE " + column + " = ? LIMIT 1";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, value);
			
			ResultSet rs = stmt.executeQuery();
			
			String result = rs.getString(column);
			
			rs.close();
			stmt.close();
			
			if (result.equals(value)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public void sendMsg(Client client, String msg) {
    	client.thread.resp.add(msg);
    }
    
    private Client getClient(String name) {
    	for (Client client : this.clients) {
    		if (client.name.equals(name)) {
    			return client;
    		}
    	}
    	
    	return null;
    }
    

    /*
     * cart - cd
     * scale - cr, cd
     * get-scale - cr
     * member - cr, cd
     * pin - cd
     * total - cr
     * get-total - cr
     * display - cr
     * payment - cr, cd
     */
    
}