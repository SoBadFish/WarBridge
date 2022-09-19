package org.sobadfish.warbridge.room;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import de.theamychan.scoreboard.network.Scoreboard;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.event.GameCloseEvent;
import org.sobadfish.warbridge.event.GameRoomStartEvent;
import org.sobadfish.warbridge.event.PlayerJoinRoomEvent;
import org.sobadfish.warbridge.event.PlayerQuitRoomEvent;
import org.sobadfish.warbridge.item.button.FollowItem;
import org.sobadfish.warbridge.item.button.RoomQuitItem;
import org.sobadfish.warbridge.item.button.TeamChoseItem;
import org.sobadfish.warbridge.manager.RandomJoinManager;
import org.sobadfish.warbridge.manager.RoomManager;
import org.sobadfish.warbridge.manager.WorldResetManager;
import org.sobadfish.warbridge.player.PlayerInfo;
import org.sobadfish.warbridge.player.team.TeamInfo;
import org.sobadfish.warbridge.player.team.config.TeamInfoConfig;
import org.sobadfish.warbridge.room.config.GameRoomConfig;
import org.sobadfish.warbridge.room.floattext.FloatTextInfo;
import org.sobadfish.warbridge.room.floattext.FloatTextInfoConfig;
import org.sobadfish.warbridge.room.world.WorldInfo;
import org.sobadfish.warbridge.tools.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 战桥房间信息
 * @author Sobadfish
 * @date 2022/9/9
 */
public class GameRoom {

    public GameRoomConfig roomConfig;

    private boolean isInit = true;

    private boolean isMax;

    public String cause = " ";

    private boolean teamAll;

    public boolean isTeleport = true;

    private final ArrayList<FloatTextInfo> floatTextInfos = new ArrayList<>();

    private List<Item> canBreak = new ArrayList<>();

    //房间内的玩家
    private final CopyOnWriteArrayList<PlayerInfo> playerInfos = new CopyOnWriteArrayList<>();

    private final LinkedHashMap<PlayerInfo, Scoreboard> scoreboards = new LinkedHashMap<>();

    private boolean hasStart;

    public int loadTime = -1;

    public int gameStart = 0;

    public boolean resetStart = false;

    private GameType type;

    private final ArrayList<TeamInfo> teamInfos = new ArrayList<>();


    /**
     * 地图配置
     * */
    public WorldInfo worldInfo;

    public boolean close;

    private GameRoom(GameRoomConfig roomConfig){
        this.roomConfig = roomConfig;
        this.worldInfo = new WorldInfo(this,roomConfig.worldInfo);

        type = GameType.WAIT;
        for(TeamInfoConfig config: getRoomConfig().getTeamConfigs()){
            teamInfos.add(new TeamInfo(this,config));
        }

        //启动事件

    }

    public ArrayList<FloatTextInfo> getFloatTextInfos() {
        return floatTextInfos;
    }

    //特供版为BOSS血条
    public LinkedHashMap<PlayerInfo, Scoreboard> getScoreboards() {
        return scoreboards;
    }

    public CopyOnWriteArrayList<PlayerInfo> getPlayerInfos() {
        playerInfos.removeIf((p)->p.disable);
        return playerInfos;
    }

    public GameRoomConfig getRoomConfig() {
        return roomConfig;
    }


    public GameType getType() {
        return type;
    }

    public enum GameType{
        /**
         * WAIT: 等待 START: 开始 END: 结束 CLOSE: 关闭
         * */
        WAIT,START,END,CLOSE
    }

    public PlayerInfo getPlayerInfo(EntityHuman player){
        if(playerInfos.contains(new PlayerInfo(player))){
            return playerInfos.get(playerInfos.indexOf(new PlayerInfo(player)));
        }
        return null;
    }

