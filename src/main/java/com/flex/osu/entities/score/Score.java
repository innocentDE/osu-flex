package com.flex.osu.entities.score;

import java.util.Date;
import java.util.List;

public class Score {

    public double accuracy;
    public long best_id;
    public Date created_at;
    public long id;
    public int max_combo;
    public String mode;
    public int mode_int;
    public List<String> mods;
    public boolean passed;
    public boolean perfect;
    public double pp;
    public String rank;
    public boolean replay;
    public int score;
    public Statistics statistics;
    public String type;
    public int user_id;
    public Beatmap beatmap;
    public Beatmapset beatmapset;
    public User user;
}
