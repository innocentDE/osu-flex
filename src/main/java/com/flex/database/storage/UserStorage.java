package com.flex.database.storage;

import com.flex.osu.entities.user.User;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserStorage extends MySqlStorage {

    public UserStorage(Connection connection) {
        super.connection = connection;
        logger = LogManager.getLogger(UserStorage.class);
    }

    public void registerUser(User user) throws SQLException {
        String query = "INSERT INTO users (id, username) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, user.id);
        statement.setString(2, user.username);
        statement.executeUpdate();
    }

    public Optional<Integer> getUserId(String username) throws SQLException {
        String query = "SELECT id FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return Optional.of(resultSet.getInt("id"));
        }
        return Optional.empty();
    }

    public boolean isUserRegistered(String username, long serverId) throws SQLException {
        String query = "SELECT u.id FROM users u " +
                "JOIN user_servers us ON u.id = us.userId " +
                "WHERE u.username = ? AND us.serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        statement.setLong(2, serverId);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public void removeUser(int id) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, id);
        statement.executeUpdate();
    }

    public List<Integer> getUserIds() throws SQLException {
        String query = "SELECT id FROM users";
        List<Integer> userIds = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            userIds.add(resultSet.getInt("id"));
        }
        return userIds;
    }

    public void insertBestId(int userId, long bestId) throws SQLException {
        String query = "UPDATE users SET bestId = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, bestId);
        statement.setInt(2, userId);
        statement.executeUpdate();
    }

    public boolean isBestId(int userId, long bestId) throws SQLException {
        String query = "SELECT bestId FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, userId);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getLong("bestId") == bestId;
        }
        return false;
    }
}
