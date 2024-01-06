package com.flex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flex.data.FlexData;
import com.flex.database.MySqlController;
import com.flex.discord.Bot;
import com.flex.osu.Flex;
import com.flex.osu.api.requests.AccessTokenProvider;

import java.net.http.HttpClient;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws SQLException, JsonProcessingException {

        MySqlController database = new MySqlController();
        database.connect(
                System.getenv("MYSQL_DB_URL"),
                System.getenv("MYSQL_DB_USERNAME"),
                System.getenv("MYSQL_DB_PASSWORD")
        );
        AccessTokenProvider provider = new AccessTokenProvider(
                System.getenv("OSU_CLIENT_ID"),
                System.getenv("OSU_CLIENT_SECRET"),
                database.getConnection(),
                HttpClient.newHttpClient()
        );
        provider.obtainAccessToken();

        Bot bot = new Bot(System.getenv("DISCORD_TOKEN"), database.getConnection());
        Flex flex = new Flex(bot.getApi(), database.getConnection());
        while(true){
            flex.start();
            try {
                Thread.sleep(FlexData.SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}