    public void sendMessageOnWatch(String msg) {
        ArrayList<PlayerInfo> watchPlayer = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.isWatch()){
                watchPlayer.add(info);
            }
        }
        watchPlayer.forEach(dp -> dp.sendMessage(msg));
    }

    public void joinWatch(PlayerInfo info) {
        //TODO 欢迎加入观察者大家庭
        if(!playerInfos.contains(info)){


            info.init();
            info.setGameRoom(this);
            if(info.getPlayer() instanceof Player) {
                WarBridgeMain.getRoomManager().playerJoin.put(info.getPlayer().getName(),getRoomConfig().name);
            }
            playerInfos.add(info);

        }
        if(info.getPlayer() instanceof Player) {
            ((Player)info.getPlayer()).setGamemode(3);
        }

        info.setPlayerType(PlayerInfo.PlayerType.WATCH);
        info.getPlayer().getInventory().setItem(RoomQuitItem.getIndex(),RoomQuitItem.get());
        info.getPlayer().getInventory().setItem(FollowItem.getIndex(), FollowItem.get());
        info.getPlayer().getInventory().setHeldItemSlot(0);
        sendMessage("&7"+info+"&7 成为了旁观者 （"+getWatchPlayers().size()+"）");
        info.sendMessage("&e你可以等待游戏结束 也可以手动退出游戏房间");
        Position position = getTeamInfos().get(0).getTeamConfig().getSpawnPosition();
        position.add(0,64,0);
        position.level = getWorldInfo().getConfig().getGameWorld();
        info.getPlayer().teleport(position);

    }

    public static GameRoom enableRoom(GameRoomConfig roomConfig){

        if(roomConfig.getWorldInfo().getGameWorld() == null){
            return null;
        }
        if(WorldResetManager.RESET_QUEUE.containsKey(roomConfig)){
            return null;
        }
        return new GameRoom(roomConfig);
    }

    public JoinType joinPlayerInfo(PlayerInfo info,boolean sendMessage){
        if(WorldResetManager.RESET_QUEUE.containsKey(roomConfig)){
            return JoinType.NO_JOIN;
        }
        if(info.getPlayer() instanceof Player) {
            if(!((Player) info.getPlayer()).isOnline()){
                return JoinType.NO_ONLINE;
            }
        }

        if(getType() != GameType.WAIT){
            if(getType() == GameType.END || getType() == GameType.CLOSE){
                return JoinType.NO_JOIN;
            }
            return JoinType.CAN_WATCH;
        }
        if(getWorldInfo().getConfig().getGameWorld() == null || getWorldInfo().getConfig().getGameWorld().getSafeSpawn() == null){
            return JoinType.NO_LEVEL;
        }

        PlayerJoinRoomEvent event = new PlayerJoinRoomEvent(info,this,WarBridgeMain.getWarBridgeMain());
        event.setSend(sendMessage);
        Server.getInstance().getPluginManager().callEvent(event);
        if(event.isCancelled()){
            return JoinType.NO_JOIN;
        }

        sendMessage(info+"&e加入了游戏 &7("+(playerInfos.size()+1)+"/"+getRoomConfig().getMaxPlayerSize()+")");
        info.init();
        info.getPlayer().getInventory().setItem(TeamChoseItem.getIndex(),TeamChoseItem.get());
        info.getPlayer().getInventory().setItem(RoomQuitItem.getIndex(),RoomQuitItem.get());
        info.setPlayerType(PlayerInfo.PlayerType.WAIT);
        info.setGameRoom(this);
        if(info.getPlayer() instanceof Player) {
            WarBridgeMain.getRoomManager().playerJoin.put(info.getPlayer().getName(),getRoomConfig().name);
        }
        playerInfos.add(info);
        info.getPlayer().teleport(getWorldInfo().getConfig().getWaitPosition());
        if(info.getPlayer() instanceof Player) {
            ((Player)info.getPlayer()).setGamemode(2);
        }
        if(isInit){
            isInit = false;
        }


        return JoinType.CAN_JOIN;

    }

    public WorldInfo getWorldInfo() {
        return worldInfo;
    }

    public ArrayList<TeamInfo> getTeamInfos() {
        return teamInfos;
    }

    public void setType(GameType type) {
        this.type = type;
    }

    /**
     * 根据名称
     * */
    private TeamInfo getTeamInfo(String name){
        for(PlayerInfo info : playerInfos){
            if(info.getTeamInfo() != null &&
                    info.getTeamInfo().getTeamConfig().getName().equalsIgnoreCase(name)){
                return info.getTeamInfo();
            }
        }
        return null;
    }

    public enum JoinType{
        //加入类型
        NO_ONLINE,NO_JOIN,NO_LEVEL,CAN_WATCH,CAN_JOIN
    }

    /**
     * 分配玩家
     * */
    private boolean allotOfAverage(){

        int t =  (int) Math.ceil(playerInfos.size() / (double)getRoomConfig().getTeamConfigs().size());
        PlayerInfo listener;
        LinkedList<PlayerInfo> noTeam = getNoTeamPlayers();
        // TODO 检测是否一个队伍里有太多的人 拆掉多余的人
        for (TeamInfo manager: teamInfos){
            if(manager.getTeamPlayers().size() > t){
                int size = t - manager.getTeamPlayers().size();
                for(int i = 0;i < size;i++){
                    PlayerInfo info = manager.getTeamPlayers().remove(manager.getTeamPlayers().size()-1);
                    noTeam.add(info);
                }
            }
        }
        if(teamInfos.size() == 1){
            TeamInfo teamInfo = teamInfos.get(0);
            noTeam.addAll(teamInfo.getTeamPlayers());
        }
        while(noTeam.size() > 0){
            for (TeamInfo manager: teamInfos){
                if(manager.getTeamPlayers().size() == 0
                        || (manager.getTeamPlayers().size() < t )){
                    if(noTeam.size() > 0) {
                        listener = noTeam.poll();
                        manager.mjoin(listener);
                    }
                }else{
                    if(manager.getTeamPlayers().size() > t){
                        int size =  manager.getTeamPlayers().size();
                        LinkedList<PlayerInfo> playerInfos = new LinkedList<>(manager.getTeamPlayers());
                        for(int i = 0;i <size - t;i++) {
                            noTeam.add(playerInfos.pollLast());
                        }
                    }
                }
            }
        }
        return true;
    }


    public LinkedList<PlayerInfo> getNoTeamPlayers(){
        LinkedList<PlayerInfo> noTeam = new LinkedList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.getTeamInfo() == null){
                noTeam.add(playerInfo);
            }
        }
        return noTeam;
    }

    /**
     * 还在游戏内的玩家
     * */
    public ArrayList<PlayerInfo> getInRoomPlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isInRoom()){
                t.add(playerInfo);
            }
        }
        return t;
    }


    public ArrayList<TeamInfo> getLiveTeam(){
        ArrayList<TeamInfo> t = new ArrayList<>();
        for(TeamInfo teamInfo: teamInfos){
            if(teamInfo.isLoading()){
                t.add(teamInfo);
            }
        }
        return t;
    }


    public ArrayList<PlayerInfo> getIPlayerInfos() {
        ArrayList<PlayerInfo> p = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.getPlayer() instanceof Player){
                if(!info.isLeave()) {
                    p.add(info);
                }
            }
        }
        return p;
    }
    /**
     * 旁观者们
     * */
    public ArrayList<PlayerInfo> getWatchPlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isWatch()){
                t.add(playerInfo);
            }
        }
        return t;
    }

    /**
     * 还在游戏内的存活玩家
     * */
    public ArrayList<PlayerInfo> getLivePlayers(){
        ArrayList<PlayerInfo> t = new ArrayList<>();
        for(PlayerInfo playerInfo: playerInfos){
            if(playerInfo.isLive()){
                t.add(playerInfo);
            }
        }
        return t;
    }



    /**
     * 仅阵亡玩家观看
     * */
    public void sendMessageOnDeath(String msg){
        ArrayList<PlayerInfo> deathPlayer = new ArrayList<>();
        for(PlayerInfo info: playerInfos){
            if(info.isDeath()){
                deathPlayer.add(info);
            }
        }
        deathPlayer.forEach(dp -> dp.sendMessage(msg));
    }


    public void sendTipMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTipMessage(msg);
        }
    }

    public void sendMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendMessage(msg);
        }
    }

    public void sendFaceMessage(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendForceMessage(msg);
        }
    }
    public void sendTitle(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTitle(msg);
        }
    }
    public void sendSubTitle(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendSubTitle(msg);
        }
    }
    public void sendTip(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendTip(msg);
        }
    }

    public void sendActionBar(String msg){
        for(PlayerInfo info: getPlayerInfos()){
            info.sendActionBar(msg);
        }
    }

    public void addSound(Sound sound){
        for(PlayerInfo info: getPlayerInfos()){
            info.addSound(sound);
        }
    }

    /**
     * 全队BUFF
     * */
    public void addEffect(Effect effect){
        for(PlayerInfo info: getLivePlayers()){
            info.addEffect(effect);
        }
    }

    /**
     * 玩家离开游戏
     * */
    public boolean quitPlayerInfo(PlayerInfo info,boolean teleport){
        if(info != null) {

            if (info.getPlayer() instanceof Player) {
                if (playerInfos.contains(info)) {
                    PlayerQuitRoomEvent event = new PlayerQuitRoomEvent(info, this,WarBridgeMain.getWarBridgeMain());
                    Server.getInstance().getPluginManager().callEvent(event);
                    if(((Player) info.getPlayer()).isOnline()) {
                        if (teleport) {
                            if(Server.getInstance().getDefaultLevel() == null){
                                info.getPlayer().teleport(info.player.getLevel().getSafeSpawn());
                            }else {
                                info.getPlayer().teleport(Server.getInstance().getDefaultLevel().getSafeSpawn());
                            }
                        }
                        info.getPlayer().removeAllEffects();
                        ((Player) info.getPlayer()).setExperience(0, 0);
                    }
                    info.cancel();
                    WarBridgeMain.getRoomManager().playerJoin.remove(info.getPlayer().getName());
                } else {
                    WarBridgeMain.getRoomManager().playerJoin.remove(info.getPlayer().getName());

                }
            } else {
                info.getPlayer().close();
                playerInfos.remove(info);

            }
        }
        if (getIPlayerInfos().size() == 0) {
            onDisable();
        }
        return true;
    }

    /** 房间被实例化后 */
    public void onUpdate(){
        if(close){
            return;
        }
        //TODO 当房间启动后
        if(getIPlayerInfos().size() == 0 && !isInit){
            onDisable();
            return;
        }
        switch (type){
            case WAIT:
                onWait();
                break;
            case START:

                worldInfo.isStart = true;
                try {
                    onStart();
                }catch (Exception e){
                    e.printStackTrace();
                    for(PlayerInfo playerInfo: new ArrayList<>(playerInfos)){
                        playerInfo.sendForceMessage("房间出现异常 请联系服主/管理员修复");
                    }
                    onDisable();
                    return;
                }

                break;
            case END:
                //TODO 房间结束
                onEnd();
                break;
            case CLOSE:
                onDisable();
                break;
            default:break;
        }

        //移除编外人员
        for(PlayerInfo info: getInRoomPlayers()){
            if(!WarBridgeMain.getRoomManager().playerJoin.containsKey(info.getPlayer().getName())){
                playerInfos.remove(info);
            }
        }

    }

    private void onEnd() {
        if(loadTime == -1){
            loadTime = 10;
        }

        for(PlayerInfo playerInfo:getLivePlayers()){
            Utils.spawnFirework(playerInfo.getPosition());
        }

        if(loadTime == 0){
            type = GameType.CLOSE;

        }

    }

    public List<Item> getCanBreak() {
        return canBreak;
    }

    private void onStart() {
        hasStart = true;
        if(loadTime == -1 && teamAll){
            for(FloatTextInfoConfig config: roomConfig.floatTextInfoConfigs){
                FloatTextInfo info = new FloatTextInfo(config).init(this);
                if(info != null){
                    floatTextInfos.add(info);
                }
            }
            //TODO 当房间开始
            for(TeamInfo teamInfo: teamInfos){
                Item i = teamInfo.getTeamConfig().getTeamConfig().getBlockWoolColor();
                if(!canBreak.contains(i)){
                    canBreak.add(i);
                }
            }

            for(PlayerInfo i : getPlayerInfos()){
                try {
                    i.spawn();
                }catch (Exception e){
                    i.sendForceMessage("&c出现未知原因影响导致无法正常传送 正在重新将你移动中");
                    try {
                        i.spawn();
                    }catch (Exception e1){
                        i.sendForceMessage("&c移动失败 请尝试重新进入游戏");
                        quitPlayerInfo(i,true);
                    }
                }
            }
            isTeleport = false;
            sendTitle("&c游戏开始");
            gameStart = 5;
            resetStart = true;
            loadTime = getRoomConfig().time;
            worldInfo = new WorldInfo(this,getRoomConfig().worldInfo);
            GameRoomStartEvent event = new GameRoomStartEvent(this,WarBridgeMain.getWarBridgeMain());
            Server.getInstance().getPluginManager().callEvent(event);

        }
        if(gameStart > 0){
            gameStart--;
        }else if(resetStart){
            //移除产生的方块
            resetStart = false;
        }
        if(loadTime > 0) {

            for (TeamInfo teamInfo : teamInfos) {
                teamInfo.onUpdate();
                if(teamInfo.score == 5){
                    teamInfo.echoVictory();
                    type = GameType.END;
                    worldInfo.setClose(true);
                    loadTime = 5;
                }
            }
            if (getLiveTeam().size() == 1) {
                TeamInfo teamInfo = getLiveTeam().get(0);
                teamInfo.echoVictory();
                type = GameType.END;
                worldInfo.setClose(true);
                loadTime = 5;
            }

        }else{
            TeamInfo successInfo = null;
            ArrayList<TeamInfo> teamInfos = getLiveTeam();
            int score = 0;
            if(teamInfos.size() > 0) {
                for (TeamInfo info : teamInfos) {
                    if(info.score > score){
                        score = info.score;
                        successInfo = info;
                    }
                    info.onUpdate();
                    info.setStop(true);

                }
                if(successInfo == null){
                    successInfo = teamInfos.get(0);
                }

                successInfo.echoVictory();

            }
            //TODO 当时间结束的一些逻辑
            type = GameType.END;
            worldInfo.setClose(true);
            loadTime = -1;
        }
    }

    private void onWait() {
        if(getPlayerInfos().size() >= getRoomConfig().minPlayerSize){
            if(loadTime == -1){
                loadTime = getRoomConfig().waitTime;
                sendMessage("&2到达最低人数限制&e "+loadTime+" &2秒后开始游戏");

            }
        }else {
            loadTime = -1;
        }
        if(getPlayerInfos().size() == getRoomConfig().getMaxPlayerSize()){
            if(!isMax){
                isMax = true;
                loadTime = getRoomConfig().getMaxWaitTime();
            }
        }
        if(loadTime >= 1) {
            sendTip("&e距离开始还剩 &a " + loadTime + " &e秒");
            if(loadTime <= 5){
                switch (loadTime){
                    case 5: sendTitle("&a5");break;
                    case 4: sendTitle("&e4");break;
                    case 3: sendTitle("&63");break;
                    case 2: sendTitle("&42");break;
                    case 1: sendTitle("&41");break;
                    default:
                        sendTitle("");break;

                }
                //音效
                addSound(Sound.RANDOM_CLICK);

            }
            if(loadTime == 1){
                type = GameType.START;
                loadTime = -1;
                if(allotOfAverage()){
                    teamAll = true;
                }


            }
        }else{
            sendTip("&a等待中");
        }
    }

    /**
     * 关闭房间
     * */
    public void onDisable(){
        if(close){
            return;
        }
        close = true;
        type = GameType.CLOSE;
        if(hasStart) {
            roomConfig.save();
            GameCloseEvent event = new GameCloseEvent(this, WarBridgeMain.getWarBridgeMain());
            Server.getInstance().getPluginManager().callEvent(event);
            worldInfo.setClose(true);
            //房间结束后的执行逻辑
            if(getRoomConfig().isAutomaticNextRound){
                sendMessage("&7即将自动进行下一局");
                for(PlayerInfo playerInfo: getInRoomPlayers()){
                    RandomJoinManager.joinManager.nextJoin(playerInfo);
                }
            }
            //TODO 房间被关闭 释放一些资源
            for (PlayerInfo info : playerInfos) {
                info.clear();
                if (info.getPlayer() instanceof Player) {
                    quitPlayerInfo(info, true);
                }
            }

            //浮空字释放
            for(FloatTextInfo floatTextInfo: floatTextInfos){
                floatTextInfo.gameFloatText.toClose();
            }

            String level = worldInfo.getConfig().getLevel();
            Level level1 = getWorldInfo().getConfig().getGameWorld();
            for(Entity entity: new CopyOnWriteArrayList<>(level1.getEntities())){
                if(entity instanceof Player){
                    //这里出现的玩家就是没有清出地图的玩家
                    entity.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
                    WarBridgeMain.getRoomManager().playerJoin.remove(entity.getName());
                    ((Player) entity).setGamemode(0);
                    entity.removeAllEffects();
                    ((Player) entity).getInventory().clearAll();
                    ((Player) entity).getEnderChestInventory().clearAll();
                    ((Player) entity).getFoodData().reset();
                    continue;
                }
                if(entity != null && !entity.isClosed()){
                    entity.close();
                }

            }
            //卸载区块就炸...
//            level1.unloadChunks();
            worldInfo.setClose(true);
            worldInfo = null;
            WorldResetManager.RESET_QUEUE.put(getRoomConfig(),level);
        }else{
            worldInfo.setClose(true);
            worldInfo = null;
            WarBridgeMain.getRoomManager().getRooms().remove(getRoomConfig().name);
            RoomManager.LOCK_GAME.remove(getRoomConfig());
        }

    }


    private Long lastTime = 0L;
    //3秒内不加分
    public void addScore(PlayerInfo playerInfo){
        TeamInfo teamInfo = playerInfo.getTeamInfo();
        if(lastTime == 0){
            lastTime = System.currentTimeMillis();
        }
        if(System.currentTimeMillis() - lastTime < 2000){
            return;
        }
        for(TeamInfo t: getTeamInfos()){
            if(t.hasScore){
                return;
            }
        }
        if(teamInfo != null){
            teamInfo.hasScore = true;
            isTeleport = true;
            playerInfo.scoreCount += 1;
            teamInfo.score += 1;
            //TODO 当队伍获得分数
            if(teamInfo.score != 5){
                gameStart = 5;
                resetStart = true;
                cause = playerInfo+"得分!";


                StringBuilder s1 = new StringBuilder();
                for(TeamInfo teamInfo1: teamInfos){
                    s1.append(teamInfo1.getTeamConfig().getNameColor()).append(teamInfo1.score).append("&7").append(" - ");

                }
                sendMessage(Utils.writeLine(19,"&6●"));
                sendMessage("");
                sendMessage(Utils.getCentontString(playerInfo+"&e得分！",19));
                sendMessage(Utils.getCentontString(s1.substring(0,s1.length() - 3),19));
                sendMessage("");
                sendMessage(Utils.writeLine(19,"&6●"));
            }

            for(PlayerInfo playerInfo1: getLivePlayers()){
                playerInfo1.spawn();
            }
            isTeleport = false;
        }
    }

}
