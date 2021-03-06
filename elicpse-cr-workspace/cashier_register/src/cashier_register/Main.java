package cashier_register;

import java.sql.*;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Scene;

public class Main extends Application {
	Boolean firstUse = true;
	Client client = null;
	TextField productId = null;
	Label error = null;
	TextArea registerDisplay = null;
	Register register = new Register();
	Printer printer = null;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			// cash register function buttons
			Button itemId = new Button("ITEM-ID");
			Button scale = new Button("SCALE");
			Button total = new Button("TOTAL");
			
			// error label for product ids
			error = new Label("Invalid product ID! Product not found!");
			error.setTextFill(Color.RED);
			error.setVisible(false);
			
			// input for product ids
			productId = new TextField();
			productId.setPromptText("Enter a product ID");
			
			// display area for transaction details and results
			registerDisplay = new TextArea();
			registerDisplay.setText("Register Display");
			registerDisplay.setEditable(false);
			
			// create a websocket connection to checkout_system
			try {
				System.out.println("creating new client");
				this.client = new Client("localhost", 4009);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			printer = new Printer(client);
			
			// event handlers for controls
			itemId.setOnAction(new itemIdHandler(productId, error, registerDisplay, firstUse, this.client));
			scale.setOnAction(new scaleHandler(registerDisplay, this.client));
			total.setOnAction(new totalHandler(registerDisplay, this.client));
			
			// positioning controls
			VBox functionKeys = new VBox(itemId, scale, total);
			functionKeys.setAlignment(Pos.CENTER);
			
			VBox input = new VBox(error, productId, registerDisplay);
			input.setAlignment(Pos.CENTER);
			
			root.setLeft(functionKeys);
			root.setCenter(input);
			
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
		
		System.out.println("onResponse: " + type);
		
		if (type.equals("scale")) {
			// update register display with calculated item price
			
			int id = Integer.parseInt(values[1]);
			int numOfItems = Integer.parseInt(values[2]);
			
			// get product record from stock database
			try {
				// get connection to stock database
				Connector db = new Connector("jdbc:sqlite:stock.db");
				Connection conn = db.getConnection();
				
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM products WHERE id = ? LIMIT 1");
				stmt.setInt(1, id);
				
				ResultSet rs = stmt.executeQuery();
				
				while(rs.next()) {
					String name = rs.getString("name");
					int price = rs.getInt("price");
					int amount = rs.getInt("amount");
					int weight = rs.getInt("weight");
					int bulk = rs.getInt("bulk");
					
					Item item = new Item(name, price, amount, weight, bulk, id);
					
					// update register display with item information
					displayInfo(item, numOfItems);
				}
				
				rs.close();
				stmt.close();
				
				//conn.close();
			} catch (Exception e) {
				System.out.println("getUser: " + e.getMessage());
			}
		} else if (type.equals("error")) {
			// enable error controls
			String errType = values[1];
			
			if (errType.equals("payment-error")) {
				// cancel transaction
				cancelTransaction();
				
				error.setText("Transaction has been cancelled!");
			}
					
			error.setDisable(false);
		} else if (type.equals("payment")) {
			printer.printReceipt(values[1]);
			register.closeDrawer();
		} else if (type.equals("total")) {
			int total = Integer.parseInt(values[1]);
			
			String totalInfo = "\nTotal: $" + Integer.toString(total) + "\n";
			
			// send message to customer_display with the total amount
			customerTotal(totalInfo);
			
			// update register display
			String info = registerDisplay.getText();
			info += totalInfo;
			this.registerDisplay.setText(info);
			
			// update receipt
			this.client.resp.add(String.join(",", "receipt", totalInfo));
			
			// open register drawer for cash
			register.openDrawer();
		} else if (type.equals("cart")) {
			for (int i = 1; i < values.length; i++) {
				writeToDisplay("\n" + values[i]);
			}
			
			writeToDisplay("\n");
		} else if (type.equals("app-name")) {
			this.client.resp.add(String.join(",", "app-name", "cashier_register"));
		}
		
		this.client.eventHandled = true;
	}
	
	// updates register display with item information
	public void displayInfo(Item item, int scaleAmt) {
		String info = "";
		
		// calculate total weight
		int totalWeight = item.weight * scaleAmt;
		
		// send total weight to customer_display
		String msg = String.join(",", "display", Integer.toString(scaleAmt));
		this.client.resp.add(msg);
		
		// update register display
		info += "Name: " + item.name + "\n";
		int price = item.price * totalWeight;
		info += "Price: $" + price + "\n";
		info += "Total weight: " + totalWeight + "\n";
		
		// send price to checkout_system
		msg = String.join(",", "total", Integer.toString(price));
		this.client.resp.add(msg);
		
		String existingInfo = this.registerDisplay.getText();
		
		this.registerDisplay.setText(existingInfo + "\n" + info);
		
		// send display text to checkout_system to be forwarded to customer_display
		msg = String.join(",", "display", info);
		this.client.resp.add(msg);
	}
	
	public void writeToDisplay(String str) {
		String info = registerDisplay.getText();
		info += str;
		this.registerDisplay.setText(info);
	}
	
	public void customerTotal(String msg) {
		this.client.resp.add("display, " + msg);
		this.client.resp.add("payment");
	}
	
	public void cancelTransaction() {
		registerDisplay.setText("Payment authorization failed! Transaction cancelled!");
		// TODO: close register drawer
	}
	
	public void printReceipt() {
		// TODO: send receipt to printer
		closeRegisterDrawer();
	}
	
	public void closeRegisterDrawer() {
		// TODO:
	}
	
	public void openRegisterDrawer() {
		// TODO:
	}
	
	public static void main(String[] args) throws IOException {
		launch(args);
	}
}
