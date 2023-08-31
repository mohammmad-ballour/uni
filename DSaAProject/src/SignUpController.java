import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.scene.Node;

public class SignUpController implements Initializable{

    @FXML
    private TextField userEmail;

    @FXML
    private TextField userFirstName;

    @FXML
    private TextField userLastName;

    @FXML
    private PasswordField userPassword;

    @FXML
    private TextField userAge;

    @FXML
    private Button guestModeButton;

    private FileWriter usersFile;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1){
        try {
            usersFile = new FileWriter(new File("C:\\Users\\pc\\Desktop\\DSaA\\DSaAProject\\src\\users.txt"), true);
        } catch (IOException e) {e.printStackTrace();}
   
    }

    @FXML
    void LoginClicked(ActionEvent event) throws IOException, InterruptedException {
        TimeUnit.SECONDS.sleep(1);    
        Parent page = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void saveInfoClicked(ActionEvent event) throws IOException, InterruptedException {
        if(userFirstName == null || userLastName == null || userEmail == null || userPassword == null || userAge == null
        || userFirstName.getText().isEmpty() || userLastName.getText().isEmpty() || userEmail.getText().isEmpty() 
        || userAge.getText().isEmpty() || userPassword.getText().isEmpty()) {
            Alert failure = new Alert(AlertType.INFORMATION);
            failure.setTitle("Error");
            failure.setContentText("Some info is missing!");
            failure.showAndWait();
            //System.out.println(userFirstName+"\n"+userLastName+"\n"+userAge+"\n"+userEmail+"\n"+userPassword+"\n");
            return;
        }

        User newUser = new User();
        newUser.setFirstName(userFirstName.getText());
        newUser.setLastName(userLastName.getText());
        newUser.setAge(Integer.valueOf(userAge.getText()));
        newUser.setPassword(userPassword.getText());
        newUser.setEmail(userEmail.getText());
        String name = newUser.getFirstName() + " " + newUser.getLastName();
        writeUserDataToFile(newUser);
        TimeUnit.SECONDS.sleep(2); 
        loadLibraryPage(event, name);
    }

    private void writeUserDataToFile(User u) throws IOException {
        usersFile.write(u.getFirstName()+" "+u.getLastName()+"\n");
        usersFile.write(u.getEmail()+"\n");
        usersFile.write(u.getPassword()+"\n");
        usersFile.write(u.getAge()+"\n");
        usersFile.close();
    }


    private void loadLibraryPage(ActionEvent event, String name) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("BooksDashBoard.fxml"));
        Parent libraryPage = loader.load();
        BooksDashBoardController controller = loader.getController();
        controller.welcomeUser(name);
        Scene libraryScene = new Scene(libraryPage);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(libraryScene);
        stage.show();
    }

    @FXML
    void guestModeButtonClicked(ActionEvent event) throws IOException {
        Parent libraryPage = FXMLLoader.load(getClass().getResource("guestMode.fxml"));
        Scene libraryScene = new Scene(libraryPage);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(libraryScene);
        stage.show();
    }


}
