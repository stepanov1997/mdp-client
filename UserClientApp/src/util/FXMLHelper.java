package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class FXMLHelper {

    static FXMLHelper singleton=null;
    private FXMLHelper(){
    }

    public static FXMLHelper getInstance() {
        return singleton==null? singleton=new FXMLHelper() : singleton;
    }

    public Scene loadNewScene(String fxml, String css, Object controller, double width, double height)
    {
        Parent root = null;
        FXMLLoader loader =  new FXMLLoader(getClass().getResource(fxml));
        loader.setController(controller);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root, width, height);
        if(css!=null)
            scene.getStylesheets().add(css);
        return scene;
    }
}
