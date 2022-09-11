package org.sobadfish.warbridge.manager;


import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.room.GameRoom;
import org.sobadfish.warbridge.room.config.GameRoomConfig;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sobadfish
 * @date 2022/9/9
 */
public class RoomManager {

    public Map<String, GameRoomConfig> roomConfig;

    public Map<String, GameRoom> getRooms() {
        return rooms;
    }

    private Map<String, GameRoom> rooms = new LinkedHashMap<>();

    public boolean hasRoom(String room){
        return roomConfig.containsKey(room);
    }

    public boolean hasGameRoom(String room){
        return rooms.containsKey(room);
    }

    private RoomManager(Map<String, GameRoomConfig> roomConfig){
        this.roomConfig = roomConfig;
    }


    public static RoomManager initGameRoomConfig(File file){
        Map<String, GameRoomConfig> map = new LinkedHashMap<>();
        if(file.isDirectory()){
            File[] dirNameList = file.listFiles();
            if(dirNameList != null && dirNameList.length > 0) {
                for (File nameFile : dirNameList) {
                    if(nameFile.isDirectory()){
                        String roomName = nameFile.getName();
                        GameRoomConfig roomConfig = GameRoomConfig.getGameRoomConfigByFile(roomName,nameFile);
                        if(roomConfig != null){
                            WarBridgeMain.sendMessageToConsole("&a加载房间 "+roomName+" 完成");
                            map.put(roomName,roomConfig);

                        }else{
                            WarBridgeMain.sendMessageToConsole("&c加载房间 "+roomName+" 失败");

                        }
                    }
                }
            }
        }
        return new RoomManager(map);
    }


}
