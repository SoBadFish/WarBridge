package org.sobadfish.warbridge.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.PlayerEnderChestInventory;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.*;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;
import de.theamychan.scoreboard.api.ScoreboardAPI;
import de.theamychan.scoreboard.network.DisplaySlot;
import de.theamychan.scoreboard.network.Scoreboard;
import de.theamychan.scoreboard.network.ScoreboardDisplay;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.event.PlayerGameDeathEvent;
import org.sobadfish.warbridge.player.message.ScoreBoardMessage;
import org.sobadfish.warbridge.player.team.TeamInfo;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.tools.Utils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sobadfish
 * @date 2022/9/9
 */
public class PlayerInfo {

    public EntityHuman player;

    private PlayerType playerType;

    private GameRoom gameRoom;

    private TeamInfo teamInfo;

    public boolean cancel;

    public boolean disable;

    public boolean isLeave;

    public int killCount = 0;

    public int scoreCount = 0;

    public int damageTime = 0;

    public int deathCount = 0;


    public int assists = 0;

    private PlayerInfo damageByInfo = null;



    public PlayerInventory inventory;

    public PlayerEnderChestInventory eInventory;

    //助攻
    public LinkedHashMap<PlayerInfo,Long> assistsPlayers = new LinkedHashMap<>();

    public LinkedHashMap<Integer,Item> armor = new LinkedHashMap<Integer,Item>(){
        {
            put(0,new ItemHelmetLeather());
            put(1,new ItemChestplateLeather());
            put(2,new ItemLeggingsLeather());
            put(3,new ItemBootsLeather());
        }
    };

    public Position getPosition(){
        return player.getPosition();
    }

    public Level getLevel(){
        return player.getLevel();
    }

    public PlayerInfo(EntityHuman player){
        this.player = player;
    }

    public int getAssists() {
        return assists;
    }

    public EntityHuman getPlayer() {
        return player;
    }

    public int getKillCount() {
        return killCount;
    }

    public String getName(){
        return player.getName();
    }

    public void setPlayer(EntityHuman player) {
        this.player = player;
    }

    public void setTeamInfo(TeamInfo teamInfo) {
        this.teamInfo = teamInfo;
    }

    public void setDamageByInfo(PlayerInfo damageByInfo) {
        if(damageByInfo != null) {

            this.damageByInfo = damageByInfo;
            damageTime = 5;
            assistsPlayers.put(damageByInfo,System.currentTimeMillis());
            //现身
            getPlayer().removeEffect(14);
        }
    }

    public PlayerInfo getDamageByInfo() {
        return damageByInfo;
    }

    /**
     * 玩家数据初始化
     * */
    public void init(){
        if(WarBridgeMain.getWarBridgeMain().getConfig().getBoolean("save-playerInventory",true)){
            inventory = getPlayer().getInventory();
            eInventory = getPlayer().getEnderChestInventory();
        }
        getPlayer().setHealth(getPlayer().getMaxHealth());
        if(getPlayer() instanceof Player) {
            ((Player)getPlayer()).getFoodData().reset();
        }
        getPlayer().getInventory().clearAll();
        getPlayer().getEnderChestInventory().clearAll();
        if(getPlayer() instanceof Player){
            ((Player) getPlayer()).removeAllWindows();
            ((Player) getPlayer()).setExperience(0,0);
        }
        //TODO 给玩家初始化物品
        getPlayer().getInventory().setHeldItemSlot(0);
    }

    private void addKill(PlayerInfo info){

        info.killCount++;
        //助攻累计
        for(PlayerInfo playerInfo: assistsPlayers.keySet()){
            if(playerInfo.equals(info)){
                continue;
            }
            playerInfo.assists++;
        }
    }

