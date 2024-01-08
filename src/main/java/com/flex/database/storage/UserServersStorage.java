package com.flex.database.storage;

import com.flex.data.FlexData;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserServersStorage extends MySqlStorage {

    public UserServersStorage(Connection connection) {
        super.connection = connection;
        logger = LogManager.getLogger(UserStorage.class);
    }

    public void addKeys(int userId, long serverId) throws SQLException {
        String query = "INSERT INTO user_servers (userId, serverId) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setLong(2, serverId);
        statement.executeUpdate();
    }

    public boolean isUserOnMultipleServers(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_servers WHERE userId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt(1) > 1;
        }
        return false;
    }

    public void removeAllKeys(long serverId) throws SQLException {
        String query = "DELETE FROM user_servers WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, serverId);
        statement.executeUpdate();
    }

    public void deleteKeys(int userId, long serverId) throws SQLException {
        String query = "DELETE FROM user_servers WHERE userId = ? AND serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setLong(2, serverId);
        statement.executeUpdate();

    }

    public Map<String, String> getServersByUser(int userId) throws SQLException {
        Map<String, String> servers = new HashMap<>();
        String query = "SELECT s.serverId, s.channelId\n" +
                "FROM servers s\n" +
                "INNER JOIN user_servers us ON s.serverId = us.serverId\n" +
                "WHERE us.userId = ?;";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            servers.put(
                    resultSet.getString("serverId"),
                    resultSet.getString("channelId")
            );
        }
        return servers;
    }

    public void setThreshold(long serverId, int threshold) throws SQLException {
        String query = "UPDATE user_servers SET threshold = ? WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, threshold);
        statement.setLong(2, serverId);
        statement.executeUpdate();
    }

    public void setThreshold(int userId, long serverId, int threshold) throws SQLException {
        String query = "UPDATE user_servers SET threshold = ? WHERE userId = ? AND serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, threshold);
        statement.setInt(2, userId);
        statement.setLong(3, serverId);
        statement.executeUpdate();
    }

    public int getThreshold(int userId, long serverId) throws SQLException {
        String query = "SELECT threshold FROM user_servers WHERE userId = ? AND serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        statement.setLong(2, serverId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("threshold");
        }
        return FlexData.DEFAULT_THRESHOLD;
    }
}
