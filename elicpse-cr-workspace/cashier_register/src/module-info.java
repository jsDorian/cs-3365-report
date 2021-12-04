module cashier_register {
	requires javafx.controls;
	requires java.sql;
	requires java.net.http;
	opens cashier_register to javafx.graphics, javafx.fxml;
}
