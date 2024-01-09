package com.flex.discord.commands;

import lombok.Getter;

@Getter
public enum Commands {

    ADD("add"),
    REMOVE("remove"),
    SET("set"),
    THRESHOLD("threshold"),
    HELP("help");

    private String name;

    Commands(String name) {
        this.name = name;
    }

    public static Commands getCommand(String name){
        for(Commands command : Commands.values()){
            if(command.getName().equals(name)){
                return command;
            }
        }
        return null;
    }
}
