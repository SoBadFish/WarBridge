package org.sobadfish.warbridge.variable;

import cn.nukkit.Player;
import com.smallaswater.npc.data.RsNpcConfig;
import com.smallaswater.npc.variable.BaseVariableV2;
import com.smallaswater.npc.variable.VariableManage;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.config.GameRoomConfig;


public class WarBridgeVariable extends BaseVariableV2 {


    public static void init() {
        VariableManage.addVariableV2("bedwar", WarBridgeVariable.class);
    }

    @Override
    public void onUpdate(Player player, RsNpcConfig rsNpcConfig) {
       initVariable();
    }


    private void initVariable(){
        for(GameRoomConfig roomConfig: WarBridgeMain.getRoomManager().getRoomConfigs()){
            GameRoom room = WarBridgeMain.getRoomManager().getRoom(roomConfig.name);
            int p = 0;
            int mp = roomConfig.getMaxPlayerSize();
            String status = "&a等待中";
            if(room != null){
                p = room.getPlayerInfos().size();
                switch (room.getType()){
                    case START:
                        status = "&c游戏中";
                        break;
                    case END:
                    case CLOSE:
                        status = "&e结算中";
                        break;
                    default:break;
                }
            }
            addVariable("%"+roomConfig.getName()+"-player%",p+"");
            addVariable("%"+roomConfig.getName()+"-maxplayer%",mp+"");
            addVariable("%"+roomConfig.getName()+"-status%",status);
        }
    }
}
