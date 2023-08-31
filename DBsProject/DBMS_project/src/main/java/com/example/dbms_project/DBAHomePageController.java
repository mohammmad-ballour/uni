package com.example.dbms_project;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DBAHomePageController {

    @FXML
    private Button createCourseButton;

    @FXML
    private Button createUserButton;

    @FXML
    private Button createDeptButton;

    @FXML
    private Button updateDeptButton;


    @FXML
    private Button updateCourseButton;

    @FXML
    private Button updateUserButton;

    @FXML
    private Button updateSectionButton;

    @FXML
    private Hyperlink logoutLink;

    final String url = "jdbc:postgresql://localhost/TAs?currentSchema=unisample";
    final String username = "ballour";
    final String password = "admin";


    @FXML
    void createCourseButtonClicked(ActionEvent event) {
        // Create a dialog to show the form
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert New Course");

        // Create a new type
        ButtonType addSectionsType = new ButtonType("Add Sections", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addSectionsType, ButtonType.CANCEL);
        // Create a form to get the user's data
        GridPane form = new GridPane();

        TextField c_id = new TextField();
        TextField title = new TextField();
        TextField book = new TextField();
        TextField department = new TextField();
        TextField credits = new TextField();
        Spinner<Integer> sections_spinner = new Spinner<>(1, 4, 1);

        Label[] labels = {new Label("course_id:"), new Label("Book:"), new Label("Title:"),
                new Label("Department: "), new Label("credits:"), new Label("#of sections")};
        Node[] nodes = {c_id, book, title, department, credits, sections_spinner};
        constructGridPane(form, labels, nodes);

        // Set the form as the dialog's content
        dialog.getDialogPane().setContent(form);

        // Request focus on the name field by default
        Platform.runLater(()-> c_id.requestFocus());

        // Convert the result to a string when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addSectionsType) {
                String user_course_id = c_id.getText();
                String user_title = title.getText();
                String user_book = book.getText();
                String user_department = department.getText();
                int user_credits =  Integer.parseInt(credits.getText());

                // Write the data to the database
                try {
                    Connection conn = DriverManager.getConnection(url, username, password);
                    String courseQuery = "INSERT INTO course (course_id, title, book, dept_name, credits) VALUES (?, ?, ?, ?, ?)";
                    String sectionQuery = "INSERT INTO section (course_id, sec_id, semester, building, room_number) VALUES (?, ?, ?, ?, ?)";

                    PreparedStatement pstmtForCourse = conn.prepareStatement(courseQuery);
                    PreparedStatement pstmtForSection = conn.prepareStatement(sectionQuery);

                    pstmtForCourse.setString(1, user_course_id);
                    pstmtForCourse.setString(2, user_title);
                    pstmtForCourse.setString(3, user_book);
                    pstmtForCourse.setString(4, user_department);
                    pstmtForCourse.setInt(5, user_credits);
                    pstmtForCourse.executeUpdate();

                    List<Section> sections = new ArrayList<>();
                    showSectionsDialog(sections_spinner, sections);
                    for(Section section: sections) {
                        try {
                            pstmtForSection.setString(1, user_course_id);
                            pstmtForSection.setString(2, section.getId());
                            pstmtForSection.setString(3, section.getSemester());
                            pstmtForSection.setString(4, section.getDefaultBuilding());
                            pstmtForSection.setString(5, section.getDefaultRoom());
                            pstmtForSection.executeUpdate();
                        } catch (SQLException | IllegalArgumentException ex) {
                            String alertMessage = "Enter valid/correct data.";
            showAlert(alertMessage);
                        }
                    }
                    pstmtForCourse.close();
                    pstmtForSection.close();
                    conn.close();

                } catch (SQLException | IllegalArgumentException ex) {
                    String alertMessage = "Enter valid/correct data.";
                    showAlert(alertMessage);
                    ex.printStackTrace();
                }
                return "Data written to database";
            }
            return null;
        });

        // Show the dialog and wait for the user to close it
        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Done!");
        });
    }

    @FXML
    void updateCourseButtonClicked(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        Label label = new Label("COURSE_ID for user to be updated/deleted: ");
        TextField user_course_id = new TextField();
        Button deleteButton = new Button("Delete Course");
        deleteButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuery = "DELETE FROM course WHERE course_id = ?";
                PreparedStatement ps = conn.prepareStatement(delQuery);
                ps.setString(1, user_course_id.getText());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                String alterMessage = "Enter correct course_id.";
                showAlert(alterMessage);
            }
        });

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("dept_name", "title", "book", "credits");
        listView.setPrefHeight(200);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set a custom cell factory with event handlers for each cell
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) {
                    // Show a dialog to get a new value for the selected item
                    String selectedItem = cell.getItem();
                    TextInputDialog textDialog = new TextInputDialog(selectedItem);
                    textDialog.setHeaderText("Enter a new value for " + selectedItem + ":");
                    textDialog.showAndWait().ifPresent(newValue -> {
                        // Update the selected item with the new value and commit the changes to database
                        try {
                            Connection conn = DriverManager.getConnection(url, username, password);
                            String oldValue = listView.getItems().get(cell.getIndex());
                            String course_query = "UPDATE course SET "+ oldValue +" = ? WHERE course_id = ?";
                            PreparedStatement pstmt = conn.prepareStatement(course_query);

                            if(oldValue.equals("credits")) {
                                pstmt.setInt(1, Integer.parseInt(newValue));
                                pstmt.setString(2, user_course_id.getText());
                            }else {
                                pstmt.setString(1, newValue);
                                pstmt.setString(2, user_course_id.getText());
                            }
                            pstmt.executeUpdate();
                            System.out.println("Updated");
                            pstmt.close();
                            conn.close();
                        } catch (SQLException | IllegalArgumentException ex) {
                            String alertMessage = "Enter valid/correct data.";
                            showAlert(alertMessage);
                        }

                    });
                }
            });
            return cell;
        });
        // Add the listview and HBox to a VBox
        HBox hbox = new HBox(label, user_course_id, deleteButton);
        hbox.setSpacing(8);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.getChildren().addAll(hbox, listView, deleteButton);
        dialog.getDialogPane().setContent(vbox);
        Platform.runLater(user_course_id::requestFocus);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    void createUserButtonlicked(ActionEvent event) {
        // Create a dialog to show the form
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert New User");

        // Set the button types (AssignCourse and Cancel)
        ButtonType assignCoursesType = new ButtonType("Assign the course", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(assignCoursesType, ButtonType.CANCEL);

        // Add the form fields
        TextField id = new TextField();
        TextField department = new TextField();
        TextField name = new TextField();
        TextField passwd = new TextField();
        CheckBox checkBox = new CheckBox();

        GridPane form = new GridPane();
        Label[] labels = {new Label("ID:"), new Label("Department:"), new Label("Name:"),
               new Label("Password:") , new Label("Grant authority to view/modify?")};
        Node[] nodes = {id, department, name, passwd, checkBox};
        constructGridPane(form, labels, nodes);

        form.add(new Label("What course will they teach? "), 0, 5);
        VBox box = new VBox();
        ListView<String> coursesList = new ListView<>();
        Button createUserButton = new Button("Create User");
        box.getChildren().addAll(coursesList, createUserButton);
        box.setSpacing(10);
        dialog.getDialogPane().setPrefHeight(400);

        createUserButton.setOnAction(e -> {
            String user_id = id.getText();
            String user_name = name.getText();
            String user_department = department.getText();
            String user_passwd = passwd.getText();

            // Write the data to the database
            try {
                Connection conn = DriverManager.getConnection(url, username, password);
                String query = "INSERT INTO instructor (id, name, dept_name, password) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, user_id);
                pstmt.setString(2, user_name);
                pstmt.setString(3, user_department);
                pstmt.setString(4, BCrypt.hashpw(user_passwd, BCrypt.gensalt()));
                pstmt.executeUpdate();
                pstmt.close();



                conn.close();
            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }
        });

        // update the list view
        getDepartmentCourses(department, coursesList);

        form.add(box, 1, 5);

        // clear the suggested courses if dept field is cleared
        department.textProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.isEmpty()) {
                coursesList.getItems().clear();
            }
        });

        // Convert the result to a string when the assignCourses button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == assignCoursesType) {
                // Write the data to the database
                try {
                    Connection conn = DriverManager.getConnection(url, username, password);
                    String assignCourseQuery = "INSERT INTO teaches (id, course_id, sec_id, semester) VALUES (?, ?, ?, ?)";
                    String selectedItem = coursesList.getSelectionModel().getSelectedItem();
                    String c_id = "", s_id = "", semester = "";
                    if (selectedItem != null) {
                        String[] res = selectedItem.split(", ");
                        c_id = res[0];
                        s_id = res[1];
                        semester = res[2];
                    }
                    PreparedStatement assignCourseStatement = conn.prepareStatement(assignCourseQuery);
                    assignCourseStatement.setString(1, id.getText());
                    assignCourseStatement.setString(2,c_id);
                    assignCourseStatement.setString(3, s_id);
                    assignCourseStatement.setString(4, semester);
                    assignCourseStatement.executeUpdate();

                    conn.close();
                } catch (SQLException | IllegalArgumentException ex) {
                    String alertMessage = "Enter valid/correct data.";
                    System.out.println(ex.getMessage());
                    showAlert(alertMessage);
                }
                return "Data written to database";
            }
            return null;
        });

        dialog.getDialogPane().setPrefHeight(400);
        // Set the form as the dialog's content
        dialog.getDialogPane().setContent(form);
        // Request focus on the id field by default
        Platform.runLater(()-> id.requestFocus());
        // Show the dialog and wait for the user to close it
        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Done!");
        });

    }

    @FXML
    void updateUserButtonClicked(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        Label idLabel = new Label("ID for user to be updated/deleted: ");
        TextField user_id = new TextField();
        Label authLabel = new Label("Grant authority to view/modify?");
        CheckBox checkBox = new CheckBox();
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("dept_name", "name", "password");
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button deleteButton = new Button("Delete Instructor");
        deleteButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuery = "DELETE FROM instructor WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(delQuery);
                ps.setString(1, user_id.getText());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                String alterMessage = "Enter correct id.";
                showAlert(alterMessage);
            }
        });

        // Set a custom cell factory with event handlers for each cell
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };

            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) {
                    // Show a dialog to get a new value for the selected item
                    String selectedItem = cell.getItem();
                    TextInputDialog textDialog = new TextInputDialog(selectedItem);
                    textDialog.setHeaderText("Enter a new value for " + selectedItem + ":");
                    textDialog.showAndWait().ifPresent(newValue -> {
                        // Update the selected item with the new value
                        try {
                            Connection conn = DriverManager.getConnection(url, username, password);
                            String oldValue = listView.getItems().get(cell.getIndex());
                            String query = "UPDATE instructor SET "+ oldValue +" = ? WHERE id = ?";

                            PreparedStatement pstmt = conn.prepareStatement(query);
                            if(oldValue.equals("password")) {
                                pstmt.setString(1, BCrypt.hashpw(newValue, BCrypt.gensalt()));
                            }
                            else {
                                pstmt.setString(1, newValue);
                            }
                            pstmt.setString(2, user_id.getText());
                            pstmt.executeUpdate();
                            System.out.println("Updated");
                            pstmt.close();
                            conn.close();
                        } catch (SQLException | IllegalArgumentException ex) {
                            String alertMessage = "Enter valid/correct data.";
                            showAlert(alertMessage);
                        }

                    });
                }
            });
            return cell;
        });

        // Grant authorities if necessary
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Connection connection = DriverManager.getConnection(url, username, password);
                String call = "";
                CallableStatement cstmt;
                if(newValue) {
                    call = "{CALL create_user_and_grant_authorities(?)}";
                } else {
                    // just revoke the authorities, does not drop the user!
                    call = "{CALL revoke_authorities(?)}";
                }
                cstmt = connection.prepareCall(call);
                cstmt.setString(1, user_id.getText());
                cstmt.execute();
                cstmt.close();
            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }
        });

        // Add the listview and HBox to a VBox
        HBox idBox = new HBox(idLabel, user_id);
        idBox.setSpacing(8);
        HBox authBox = new HBox(authLabel, checkBox);
        authBox.setSpacing(5);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        listView.setPrefHeight(100);
        vbox.getChildren().addAll(idBox, listView, authBox, deleteButton);
        dialog.getDialogPane().setContent(vbox);
        Platform.runLater(user_id::requestFocus);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    void updateSectionButtonClicked(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        Label courseLabel = new Label("COURSE_ID for section to be updated/deleted: ");
        Label sectionLabel = new Label("SECTION_ID for section to be updated/deleted: ");
        TextField user_course_id = new TextField();
        TextField user_sec_id = new TextField();
        Button deleteButton = new Button("Delete Section");

        deleteButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuery = "DELETE FROM section WHERE course_id = ? and sec_id = ?";
                PreparedStatement ps = conn.prepareStatement(delQuery);
                ps.setString(1, user_course_id.getText());
                ps.setString(2, user_sec_id.getText());
                int rows_affected = ps.executeUpdate();
                if(rows_affected == 0) {
                    String alterMessage = "Enter correct course_id/section_id.";
                    showAlert(alterMessage);
                }
                ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("semester", "building", "room_number");
        listView.setPrefHeight(150);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set a custom cell factory with event handlers for each cell
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) {
                    // Show a dialog to get a new value for the selected item
                    String selectedItem = cell.getItem();
                    TextInputDialog textDialog = new TextInputDialog(selectedItem);
                    textDialog.setHeaderText("Enter a new value for " + selectedItem + ":");
                    textDialog.showAndWait().ifPresent(newValue -> {
                        // Update the selected item with the new value
                        try {
                            Connection conn = DriverManager.getConnection(url, username, password);
                            String oldValue = listView.getItems().get(cell.getIndex());
                            String query = "UPDATE section SET "+ oldValue +" = ? WHERE course_id = ? and sec_id = ?";
                            if(user_course_id.getText().isEmpty() || user_sec_id.getText().isEmpty()){
                                showAlert("Fill the empty fields");
                            }
                            PreparedStatement pstmt = conn.prepareStatement(query);
                            pstmt.setString(1, newValue);
                            pstmt.setString(2, user_course_id.getText());
                            pstmt.setString(3, user_sec_id.getText());
                            pstmt.executeUpdate();
                            pstmt.close();
                            conn.close();
                        } catch (SQLException | IllegalArgumentException ex) {
                            String alertMessage = "Enter valid/correct data.";
                            if(ex.getMessage().contains("violates foreign key constraint") ||
                                    ex.getMessage().contains("duplicate key value violates unique constraint")) {
                                alertMessage = "This section is already assigned to a T.A in the current semester. " +
                                        "You may add a new section!";
                            }
                            showAlert(alertMessage);
                        }

                    });
                }
            });
            return cell;
        });
        // Add the listview and HBox to a VBox
        VBox userInput = new VBox(new HBox(courseLabel, user_course_id), new HBox(sectionLabel, user_sec_id), deleteButton);
        userInput.setSpacing(5);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.getChildren().addAll(userInput, listView);
        dialog.getDialogPane().setContent(vbox);
        Platform.runLater(user_course_id::requestFocus);    // the warning is killing me!
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    void createDeptButtonClicked(ActionEvent event) {
        // Create a dialog to show the form
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert New Department");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Add the form fields
        TextField departmentName = new TextField();
        TextField building = new TextField();
        TextField budget = new TextField();

        GridPane form = new GridPane();
        Label[] labels = {new Label("dept-name:"), new Label("building:"), new Label("budget:")};
        Node[] nodes = {departmentName, building, budget};
        constructGridPane(form, labels, nodes);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Write the data to the database
                String inputName = departmentName.getText();
                String inputBuilding = building.getText();
                String inputBudget = budget.getText();

                // Write the data to the database
                try {
                    Connection conn = DriverManager.getConnection(url, username, password);
                    String query = "INSERT INTO department (dept_name, building, budget) VALUES (?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, inputName);
                    pstmt.setString(2, inputBuilding);
                    pstmt.setFloat(3, Float.valueOf(inputBudget));
                    pstmt.executeUpdate();
                    pstmt.close();

                    conn.close();
                } catch (SQLException | IllegalArgumentException ex) {
                    String alertMessage = "Enter valid/correct data.";
                    showAlert(alertMessage);
                }
                return "Data written to database";
            }
            return null;
        });

        dialog.getDialogPane().setPrefHeight(300);
        // Set the form as the dialog's content
        dialog.getDialogPane().setContent(form);
        // Request focus on the id field by default
        Platform.runLater(()-> departmentName.requestFocus());
        // Show the dialog and wait for the user to close it
        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Done!");
        });
    }

    @FXML
    void updateDeptButtonClicked(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        Label dept_name = new Label("Name of department to be updated/deleted: ");
        TextField deptField = new TextField();
        Button deleteButton = new Button("Delete Department");

        deleteButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuery = "DELETE FROM department WHERE dept_name = ?";
                PreparedStatement ps = conn.prepareStatement(delQuery);
                ps.setString(1, deptField.getText());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                showAlert("Enter correct dept name!");
                ex.printStackTrace();
            }
        });

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("building", "budget");
        listView.setPrefHeight(150);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Set a custom cell factory with event handlers for each cell
        listView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) {
                    // Show a dialog to get a new value for the selected item
                    String selectedItem = cell.getItem();
                    TextInputDialog textDialog = new TextInputDialog(selectedItem);
                    textDialog.setHeaderText("Enter a new value for " + selectedItem + ":");
                    textDialog.showAndWait().ifPresent(newValue -> {
                        // Update the selected item with the new value
                        try {
                            Connection conn = DriverManager.getConnection(url, username, password);
                            String oldValue = listView.getItems().get(cell.getIndex());
                            String query = "UPDATE department SET "+ oldValue +" = ? WHERE dept_name = ?";
                            if(deptField.getText().isEmpty()) {
                                showAlert("Fill the empty fields");
                            }
                            PreparedStatement pstmt = conn.prepareStatement(query);
                            if(oldValue.equals("budget")) {
                                pstmt.setInt(1, Integer.parseInt(newValue));
                            } else {
                            pstmt.setString(1, newValue);
                            }
                            pstmt.setString(2, deptField.getText());
                            pstmt.executeUpdate();
                            pstmt.close();
                            conn.close();
                        } catch (SQLException | IllegalArgumentException ex) {
                            String alertMessage = "Enter valid/correct data.";
                            showAlert(alertMessage);
                        }

                    });
                }
            });
            return cell;
        });

        // Add the listview and HBox to a VBox
        HBox hbox = new HBox(dept_name, deptField, deleteButton);
        hbox.setSpacing(8);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.getChildren().addAll(hbox, listView, deleteButton);
        dialog.getDialogPane().setContent(vbox);
        Platform.runLater(deptField::requestFocus);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();

    }

    @FXML
    void logoutClicked(ActionEvent event) throws IOException, InterruptedException {
        Parent page = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(page);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        TimeUnit.SECONDS.sleep(1);
        stage.show();
    }

    private void showSectionsDialog(Spinner<Integer> sections_spinner, List<Section> sections) {
//        System.out.println("In showSectionsDialog");
        Dialog<List<Section>> sectionDialog = new Dialog<>();
        sectionDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        List<Label> sectionLabels = new ArrayList<>();
        List<TextField> sectionIdFields = new ArrayList<>();
        List<TextField> sectionSemesterFields = new ArrayList<>();
        List<TextField> buildingFields = new ArrayList<>();
        List<TextField> roomIDFields = new ArrayList<>();

        int numSections = sections_spinner.getValue();
        for (int i = 1; i <= numSections; i++) {
            Label label = new Label("Section " + i + ":");
            TextField sec_id = new TextField();
            TextField sem = new TextField();
            TextField building = new TextField();
            TextField room_id = new TextField();

            sectionLabels.add(label);
            sectionIdFields.add(sec_id);
            sectionSemesterFields.add(sem);
            buildingFields.add(building);
            roomIDFields.add(room_id);
        }
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 150, 10, 10));
        for (int i = 0; i < numSections; i++) {
            Label label = sectionLabels.get(i);
            TextField id = sectionIdFields.get(i);
            TextField sem = sectionSemesterFields.get(i);
            TextField building = buildingFields.get(i);
            TextField room_id = roomIDFields.get(i);

            id.setPromptText("Enter sec_id:");
            sem.setPromptText("Enter semester:");
            building.setPromptText("Enter building:");
            room_id.setPromptText("Enter room_id");

            grid.add(label, 0, i);
            grid.add(id, 1, i);
            grid.add(sem, 2, i);
            grid.add(building, 3, i);
            grid.add(room_id, 4, i);
        }
        sectionDialog.getDialogPane().setContent(grid);

        sectionDialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                for (int i = 0; i < numSections; i++) {
                    String id = sectionIdFields.get(i).getText();
                    String semester = sectionSemesterFields.get(i).getText();
                    String building = buildingFields.get(i).getText();
                    String room_id = roomIDFields.get(i).getText();
                    sections.add(new Section(id, semester, building, room_id));
                }
                return sections;
            }
            return null;
        });
        sectionDialog.showAndWait();
    }
    private void getDepartmentCourses(TextField department, ListView<String> availableSections) {
            department.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    try {
                        Connection conn = DriverManager.getConnection(url, username, password);
                        String dept_name = department.getText();
                        // available sections (NOT assigned to any TA yet)
                        String select_query = "SELECT * FROM teaches t full outer JOIN section s " +
                                "using(course_id, sec_id, semester) natural join course c where t.id is null and c.dept_name = ?";
                        PreparedStatement select_pstmt = conn.prepareStatement(select_query);
                        select_pstmt.setString(1, dept_name);
                        ResultSet rs = select_pstmt.executeQuery();

                        String course_id = "", section_id = "", semester = "";
                        while (rs.next()) {
                            course_id = rs.getString("course_id");
                            section_id = rs.getString("sec_id");
                            semester = rs.getString("semester");
                            String course = course_id + ", " + section_id + ", " + semester;
                            availableSections.getItems().add(course);
                        }
                        if(availableSections.getItems().isEmpty()) {
                            String alertMessage = "No available courses in " + department.getText() + " department.";
                            showAlert(alertMessage);
                        }

                    } catch (SQLException | IllegalArgumentException e) {
                        String alertMessage = "Enter valid/correct data.";
                        showAlert(alertMessage);
                    }
                }
            });
    }

    private void showAlert(String message) {
        Alert failure = new Alert(Alert.AlertType.INFORMATION);
        failure.setTitle("Notification");
        failure.setContentText(message);
        failure.showAndWait();
    }

    private void constructGridPane(GridPane grid, Label[] labels, Node[] nodes) {
        grid.setHgap(10);
        grid.setVgap(10);
        int size = labels.length;
        for(int i = 0; i < size; i++) {
            grid.add(labels[i], 0, i);
            grid.add(nodes[i], 1, i);
        }
    }


    @AllArgsConstructor
    public static class Section {
        @Setter @Getter String id;
        @Setter @Getter String semester;
        @Setter @Getter String defaultBuilding;
        @Setter @Getter String defaultRoom;
    }
}
