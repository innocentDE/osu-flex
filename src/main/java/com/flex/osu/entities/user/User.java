package com.flex.osu.entities.user;

import lombok.Getter;

import java.util.Date;
import java.util.List;

public class User {

    public String avatar_url;
    public String country_code;
    public int id;
    public boolean is_active = false;
    public boolean is_bot = false;
    public boolean is_deleted = false;
    @Getter
    public boolean is_online = false;
    public boolean is_supporter = false;
    public Date last_visit;
    public String username;
    public String cover_url;
    public boolean has_supported = false;
    public Date join_date;
    public String playmode;
    public List<String> playstyle;
    public Statistics statistics;
    public RankHistory rank_history;
}