    /**
     * 发送无前缀信息
     * */
    public void sendTipMessage(String msg){
        if(getGameRoom() != null) {
            if (cancel || isLeave  || getGameRoom().getType() == GameRoom.GameType.END) {
                return;
            }
            WarBridgeMain.sendTipMessageToObject(msg, getPlayer());
        }
    }
    /**
     * 发送信息
     * */
    public void sendMessage(String msg){
        if(getGameRoom() != null) {
            if (cancel || isLeave  || getGameRoom().getType() == GameRoom.GameType.END) {
                return;
            }
            WarBridgeMain.sendMessageToObject(msg, getPlayer());
        }
    }
    /**
     * 发送音效
     * */
    public void addSound(Sound sound){
        if(cancel || isLeave){
            return;
        }
        addSound(sound,1f,1f);
    }
    /**
     * 发送音效
     * */
    public void addSound(Sound sound,float v1,float v2){
        if(cancel || isLeave){
            return;
        }
        getPlayer().getLevel().addSound(getPlayer(),sound,v1,v2,(Player)getPlayer());
    }
    /**
     * 发送音效
     * */
    public void addSound(cn.nukkit.level.sound.Sound sound){
        if(cancel || isLeave){
            return;
        }
        getPlayer().getLevel().addSound(sound,(Player) getPlayer());
    }


    /**
     * 增加效果
     * */
    public void addEffect(Effect effect) {
        if(cancel || isLeave){
            return;
        }
        getPlayer().addEffect(effect);
    }

