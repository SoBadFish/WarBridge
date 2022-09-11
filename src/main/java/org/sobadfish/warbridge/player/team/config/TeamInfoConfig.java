package org.sobadfish.warbridge.player.team.config;

import cn.nukkit.utils.BlockColor;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author SoBadFish
 * 2022/1/3
 */
@Data
public class TeamInfoConfig {

    private TeamConfig teamConfig;



    /**
     * 出生坐标
     * */
    private String spawnPosition;



    public TeamInfoConfig(TeamConfig teamConfig, String spawnPosition){
        this.teamConfig = teamConfig;
        this.spawnPosition = spawnPosition;

    }


    public static TeamInfoConfig getInfoByMap(TeamConfig teamConfig, Map<?,?> map){
//        Position bedPosition = WorldInfoConfig.getPositionByString();
//        Position spawnPosition = WorldInfoConfig.getPositionByString(.toString());

        return new TeamInfoConfig(teamConfig,map.get("position").toString());
    }



    public String getName(){
        return teamConfig.getName();
    }

    public String getNameColor(){
        return teamConfig.getNameColor();
    }

    public BlockColor getRgb(){
        return teamConfig.getRgb();
    }

    public LinkedHashMap<String, Object> save(){
        LinkedHashMap<String, Object> config = new LinkedHashMap<>();
        config.put("name",teamConfig.getName());
        config.put("position",spawnPosition);
        return config;
    }
}
