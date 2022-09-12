package org.sobadfish.warbridge.room.world;

import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.config.WorldInfoConfig;

/**
 * @author Sobadfish
 * @date 2022/9/9
 */
public class WorldInfo {

    private GameRoom room;


    private boolean isClose;

    public boolean isStart;

    private WorldInfoConfig config;

    public WorldInfo(GameRoom room,WorldInfoConfig config){
        this.config = config;
        this.room = room;

    }

    public WorldInfoConfig getConfig() {
        return config;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public void onUpdate() {
        //TODO 地图更新
    }
}
