package org.sobadfish.warbridge.player.team.config;

import cn.nukkit.level.Position;
import cn.nukkit.utils.BlockColor;
import lombok.Data;
import org.sobadfish.warbridge.room.config.WorldInfoConfig;

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

    /**
     * 得分点
     * */
    private String scorePosition;



    public TeamInfoConfig(TeamConfig teamConfig, String spawnPosition,String scorePosition){
        this.teamConfig = teamConfig;
        this.spawnPosition = spawnPosition;
        this.scorePosition = scorePosition;

    }




    public static TeamInfoConfig getInfoByMap(TeamConfig teamConfig, Map<?,?> map){

        return new TeamInfoConfig(teamConfig,map.get("position").toString(),map.get("score").toString());
    }

    public Position getScorePosition() {
        return WorldInfoConfig.getPositionByString(scorePosition);
    }

    public Position getSpawnPosition() {
        return WorldInfoConfig.getPositionByString(spawnPosition);
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
        config.put("score",scorePosition);
        return config;
    }
}
