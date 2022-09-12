package org.sobadfish.warbridge.room.floattext;

import cn.nukkit.level.Position;
import org.sobadfish.warbridge.entity.BedWarFloatText;
import org.sobadfish.warbridge.manager.FloatTextManager;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.config.WorldInfoConfig;


public class FloatTextInfo {

    public FloatTextInfoConfig floatTextInfoConfig;

    public BedWarFloatText bedWarFloatText;

    public FloatTextInfo(FloatTextInfoConfig config){
        this.floatTextInfoConfig = config;
    }

    public FloatTextInfo init(GameRoom room){
        try{
            Position position = WorldInfoConfig.getPositionByString(floatTextInfoConfig.position);
            bedWarFloatText = BedWarFloatText.showFloatText(floatTextInfoConfig.name,position,"");
            if(bedWarFloatText != null){
                bedWarFloatText.room = room;
            }

        }catch (Exception e){
            return null;
        }

        return this;
    }

    public boolean stringUpdate(GameRoom room){
        String text = floatTextInfoConfig.text;
        if(room == null){
            return false;
        }
        if(room.getWorldInfo() == null){
            return false;
        }

        if(bedWarFloatText != null){
            if(bedWarFloatText.isClosed()){
                FloatTextManager.removeFloatText(bedWarFloatText);
                init(room);
            }
            bedWarFloatText.setText(text);
        }
        return true;
    }
}
