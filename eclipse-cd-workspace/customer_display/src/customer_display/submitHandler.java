package customer_display;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import java.sql.*;
import java.util.ArrayList;

public class submitHandler implements EventHandler<ActionEvent> {
	public Label error;
	public TextField cardNumber;
	public TextField pin;
	public TextField cart;
	public TextField amount;
	public TextArea display;
	public Client client;
	public Boolean cartFirstUse = true;
	public Boolean memberFirstUse = true;
	
	public submitHandler(Label error, TextField cardNumber, TextField pin, TextField cart, TextField amount, TextArea display, Client client) {
		this.error = error;
		this.cardNumber = cardNumber;
		this.pin = pin;
		this.amount = amount;
		this.display = display;
		this.client = client;
		this.cart = cart;
	}
	
	@Override
	public void handle(ActionEvent event) {
		String action = getAction();
		
		if (action.equals("payment")) {
			paymentAction();
		} else if (action.equals("scale")) {
			scaleAction();
		} else if (action.equals("cart")) {
			cartAction();
		} else {
			memberAction();
		}
		
		emptyControls();
	}
	
	public void memberAction() {
		String cart = this.cart.getText();
		String msg = null;
				
		// TODO: sanitize input
		// send member info to checkout_system
		
		
		if (memberFirstUse) {
			
			msg = String.join(",", "member", cart);
			memberFirstUse = false;
			this.client.resp.add(msg);
			this.cart.setText("Enter membership pin...");
			this.cart.setDisable(true);
			return;
		}
		
		if (!memberFirstUse) {
			msg = String.join(",", "pin", cart);
			this.cart.clear();
			this.cart.setDisable(true);
			this.client.resp.add(msg);
			return;
		}
	}
	
	public void cartAction() {
		String cart = this.cart.getText();
		
		// send cart to cashier_register
		String msg = String.join(",", "cart", cart);
		this.client.resp.add(msg);
		
		this.cart.setText("Enter membership phone number...");
		this.cart.setDisable(true);
	}
	
	public void paymentAction() {
		String cardNumber = this.cardNumber.getText();
		String pin = this.pin.getText();
		
		// send card info to payment_authorization_system
		String msg = String.join(",", "payment", cardNumber, pin);
		this.client.resp.add(msg);
		
		this.cardNumber.setDisable(true);
		this.pin.setDisable(true);
	}
	
	public void scaleAction() {
		int amount = Integer.parseInt(this.amount.getText());
		
		// send amount to checkout_system
		String msg = String.join(",", "amount", Integer.toString(amount));
		this.client.resp.add(msg);
		
		this.amount.setDisable(true);
	}
	
	public void emptyControls() {
		this.error.setText(null);
		this.cardNumber.setText(null);
		this.pin.setText(null);
		this.amount.setText(null);
	}
	
	public String getAction() {
		var amount = this.amount.getText();
		var cart = this.cart.getText();
		
		if (amount == "" && cart == "") {
			return "payment";
		} else {
			if (cart == "") {
				return "scale";
			} else {
				if (cartFirstUse) {
					cartFirstUse = false;
					return "cart";
				} else {
					return "member";
				}
			}
		}
	}
}
