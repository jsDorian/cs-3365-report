package cashier_register;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import java.sql.*;

public class scaleHandler implements EventHandler<ActionEvent> {
	TextArea display;
	String dbAddr = "jdbc:sqlite:stock.db";
	Connector db;
	Connection conn;
	Client client;
	
	public scaleHandler(TextArea display, Client client) {
		this.display = display;
		this.client = client;
		
		// get connection to stock database
		db = new Connector(dbAddr);
		conn = db.getConnection();
	}
	
	@Override
	public void handle(ActionEvent event) {
		// get product id of product on scale from checkout_system
		getScaleWeight();
	}
	
	public void getScaleWeight() {
		this.client.resp.add("get-scale");
	}
}