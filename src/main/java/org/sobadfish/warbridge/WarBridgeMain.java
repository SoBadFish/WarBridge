package org.sobadfish.warbridge;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.warbridge.command.WarBridgeAdminCommand;
import org.sobadfish.warbridge.command.WarBridgeCommand;
import org.sobadfish.warbridge.command.WarBridgeSpeakCommand;
import org.sobadfish.warbridge.manager.MenuRoomManager;
import org.sobadfish.warbridge.manager.RoomManager;
import org.sobadfish.warbridge.manager.ThreadManager;
import org.sobadfish.warbridge.manager.data.PlayerDataManager;
import org.sobadfish.warbridge.manager.data.PlayerTopManager;
import org.sobadfish.warbridge.panel.lib.AbstractFakeInventory;
import org.sobadfish.warbridge.proxy.ItemProxy;
import org.sobadfish.warbridge.room.config.GameRoomConfig;
import org.sobadfish.warbridge.variable.TipVariable;
import org.sobadfish.warbridge.variable.WarBridgeVariable;

import java.io.File;

/**
 *  __      __        ___     _    _
 *  \ \    / /_ _ _ _| _ )_ _(_)__| |__ _ ___
 *   \ \/\/ / _` | '_| _ \ '_| / _` / _` / -_)
 *    \_/\_/\__,_|_| |___/_| |_\__,_\__, \___|
 *                                  |___/     j
 * @author Sobadfish
 * 13:07
 */
public class WarBridgeMain extends PluginBase {


    public static WarBridgeMain warBridgeMain;

    public static final String GAME_NAME = "WarBridge";

    private static RoomManager roomManager;

    private static MenuRoomManager menuRoomManager;

    private static PlayerDataManager dataManager;

    private static PlayerTopManager topManager;


    public static int upExp;

    @Override
    public void onEnable() {
        warBridgeMain = this;
        checkServer();
        this.getLogger().info(TextFormat.colorize('&',"&e __      __        ___     _    _          "));
        this.getLogger().info(TextFormat.colorize('&',"&e \\ \\    / /_ _ _ _| _ )_ _(_)__| |__ _ ___ "));
        this.getLogger().info(TextFormat.colorize('&',"&e  \\ \\/\\/ / _` | '_| _ \\ '_| / _` / _` / -_)"));
        this.getLogger().info(TextFormat.colorize('&',"&e   \\_/\\_/\\__,_|_| |___/_| |_\\__,_\\__, \\___|"));
        this.getLogger().info(TextFormat.colorize('&',"&e                                 |___/     "));
        this.getLogger().info(TextFormat.colorize('&',"&e"));
        this.getLogger().info(TextFormat.colorize('&',"&e正在加载WarBridge 战桥插件 本版本为&av"+this.getDescription().getVersion()));
        this.getLogger().info(TextFormat.colorize('&',"&c插件作者:&b sobadfish(某吃瓜咸鱼) &aQQ：&e1586235767"));
        this.getLogger().info(TextFormat.colorize('&',"&c本插件为原创插件 部分源代码出处已标明原作者"));
        this.getLogger().info(TextFormat.colorize('&',"&a战桥插件加载完成，祝您使用愉快"));

        ItemProxy.init();
        loadConfig();
        this.getServer().getCommandMap().register("warbridge",new WarBridgeAdminCommand("wba"));
        this.getServer().getCommandMap().register("warbridge",new WarBridgeCommand("wb"));
        this.getServer().getCommandMap().register("warbridge",new WarBridgeSpeakCommand("wbs"));
        ThreadManager.init();
        try{
            Class.forName("com.smallaswater.npc.variable.BaseVariableV2");
            WarBridgeVariable.init();
        }catch (Exception ignore){}
        try{
            Class.forName("tip.utils.variables.BaseVariable");
            TipVariable.init();
        }catch (Exception ignore){}
        this.getLogger().info(TextFormat.colorize('&',"&a战桥插件加载完成，祝您使用愉快"));
    }

    public static void sendMessageToConsole(String msg){
        sendMessageToObject(msg,null);
    }

    public static String getTitle(){
        return TextFormat.colorize('&',warBridgeMain.getConfig().getString("title"));
    }

