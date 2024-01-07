package com.flex.osu.entities;

import com.flex.osu.entities.score.Score;
import com.flex.osu.entities.user.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OsuData {

    private User user;
    private Score score;

    private int scoreIndex;
    private boolean isBest;

    public OsuData(){
        user = new User();
        score = new Score();
        scoreIndex = 0;
        isBest = false;
    }

}
