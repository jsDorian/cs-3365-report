module checkout_system {
	requires javafx.controls;
	requires java.sql;
	requires java.net.http;
	opens checkout_system to javafx.graphics, javafx.fxml;
}