    /**
     * 发送信息
     * */
    public void sendTitle(String msg,int time){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            WarBridgeMain.sendTitle(msg, time * 20, (Player) getPlayer());
        }
    }
    /**
     * 发送信息
     * */
    public void sendTitle(String msg){
        sendTitle(msg,1);
    }
    /**
     * 发送信息
     * */
    public void sendSubTitle(String msg){
        if(cancel || isLeave ){
            return;
        }
        if(getPlayer() instanceof Player) {
            WarBridgeMain.sendSubTitle(msg, ((Player) getPlayer()));
        }
    }

    public void removeScoreBoard(){
        if(gameRoom != null) {
            if (gameRoom.getScoreboards().containsKey(this)) {
                if (getPlayer() instanceof Player) {
                    ScoreboardAPI.removeScorebaord((Player) getPlayer(),
                            gameRoom.getScoreboards().get(this));
                    gameRoom.getScoreboards().remove(this);
                }

            }
        }
    }

    public boolean  isDeath(){
        return  playerType == PlayerType.DEATH;
    }

    public boolean isWatch(){
        return  playerType == PlayerType.WATCH;
    }

    public boolean isInRoom(){
        return !cancel && !isLeave;
    }

    public boolean isLive(){
        return !cancel && !isLeave && playerType != PlayerType.WATCH;
    }

    public void sendScore(ScoreBoardMessage message){
        if(getPlayer() instanceof Player) {
            if (message == null || getPlayer() == null || !((Player) getPlayer()).isOnline()) {
                removeScoreBoard();
                return;
            }
            if (((Player) getPlayer()).isOnline()) {
                try {
                    Scoreboard scoreboard = ScoreboardAPI.createScoreboard();
                    String title = message.getTitle();
                    ScoreboardDisplay scoreboardDisplay = scoreboard.addDisplay(DisplaySlot.SIDEBAR,
                            "dumy", TextFormat.colorize('&', title));

                    ArrayList<String> list = message.getLore();
                    for (int line = 0; line < list.size(); line++) {
                        String s = list.get(line);

                        scoreboardDisplay.addLine(TextFormat.colorize('&', s), line);
                    }
                    try {
                        gameRoom.getScoreboards().get(this).hideFor((Player) player);
                    } catch (Exception ignored) {
                    }
                    scoreboard.showFor((Player) player);
                    gameRoom.getScoreboards().put(this, scoreboard);
                } catch (Exception ignored) {
                }
            }
        }

    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }
    public boolean isLeave() {
        return isLeave;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    /**
     * 发送信息
     * */
    public void sendActionBar(String msg){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            ((Player) getPlayer()).sendActionBar(TextFormat.colorize('&',msg));
        }
    }

    /**
     * 发送信息
     * */
    public void sendTip(String msg){
        if(cancel || isLeave){
            return;
        }
        if(getPlayer() instanceof Player) {
            WarBridgeMain.sendTip(msg, (Player) getPlayer());
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceMessage(String msg){
        WarBridgeMain.sendMessageToObject(msg, getPlayer());
    }

    /**
     * 发送强制信息
     * */
    public void sendForceTitle(String msg){
        if(player instanceof Player){
            ((Player) player).sendTitle(TextFormat.colorize('&',msg));
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceTitle(String msg,int time){
        if(player instanceof Player){
            ((Player) player).sendTitle(TextFormat.colorize('&',msg),null,0,time,0);
        }
    }

    /**
     * 发送强制信息
     * */
    public void sendForceSubTitle(String msg){
        if(player instanceof Player){
            ((Player) player).setSubtitle(TextFormat.colorize('&',msg));
        }
    }

    public void spawn() {
        if(isSendkey){
            isSendkey = false;
        }
        if (player instanceof Player) {
            ((Player) player).setFoodEnabled(true);
            ((Player) player).getFoodData().reset();

            if (!((Player) player).isOnline()) {
                playerType = PlayerType.LEAVE;
                return;
            }
        }

        player.getInventory().clearAll();


        boolean teleport;
        try {
            teleport = player.teleport(teamInfo.getTeamConfig().getSpawnPosition());
        }catch (Exception e){
            teleport = false;
        }
        if(!teleport){
            throw new NullPointerException("无法将玩家传送到队伍出生点");
        }
        if (getPlayer() instanceof Player) {
            ((Player) getPlayer()).setGamemode(0);

            ((Player) getPlayer()).setExperience(0,0);


        }
        //TODO 初始装备
        for (Map.Entry<Integer, Item> entry : armor.entrySet()) {
            Item item;
            if(entry.getValue() instanceof ItemColorArmor){
                ItemColorArmor colorArmor = (ItemColorArmor) entry.getValue();
                colorArmor.setColor(getTeamInfo().getTeamConfig().getRgb());
                item = colorArmor;
            }else{
                item = entry.getValue();
            }

            player.getInventory().setArmorItem(entry.getKey(), item);
        }
        //给予玩家初始物品
        player.getInventory().addItem(new ItemSwordIron());
        player.getInventory().addItem(new ItemBow());
        ItemPickaxeDiamond diamond = new ItemPickaxeDiamond();
        diamond.addEnchantment(Enchantment.getEnchantment(15).setLevel(2));
        player.getInventory().addItem(diamond);
        Item item1 = Item.get(280);
        item1.addEnchantment(Enchantment.getEnchantment(12));
        player.getInventory().addItem(item1);
        Item item = getTeamInfo().getTeamConfig().getTeamConfig().getBlockWoolColor();
        item.setCount(128);
        player.getInventory().addItem(item);
        ItemAppleGold gold = new ItemAppleGold();
        gold.setCount(8);
        player.getInventory().addItem(gold);
//        player.getInventory().setItem(7,new ItemDiamond());
        player.getInventory().setItem(8,new ItemArrow(0,16));



        //TODO 给予玩家初始物品




        playerType = PlayerType.START;

    }


    /**
     * 取消
     * */
    public void cancel(){
        leave();
        if (player.isImmobile()) {
            player.setImmobile(false);
        }

        cancel = true;
        disable = true;
        getGameRoom().getPlayerInfos().remove(this);
        setGameRoom(null);
    }

    private void leave(){
        clear();
        if(getTeamInfo() != null){
            getTeamInfo().quit(this);
        }
        sendScore(null);
        setLeave(true);
        playerType = PlayerType.LEAVE;
    }

    public void setLeave(boolean leave) {
        isLeave = leave;
        if(leave){
            playerType = PlayerType.LEAVE;

        }else{
            if(getGameRoom().getType() == GameRoom.GameType.WAIT){
                playerType = PlayerType.WAIT;
            }
            if(getGameRoom().getType() == GameRoom.GameType.START){
                playerType = PlayerType.START;
            }
        }
    }

    public void clear(){
        if(player instanceof Player){
            if(((Player) player).isOnline()){
                player.setNameTag(player.getName());
                player.getInventory().clearAll();
                player.getEnderChestInventory().clearAll();
                ((Player) player).getFoodData().reset();
                player.setHealth(player.getMaxHealth());
                ((Player) player).setExperience(0,0);
                if(inventory != null && eInventory != null){
                    player.getInventory().setContents(inventory.getContents());
                    player.getEnderChestInventory().setContents(eInventory.getContents());
                }
                if(getPlayer() instanceof Player) {
                    ((Player) getPlayer()).setGamemode(0);
                }
            }
        }
    }



    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PlayerInfo){
            return ((PlayerInfo) obj).getPlayer().getName().equalsIgnoreCase(getPlayer().getName());
        }
        return false;
    }
    @Override
    public String toString(){
        String teamName = "&r";
        String playerName = player.getName();
        if(teamInfo != null && !isWatch()){
            teamName = teamInfo.getTeamConfig().getNameColor();
        }else if(isWatch()){
            teamName = "&7 ";
        }

        return teamName+playerName;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
    }

    private int spawnTime = 0;




    public static String formatTime1(int s){
        int min = s / 60;
        int ss = s % 60;
        String mi = min+"";
        String sss = ss+"";
        if(min < 10){
            mi = "0"+mi;
        }
        if(ss < 10){
            sss = "0"+ss;
        }
        if(min > 0){

            return mi+":"+sss;
        }else{
            return "00:"+sss+"";
        }

    }

    private ArrayList<String> getLore(boolean isWait){
        ArrayList<String> lore = new ArrayList<>();
        String levelName = WarBridgeMain.getMenuRoomManager().getNameByRoom(gameRoom.getRoomConfig());
        if(levelName == null){
            levelName = " -- ";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        lore.add("&7"+format.format(new Date()));
        lore.add("游戏模式: &a"+levelName);

        lore.add(" ");
        if(isWait){
            lore.add("玩家数: &a"+gameRoom.getPlayerInfos().size()+" &r/&a "+gameRoom.getRoomConfig().getMaxPlayerSize());
            lore.add("等待中....");
            lore.add("   ");

        }else{

            lore.add("剩余时间: &a"+formatTime1(getGameRoom().loadTime));
            lore.add("    ");
            for(TeamInfo teamInfo: gameRoom.getTeamInfos()){
                String me = "";
                if(getTeamInfo() != null && getTeamInfo().equals(teamInfo)){
                    me = "&7(我)";
                }
                lore.add("◎ "+ teamInfo +": &r  &c"+ Utils.getLine(5,Utils.getPercent(teamInfo.score,5),teamInfo.getTeamConfig().getNameColor()+"●","&7●") +me);
            }
            lore.add("       ");
            lore.add("&b击杀数: &a"+killCount);
            lore.add("&e助攻数: &a"+assists);

            lore.add("         ");
        }
        Object obj = WarBridgeMain.getWarBridgeMain().getConfig().get("game-logo");
        if(obj instanceof List){
            for(Object s : (List<?>)obj){
                lore.add(s.toString());
            }
        }else{
            lore.add(WarBridgeMain.getWarBridgeMain().getConfig().getString("game-logo","&l&cT&6o&eC&ar&ba&9f&dt"));
        }
        return lore;
    }


    private boolean isSendkey = false;

    /**
     * 定时任务
     * */
    public void onUpdate(){

        //TODO 玩家更新线程
        if(gameRoom != null && gameRoom.gameStart > 0){
            if(teamInfo != null){
                if(teamInfo.score == 5){
                    return;
                }
            }
            if(!player.isImmobile() && !isWatch()) {
                player.setImmobile(true);
            }
            sendTitle(gameRoom.cause, 2);
            sendSubTitle("&7将在&a"+gameRoom.gameStart+"&7秒后开始");
        }else {
            if (player.isImmobile()) {
                sendTitle(" ", 2);
                sendSubTitle("&a开战！");
                player.setImmobile(false);
            }
        }
        if(playerType == PlayerType.START){
            //TODO 游戏开始后 可以弄一些buff
            player.setNameTag(TextFormat.colorize('&',teamInfo.getTeamConfig().getNameColor()+player.getName()+" \n&c❤&7"+String.format("%.1f",player.getHealth())));


        }else if(playerType == PlayerType.WAIT){
            if(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition().getY() - player.getY() > getGameRoom().getRoomConfig().callbackY){
                if(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition() == null){
                    if(getGameRoom() != null){
                        getGameRoom().quitPlayerInfo(this,true);
                        sendMessage("&c房间出现了错误 （未识别到等待大厅）已将你送回出生点");
                    }
                    return;
                }
                player.teleport(getGameRoom().getRoomConfig().getWorldInfo().getWaitPosition());
            }
        }



        try{
            Class.forName("de.theamychan.scoreboard.api.ScoreboardAPI");
        }catch (Exception e){
            return;
        }
        if(playerType != PlayerType.LEAVE){
            //TODO 计分板的一些内容

            ScoreBoardMessage boardMessage = new ScoreBoardMessage(WarBridgeMain.getScoreBoardTitle());
            boardMessage.setLore(getLore(playerType == PlayerType.WAIT));
            sendScore(boardMessage);
        }else{
            sendScore(null);
        }

    }

    public enum PlayerType{
        /**
         * WAIT: 等待 START: 开始 DEATH: 死亡(等待复活)  LEAVE: 离开 WATCH 观察(真正的死亡)
         * */
        WAIT,START,DEATH,LEAVE,WATCH
    }

    @Override
    public int hashCode() {
        return player.hashCode();
    }

    public void death(EntityDamageEvent event){

        player.setHealth(player.getMaxHealth());
        if(player instanceof Player){
            ((Player) player).removeAllWindows();
            ((Player) player).getUIInventory().clearAll();
        }
        PlayerGameDeathEvent event1 = new PlayerGameDeathEvent(this,getGameRoom(),WarBridgeMain.getWarBridgeMain());
        Server.getInstance().getPluginManager().callEvent(event1);
        if(getPlayer() instanceof Player) {
            ((Player) getPlayer()).setGamemode(3);
        }

        player.removeAllEffects();
        if(getGameRoom().getWorldInfo().getConfig().getGameWorld() == null){
            return;
        }
//        player.teleport(teamInfo.getTeamConfig().getSpawnPosition());
        deathCount++;
        if(event != null) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if(damageByInfo != null){
                    gameRoom.sendMessage(this + " &e被 &r" + damageByInfo + " 推入虚空。");
                    addKill(damageByInfo);
                }
                gameRoom.sendMessage(this + "&e掉入虚空");

            } else if (event instanceof EntityDamageByEntityEvent) {
                Entity entity = ((EntityDamageByEntityEvent) event).getDamager();
                if (entity instanceof Player) {
                    PlayerInfo info = WarBridgeMain.getRoomManager().getPlayerInfo((Player) entity);
                    String killInfo = "击杀";
                    if(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE){
                        killInfo = "射杀";
                    }
                    if (info != null) {
                        addKill(info);
                        gameRoom.sendMessage(this + " &e被 &r" + info + " "+killInfo+"了。");
                    }
                } else {
                    gameRoom.sendMessage(this + " &e被 &r" + entity.getName() + " 击败了");
                }
            } else {
                if(damageByInfo != null){
                    addKill(damageByInfo);
                    gameRoom.sendMessage(this + " &e被 &r" + damageByInfo + " 击败了");
                }else {
                    String deathInfo = "&e死了";
                    switch (event.getCause()){
                        case LAVA:
                            deathInfo = "&e被岩浆烧死了";
                            break;
                        case FALL:
                            deathInfo = "&e摔死了";
                            break;
                        case FIRE:
                            deathInfo = "&e被烧了";
                            break;
                        case HUNGER:
                            deathInfo = "&e饿死了";
                            break;
                        default:break;
                    }
                    gameRoom.sendMessage(this +deathInfo);
                }
            }
        }
//        playerType = PlayerType.DEATH;
        damageByInfo = null;
        player.getInventory().clearAll();
        player.getOffhandInventory().clearAll();
        if(playerType == PlayerType.WATCH){
            getGameRoom().joinWatch(this);
        }
        //重新复活
        spawn();

    }

}
