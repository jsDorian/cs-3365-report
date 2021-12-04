module customer_display {
	requires javafx.controls;
	requires java.sql;
	requires java.net.http;
	opens customer_display to javafx.graphics, javafx.fxml;
}