package org.sobadfish.warbridge.tools;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.BaseEntity;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFirework;
import cn.nukkit.item.ItemFirework;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.DyeColor;
import org.sobadfish.warbridge.WarBridgeMain;
import org.sobadfish.warbridge.room.GameRoom;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.SplittableRandom;


public class Utils {

    private static final SplittableRandom RANDOM = new SplittableRandom(System.currentTimeMillis());

    public static int rand(int min, int max) {
        return min == max ? max : RANDOM.nextInt(max + 1 - min) + min;
    }

    public static double rand(double min, double max) {
        return min == max ? max : min + Math.random() * (max - min);
    }

    public static float rand(float min, float max) {
        return min == max ? max : min + (float) Math.random() * (max - min);
    }

    public static boolean rand() {
        return RANDOM.nextBoolean();
    }

    public static double calLinearFunction(Vector3 pos1, Vector3 pos2, double element, int type) {
        if (pos1.getFloorY() != pos2.getFloorY()) {
            return 1.7976931348623157E308D;
        } else if (pos1.getX() == pos2.getX()) {
            return type == 1 ? pos1.getX() : 1.7976931348623157E308D;
        } else if (pos1.getZ() == pos2.getZ()) {
            return type == 0 ? pos1.getZ() : 1.7976931348623157E308D;
        } else {
            return type == 0 ? (element - pos1.getX()) * (pos1.getZ() - pos2.getZ()) / (pos1.getX() - pos2.getX()) + pos1.getZ() : (element - pos1.getZ()) * (pos1.getX() - pos2.getX()) / (pos1.getZ() - pos2.getZ()) + pos1.getX();
        }
    }

    public static ArrayList<Player> getAroundOfPlayers(Position player, int size) {
        ArrayList<Player> players = new ArrayList<>();
        for (Entity entity : getAroundPlayers(player, size, false)) {
            players.add((Player) entity);
        }
        return players;
    }

    public static LinkedList<Entity> getAroundPlayers(Position player, int size, boolean isEntity) {
        LinkedList<Entity> explodePlayer = new LinkedList<>();
        for (Entity player1 : player.level.getEntities()) {

            if (player1.x < player.x + size && player1.x > player.x - size && player1.z < player.z + size && player1.z > player.z - size && player1.y < player.y + size && player1.y > player.y - size) {
                if (!isEntity && player instanceof Player && ((Player) player).getGamemode() != 3) {
                    explodePlayer.add(player1);
                } else if (isEntity && !(player1 instanceof BaseEntity)) {
                    explodePlayer.add(player1);
                }

            }
        }
        return explodePlayer;
    }

    /**
     * 获取百分比
     * */
    public static double getPercent(int n,int max){
        double r = 0;
        if(n > 0){
            r = (double) n / (double) max;
        }
        return r;
    }

    /**
     * 绘制颜色条
     * @param size 长度
     * @param percent 百分比
     * @param hasColor 颜色块
     * @param noColor 没有颜色的块
     *
     * @return 颜色条
     * */
    public static String getLine(int size,double percent,String hasColor,String noColor){
        int l = (int) (size * percent);
        int other = size - l;
        StringBuilder ls = new StringBuilder();
        if(l > 0){
            for(int i = 0;i < l;i++){
                ls.append(hasColor);
            }
        }
        StringBuilder others = new StringBuilder();
        if(other > 0){
            for(int i = 0;i < other;i++){
                others.append(noColor);
            }
        }
        return ls +others.toString();
    }

    public static boolean inArea(GameRoom room,Position player, Position location, boolean y){
        double radius = room.roomConfig.pointRadius;
        Position n1 = new Position(location.getX() -radius,location.getY(),location.getZ() - radius,location.level);
        Position n2 = new Position(location.getX() +radius,location.getY(),location.getZ() + radius,location.level);
        if(player.level.getFolderName().equalsIgnoreCase(location.level.getFolderName())) {
            if (player.x >= Math.min(n1.getX(), n2.getX())
                    && player.x <= Math.max(n1.getX(), n2.getX())
                    && player.z >= Math.min(n1.getZ(), n2.getZ())
                    && player.z <= Math.max(n1.getZ(), n2.getZ())
            ) {
                if(y) {
                    return player.y <= location.y;
                }else{
                    return true;
                }

            }
        }
        return false;
    }


