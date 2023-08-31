module com.example.dbms_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires java.naming;
    requires jbcrypt;
    requires org.controlsfx.controls;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j.core;
    requires lombok;

    opens com.example.dbms_project to javafx.fxml;
    exports com.example.dbms_project;
}