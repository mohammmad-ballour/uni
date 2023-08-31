package com.example.dbms_project;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    @FXML
    private CheckBox checkButton;

    @FXML
    private TextField userID;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    final String url = "jdbc:postgresql://localhost/TAs?currentSchema=unisample";
    final String user = "ballour";
    final String pd = "admin";

    @FXML
    void loginButtonClicked(ActionEvent event) throws IOException, SQLException {
        boolean isAdmin = checkButton.isSelected();
        if(validate(userID.getText(), password.getText(), isAdmin)) {
            if (isAdmin) {
                loadDBAPage(event);
            } else {
                loadTAPage(event);
            }
        } else { // Validation failed!
            Alert failure = new Alert(Alert.AlertType.INFORMATION);
            failure.setTitle("Notification");
            failure.setContentText("Enter correct data!");
            failure.showAndWait();
        }

    }

    private void loadDBAPage(ActionEvent event) throws IOException {
        Parent page = FXMLLoader.load(getClass().getResource("DBA_HomePage.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Admins only");
        stage.show();
    }

    private void loadTAPage(ActionEvent event) throws IOException, SQLException {
        Connection conn = DriverManager.getConnection(url, user, pd);
        String checkExistence = "select exists (select 1 from  pg_roles where rolname = ?)";
        String checkPrivileges = "SELECT (has_table_privilege(?, 'lecture', 'INSERT')" +
                " and has_table_privilege(?, 'attends', 'INSERT') and has_table_privilege(?, 'student', 'INSERT')" +
                "and has_table_privilege(?, 'student_phones', 'INSERT'))";

        PreparedStatement ps1 = conn.prepareStatement(checkExistence);
        String rolename = username.getText().concat(userID.getText());  // can be the id only
        ps1.setString(1, rolename);
        ResultSet rs1 = ps1.executeQuery();
        boolean exists = false;
        boolean hasPrivileges = false;

        if (rs1.next()) {
            exists = rs1.getBoolean(1);
        }
        if (exists) {
            PreparedStatement ps2 = conn.prepareStatement(checkPrivileges);
            ps2.setString(1, rolename);
            ps2.setString(2, rolename);
            ps2.setString(3, rolename);
            ps2.setString(4, rolename);
            ResultSet rs2 = ps2.executeQuery();

            if (rs2.next()) {
                hasPrivileges = rs2.getBoolean(1);
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TAHomePage.fxml"));
        Parent page = fxmlLoader.load();
        if(!hasPrivileges)  {
            // Disable modify buttons
            TAHomePageController controller = fxmlLoader.getController();
            controller.getUpdateLectureButton().setDisable(true);
            controller.getUpdateStudentButton().setDisable(true);
            controller.getCreateLectureButton().setDisable(true);
            controller.getCreateStudentButton().setDisable(true);
        }
        conn.close();
        //show view buttons anyway
        Scene scene = new Scene(page);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("TAs page");
        stage.show();

    }
    private boolean validate(String user_id, String passwd, boolean isAdmin) throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, pd);
        // get password from instructor table
        if (!isAdmin) {
            String query = "SELECT id, name, password FROM instructor WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("name");
                String hashedPassword = rs.getString("password");
                if (id.equals(user_id) && BCrypt.checkpw(passwd, hashedPassword)) { //&& name.equals(username.getText())
                    return true;
                }
            }
            stmt.close();
            rs.close();
            // cannot get password from pg_shadow (encrypted!) so validation by sysid and name, Consider id as passwd ^_^
        } else {
            PreparedStatement ps = conn.prepareStatement("SELECT usesysid, passwd from pg_shadow where usename = ? and usesuper = ?");
            ps.setString(1, username.getText());
            ps.setBoolean(2, true);
            ResultSet rs = ps.executeQuery();
            String sysid = "";
            String adminPassword = "";

            // if 'username' is a superuser
            if (rs.next()) {
                sysid = rs.getString("usesysid");
                adminPassword = rs.getString("passwd");
                if (sysid.equals(user_id)) return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Notification");
                alert.setContentText(username.getText() + " is not an admin!");
                alert.showAndWait();
                System.exit(-1);
            }
            ps.close();
            rs.close();
        }
        conn.close();
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(checkButton::requestFocus);
        checkButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal) password.setVisible(false);
            else password.setVisible(true);
        });
//        userID.setText("00102");
//        username.setText("Hasan");
//        password.setText("hasanta");
    }
}
