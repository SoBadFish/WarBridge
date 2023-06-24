package org.sobadfish.warbridge.player.team.config;


import cn.nukkit.item.Item;
import cn.nukkit.utils.BlockColor;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SoBadFish
 * 2022/1/2
 */

public class TeamConfig {

    private String name;

    private String nameColor;

    private Item blockWoolColor;

    /**
     * 盔甲
     * */
    private LinkedHashMap<Integer,Item> inventoryArmor = new LinkedHashMap<>();

    /**
     * 队伍初始物品
     * */
    private LinkedHashMap<Integer,Item> inventoryItem = new LinkedHashMap<>();

    private BlockColor rgb;

    private TeamConfig(String name, String nameColor, Item blockWoolColor, BlockColor rgb){
        this.name = name;
        this.nameColor = nameColor;
        this.blockWoolColor = blockWoolColor;
        this.rgb = rgb;
    }

    public String getName() {
        return name;
    }

    public Item getBlockWoolColor() {
        return blockWoolColor;
    }

    public BlockColor getRgb() {
        return rgb;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setInventoryArmor(LinkedHashMap<Integer, Item> inventoryArmor) {
        this.inventoryArmor = inventoryArmor;
    }

    public void setInventoryItem(LinkedHashMap<Integer, Item> inventoryItem) {
        this.inventoryItem = inventoryItem;
    }


    public static TeamConfig getInstance(Map<?,?> map){
        String name = map.get("name").toString();
        String nameColor = map.get("nameColor").toString();
        Map<?,?> m = (Map<?,?>) map.get("rgb");
        int r = Integer.parseInt(m.get("r").toString());
        int g = Integer.parseInt(m.get("g").toString());
        int b = Integer.parseInt(m.get("b").toString());
        TeamConfig teamConfig = new TeamConfig(name,nameColor,Item.fromString(map.get("blockWoolColor")
                .toString()),new BlockColor(r,g,b));
        if(map.containsKey("inventory")){
            Map<?,?> inventoryMap = (Map<?, ?>) map.get("inventory");
            if(inventoryMap.containsKey("armor")){
                teamConfig.setInventoryArmor(decodeItemList((List<?>) inventoryMap.get("armor")));
            }
            if(inventoryMap.containsKey("inventory")){
                teamConfig.setInventoryItem(decodeItemList((List<?>) inventoryMap.get("inventory")));
            }
        }
        return teamConfig;
    }

    //解析物品对象
    private static LinkedHashMap<Integer,Item> decodeItemList(List<?> list){
        LinkedHashMap<Integer,Item> items = new LinkedHashMap<>();
        int i = 0;
        for(Object o: list){
            Item item = Item.fromString(o.toString());
            if(item.getId() > 0) {
                items.put(i,item);
            }
            i++;
        }
        return items;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof TeamConfig){
            return ((TeamConfig) obj).getName().equalsIgnoreCase(getName());
        }
        return false;
    }

}
