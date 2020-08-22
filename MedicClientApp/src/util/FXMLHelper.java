package util;

import controller.DocumentationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FXMLHelper {
    private static final Logger LOGGER = Logger.getLogger(FXMLHelper.class.getName());

    static FXMLHelper singleton=null;
    private FXMLHelper(){
    }

    public static FXMLHelper getInstance() {
        return singleton==null? singleton=new FXMLHelper() : singleton;
    }

    public Scene loadNewScene(String fxml, String css, Object controller)
    {
        Parent root = null;
        FXMLLoader loader =  new FXMLLoader(getClass().getResource(fxml));
        loader.setController(controller);
        try {
            root = loader.load();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot load page..", "Cannot ");
        }
        Scene scene = new Scene(root);
        if(css!=null)
            scene.getStylesheets().add(css);
        return scene;
    }
}
