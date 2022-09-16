package org.sobadfish.warbridge.variable;

import cn.nukkit.Player;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.player.PlayerData;
import tip.utils.Api;
import tip.utils.variables.BaseVariable;

public class TipVariable extends BaseVariable {

    public TipVariable(Player player) {
        super(player);
    }

    public static void init() {
        Api.registerVariables("Bedwar",TipVariable.class);
    }

    @Override
    public void strReplace() {
        //等级
        PlayerData data = WarBridgeMain.getDataManager().getData(player.getName());
        addStrReplaceString("%wb-level%",data.getLevelString());
        addStrReplaceString("%wb-exp%",data.getExpString(data.getExp())+"");
        addStrReplaceString("%wb-nextExp%",data.getExpString(data.getNextLevelExp())+"");
        addStrReplaceString("%wb-line%",data.getExpLine(10)+"");
        addStrReplaceString("%wb-per%",String.format("%.2f",data.getExpPercent() * 100)+"");
        addStrReplaceString("%wb-kill%",data.getFinalData(PlayerData.DataType.KILL)+"");
        addStrReplaceString("%wb-victory%",data.getFinalData(PlayerData.DataType.VICTORY)+"");
        addStrReplaceString("%wb-score%",data.getFinalData(PlayerData.DataType.SCORE)+"");

    }
}
