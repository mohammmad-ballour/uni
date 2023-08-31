import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class loginController implements Initializable {
    
    @FXML
    private TextField userEmail;

    @FXML
    private TextField userName;

    @FXML
    private PasswordField userPassword;

    private File file;
    private RandomAccessFile usersFile;
    private Scanner scanner;

    private Map<String, String> map = new HashMap<>();

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        try {
            File file = new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\users.txt");
            usersFile = new RandomAccessFile(file, "r");
            scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                usersFile.skipBytes(scanner.nextLine().length());   //for the name
                String email = scanner.nextLine();
                String password = scanner.nextLine();
                map.put(email, password);
                usersFile.skipBytes(scanner.nextLine().length());   //for the age
            }	
            scanner.close();
            usersFile.close();
        } catch (IOException e) {e.printStackTrace();}
    
    }

    @FXML
    private void enterPressed(KeyEvent e) {        
        userPassword.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER && validate(userEmail.getText(), userPassword.getText())) {
               try {
                loadLibraryPage(e, userName.getText());
                } catch (IOException ex) {ex.printStackTrace();}
            }
        });   
    }

    private boolean validate(String email, String password) {
        for(Map.Entry entry: map.entrySet()) {
            if(entry.getKey().equals(email) && entry.getValue().equals(password)) 
                return true;
        }
        Alert failure = new Alert(AlertType.INFORMATION);
        failure.setTitle("Error");
        failure.setContentText("Check your info!");
        failure.showAndWait();
        return false;
    }

    private void loadLibraryPage(KeyEvent event, String userName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BooksDashBoard.fxml"));
        Parent libraryPage = loader.load();
        BooksDashBoardController dashboardController = loader.getController();
        dashboardController.welcomeUser(userName); 
        Scene libraryScene = new Scene(libraryPage);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(libraryScene);
        stage.show();
    }

}
