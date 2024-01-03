package com.flex.database.storage;

import org.apache.logging.log4j.LogManager;

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
            return System.currentTimeMillis() - createdAt.getTime() > 24 * 60 * 60 * 1000;
        } else {
            return true;
        }
    }

    public int getExpiry() throws SQLException {
        String query = "SELECT MAX(createdAt) AS createdAt FROM credentials";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Timestamp createdAt = resultSet.getTimestamp("createdAt");
            long differenceInMinutes = (System.currentTimeMillis() - createdAt.getTime()) / (1000 * 60);
            long remainingMinutes = 24 * 60 - differenceInMinutes;
            return remainingMinutes > 0 ? (int) remainingMinutes : 0;
        } else {
            throw new RuntimeException("No access token found");
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
