package com.flex.database.storage;

import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerStorage extends MySqlStorage {


    public ServerStorage(Connection connection){
        super.connection = connection;
    }

    public boolean isServerRegistered(long serverId) throws SQLException {
        String query = "SELECT * FROM servers WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, serverId);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public void registerServer(long serverId, long channelId) throws SQLException {
        String query = "INSERT INTO servers (serverId, channelId) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, serverId);
        statement.setLong(2, channelId);
        statement.executeUpdate();
    }

    public void unregisterServer(long serverId) throws SQLException {
        String query = "DELETE FROM servers WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, serverId);
        statement.executeUpdate();
    }

    public void setChannel(long serverId, long channelId) throws SQLException {
        String query = "UPDATE servers SET channelId = ? WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, channelId);
        statement.setLong(2, serverId);
        statement.executeUpdate();
    }
}
