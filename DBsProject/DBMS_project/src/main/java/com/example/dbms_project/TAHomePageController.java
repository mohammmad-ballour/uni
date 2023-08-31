package com.example.dbms_project;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import lombok.ToString;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TAHomePageController {

    @FXML @Setter @Getter
    private Button createLectureButton;

    @FXML @Setter @Getter
    private Button createStudentButton;

    @FXML
    private Hyperlink logoutLink;

    @FXML @Setter @Getter
    private Button updateLectureButton;

    @FXML @Setter @Getter
    private Button updateStudentButton;

    @FXML
    private Button searchLecture;

    @FXML
    private Button checkLectureButton;

    @FXML
    private Button checkSectionButton;

    @FXML
    private Button checkStudentButton;

    @FXML
    private Button insertAttendanceButton;

    final String url = "jdbc:postgresql://localhost/TAs?currentSchema=unisample";
    final String username = "ballour";
    final String password = "admin";

    @FXML
    void createLectureButtonClicked(ActionEvent event) {
        // Create a dialog to show the grid
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert New Lecture");

        // Set the button types (OK and Cancel)
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create a grid to get the user's data
        GridPane grid = new GridPane();

        Label[] labels = {new Label("course_id:"), new Label("section_id:"), new Label("semester:"),
            new Label("lecture date:"), new Label("lecture title:"), new Label("start time:"),
            new Label("end time:"), new Label("building:"), new Label("room_number:")};

        TextField course_idField = new TextField();
        TextField section_idField = new TextField();
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        DatePicker lectureDate = new DatePicker();
        TextField lectureStartTime = new TextField();
        lectureStartTime.setPromptText("hh:mm");
        TextField lectureEndTime = new TextField();
        lectureEndTime.setPromptText("hh:mm");
        TextField lectureTitle = new TextField();
        TextField lectureBuilding = new TextField();
        TextField lectureRoomID = new TextField();
        Node[] nodes = {course_idField, section_idField, semesters, lectureDate, lectureTitle, lectureStartTime,
        lectureEndTime, lectureBuilding, lectureRoomID};

        // add labels and nodes to the grid
        constructGridPane(grid, labels, nodes);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String course_id = course_idField.getText();
                String section_id = section_idField.getText();
                String semester = semesters.getSelectionModel().getSelectedItem();
                String building = lectureBuilding.getText();
                String roomID = lectureRoomID.getText();
                Date date = Date.valueOf(lectureDate.getValue());
                String startTime = lectureStartTime.getText();
                String endTime = lectureEndTime.getText();
                String title = lectureTitle.getText();

                try(Connection conn = DriverManager.getConnection(url, username, password)) {
                    String check_call = "{ ? = CALL is_available(?, ?, ?, ?, ?, ?) }";
                    CallableStatement checkStatement = conn.prepareCall(check_call);
                    String insertQuery = "INSERT INTO lecture (course_id, sec_id, semester, lec_date, lec_title, start_time, end_time)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?);";
                    PreparedStatement insertStatement = conn.prepareStatement(insertQuery);

                    checkStatement.registerOutParameter(1, Types.BOOLEAN);
                    checkStatement.setString(2, semester);
                    checkStatement.setDate(3, date);
                    checkStatement.setTime(4, Time.valueOf(startTime+":00"));
                    checkStatement.setTime(5, Time.valueOf(endTime+":00"));
                    checkStatement.setString(6, building);
                    checkStatement.setString(7, roomID);
                    checkStatement.execute();
                    boolean isAavailable = checkStatement.getBoolean(1);
                    if(!isAavailable) {
                        String altertMessage = "Your lecture overlaps with another existing one!";
                       showAlert(altertMessage);
                    } else {
                        insertStatement.setString(1, course_id);
                        insertStatement.setString(2, section_id);
                        insertStatement.setString(3, semester);
                        insertStatement.setDate(4, date);
                        insertStatement.setString(5, title);
                        insertStatement.setTime(6, Time.valueOf(startTime+":00"));
                        insertStatement.setTime(7, Time.valueOf(endTime+":00"));
                        insertStatement.executeUpdate();
                    }
                    insertStatement.close();
                    checkStatement.close();

                } catch (SQLException | IllegalArgumentException e) {
                    String alertMessage = "Enter valid/correct lecture data";
                    showAlert(alertMessage);
                }
            }
            return null;
        });

        dialog.getDialogPane().setPrefHeight(500);
        // Set the grid as the dialog's content
        dialog.getDialogPane().setContent(grid);
        // Request focus on the id field by default
        Platform.runLater(course_idField::requestFocus);
        // Show the dialog and wait for the user to close it
        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Done!");
        });

    }

    @FXML
    void createStudentButtonClicked(ActionEvent event) {
        // Create a dialog to show the grid
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insert New Student");

        // Set the button types (OK and Cancel)
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create a grid to get the user's data
        GridPane grid = new GridPane();

        Label[] labels = {new Label("student_id"), new Label("first name:"), new Label("second name:"),
                new Label("third name:"), new Label("last name:"), new Label("residence:"), new Label("department:"),
                new Label("credits:"), new Label("phone no:"),  new Label("second phone no:")};

        TextField student_idField = new TextField();
        TextField firstNameField = new TextField();
        TextField secondNameField = new TextField();
        TextField thirdNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField residenceField = new TextField();
        TextField departmentField = new TextField();
        TextField creditsField = new TextField("0");
        creditsField.setEditable(false);
        TextField firstPhoneField = new TextField();
        TextField secondPhoneField = new TextField();
        secondPhoneField.setPromptText("Optional");

        Node[] nodes = {student_idField, firstNameField, secondNameField, thirdNameField, lastNameField,
                residenceField, departmentField, creditsField, firstPhoneField, secondPhoneField};

        constructGridPane(grid, labels, nodes);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String student_id = student_idField.getText();
                String firstName = firstNameField.getText();
                String secondName = secondNameField.getText();
                String thirdName = thirdNameField.getText();
                String lastName = lastNameField.getText();
                String residence = residenceField.getText();
                String department = departmentField.getText();
                String firstPhoneNo = firstPhoneField.getText();
                String secondPhoneNo = firstPhoneField.getText();

                try(Connection conn = DriverManager.getConnection(url, username, password)) {
                    String query = "INSERT INTO student (id, first_name, second_name, third_name, last_name, " +
                            "residence, dept_name, tot_cred) values(?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setString(1, student_id);
                    ps.setString(2, firstName);
                    ps.setString(3, secondName);
                    ps.setString(4, thirdName);
                    ps.setString(5, lastName);
                    ps.setString(6, residence);
                    ps.setString(7, department);
                    // 'credits' is always 0 (fresh student!)
                    ps.setInt(8, 0);
                    ps.executeUpdate();

                    String phoneQuery = "INSERT INTO student_phones (id, phone_no) values(?, ?)";
                    PreparedStatement insertPhone1 = conn.prepareStatement(phoneQuery);
                    insertPhone1.setString(1, student_id);
                    insertPhone1.setString(2, firstPhoneNo);
                    if(!secondPhoneNo.isEmpty() && !firstPhoneNo.equals(secondPhoneNo)) {
                        PreparedStatement insertPhone2 = conn.prepareStatement(phoneQuery);
                        insertPhone2.setString(1, student_id);
                        insertPhone2.setString(2, secondPhoneNo);
                        insertPhone2.executeUpdate();
                        insertPhone2.close();
                    }
                    insertPhone1.executeUpdate();
                    insertPhone1.close();
                    ps.close();
                } catch (SQLException e) {
                    String alertMessage = "Enter valid/correct student data.";
                    showAlert(alertMessage);
                }
            }
            return null;
        });

        dialog.getDialogPane().setPrefHeight(400);

        // Set the grid as the dialog's content
        dialog.getDialogPane().setContent(grid);

        // Request focus on the id field by default
        Platform.runLater(student_idField::requestFocus);

        // Show the dialog and wait for the user to close it
        dialog.showAndWait().ifPresent(result -> {
            System.out.println("Done!");
        });
    }

    @FXML
    void updateLectureButtonClicked(ActionEvent event)  {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        TextField courseIDField = new TextField();
        TextField sectionIDField = new TextField();

        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        DatePicker datePicker = new DatePicker();
        Button updateButton = new Button("Update Lecture");
        Button deleteButton = new Button("Delete Lecture");
        HBox options = new HBox(updateButton, deleteButton);
        options.setSpacing(10);

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id:"), new Label("section_id:"), new Label("semester:"),
                new Label("date:"), new Label("option:")};
        Node[] nodes = {courseIDField, sectionIDField, semesters, datePicker, options};
        constructGridPane(grid, labels, nodes);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefHeight(300);
        dialog.getDialogPane().setPrefWidth(350);
        Platform.runLater(courseIDField::requestFocus);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        deleteButton.setOnAction(ev -> {
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuerty = "DELETE FROM lecture WHERE course_id = ? and sec_id = ? and semester = ? and lec_date = ?";
                PreparedStatement delPS = conn.prepareStatement(delQuerty);
                delPS.setString(1, courseIDField.getText());
                delPS.setString(2, sectionIDField.getText());
                delPS.setString(3, semesters.getSelectionModel().getSelectedItem());
                delPS.setDate(4, Date.valueOf(datePicker.getValue()));
                int rows_affected = delPS.executeUpdate();
                if(rows_affected == 0) {
                    String alterMessage = "Enter correct lecture data.";
                    showAlert(alterMessage);
                }
                delPS.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        });

        updateButton.setOnAction(ev -> {
            Dialog<String> updateInfo = new Dialog<>();
            Label titleLabel = new Label("title:");
            TextField titleField = new TextField();
            titleField.setPromptText("Leave it empty if will not be changed");
            Label startTimeLabel = new Label("start time:");
            TextField startTimeField = new TextField();
            Label endTimeLabel = new Label("end time:");
            TextField endTimeField = new TextField();

            GridPane infoGrid = new GridPane();
            infoGrid.add(titleLabel, 0, 0);
            infoGrid.add(titleField, 1, 0);

            HBox timeBox = new HBox();
            timeBox.setSpacing(10);
            timeBox.getChildren().addAll(startTimeLabel, startTimeField, endTimeLabel, endTimeField);
            infoGrid.add(timeBox, 0, 2);

            updateInfo.getDialogPane().setContent(infoGrid);
            Platform.runLater(titleField::requestFocus);
            updateInfo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            updateInfo.showAndWait();

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    String newLectureTitle = titleField.getText();
                    String newStartTime = startTimeField.getText();
                    String newEndTime = endTimeField.getText();

                    try (Connection conn = DriverManager.getConnection(url, username, password)) {
                        String select_query = "SELECT lec_title, start_time, end_time, building, room_number" +
                            " FROM lecture NATURAL JOIN section WHERE course_id = ? and sec_id = ? and semester = ? and lec_date = ?";
                        PreparedStatement selectPS = conn.prepareStatement(select_query);
                        selectPS.setString(1, courseIDField.getText());
                        selectPS.setString(2, sectionIDField.getText());
                        selectPS.setString(3, semesters.getSelectionModel().getSelectedItem());
                        selectPS.setDate(4, Date.valueOf(datePicker.getValue()));
                        ResultSet resultSet = selectPS.executeQuery();

                        String oldLectureTitle = "", building = "", room_number = "";
                        Time oldLectureStartTime = null, oldLectureEndTime = null;

                        while (resultSet.next()) {
                            oldLectureTitle = resultSet.getString(1);
                            oldLectureStartTime = resultSet.getTime(2);
                            oldLectureEndTime = resultSet.getTime(3);
                            building = resultSet.getString(4);
                            room_number = resultSet.getString(5);
                        }

                        if (!newStartTime.isEmpty() && !newEndTime.isEmpty()) {
                            Time newSTime = Time.valueOf(newStartTime + ":00");
                            Time newETime = Time.valueOf(newEndTime + ":00");
                            CallableStatement cs = conn.prepareCall("{ ? = CALL is_available(?, ?, ?, ?, ?, ?) }");
                            cs.registerOutParameter(1, Types.BOOLEAN);
                            cs.setString(2, semesters.getSelectionModel().getSelectedItem());
                            cs.setDate(3, Date.valueOf(datePicker.getValue()));
                            cs.setTime(4, newSTime);
                            cs.setTime(5, newETime);
                            cs.setString(6, building);
                            cs.setString(7, room_number);
                            cs.execute();
                            boolean isAvailable = cs.getBoolean(1);
                            String updateQuery = "UPDATE lecture SET lec_title = ?, start_time = ?, end_time = ? " +
                                    "WHERE course_id = ? AND sec_id = ? AND semester = ? AND lec_date = ?";
                            PreparedStatement updatePS = conn.prepareStatement(updateQuery);

                            if (isAvailable) {
                                String title = "";
                                title = newLectureTitle.isEmpty()? oldLectureTitle : newLectureTitle;
                                updatePS.setString(1, title);
                                updatePS.setTime(2, newSTime);
                                updatePS.setTime(3, newETime);
                                updatePS.setString(4, courseIDField.getText());
                                updatePS.setString(5, sectionIDField.getText());
                                updatePS.setString(6, semesters.getSelectionModel().getSelectedItem());
                                updatePS.setDate(7, Date.valueOf(datePicker.getValue()));
                                updatePS.executeUpdate();
                                updatePS.close();
                            } else {
                                String alertMessage = "Your lecture overlaps with another one!";
                                showAlert(alertMessage);
                            }

                            // changes just on lec_title
                        } else {
                            String updateQuery = "UPDATE lecture SET lec_title = ?, start_time = ?, end_time = ? " +
                                    "WHERE course_id = ? AND sec_id = ? AND semester = ? AND lec_date = ?";
                            PreparedStatement updateTitle = conn.prepareStatement(updateQuery);
                            updateTitle.setString(1, newLectureTitle);
                            updateTitle.setTime(2, oldLectureStartTime);
                            updateTitle.setTime(3, oldLectureEndTime);
                            updateTitle.setString(4, courseIDField.getText());
                            updateTitle.setString(5, sectionIDField.getText());
                            updateTitle.setString(6, semesters.getSelectionModel().getSelectedItem());
                            updateTitle.setDate(7, Date.valueOf(datePicker.getValue()));
                            updateTitle.executeUpdate();
                            updateTitle.close();
                        }

                    } catch (SQLException | IllegalArgumentException e) {
                        String alertMessage = "Enter valid/correct lecture data";
                        showAlert(alertMessage);
                    }
                }
                return null;
            });
        });
        dialog.showAndWait();
    }

    @FXML
    void updateStudentButton(ActionEvent event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select fields to update");

        // Create a label, text field and a list view with multiple selection
        Label label = new Label("ID for student to be updated/deleted: ");
        TextField user_id = new TextField();
        Button deleteButton = new Button("Delete Student");
        deleteButton.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String delQuerty = "DELETE FROM student WHERE id = ?";
                PreparedStatement delPS = conn.prepareStatement(delQuerty);
                delPS.setString(1, user_id.getText());
                int rows_affected = delPS.executeUpdate();
                if(rows_affected == 0) {
                    String alterMessage = "Enter correct id.";
                    showAlert(alterMessage);
                }
                delPS.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll("first_name", "second_name", "third_name", "last_name", "residence",
                "dept_name", "phone no");
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
                    if(selectedItem.equals("phone no"))
                        textDialog.setHeaderText("Enter the phone no you want to update:");
                    else
                        textDialog.setHeaderText("Enter a new value for " + selectedItem + ":");

                    textDialog.showAndWait().ifPresent(newValue -> {
                        try(Connection conn = DriverManager.getConnection(url, username, password);) {
                            if(selectedItem.equals("phone no")) {
                                textDialog.setHeaderText("Enter the new phone no:");
                                textDialog.showAndWait().ifPresent(newPhoneNo -> {
                                // System.out.println("Old Phone: "+ newValue);
                                // System.out.println("New Phone: "+ newPhone);
                                String updatePhoneQuery = "UPDATE student_phones SET phone_no = ? WHERE id = ? and phone_no = ?";
                                try {
                                    PreparedStatement pstmt = conn.prepareStatement(updatePhoneQuery);
                                    pstmt.setString(1, newPhoneNo);
                                    pstmt.setString(2, user_id.getText());
                                    // new Value is oldPhone, from the prev text dialog
                                    pstmt.setString(3, newValue);
                                    pstmt.executeUpdate();
                                    pstmt.close();
                                } catch (SQLException | IllegalArgumentException exception) {
                                    String alertMessage = "Enter valid/correct data.";
                                    showAlert(alertMessage);
                                }

                                });
                            } else {
                                String oldValue = listView.getItems().get(cell.getIndex()); // something rather than phone_no ^_^
                                String updateStudentQuery = "UPDATE student SET "+ oldValue + " = ? WHERE id = ?";
                                PreparedStatement pstmt = conn.prepareStatement(updateStudentQuery);
                                pstmt.setString(1, newValue);
                                pstmt.setString(2, user_id.getText());
                                pstmt.executeUpdate();
                                pstmt.close();
                            }
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
        HBox hbox = new HBox(label, user_id);
        hbox.setSpacing(8);
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        listView.setPrefHeight(200);
        vbox.getChildren().addAll(hbox, listView, deleteButton);
        dialog.getDialogPane().setContent(vbox);
        Platform.runLater(user_id::requestFocus);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    @FXML
    void searchLectureClicked(ActionEvent event) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Search Lecture");

        TextField courseIDField = new TextField();
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id: "), new Label("semester: ")};
        Node[] nodes = {courseIDField, semesters};
        constructGridPane(grid, labels, nodes);

        Button searchButton = new Button("Search");
        grid.add(searchButton, 0, 2);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));
        borderPane.setLeft(grid);
        BorderPane.setMargin(grid, new Insets(0, 15, 0, 0));

        searchButton.setOnAction(click -> {
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                String query = "select sec_id, lec_date, lec_title from lecture where course_id = ? and semester = ? order by 1, 2";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, courseIDField.getText());
                pstmt.setString(2, semesters.getSelectionModel().getSelectedItem());
                ResultSet rs = pstmt.executeQuery();
                ObservableList<LectureInfo> lectures = FXCollections.observableArrayList();
                while (rs.next()) {
                    String sec_id = rs.getString("sec_id");
                    Date date = rs.getDate("lec_date");
                    String title = rs.getString("lec_title");
                    lectures.add(new LectureInfo(sec_id, date, title));
                }
                TableView<LectureInfo> tableView = new TableView<>(lectures);
                TableColumn<LectureInfo, String> secIDColumn = new TableColumn<>("sec_id");
                secIDColumn.setCellValueFactory(new PropertyValueFactory<>("sectionID"));
                secIDColumn.setMinWidth(50);
                secIDColumn.setMaxWidth(100);

                TableColumn<LectureInfo, Date> dateColumn = new TableColumn<>("lec_date");
                dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
                dateColumn.setMinWidth(100);
                dateColumn.setMaxWidth(150);

                TableColumn<LectureInfo, String> titleColumn = new TableColumn<>("lec_title");
                titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
                titleColumn.setMinWidth(300);
                titleColumn.setMaxWidth(500);

                // disable auto-resizing columns
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                tableView.setMaxWidth(900);
                tableView.getColumns().addAll(secIDColumn, dateColumn, titleColumn);
                Label label = new Label(courseIDField.getText()+" lectures ("+
                        semesters.getSelectionModel().getSelectedItem()+" semester)");
                borderPane.setTop(label);
                BorderPane.setAlignment(label, Pos.CENTER);
                borderPane.setCenter(tableView);

                pstmt.close();
                rs.close();
            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }


        });
        dialog.getDialogPane().setContent(borderPane);
        Platform.runLater(courseIDField::requestFocus);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(500);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();

    }

    @FXML
    void checkStudentButtonClicked(ActionEvent event) {
        Dialog<String> infoDialog = new Dialog<>();
        BorderPane root = new BorderPane();

        TextField courseIDField = new TextField();
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        TextField studentIDField = new TextField();

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id: "), new Label("semester: "), new Label("student_id: ")};
        Node[] nodes = {courseIDField, semesters, studentIDField};
        constructGridPane(grid, labels, nodes);

        Button submitButton = new Button("Submit");
        grid.add(submitButton, 0, 3);

        root.setLeft(grid);
        BorderPane.setMargin(grid, new Insets(0, 10, 0, 0));

        submitButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {

                String query = "SELECT lec_date, status FROM attends WHERE id = ? and course_id = ? and semester = ? ORDER BY 1";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, studentIDField.getText());
                ps.setString(2, courseIDField.getText());
                ps.setString(3, semesters.getSelectionModel().getSelectedItem());
                ResultSet rs = ps.executeQuery();
                ObservableList<StudentAttendanceInfo> data = FXCollections.observableArrayList();

                while (rs.next()) {
                    Date date = rs.getDate(1);
                    String status = (rs.getString(2).equals("present"))? "1" : "0";
                    data.add(new StudentAttendanceInfo(date, status));
                }
                TableView<StudentAttendanceInfo> tableView =  new TableView<>(data);

                TableColumn<StudentAttendanceInfo, Date> dateColumn = new TableColumn<>("lecture date");
                dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

                TableColumn<StudentAttendanceInfo, String> statusColumn = new TableColumn<>("status");
                // fill the cell with status
                statusColumn.setCellValueFactory(cellData -> cellData.getValue().getStatusProperty());

                // make the cell editable, TextFieldTableCell is an editable subclass of TableCell
                statusColumn.setCellFactory(TextFieldTableCell.forTableColumn());

                // commit changes to the database
                statusColumn.setOnEditCommit(ev -> {
                    StudentAttendanceInfo info = ev.getRowValue();
                    Date date = info.getDate();
                    String newValue = ev.getNewValue();

                    String updateStudentStatue = "UPDATE attends SET status = ? where id = ? and lec_date = ?";
                    try {
                        Connection connection = DriverManager.getConnection(url, username, password);
                        PreparedStatement updateStmt = connection.prepareStatement(updateStudentStatue);
                        updateStmt.setString(1, newValue.equals("1") ? "present" : "absent");
                        updateStmt.setString(2, studentIDField.getText());
                        updateStmt.setDate(3, date);
                        updateStmt.executeUpdate();
                        updateStmt.close();
                        connection.close();
                    } catch (SQLException | IllegalArgumentException exception) {
                        String alertMessage = "Enter valid/correct data.";
                        showAlert(alertMessage);
                    }

                });

                tableView.setEditable(true);
                tableView.getColumns().addAll(dateColumn, statusColumn);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                root.setCenter(tableView);

            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }
        });

        infoDialog.getDialogPane().setContent(root);
        infoDialog.getDialogPane().setPrefWidth(600);
        infoDialog.getDialogPane().setPrefHeight(500);
        infoDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Platform.runLater(courseIDField::requestFocus);
        infoDialog.showAndWait();
    }

    @FXML
    void checkLectureButtonClicked(ActionEvent event) {
        Dialog<String> infoDialog = new Dialog<>();
        BorderPane root = new BorderPane();

        TextField courseIDField = new TextField();
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Search by date:");
        TextField titleField = new TextField();
        titleField.setPromptText("Search by title:");

        List<String> lectures = new ArrayList<>();
        courseIDField.textProperty().addListener((observable, oldValue, newValue) ->{
            if(newValue != null && !newValue.isEmpty()) {
                try(Connection conn = DriverManager.getConnection(url, username, password)) {
                    String sql = "SELECT DISTINCT lec_title FROM lecture WHERE course_id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, courseIDField.getText());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String lecture = rs.getString("lec_title");
                        lectures.add(lecture);
                    }
                    System.out.println(lectures);
                    ps.close();
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                TextFields.bindAutoCompletion(titleField, lectures);
            }
        });

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id:"), new Label("semester:"), new Label("date:"), new Label("title:")};
        Node[] nodes = {courseIDField, semesters, datePicker, titleField};
        constructGridPane(grid, labels, nodes);

        Button submitButton = new Button("Submit");
        grid.add(submitButton, 0, 4);

        root.setLeft(grid);
        BorderPane.setMargin(grid, new Insets(0, 10, 0, 0));

        submitButton.setOnAction(e -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                PreparedStatement ps = null;
                PreparedStatement statsStmt = null;
                ResultSet rs = null;
                ResultSet statsRS = null;
                // search by date
                if(datePicker.getValue() != null && titleField.getText().isEmpty()) {
                    String query01 = "SELECT s.id, (s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name)" +
                            " as full_name, status FROM attends natural join student s" +
                            " WHERE course_id = ? and semester = ? and lec_date = ? order by 1";

                    String statsQuery01 = "select count(case when status = 'present' then 1 end), " +
                            "count(case when status = 'present' then 1 end) / count(*)::float as ratio from attends " +
                            "where course_id = ? and semester = ? and lec_date = ? order by 1; ";

                    ps = conn.prepareStatement(query01);
                    statsStmt = conn.prepareStatement(statsQuery01);

                    ps.setDate(3, Date.valueOf(datePicker.getValue()));
                    statsStmt.setDate(3, Date.valueOf(datePicker.getValue()));

                    // search by title
                } else if(datePicker.getValue() == null && !titleField.getText().isEmpty()) {
                    String query02 = "SELECT s.id, (s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name)" +
                            " as full_name, status FROM attends natural join student s natural join lecture l" +
                            " WHERE course_id = ? and semester = ? and lec_title = ? order by 1";

                    String statsQuery02 = "select count(case when status = 'present' then 1 end), " +
                        "count(case when status = 'present' then 1 end) / count(*)::float as ratio from attends natural join lecture " +
                            "where course_id = ? and semester = ? and lec_title = ? order by 1; ";

                    ps = conn.prepareStatement(query02);
                    statsStmt = conn.prepareStatement(statsQuery02);

                    ps.setString(3, titleField.getText());
                    statsStmt.setString(3, titleField.getText());
                    // search by both date and title
                } else {
                    String query03 = "SELECT s.id, (s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name)" +
                            " as full_name, status FROM attends natural join student s natural join lecture l" +
                            " WHERE course_id = ? and semester = ? and lec_date = ? and lec_title = ? order by 1";

                    String statsQuery03 = "select count(case when status = 'present' then 1 end), " +
                    "count(case when status = 'present' then 1 end) / count(*)::float as ratio from attends natural join lecture " +
                            "where course_id = ? and semester = ? and lec_date = ? and lec_title = ? order by 1; ";

                    ps = conn.prepareStatement(query03);
                    statsStmt = conn.prepareStatement(statsQuery03);
                    ps.setDate(3, Date.valueOf(datePicker.getValue()));
                    ps.setString(4, titleField.getText());
                    statsStmt.setDate(3, Date.valueOf(datePicker.getValue()));
                    statsStmt.setString(4, titleField.getText());
                }
                // set course_id and semester for all cases
                ps.setString(1, courseIDField.getText());
                ps.setString(2, semesters.getSelectionModel().getSelectedItem());
                statsStmt.setString(1, courseIDField.getText());
                statsStmt.setString(2, semesters.getSelectionModel().getSelectedItem());

                rs = ps.executeQuery();
                statsRS = statsStmt.executeQuery();

                ObservableList<LectureAttendanceInfo> data = FXCollections.observableArrayList();

                while (rs.next()) {
                    String id = rs.getString(1);
                    String fullName = rs.getString(2);
                    int status = (rs.getString(3).equals("present"))? 1 : 0;
                    data.add(new LectureAttendanceInfo(id, fullName, status));
                }
                TableView<LectureAttendanceInfo> tableView =  new TableView<>(data);

                TableColumn<LectureAttendanceInfo, String> idColumn = new TableColumn<>("ID");
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

                TableColumn<LectureAttendanceInfo, String> nameColumn = new TableColumn<>("fullName");
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

                TableColumn<LectureAttendanceInfo, Integer> statusColumn = new TableColumn<>("status");
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

                tableView.getColumns().addAll(idColumn, nameColumn, statusColumn);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                root.setCenter(tableView);


                if (statsRS.next()) {
                    int count = statsRS.getInt(1);
                    float percentage = statsRS.getFloat(2);
                    HBox hbox = new HBox(5);
                    Label numberOfStudentsPresent = new Label("#StudentsPresent  ");
                    numberOfStudentsPresent.setWrapText(true);  // wrap as possible

                    Label attendaceRate = new Label("Attendance Rate:   ");
                    attendaceRate.setWrapText(true);

                    TextField t1 = new TextField(count+"");
                    TextField t2 = new TextField(percentage+"");
                    t1.setEditable(false);
                    t2.setEditable(false);

                    hbox.getChildren().addAll(numberOfStudentsPresent, t1, attendaceRate, t2);
                    root.setBottom(hbox);
                    BorderPane.setAlignment(hbox, Pos.BOTTOM_CENTER);
                    BorderPane.setMargin(hbox, new Insets(10, 0, 0, 0));
                }


            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }
        });
        infoDialog.getDialogPane().setContent(root);
        infoDialog.getDialogPane().setPrefWidth(700);
        infoDialog.getDialogPane().setPrefHeight(600);
        infoDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Platform.runLater(courseIDField::requestFocus);
        infoDialog.showAndWait();
    }

    @FXML
    void checkSectionButtonClicked(ActionEvent event) {
        Dialog<String> infoDialog = new Dialog<>();
        ButtonType reportType = new ButtonType("See Report", ButtonBar.ButtonData.OK_DONE);
        BorderPane root = new BorderPane();

        TextField courseIDField = new TextField();
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        semesters.setValue("Spring");

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id:"), new Label("semester: ")};
        Node[] nodes = {courseIDField, semesters};
        constructGridPane(grid, labels, nodes);

        Button submitButton = new Button("Submit");
        grid.add(submitButton, 0, 2);

        root.setLeft(grid);
        BorderPane.setMargin(grid, new Insets(0, 10, 0, 0));

        submitButton.setOnAction(ev -> {
            try(Connection conn = DriverManager.getConnection(url, username, password)) {
                String query = "select id, s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name" +
                        " as full_name , sec_id, lec_date, status from attends natural join student s" +
                        " where course_id = ? and semester = ? order by 3, 4, 1";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, courseIDField.getText());
                ps.setString(2, semesters.getSelectionModel().getSelectedItem());
                ResultSet rs = ps.executeQuery();

                ObservableList<SectionAttendanceInfo> lectures = FXCollections.observableArrayList();
                while (rs.next()) {
                    String studentID = rs.getString(1);
                    String name = rs.getString(2);
                    String sectionID = rs.getString(3);
                    Date lecDate = rs.getDate(4);
                    int status = rs.getString(5).equals("present")? 1 : 0;
                    lectures.add(new SectionAttendanceInfo(studentID, name, sectionID, lecDate, status));
                }

                TableView<SectionAttendanceInfo> tableView =  new TableView<>(lectures);

                TableColumn<SectionAttendanceInfo, String> idColumn = new TableColumn<>("student_id");
                idColumn.setCellValueFactory(new PropertyValueFactory<>("studentID"));

                TableColumn<SectionAttendanceInfo, String> nameColumn = new TableColumn<>("name");
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));

                TableColumn<SectionAttendanceInfo, String> sectionColumn = new TableColumn<>("section_id");
                sectionColumn.setCellValueFactory(new PropertyValueFactory<>("sectionID"));

                TableColumn<SectionAttendanceInfo, Date> dateColumn = new TableColumn<>("lec_date");
                dateColumn.setCellValueFactory(new PropertyValueFactory<>("lectureDate"));

                TableColumn<SectionAttendanceInfo, Integer> statusColumn = new TableColumn<>("status");
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

                tableView.getColumns().addAll(idColumn, nameColumn, sectionColumn, dateColumn, statusColumn);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                root.setCenter(tableView);
                BorderPane.setMargin(tableView, new Insets(10, 0, 0, 10));

                infoDialog.setResultConverter(buttonType -> {
                    if(buttonType == reportType) {
                        Dialog<String> reportDialog = new Dialog<>();
                        BorderPane pane = new BorderPane();

                        TableView<StatsInfo> statsInfoTable = getStatisticalInfo(courseIDField, semesters);
                        pane.setCenter(statsInfoTable);
                        BorderPane.setMargin(statsInfoTable, new Insets(20));

                        Button exportToExcelButton = new Button("export To Excel");
                        pane.setBottom(exportToExcelButton);
                        BorderPane.setAlignment(exportToExcelButton, Pos.CENTER);

                        exportToExcelButton.setOnAction(e -> {
                            try {
                                ObservableList<StatsInfo> studentsInfo = statsInfoTable.getItems();
                                List<String> idCol = new ArrayList<>();
                                List<String> nameCol = new ArrayList<>();
                                List<Integer> countCol = new ArrayList<>();
                                List<Float> avgCol = new ArrayList<>();

                                for (StatsInfo studentInfo : studentsInfo) {
                                    idCol.add(studentInfo.getId());
                                    nameCol.add(studentInfo.getName());
                                    countCol.add(studentInfo.getCount());
                                    avgCol.add(studentInfo.getAvg());
                                }
                                String filePath = "output.xlsx";

                                Workbook workbook = new XSSFWorkbook();
                                Sheet sheet = workbook.createSheet("Sheet1");
                                Row headerRow = sheet.createRow(0);
                                headerRow.createCell(0).setCellValue("id");
                                headerRow.createCell(1).setCellValue("name");
                                headerRow.createCell(2).setCellValue("#of attendances");
                                headerRow.createCell(3).setCellValue("attendance rate");

                                int rowNum = 1;
                                for (int i = 0; i < idCol.size(); i++) {
                                    Row row = sheet.createRow(rowNum++);
                                    Cell cell1 = row.createCell(0);
                                    cell1.setCellValue(idCol.get(i));

                                    Cell cell2 = row.createCell(1);
                                    cell2.setCellValue(nameCol.get(i));

                                    Cell cell3 = row.createCell(2);
                                    cell3.setCellValue(countCol.get(i));

                                    Cell cell4 = row.createCell(3);
                                    cell4.setCellValue(avgCol.get(i));
                                }

                                FileOutputStream fileOut = new FileOutputStream(filePath);
                                workbook.write(fileOut);
                                fileOut.close();

                                System.out.println("Excel file created successfully!");

                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                        });

                        reportDialog.getDialogPane().setContent(pane);
                        reportDialog.getDialogPane().setPrefWidth(600);
                        reportDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        reportDialog.showAndWait();
                    }
                    return null;
                });

                ps.close();
                rs.close();

            } catch (SQLException | IllegalArgumentException ex) {
                String alertMessage = "Enter valid/correct data.";
                showAlert(alertMessage);
            }

        });
        infoDialog.getDialogPane().setContent(root);
        infoDialog.getDialogPane().setPrefHeight(700);
        infoDialog.getDialogPane().setPrefWidth(900);
        infoDialog.getDialogPane().getButtonTypes().addAll(reportType, ButtonType.CANCEL);
        Platform.runLater(courseIDField::requestFocus);
        infoDialog.showAndWait();
    }

    @FXML
    void insertAttendanceButtonClicked(ActionEvent event) {
        Dialog<String> infoDialog = new Dialog<>();
        BorderPane root = new BorderPane();

        TextField courseIDField = new TextField("CS301");   // for testing
        TextField sectionIDField = new TextField("101");    // for testing
        ComboBox<String> semesters = new ComboBox<>();
        semesters.getItems().addAll("Winter", "Spring", "Summer", "Fall");
        DatePicker picker = new DatePicker();
        picker.setValue(LocalDate.of(2023, 5, 1)); // for testing

        GridPane grid = new GridPane();
        Label[] labels = {new Label("course_id:"), new Label("section_id:"), new Label("semester:"), new Label("date:")};
        Node[] nodes = {courseIDField, sectionIDField, semesters, picker};
        constructGridPane(grid, labels, nodes);

        RadioButton fillManuallyButton = new RadioButton("Fill manually");
        RadioButton importFromExcelButton = new RadioButton("Import from excel");
        ToggleGroup group = new ToggleGroup();
        fillManuallyButton.setToggleGroup(group);
        importFromExcelButton.setToggleGroup(group);
        HBox hbox = new HBox(fillManuallyButton, importFromExcelButton);
        hbox.setSpacing(20);
        grid.add(hbox, 0, 4);

        root.setLeft(grid);
        BorderPane.setMargin(grid, new Insets(0, 10, 0, 0));

        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            String option = ((RadioButton) newVal).getText();

            // query to retrieve enrolled students data
            String checkQuery = "select id from takes where course_id = ? and sec_id = ? and semester = ?";
            if (option.equals(fillManuallyButton.getText())) {
                ObservableList<InsertAttendanceInfo> data = FXCollections.observableArrayList();

                TableView<InsertAttendanceInfo> tableView = new TableView<>(data);

                TableColumn<InsertAttendanceInfo, String> idColumn = new TableColumn<>("id/name/phone_no");
                idColumn.setCellValueFactory(new PropertyValueFactory<>("identifier"));

                TableColumn<InsertAttendanceInfo, String> statusColumn = new TableColumn<>("status");
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));


                Set<String> studentsDataSet = new HashSet<>();
                try(Connection connection = DriverManager.getConnection(url, username, password)) {
                    String sql = "select id, s.first_name || ' ' || s.second_name || ' ' || s.third_name || ' ' || s.last_name " +
                            "as full_name, sp.phone_no from takes t natural join student s natural join student_phones sp " +
                            "where course_id = ? and sec_id = ? and semester = ?";

                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setString(1, courseIDField.getText());
                    ps.setString(2, sectionIDField.getText());
                    ps.setString(3, semesters.getSelectionModel().getSelectedItem());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String id = rs.getString(1);
                        String fullname = rs.getString(2);
                        String phone = rs.getString(3);
                        studentsDataSet.add(id);
                        studentsDataSet.add(fullname);
                        studentsDataSet.add(phone);
                    }
                    ps.close();
                    rs.close();
                } catch (SQLException s) {
                    s.printStackTrace();
                }

                tableView.getColumns().addAll(idColumn, statusColumn);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                TextField identifierField = new TextField();
                TextField statusField = new TextField();
                Button enterButton = new Button("Enter");
                identifierField.setPrefWidth(140);
                statusField.setPrefWidth(30);
                enterButton.setPrefWidth(50);
                AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion
                        (identifierField, new ArrayList<>(studentsDataSet));

                HBox hBox1 = new HBox(new Label("id: "), identifierField);
                HBox hBox2 = new HBox(new Label("status: "), statusField);
                HBox container = new HBox(hBox1, hBox2, enterButton);
                hBox1.setSpacing(10);
                hBox2.setSpacing(10);
                container.setSpacing(10);
                enterButton.setOnAction(ev -> {
                    String status = statusField.getText();
                    if(status.equals("1") || status.equals("0")) {
                        tableView.getItems().add(new InsertAttendanceInfo(identifierField.getText(), status));
                        identifierField.clear();
                        statusField.clear();
                    } else {
                        showAlert("status is either 1 or 0");
                    }
                });


                Button commitButton = new Button("commit to database");
                VBox vbox = new VBox(tableView, container, commitButton);
                vbox.setSpacing(15);
                root.setCenter(vbox);
                BorderPane.setMargin(vbox, new Insets(10));
                vbox.setAlignment(Pos.CENTER);

                commitButton.setOnAction(commit -> {
                    PreparedStatement insertPS = null;
                    // all enrolled students
                    Set<String> ids = new HashSet();

                    try (Connection conn = DriverManager.getConnection(url, username, password)) {

                        PreparedStatement checkPS = conn.prepareStatement(checkQuery);
                        checkPS.setString(1, courseIDField.getText());
                        checkPS.setString(2, sectionIDField.getText());
                        checkPS.setString(3, semesters.getSelectionModel().getSelectedItem());

                        ResultSet rs = checkPS.executeQuery();
                        while (rs.next()) {
                            ids.add(rs.getString(1));
                        }
                        String insertQuery = "insert into attends (id, course_id, sec_id, semester, lec_date, status) " +
                                "values (?, ?, ?, ?, ?, ?)";
                        insertPS = conn.prepareStatement(insertQuery);

                        for (InsertAttendanceInfo row : tableView.getItems()) {
                            String identifier = row.getIdentifier();
                            if (identifier.isEmpty()) continue;
                            identifier = getRealID(conn, identifier, null, null);
                            insertIntoAttends(insertPS, identifier, courseIDField, sectionIDField, semesters, picker, row.getStatus());
                            // TA has entered it manually
                            ids.remove(identifier);
                            checkPS.close();
                            rs.close();
                        }
                        // TA has NOT entered data manually, so set status to 0 by default
                        for (String id : ids) {
                            System.out.println("remaining: "+id);
                            insertIntoAttends(insertPS, id, courseIDField, sectionIDField, semesters, picker, "absent");
                        }
                        if(insertPS != null) insertPS.close();

                    } catch (SQLException | IllegalArgumentException exception) {
                        exception.printStackTrace();
                        String alertMessage = "Enter valid/correct data.";
                        showAlert(alertMessage);
                    }

                });

            } else {
                root.setCenter(null);   // remove the tableview
                String insertQuery = "insert into attends (id, course_id, sec_id, semester, lec_date, status) " +
                        "values (?, ?, ?, ?, ?, ?)";
                Set<String> enrolled_ids = new HashSet<>();

                TextInputDialog dialog = new TextInputDialog();
                dialog.setHeaderText("Enter the file name:");
                dialog.setContentText("File Name:");
                dialog.showAndWait().ifPresent(filename -> {
                    try(Workbook workbook = new XSSFWorkbook(filename+".xlsx");
                        Connection conn = DriverManager.getConnection(url, username, password);
                        PreparedStatement insertPS = conn.prepareStatement(insertQuery);
                        PreparedStatement checkPS = conn.prepareStatement(checkQuery)) {
                        checkPS.setString(1, courseIDField.getText());
                        checkPS.setString(2, sectionIDField.getText());
                        checkPS.setString(3, semesters.getSelectionModel().getSelectedItem());

                        ResultSet rs = checkPS.executeQuery();

                        while (rs.next()) {
                            enrolled_ids.add(rs.getString(1));
                        }

                        Sheet sheet = workbook.getSheetAt(0);
                        for(Row row: sheet) {
                            Cell studentIdCell = row.getCell(0);
                            Cell statusCell = row.getCell(1);
                            studentIdCell.setCellType(CellType.STRING);
                            String id = getRealID(conn, studentIdCell.getStringCellValue(), null, null);
                            if(enrolled_ids.contains(id)) {
                                insertPS.setString(1, id);
                                insertPS.setString(2, courseIDField.getText());
                                insertPS.setString(3, sectionIDField.getText());
                                insertPS.setString(4, semesters.getSelectionModel().getSelectedItem());
                                insertPS.setDate(5, Date.valueOf(picker.getValue()));
                                insertPS.setString(6, statusCell.getNumericCellValue() == 1? "present" : "absent");
                                // perform the insertions as a single unit.
                                insertPS.addBatch();
                            } else {
                                System.out.println("DIRTY DATA ARE REJECTED.\nSome students in the sheet are not enrolled");
                                continue;
                            }
                        }
                        insertPS.executeBatch();
                    } catch (IOException | SQLException | org.apache.poi.openxml4j.exceptions.InvalidOperationException ex) {
                        String alertMessage = "Cannot find file";
                        if(ex instanceof SQLException) {
                            alertMessage = "(Re)Enter valid/correct data";
                            showAlert(alertMessage);
                        } else
                         showAlert(alertMessage);
                    }
                });

            }
        });


        Platform.runLater(courseIDField::requestFocus);
        infoDialog.getDialogPane().setContent(root);
        infoDialog.getDialogPane().setPrefWidth(800);
        infoDialog.getDialogPane().setPrefHeight(600);
        infoDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        infoDialog.showAndWait();
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

    // helper methods

    private TableView<StatsInfo> getStatisticalInfo(TextField courseIDField, ComboBox<String> semesters) {
        TableView<StatsInfo> statsTable = null;
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String statsQuery = "select id, s.first_name || ' ' || s.second_name || '. ' || s.third_name || '. ' || s.last_name as full_name ," +
                    "count(case when status = 'present' then 1 end), count(case when status = 'present' then 1 end) / count(*)::float as ratio " +
                    "from attends natural join student s where course_id = ? and semester = ? group by 1,2 order by 1";
            PreparedStatement statsStmt = conn.prepareStatement(statsQuery);
            statsStmt.setString(1, courseIDField.getText());
            statsStmt.setString(2, semesters.getSelectionModel().getSelectedItem());

            ObservableList<StatsInfo> statsData = FXCollections.observableArrayList();
            ResultSet statsRS = statsStmt.executeQuery();
            while (statsRS.next()) {
                String id = statsRS.getString(1);
                String name = statsRS.getString(2);
                int count = statsRS.getInt(3);
                float avg = statsRS.getFloat(4);
                statsData.add(new StatsInfo(id, name, count, avg));
            }
            statsTable = new TableView<>(statsData);

            TableColumn<StatsInfo, String> sIDColumn = new TableColumn<>("student_id");
            sIDColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

            TableColumn<StatsInfo, String> sNameColumn = new TableColumn<>("name");
            sNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<StatsInfo, Integer> sCountColumn = new TableColumn<>("#of attendance");
            sCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));

            TableColumn<StatsInfo, Integer> sAvgColumn = new TableColumn<>("attendance rate");
            sAvgColumn.setCellValueFactory(new PropertyValueFactory<>("avg"));

            statsTable.getColumns().addAll(sIDColumn, sNameColumn, sCountColumn, sAvgColumn);
            statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            statsStmt.close();
            statsRS.close();

        } catch (SQLException | IllegalArgumentException e) {
            String alertMessage = "Enter valid/correct data.";
            showAlert(alertMessage);
        }
        return statsTable;
    }

    private String getRealID(Connection conn, String identifier, PreparedStatement ps, ResultSet rs)
            throws SQLException {
        String getID = "";
        // identifier is a phone number
        if (identifier.matches("^059-.*")) {
            getID = "SELECT id FROM student_phones WHERE phone_no = ?";
            ps = conn.prepareStatement(getID);
            ps.setString(1, identifier);
            rs = ps.executeQuery();
            if (rs.next()) identifier = rs.getString("id"); // corresponding id
            else System.out.println("No corresponding phone number!");

            // identifier is fullname
        } else if (identifier.matches("^[a-zA-Z .]+$")) {
            getID = "SELECT id FROM student WHERE first_name = ? and second_name = ? " +
                    "and third_name = ? and last_name = ?";
            ps = conn.prepareStatement(getID);
            String[] nameParts = identifier.split(" ");
            ps.setString(1, nameParts[0]);
            ps.setString(2, nameParts[1]);
            ps.setString(3, nameParts[2]);
            ps.setString(4, nameParts[3]);
            rs = ps.executeQuery();
            if (rs.next()) identifier = rs.getString("id"); // corresponding id
            else System.out.println("No corresponding name!");

            // verify the ID
        } else {
            getID = "SELECT 1 FROM student WHERE id = ?";
            ps = conn.prepareStatement(getID);
            ps.setString(1, identifier);
            rs = ps.executeQuery();
            if (!rs.next()) System.out.println("No corresponding id!");
        }
        ps.close();
        rs.close();
        return identifier;
    }

    private void insertIntoAttends(PreparedStatement insertPS, String identifier, TextField courseIDField,
        TextField sectionIDField, ComboBox<String> semesters, DatePicker picker, String status) throws SQLException {
        insertPS.setString(1, identifier);
        insertPS.setString(2, courseIDField.getText());
        insertPS.setString(3, sectionIDField.getText());
        insertPS.setString(4, semesters.getSelectionModel().getSelectedItem());
        insertPS.setDate(5, Date.valueOf(picker.getValue()));
        insertPS.setString(6, status.equals("1")? "present" : "absent");
        insertPS.executeUpdate();
        System.out.println("committed");
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
        grid.setPadding(new Insets(10));
        int size = labels.length;
        for(int i = 0; i < size; i++) {
            grid.add(labels[i], 0, i);
            grid.add(nodes[i], 1, i);
        }
    }


    @AllArgsConstructor
    public static class LectureInfo {
        @Setter @Getter private String sectionID;
        @Setter @Getter private Date date;
        @Setter @Getter private String title;
    }

    public static class StudentAttendanceInfo {
        @Getter private Date date;
        private final SimpleStringProperty statusProperty;

        public StudentAttendanceInfo(Date date, String status) {
            this.date = date;
            this.statusProperty = new SimpleStringProperty(status);
        }

        public ObservableValue<String> getStatusProperty() {
            return statusProperty;
        }

        public String getStatus() {
            return statusProperty.get();
        }
    }

    @AllArgsConstructor
    public static class LectureAttendanceInfo {
        @Setter @Getter private String id;
        @Setter @Getter private String fullName;
        @Setter @Getter private int status;
    }

    @AllArgsConstructor
    public static  class SectionAttendanceInfo {
        @Setter @Getter private String studentID;
        @Setter @Getter private String fullName;
        @Setter @Getter private String sectionID;
        @Setter @Getter private Date lectureDate;
        @Setter @Getter private int status;
    }

    @AllArgsConstructor @ToString
    public static class StatsInfo {
        @Setter @Getter private String id;
        @Setter @Getter private String name;
        @Setter @Getter private int count;
        @Setter @Getter private float avg;
    }

    @AllArgsConstructor
    public static class InsertAttendanceInfo {
        @Setter @Getter private String identifier;
        @Setter @Getter private String status;
    }
}