import java.io.*;
import java.net.URL;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;


public class BooksDashBoardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private GridPane grid;

    @FXML
    private ListView<String> listView;

    private List<Book> booksList;
    private Library library;
    final WebView browser = new WebView();
    final WebEngine engine = browser.getEngine();
    public static final int MAX_COLUMN = 3;

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
                itemController.showBook(booksList.get(i));  // a NullPointerException will occur if imagePath is null
                if(column == MAX_COLUMN) {
                    column = 0;
                    row++;
                }
                grid.add(container, column++, row); 
            }

        }catch(Exception e) {e.printStackTrace();} 
    }

    @FXML
    void LogoutClicked(ActionEvent event) throws IOException {
        Parent dashBoardPage = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene dashBoardScene = new Scene(dashBoardPage);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(dashBoardScene);
        stage.show();
    }


    public void addToListView(String item) {
        System.out.println(item);
        listView.getItems().add(item);
    }

    
    public void welcomeUser(String userName) {
        welcomeLabel.setText("Welcome "+userName);
    }

    @FXML
    void contactUsClicked(ActionEvent event) {
        String url = "http://google.com";
        engine.load(url);
    }


}