    public static void toDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    toDelete(file1);
                } else {
                    String name = file1.getName();
                    file1.delete();
                }
            }
        }
        String name = file.getName();
        file.delete();

    }

    /**
     * 放烟花
     */
    public static void spawnFirework(Position position) {

        Level level = position.getLevel();
        ItemFirework item = new ItemFirework();
        CompoundTag tag = new CompoundTag();
        Random random = new Random();
        CompoundTag ex = new CompoundTag();
        ex.putByteArray("FireworkColor", new byte[]{
                (byte) DyeColor.values()[random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].getDyeData()
        });
        ex.putByteArray("FireworkFade", new byte[0]);
        ex.putBoolean("FireworkFlicker", random.nextBoolean());
        ex.putBoolean("FireworkTrail", random.nextBoolean());
        ex.putByte("FireworkType", ItemFirework.FireworkExplosion.ExplosionType.values()
                [random.nextInt(ItemFirework.FireworkExplosion.ExplosionType.values().length)].ordinal());
        tag.putCompound("Fireworks", (new CompoundTag("Fireworks")).putList(new ListTag<CompoundTag>("Explosions").add(ex)).putByte("Flight", 1));
        item.setNamedTag(tag);
        CompoundTag nbt = new CompoundTag();
        nbt.putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("", position.x + 0.5D))
                .add(new DoubleTag("", position.y + 0.5D))
                .add(new DoubleTag("", position.z + 0.5D))
        );
        nbt.putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("", 0.0D))
                .add(new DoubleTag("", 0.0D))
                .add(new DoubleTag("", 0.0D))
        );
        nbt.putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("", 0.0F))
                .add(new FloatTag("", 0.0F))

        );
        nbt.putCompound("FireworkItem", NBTIO.putItemHelper(item));
        EntityFirework entity = new EntityFirework(level.getChunk((int) position.x >> 4, (int) position.z >> 4), nbt);
        entity.spawnToAll();
    }



    public static String writeLine(int size,String line){
        StringBuilder s = new StringBuilder();
        for(int i = 0;i< size;i++){
            s.append(line);
        }
        return s.toString();
    }

    public static String getCentontString(String input,int lineWidth){
        input = input.replace(' ','$');
        return justify(input,lineWidth,'c').replace('$',' ');
    }
    /**
     * 字符串居中算法
     *
     * @param input 输入的字符串
     * @param lineWidth 一共多少行
     * @param just 对齐方法 l: 左对齐 c: 居中 r右对齐
     *
     * @return 对齐的字符串
    * */
    public static String justify(String input, int lineWidth, char just) {
        StringBuilder sb = new StringBuilder("");
        char[] inputText = input.toCharArray();
        ArrayList<String> words = new ArrayList<>();
        for (int i = 0; i < inputText.length; i++) {
            if (inputText[i] != ' ' && inputText[i] != '\n') {
                sb.append(inputText[i]);
            } else {
                inputText[i] = '\n';
                words.add(sb.toString());
                //clear content
                sb = new StringBuilder("");
            }
        }
        //add last word because the last char is not space/'\n'.
        words.add(sb.toString());
        for (String s : words) {
            if (s.length() >= lineWidth) {
                lineWidth = s.length();
            }
        }
        char[] output = null;
        StringBuilder sb2 = new StringBuilder("");
        StringBuilder line;
        for (String word : words) {
            line = new StringBuilder();
            for (int i = 0; i < lineWidth; i++) {
                line.append(" ");
            }
            switch (just) {
                case 'l':
                    line.replace(0, word.length(), word);
                    break;
                case 'r':
                    line.replace(lineWidth - word.length(), lineWidth, word);
                    break;
                case 'c':
                    //all the spaces' length
                    int rest = lineWidth - word.length();
                    int begin = 0;
                    if (rest % 2 != 0) {
                        begin = (rest / 2) + 1;
                    } else {
                        begin = rest / 2;
                    }
                    line.replace(begin, begin + word.length(), word);
                    break;
                    default:break;
            }

            line.append('\n');
            sb2.append(line);
        }
        return sb2.toString();

    }
    /**
     * 复制文件
     * */
    public static boolean copyFiles(File old,File target){

        int load = 1;
        File[] files = old.listFiles();
        if(files != null){
            for (File value : files) {
                WarBridgeMain.sendMessageToConsole("复制地图中 ... "+((load / (float)files.length) * 100) +"%");
                load++;
                if (value.isFile()) {
                    // 复制文件
                    try {
                        File file1 = new File(target +File.separator + value.getName());
                        if(!file1.exists()){
                            try{
                                file1.createNewFile();
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        copyFile(value, file1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (value.isDirectory()) {
                    // 复制目录
                    String sourceDir = old + File.separator + value.getName();
                    String targetDir = target+ File.separator + value.getName();
                    try {
                        copyDirectiory(sourceDir, targetDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return true;
    }

    private static void copyFile(File sourceFile,File targetFile)
            throws IOException{
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff=new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff=new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 5];
        int len;
        while ((len =inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();

        //关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }
    /**复制文件夹   */
    private static void copyDirectiory(String sourceDir, String targetDir)
            throws IOException {
        // 新建目标目录
        File file = new File(targetDir);
        if(!file.exists()) {
            if (!file.mkdirs()) {
                Server.getInstance().getLogger().error("新建" + targetDir + "失败");
            }
        }
        // 获取源文件夹当前下的文件或目录
        File[] files = (new File(sourceDir)).listFiles();
        if(files != null){
            for (File value : files) {
                if (value.isFile()) {
                    // 源文件
                    // 目标文件
                    File targetFile = new
                            File(new File(targetDir).getAbsolutePath()
                            + File.separator + value.getName());
                    copyFile(value, targetFile);

                }
                if (value.isDirectory()) {
                    // 准备复制的源文件夹
                    String dir1 = sourceDir + File.separator + value.getName();
                    // 准备复制的目标文件夹
                    String dir2 = targetDir + File.separator + value.getName();
                    copyDirectiory(dir1, dir2);
                }
            }
        }

    }


}
