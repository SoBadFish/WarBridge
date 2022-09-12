package org.sobadfish.warbridge.thread;

import cn.nukkit.scheduler.PluginTask;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.floattext.FloatTextInfo;
import org.sobadfish.warbridge.room.world.WorldInfo;

public class WorldInfoMasterThread extends PluginTask<WarBridgeMain> {

    private final WorldInfo worldInfo;
    private final GameRoom room;

    public WorldInfoMasterThread(GameRoom room,WorldInfo worldInfo,WarBridgeMain bedWarMain) {
        super(bedWarMain);
        this.worldInfo = worldInfo;
        this.room = room;
    }

    @Override
    public void onRun(int i) {
        if (worldInfo != null){
            worldInfo.onUpdate();
            for (FloatTextInfo floatTextInfo : room.getFloatTextInfos()) {
                if (!floatTextInfo.stringUpdate(room)) {

                    break;
                }
            }
        }
    }
}
