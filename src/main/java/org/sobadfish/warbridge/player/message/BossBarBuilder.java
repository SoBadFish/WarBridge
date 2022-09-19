package org.sobadfish.warbridge.player.message;

import cn.nukkit.Player;
import cn.nukkit.utils.BossBarColor;
import cn.nukkit.utils.DummyBossBar;
import cn.nukkit.utils.DummyBossBar.Builder;
import org.sobadfish.warbridge.player.PlayerInfo;
import org.sobadfish.warbridge.room.GameRoom;



/**
 * @author Sobadfish
 * @date 2022/9/17
 */
public class BossBarBuilder extends Builder{

    public BossBarBuilder(Player player) {
        super(player);
    }
    public long bossId;


    public static void createBossBar(PlayerInfo playerInfo, GameRoom gameRoom) {
        if (!gameRoom.getScoreboards().containsKey(playerInfo)) {
            if(playerInfo.player instanceof Player){
                BossBarBuilder bossBar = new BossBarBuilder((Player) playerInfo.player);
                bossBar.length(0.0F);
                bossBar.text("load");
                bossBar.bossId = ((Player)playerInfo.player).createBossBar(gameRoom.getScoreboards().get(playerInfo).build());
            }

        }

    }

    public static void removeBossBar(PlayerInfo playerInfo, GameRoom gameRoom) {
        if(playerInfo.player instanceof Player) {
            if (gameRoom.getScoreboards().containsKey(playerInfo) && ((Player)playerInfo.player).getDummyBossBar(gameRoom.getScoreboards().get(playerInfo).bossId) != null) {
                ((Player)playerInfo.player).removeBossBar(gameRoom.getScoreboards().get(playerInfo).bossId);
            }
        }

    }

    public static void displayBoss(PlayerInfo playerInfo, GameRoom room, String text, int time) {
        if (room.getScoreboards().containsKey(playerInfo) && room.getScoreboards().get(playerInfo) != null) {
            if(playerInfo.player instanceof Player){
                Player player = (Player) playerInfo.player;
                if (player.getDummyBossBar(room.getScoreboards().get(playerInfo).bossId) == null) {
                    room.getScoreboards().remove(playerInfo);
                    return;
                }

                DummyBossBar bossBar = player.getDummyBossBar(room.getScoreboards().get(playerInfo).bossId);
                bossBar.setText(text);

                try {
                    Class.forName("cn.nukkit.utils.BossBarColor");
                    bossBar.setColor(BossBarColor.PINK);
                } catch (Exception ignore) {}
                player.createBossBar(bossBar);
            }

        }

    }
}
