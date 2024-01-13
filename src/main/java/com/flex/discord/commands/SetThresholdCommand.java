package com.flex.discord.commands;

import com.flex.data.FlexData;
import com.flex.database.storage.UserServersStorage;
import com.flex.database.storage.UserStorage;
import com.flex.osu.api.requests.FlexRequests;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.sql.SQLException;
import java.util.Optional;

public class SetThresholdCommand extends SlashCommand{

    private final UserServersStorage userServersStorage;
    private final UserStorage userStorage;

    public SetThresholdCommand(JDA api, FlexRequests requests) {
        super(
                api,
                requests,
                "threshold",
                "Set the threshold for osu!flex for triggering a new top play notification"
        );
        userServersStorage = new UserServersStorage(requests.getConnection());
        userStorage = new UserStorage(requests.getConnection());
    }

    @Override
    protected void createCommand() {
        super.command = Commands.slash(name, description)
                .addOption(OptionType.INTEGER, "threshold", "The threshold to set [min=1, max=100, default=20]", true)
                .addOption(OptionType.STRING, "user", "The user to set the threshold for", false);
    }

    public void execute(SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            event.deferReply().setEphemeral(true).queue();

            String username;
            int userId;
            int threshold;

            if(isUserOptionPresent(event)){
                try {
                    username = getUsernameFromOption(event);
                    userId = getUserId(username);
                    threshold = getThresholdFromOption(event);
                    setUserThreshold(event, userId, threshold, username);
                } catch (IllegalArgumentException e) {
                    super.sendMessage(event, e.getMessage());
                } catch (SQLException e) {
                    super.sendMessage(event, "Something went wrong");
                }
            } else {
                try {
                    threshold = getThresholdFromOption(event);
                    setGlobalThreshold(event, threshold);
                } catch (IllegalArgumentException e) {
                    super.sendMessage(event, e.getMessage());
                } catch (SQLException e) {
                    super.sendMessage(event, "Something went wrong");
                }
            }
        }
    }

    private int getThresholdFromOption(SlashCommandInteractionEvent event) throws IllegalArgumentException {
        int threshold = event.getOption("threshold").getAsInt();
        if (threshold < 1 || threshold > 100) {
            throw new IllegalArgumentException("Threshold must be between 1 and 100");
        }
        return threshold;
    }

    private String getUsernameFromOption(SlashCommandInteractionEvent event) throws IllegalArgumentException {
        String username = event.getOption("user").getAsString();
        if (username.length() > FlexData.MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException("Invalid username! Must be less than 20 characters.");
        }
        return username;
    }

    private boolean isUserOptionPresent(SlashCommandInteractionEvent event) {
        return event.getOption("user") != null;
    }

    private int getUserId(String user) throws SQLException, IllegalArgumentException {
        Optional<Integer> userId = userStorage.getUserId(user);
        if (userId.isEmpty()) {
            throw new IllegalArgumentException(String.format("User %s is not registered on osu!flex", user));
        }
        return userId.get();
    }

    private void setGlobalThreshold(SlashCommandInteractionEvent event, int threshold) throws SQLException {
        userServersStorage.setThreshold(event.getGuild().getIdLong(), threshold);
        sendMessage(event, String.format("Threshold set to %d for all users", threshold));
    }

    private void setUserThreshold(SlashCommandInteractionEvent event, int userId, int threshold, String username) throws SQLException {
        userServersStorage.setThreshold(userId, event.getGuild().getIdLong(), threshold);
        sendMessage(event, String.format("Threshold set to %d for %s", threshold, username));
    }
}
