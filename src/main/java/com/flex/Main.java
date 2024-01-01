package com.flex;

import com.fasterxml.jackson.core.JsonProcessingException;
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
                System.getenv("FLEX_DB_URL"),
                System.getenv("FLEX_DB_USERNAME"),
                System.getenv("FLEX_DB_PASSWORD")
        );
        AccessTokenProvider provider = new AccessTokenProvider(
                System.getenv("FLEX_CLIENT_ID"),
                System.getenv("FLEX_CLIENT_SECRET"),
                database.getConnection(),
                HttpClient.newHttpClient()
        );
        provider.obtainAccessToken();

        Bot bot = new Bot(System.getenv("FLEX_DISCORD_TOKEN"), database.getConnection());
        Flex flex = new Flex(bot.getApi(), database.getConnection());
        while(true){
            flex.start();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}