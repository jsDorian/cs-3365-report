package cashier_register;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import java.sql.*;

public class itemIdHandler implements EventHandler<ActionEvent> {
	TextField field;
	Label error;
	TextArea display;
	String dbAddr = "jdbc:sqlite:stock.db";
	Connector db;
	Connection conn;
	Boolean firstUse;
	Client client;
	
	public itemIdHandler(TextField field, Label error, TextArea display, Boolean firstUse, Client client) {
		this.field = field;
		this.error = error;
		this.display = display;
		this.firstUse = firstUse;
		this.client = client;
		
		// get connection to stock database
		db = new Connector(dbAddr);
		conn = db.getConnection();
	}
	
	@Override
	public void handle(ActionEvent event) {
		if (firstUse) {
			// send message to customer_display requesting membership info after scanning first item 
			requestMembership();
			firstUse = false;
		}
		
		// get product name from control
		String name = field.getText();
		
		field.setText("");
		
		// get product record from stock database and display error if item not found
		try {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE name = ? LIMIT 1");
			stmt.setString(1, name);
			
			ResultSet rs = stmt.executeQuery();
			
			int rsLength = 0;
			
			while(rs.next()) {
				int id = rs.getInt("id");
				name = rs.getString("name");
				int price = rs.getInt("price");
				int amount = rs.getInt("amount");
				int weight = rs.getInt("weight");
				int bulk = rs.getInt("bulk");
				
				Item item = new Item(name, price, amount, weight, bulk, id);
				
				// update register display with item information
				if (amount > 0) {
					displayInfo(item);
				} else {
					this.error.setText("Out of stock!");
					this.error.setVisible(true);
					this.client.resp.add("resupply," + id);
				}
				
				rsLength = rs.getRow();
			}
			
			// set error if result set is empty because the item is not in inventory
			if (rsLength == 0) {
				this.error.setVisible(true);
			}
			
			rs.close();
			stmt.close();
			
			//conn.close();
		} catch (Exception e) {
			System.out.println("error: " + e.getMessage());
		}
	}
	
	// updates register display with item information
	public void displayInfo(Item item) {
		String info = "";
		
		// instruct cashier to weigh items if the item is a bulk item
		if (item.bulk == 1) {
			info += "Bulk item! Please weigh the item!\n";
			
			// send message to customer_display to weigh items
			weighItem(item.id, item.name);
		} else {
			info += "Name: " + item.name + "\n";
			info += "Price: $" + item.price + "\n";
			
			// send price to checkout_system
			addToTotal(item.price);
			
			// update receipt
			addToReceipt(info);
		}
		
		String existingInfo = this.display.getText();
		
		this.display.setText(existingInfo + "\n" + info);
		
		// send display text to checkout_system to be forwarded to customer_display
		displayToCustomer(info);
	}
	
	public void addToReceipt(String info) {
		String msg = String.join(",", "receipt", info);
		this.client.resp.add(msg);
	}
	
	public void requestMembership() {
		System.out.println("sending member msg");
		this.client.resp.add("member");
	}
	
	public void weighItem(int id, String name) {
		String msg = String.join(",", "scale", Integer.toString(id), name);
		this.client.resp.add(msg);
	}
	
	public void addToTotal(int price) {
		String msg = String.join(",", "total", Integer.toString(price));
		this.client.resp.add(msg);
	}
	
	public void displayToCustomer(String info) {
		System.out.println("sending display msg");
		String msg = String.join(",", "display", info);
		this.client.resp.add(msg);
	}
}
