package payment_authorization_system;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;

public class Main extends Application {
	Client client = null;
	Label error = null;
	TextArea display = null;
	Button submit = null;
	String cardNumber = null;
	String cardPin = null;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			// display area for transaction details and results
			display = new TextArea();
			display.setText("Internal System Display");
			display.setEditable(false);
			
			// create a websocket connection to checkout_system
			try {
				System.out.println("creating new client");
				this.client = new Client("localhost", 4009);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// positioning controls
			VBox output = new VBox(display);
			output.setAlignment(Pos.CENTER);
			
			root.setCenter(output);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("activating client");
		client.activate();
		System.out.println("creating new socketListener thread");
		new Thread(()->socketListener()).start();
	}
	
	public void socketListener() {
		Boolean remove = false;
		
		while (!remove) {
			String value = null;
			
			if (Main.this.client.eventHandled) {
				try {
					Main.this.client.eventHandled = false;
					
					value = Main.this.client.queue.remove();
		    		
		    		String[] result = value.split(",");
		    		
		    		Main.this.onResponse(result);
				} catch (Exception e) {
					Main.this.client.eventHandled = true;
				}
			}
		}
	}
	
	public void onResponse(String[] values) {
		String type = values[0];
		
		if (type.equals("authorize")) {
			// authorize payment
			
			String cardNumber = values[1];
			String cardPin = values[2];
			
			boolean valid = verifyData("bank", "bank_accounts", "card_number", cardNumber);
			
			if (valid) {
				valid = verifyData("bank", "bank_accounts", "pin", cardPin);
			} else {
				client.resp.add(String.join(",", "error", "card-number"));
			}
			
			if (valid) {
				client.resp.add(String.join(",", "authorize", "card-authorized"));
			} else {
				client.resp.add(String.join(",", "error", "pin-number"));
			}
		} else if (type.equals("process-payment")) {
			int balance = getData("bank", "bank_accounts", "card_number", values[1], "amount");
			int amount = balance - Integer.parseInt(values[2]);
			
			modifyData("bank", "bank_accounts", "amount", amount, "card_number", values[1]);
			
			client.resp.add("payment-processed, success");
		} else if (type.equals("app-name")) {
			client.resp.add("app-name, payment_authorization_system");
		}
		
		this.client.eventHandled = true;
	}
	
	public int getData(String addr, String table, String column, String value, String column1) {
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
			
			int result = rs.getInt(column1);
			
			rs.close();
			stmt.close();
			
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
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
			
			if (result == value) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
    }
	
	public void modifyData(String addr, String table, String column, int value, String column1, String value1) {
		String dbAddr = "jdbc:sqlite:" + addr + ".db";
    	Connector db;
    	Connection conn;
    	
    	db = new Connector(dbAddr);
		conn = db.getConnection();
		
		try {
			String query = "UPDATE " + table + " SET " + column + " = ? WHERE " + column1 + " = ? LIMIT 1";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, value);
			stmt.setString(2, value1);
			
			stmt.executeUpdate();
			
			stmt.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			client.resp.add(String.join(",", "error", "insufficient-funds"));
		}
	}
	
	public void displayInfo(String info) {
		String existingInfo = this.display.getText();
		
		this.display.setText(existingInfo + "\n" + info);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
