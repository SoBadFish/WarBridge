package org.sobadfish.warbridge.room.config;

import org.sobadfish.warbridge.player.team.config.TeamConfig;
import org.sobadfish.warbridge.player.team.config.TeamInfoConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * @author Sobadfish
 * @date 2022/9/9
 */
public class GameRoomConfig {

    public String name;

    /**
     * 队伍数据信息
     * */
    public LinkedHashMap<String, TeamConfig> teamCfg = new LinkedHashMap<>();

    /**
     * 队伍
     * */
    public ArrayList<TeamInfoConfig> teamConfigs;

    /**
     * 是否允许旁观
     * */
    public boolean hasWatch = true;


    /**
     * 等待大厅拉回坐标
     * */
    public int callbackY = 17;




    /**
     * 地图配置
     * */
    public WorldInfoConfig worldInfo;

    public static GameRoomConfig getGameRoomConfigByFile(String roomName, File nameFile) {
        //TODO 构建房间配置逻辑

       return null;

    }
}
