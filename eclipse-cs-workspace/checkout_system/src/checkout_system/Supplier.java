package checkout_system;

import java.sql.*;

public class Supplier {
	String dbAddr = "jdbc:sqlite:stock.db";
	Connector db;
	Connection conn;
	
	public Supplier() {
		db = new Connector(dbAddr);
		conn = db.getConnection();
	}
	
	public void makeOrder(int itemId, int amount) {
		try {
			String query = "UPDATE products SET amount = ? WHERE id = ? LIMIT 1";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setInt(1, amount);
			stmt.setInt(2, itemId);
			
			stmt.executeUpdate();
			
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
