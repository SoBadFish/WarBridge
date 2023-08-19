package org.sobadfish.warbridge.thread;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.scheduler.PluginTask;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.player.PlayerInfo;
import org.sobadfish.warbridge.player.team.TeamInfo;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.tools.Utils;

/**
 * @author Sobadfish
 * 22:22
 */
public class PlayerLocationRunnable extends PluginTask<WarBridgeMain> {
    public PlayerLocationRunnable(WarBridgeMain warBridgeMain) {
        super(warBridgeMain);
    }

    @Override
    public void onRun(int i) {
        for(Player player: Server.getInstance().getOnlinePlayers().values()){
            if(player != null && player.isOnline()){
                PlayerInfo info = WarBridgeMain.getRoomManager().getPlayerInfo(player);
                if(info != null && info.getTeamInfo() != null){
                    TeamInfo teamInfo = info.getTeamInfo();
                    GameRoom room = info.getGameRoom();
                    if(room != null && room.getType() == GameRoom.GameType.START){
                        if(info.isWatch()){
                            return;
                        }
                        Position position;
                        for(TeamInfo teamInfo1: room.getTeamInfos()){
                            position = teamInfo1.getTeamConfig().getScorePosition();
                            if(Utils.inArea(room,player,position,true) && room.gameStart == 0){
                                if(teamInfo1.equals(teamInfo)){
                                    info.spawn();
                                }else{
                                    room.addScore(info);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
