package checkout_system;

import java.sql.*;

public class Connector {
	Connection conn = null;
	
	public Connector(String addr) {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(addr);
		} catch (Exception e) {
			System.out.println("SQLException: " + e.getMessage());
		}
	}
	
	public Connection getConnection() {
		return conn;
	}
}
