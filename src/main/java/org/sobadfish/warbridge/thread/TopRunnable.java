package org.sobadfish.warbridge.thread;


import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.manager.ThreadManager;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.top.TopItemInfo;

public class TopRunnable extends ThreadManager.AbstractBedWarRunnable {

    @Override
    public GameRoom getRoom() {
        return null;
    }

    public long time = 0;

    @Override
    public String getThreadName() {
        String color = "&a";
        if(isClose){
            color = "&7";
        }
        return color+"排行榜更新 &7("+ WarBridgeMain.getTopManager().topItemInfos.size()+") &a"+time+" ms";
    }

    @Override
    public void run() {
        try {
            long t1 = System.currentTimeMillis();
            if (isClose) {
                ThreadManager.cancel(this);
            }

            if (WarBridgeMain.getWarBridgeMain().isDisabled()) {
                isClose = true;
                return;
            }
            for (TopItemInfo topItem : WarBridgeMain.getTopManager().topItemInfos) {
                if (!WarBridgeMain.getTopManager().dataList.contains(topItem.topItem)) {
                    topItem.floatText.toClose();
                    WarBridgeMain.getTopManager().topItemInfos.remove(topItem);
                    continue;
                }
                if (topItem.floatText != null) {
                    if (topItem.floatText.player == null) {
                        continue;
                    }
                    topItem.floatText.setText(topItem.topItem.getListText());
                } else {
                    WarBridgeMain.getTopManager().topItemInfos.remove(topItem);
                }
            }
            time = System.currentTimeMillis() - t1;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
