package ryanshy.sankaku_desktop_app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
		
    public String getGreeting() {
        return "Hello World!";
    }

    

	@Override
	public void start(Stage primaryStage) {
		String javaVersion = System.getProperty("java.version");
		String javafxVersion = System.getProperty("javafx.version");
		Label l = new Label("Hello, JavaFX "+javafxVersion+", running on Java "+javaVersion+".");
		Scene scene = new Scene(new StackPane(l), 640, 480);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
        //System.out.println(new App().getGreeting());
        launch(args);
    }
    
    
}
