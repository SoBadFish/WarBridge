package org.sobadfish.warbridge.room.floattext;

import cn.nukkit.level.Position;
import org.sobadfish.warbridge.entity.GameFloatText;
import org.sobadfish.warbridge.manager.FloatTextManager;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.config.WorldInfoConfig;


public class FloatTextInfo {

    public FloatTextInfoConfig floatTextInfoConfig;

    public GameFloatText gameFloatText;

    public FloatTextInfo(FloatTextInfoConfig config){
        this.floatTextInfoConfig = config;
    }

    public FloatTextInfo init(GameRoom room){
        try{
            Position position = WorldInfoConfig.getPositionByString(floatTextInfoConfig.position);
            gameFloatText = GameFloatText.showFloatText(floatTextInfoConfig.name,position,"");
            if(gameFloatText != null){
                gameFloatText.room = room;
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

        if(gameFloatText != null){
            if(gameFloatText.isClosed()){
                FloatTextManager.removeFloatText(gameFloatText);
                init(room);
            }
            gameFloatText.setText(text);
        }
        return true;
    }
}