    public static String getScoreBoardTitle(){
        return TextFormat.colorize('&',warBridgeMain.getConfig().getString("scoreboard-title","&f[&a迷你战墙&f]"));
    }

    public static void sendTipMessageToObject(String msg,Object o){
        String message = TextFormat.colorize('&',msg);
        if(o != null){
            if(o instanceof Player){
                if(((Player) o).isOnline()) {
                    ((Player) o).sendMessage(message);
                    return;
                }
            }
        }
        warBridgeMain.getLogger().info(message);

    }

    public static int getUpExp(){
        return upExp;
    }

    /**
     * 加载配置文件
     */
    public void loadConfig(){
        saveDefaultConfig();
        reloadConfig();
        upExp = getConfig().getInt("up-exp",500);
        File mainFileDir = new File(this.getDataFolder()+File.separator+"rooms");
        if(!mainFileDir.exists()){
            if(!mainFileDir.mkdirs()){
                sendMessageToConsole("&c创建文件夹 rooms失败");
            }
        }

        roomManager = RoomManager.initGameRoomConfig(mainFileDir);
        sendMessageToConsole("&a房间数据全部加载完成");
        this.getServer().getPluginManager().registerEvents(roomManager,this);
        if(getConfig().getAll().size() == 0) {
            this.saveResource("config.yml", true);
            reloadConfig();
        }
        menuRoomManager = new MenuRoomManager(getConfig());
        dataManager = PlayerDataManager.asFile(new File(this.getDataFolder()+File.separator+"player.json"));
        //初始化排行榜
        topManager = PlayerTopManager.asFile(new File(this.getDataFolder()+File.separator+"top.json"));
        if(topManager != null){
            topManager.init();
        }

    }

    public static PlayerDataManager getDataManager() {
        return dataManager;
    }

    public static PlayerTopManager getTopManager() {
        return topManager;
    }

    public static RoomManager getRoomManager() {
        return roomManager;
    }

    public static MenuRoomManager getMenuRoomManager() {
        return menuRoomManager;
    }

    public static WarBridgeMain getWarBridgeMain() {
        return warBridgeMain;
    }

    public static void sendMessageToObject(String msg, Object o){
        String message = TextFormat.colorize('&',getTitle()+" &b>>&r "+msg);
        sendTipMessageToObject(message,o);
    }

    public static void sendSubTitle(String msg,Player o){
        String message = TextFormat.colorize('&',msg);
        if(o != null){
            if(o.isOnline()) {
                o.setSubtitle(message);
            }
        }else{
            warBridgeMain.getLogger().info(message);
        }
    }

    public static void sendTitle(String msg,int time,Player o){
        String message = TextFormat.colorize('&',msg);
        if(o != null){
            if(o.isOnline()) {
                o.sendTitle(message,null,0,time,0);
            }
        }else{
            warBridgeMain.getLogger().info(message);
        }
    }

    public static void sendTip(String msg,Player o){
        String message = TextFormat.colorize('&',msg);
        if(o != null){
            if(o.isOnline()) {
                o.sendTip(message);
            }
        }else{
            warBridgeMain.getLogger().info(message);
        }
    }

    private void checkServer(){
        boolean ver = false;
        //双核心兼容
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT_PM1E");
            ver = true;

        } catch (ClassNotFoundException | NoSuchFieldException ignore) { }
        try {
            Class<?> c = Class.forName("cn.nukkit.Nukkit");
            c.getField("NUKKIT").get(c).toString().equalsIgnoreCase("Nukkit PetteriM1 Edition");
            ver = true;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignore) {
        }

        AbstractFakeInventory.IS_PM1E = ver;
        if(ver){
            sendMessageToConsole("&e当前核心为 Nukkit PM1E");
        }else{
            sendMessageToConsole("&e当前核心为 Nukkit");
        }
    }

    @Override
    public void onDisable() {
        if(topManager != null){
            topManager.save();
        }
        if(dataManager != null){
            dataManager.save();
        }
        if(roomManager != null){
            for (GameRoomConfig roomConfig: roomManager.getRoomConfigs()){
                roomConfig.save();
            }
        }
    }

//    public enum UiType{
//        /**
//         * auto: 自动
//         *
//         * packet: GUI界面
//         *
//         * ui: 箱子界面
//         * */
//        AUTO,PACKET,UI
//    }
}
