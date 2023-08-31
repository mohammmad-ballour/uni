import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class guestModeController implements Initializable {
    
    @FXML
    private GridPane grid;

    private Library library;
    private List<Book> booksList;
    public static final int MAX_COLUMN = 2;
    final WebView browser = new WebView();
    final WebEngine engine = browser.getEngine();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            library = new Library();
            booksList = library.getBooksFromFile(new File("C:/Users/pc/Desktop/DSaA/DSaAProject/src/Books.txt"));
            int column = 0, row = 1;

            for(int i = 0; i < booksList.size(); i++) {
                if(booksList.get(i).getQuantity() <= 0) continue;
                FXMLLoader itemLoader = new FXMLLoader(getClass().getResource("item.fxml"));
                HBox container = itemLoader.load();
                ItemController itemController = itemLoader.getController();
                itemController.showBook(booksList.get(i));
                if(column == MAX_COLUMN) {
                    column = 0;
                    row++;
                }
                grid.add(container, column++, row);
            }
            disableBuyButtons();

        }catch(Exception e) {e.printStackTrace();} 
    }

    private void disableBuyButtons() {
        ObservableList<Node> children = grid.getChildren();
        for(Node child: children) {
            VBox vbox = (VBox) ((HBox) child).getChildren().get(1);
            Button buyButton = (Button) (vbox.getChildren().get(5));
            buyButton.setDisable(true);
        }
    }

    @FXML
    void RegisterClicked(ActionEvent event) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
    
    @FXML
    void contactUsClicked(ActionEvent event) {
        String url = "http://google.com";
        engine.load(url);
    } 
    
    
}
