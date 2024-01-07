package com.flex.database.storage;

import net.dv8tion.jda.api.entities.Guild;
import org.apache.logging.log4j.LogManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServerStorage extends MySqlStorage {


    public ServerStorage(Connection connection){
        super.connection = connection;
        logger = LogManager.getLogger(UserStorage.class);
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

    public void registerServers(List<Guild> guilds) throws SQLException {
        String query = "INSERT INTO servers (serverId, channelId) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(query);
        for(Guild guild : guilds){
            statement.setLong(1, guild.getIdLong());
            statement.setLong(2, guild.getDefaultChannel().getIdLong());
            statement.addBatch();
        }
        statement.executeBatch();
    }

    public void unregisterServer(long serverId) throws SQLException {
        String query = "DELETE FROM servers WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, serverId);
        statement.executeUpdate();
    }

    public void unregisterServers(List<Long> serverIds) throws SQLException {
        String query = "DELETE FROM servers WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        for(long serverId : serverIds){
            statement.setLong(1, serverId);
            statement.addBatch();
        }
        statement.executeBatch();
    }

    public void setChannel(long serverId, long channelId) throws SQLException {
        String query = "UPDATE servers SET channelId = ? WHERE serverId = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, channelId);
        statement.setLong(2, serverId);
        statement.executeUpdate();
    }

    public List<Long> getServerIds() throws SQLException {
        String query = "SELECT serverId FROM servers";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();
        List<Long> serverIds = new ArrayList<>();
        while(resultSet.next()){
            serverIds.add(resultSet.getLong("serverId"));
        }
        return serverIds;
    }
}
