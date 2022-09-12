package org.sobadfish.warbridge.event;

import cn.nukkit.plugin.Plugin;
import org.sobadfish.warbridge.player.team.TeamInfo;
import org.sobadfish.warbridge.room.GameRoom;


/**
 * @author SoBadFish
 * 2022/1/15
 */
public class TeamVictoryEvent extends GameRoomEvent{

    private final TeamInfo teamInfo;

    public TeamVictoryEvent(TeamInfo teamInfo, GameRoom room, Plugin plugin) {
        super(room, plugin);
        this.teamInfo = teamInfo;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }
}
