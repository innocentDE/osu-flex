package com.flex.database;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySqlController {

    @Getter
    private Connection connection;
    private final Logger logger = LogManager.getLogger(MySqlController.class);

    public MySqlController(){
        registerDriver();
    }

    private void registerDriver(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e){
            logger.debug(e.getMessage());
            System.exit(1);
        }
    }

    public void connect(String url, String username, String password){
        try {
            connection = DriverManager.getConnection(url, username, password);
            logger.debug("Successfully connected to the database");
        } catch (SQLException e) {
            logger.debug(e.getMessage());
            System.exit(1);
        }
    }
}
