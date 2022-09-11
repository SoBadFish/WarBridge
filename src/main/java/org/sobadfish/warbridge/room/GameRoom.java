package org.sobadfish.warbridge.room;

import org.sobadfish.warbridge.player.PlayerInfo;
import org.sobadfish.warbridge.room.config.GameRoomConfig;
import org.sobadfish.warbridge.room.world.WorldInfo;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 战桥房间信息
 * @author Sobadfish
 * @date 2022/9/9
 */
public class GameRoom {

    public GameRoomConfig roomConfig;

    private final CopyOnWriteArrayList<PlayerInfo> playerInfos = new CopyOnWriteArrayList<>();

    private boolean hasStart;

    public int loadTime = -1;

    private GameType type;


    /**
     * 地图配置
     * */
    public WorldInfo worldInfo;

    public boolean close;

    private GameRoom(GameRoomConfig roomConfig){
        this.roomConfig = roomConfig;
        this.worldInfo = new WorldInfo(this,roomConfig.worldInfo);

        type = GameType.WAIT;

        //启动事件

    }

    public GameRoomConfig getRoomConfig() {
        return roomConfig;
    }

    public enum GameType{
        /**
         * WAIT: 等待 START: 开始 END: 结束 CLOSE: 关闭
         * */
        WAIT,START,END,CLOSE
    }


}
