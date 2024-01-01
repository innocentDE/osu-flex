package com.flex.database.storage;

import org.apache.logging.log4j.Logger;

import java.sql.Connection;

public abstract class MySqlStorage {

    Connection connection;
    Logger logger;
}
