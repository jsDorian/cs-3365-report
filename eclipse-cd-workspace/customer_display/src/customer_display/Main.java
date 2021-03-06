package customer_display;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Scene;

public class Main extends Application {
	Client client = null;
	Label error = null;
	TextField cardNumber = null;
	TextField pin = null;
	TextField cart = null;
	TextField amount = null;
	TextArea display = null;
	Button submit = null; 
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			// customer input submit button
			submit = new Button("Submit");
			
			// error label for wrong inputs
			error = new Label("Invalid entry! Try again!");
			error.setTextFill(Color.RED);
			error.setVisible(false);
			
			// inputs for payment ids
			cardNumber = new TextField();
			cardNumber.setPromptText("Enter card number...");
			cardNumber.setDisable(true);
			
			pin = new TextField();
			pin.setPromptText("Enter card pin...");
			pin.setDisable(true);
			
			// input for items
			cart = new TextField();
			cart.setPromptText("Enter cart items separated by commas...");
			
			amount = new TextField();
			amount.setPromptText("Enter number of items...");
			amount.setDisable(true);
			
			// display area for transaction details and results
			display = new TextArea();
			display.setText("Customer Display");
			display.setEditable(false);
			
			// create a websocket connection to checkout_system
			try {
				System.out.println("creating new client");
				this.client = new Client("localhost", 4009);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			// event handler for customer input submission
			submit.setOnAction(new submitHandler(error, cardNumber, pin, cart, amount, display, this.client));
			
			// positioning controls
			VBox textControls = new VBox(error, cardNumber, pin, cart, amount, display, submit);
			textControls.setAlignment(Pos.CENTER);
			
			root.setCenter(textControls);
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
		
		if (type.equals("member")) {
			// enable membership controls
			cart.setPromptText("Enter your membership phone number...");
			cart.setDisable(false);
		} else if (type.equals("pin")) {
			// enable membership controls
			cart.setPromptText("Enter your membership pin...");
			cart.setDisable(false);
		} else if (type.equals("error")) {
			// enable error controls
			String errType = values[1];
			
			if (errType.equals("payment-error")) {
				String errArgs = values[2];
				
				if (errArgs.equals("card-number")) {
					error.setText("Invalid card number! Try again!");
				} else if (errArgs.equals("pin-number")) {
					error.setText("Invalid pin! Try again!");
				} else {
					error.setText("Invalid input! Try again!");
				}
			}
			
			if (errType.equals("member-err")) {
				String errArgs = values[2];
				
				if (errArgs.equals("phone-number")) {
					error.setText("Invalid phone number! Try again!");
				} else if (errArgs.equals("pin")) {
					error.setText("Invalid pin! Try again!");
				} else {
					error.setText("Invalid input! Try again!");
				}
			}
			
			if (errType.equals("cart-err")) {
				String errArgs = values[2];
				
				if (errArgs.equals("item")) {
					error.setText("Invalid item " + errArgs + " was not found! Try again!");
				} else {
					error.setText("Invalid input! Try again!");
				}
			}
			
			error.setDisable(false);
		} else if (type.equals("scale")) {
			// enable scale controls
			String itemName = values[1];
			
			amount.setPromptText("Enter the number of " + itemName + "'s in the cart...");
			amount.setDisable(false);
		} else if (type.equals("payment")) {
			// enable payment controls
			amount.clear();
			cart.clear();
			cardNumber.setText("Enter card number...");
			cardNumber.setDisable(false);
			pin.setText("Enter card pin...");
			pin.setDisable(false);
		} else if (type.equals("display")) {
			String info = values[1];
			displayInfo(info);
		} else if (type.equals("app-name")) {
			this.client.resp.add(String.join(",", "app-name", "customer_display"));
		} else if (type.equals("print-receipt")) {
			String info = "Your Receipt\n------------------------------\n" + values[1];
			displayInfo(info);
		}
		
		this.client.eventHandled = true;
	}
	
	public void displayInfo(String info) {
		String existingInfo = this.display.getText();
		
		this.display.setText(existingInfo + "\n" + info);
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
