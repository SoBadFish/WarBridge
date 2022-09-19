package org.sobadfish.warbridge.player.message;

import java.util.ArrayList;

/**
 * @author SoBadFish
 * 2022/1/3
 */

public class ScoreBoardMessage {

    private final String title;

    private ArrayList<String> lore = new ArrayList<>();


    public ScoreBoardMessage(String title){
        this.title = title;
    }

    public void setLore(ArrayList<String> lore) {
        this.lore = lore;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getLore() {
        return lore;
    }
}
