package cashier_register;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import java.sql.*;

public class totalHandler implements EventHandler<ActionEvent> {
	TextArea display;
	String dbAddr = "jdbc:sqlite:stock.db";
	Connector db;
	Connection conn;
	Client client;
	
	public totalHandler(TextArea display, Client client) {
		this.display = display;
		this.client = client;
	}
	
	@Override
	public void handle(ActionEvent event) {
		// send message to checkout_system
		getTotal();
	}
	
	public void getTotal() {
		this.client.resp.add("get-total");
	}
}
