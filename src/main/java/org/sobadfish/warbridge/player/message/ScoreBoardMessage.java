package org.sobadfish.warbridge.player.message;

/**
 * @author SoBadFish
 * 2022/1/3
 */

public class ScoreBoardMessage {

    private final String title;


    public ScoreBoardMessage(String title){
        this.title = title;
    }


    public String getTitle() {
        return title;
    }


}
