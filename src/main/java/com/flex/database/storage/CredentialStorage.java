package com.flex.database.storage;

import com.flex.data.FlexData;

import java.sql.*;

public class CredentialStorage extends MySqlStorage {

    public CredentialStorage(Connection connection) {
        super.connection = connection;
    }

    public void insertAccessToken(String accessToken) throws SQLException {
        String query = "INSERT INTO credentials (createdAt, accessToken) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        statement.setString(2, accessToken);
        statement.executeUpdate();
    }

    public boolean hasAccessToken() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM credentials";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("count") > 0;
        } else {
            return false;
        }
    }

    public boolean isAccessTokenExpired() throws SQLException {
        String query = "SELECT MAX(createdAt) AS createdAt FROM credentials";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Timestamp createdAt = resultSet.getTimestamp("createdAt");
            return System.currentTimeMillis() - createdAt.getTime() > FlexData.ACCESS_TOKEN_EXPIRY;
        } else {
            return true;
        }
    }

    public String getAccessToken() throws SQLException {
        String query = "SELECT accessToken FROM credentials ORDER BY createdAt DESC LIMIT 1";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString("accessToken");
        } else {
            throw new RuntimeException("No access token found");
        }
    }
}
