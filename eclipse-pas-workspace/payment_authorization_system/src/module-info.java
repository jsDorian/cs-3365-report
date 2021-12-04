module payment_authorization_system {
	requires javafx.controls;
	requires java.sql;
	requires java.net.http;
	opens payment_authorization_system to javafx.graphics, javafx.fxml;
}