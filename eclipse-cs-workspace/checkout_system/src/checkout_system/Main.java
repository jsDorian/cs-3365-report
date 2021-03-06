package checkout_system;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;

public class Main extends Application {
	public static int port = 4009;
	public static TextArea display = new TextArea();
	public Server server = new Server(port, display);
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			primaryStage.setScene(scene);
			primaryStage.show();
			
			// display area for transaction details and results
			display.setText("Internal System Display");
			display.setEditable(false);
			
			// positioning controls
			VBox output = new VBox(display);
			output.setAlignment(Pos.CENTER);
			
			root.setCenter(output);
			
			try {
				server.activate();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop(){
		writeToDisplay("Server disabled: shutting down!");
		server.stop();
	}
	
	public void writeToDisplay(String msg) {
    	String text = display.getText();
    	
    	text += "\n" + msg;
    	
    	display.setText(text);
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
