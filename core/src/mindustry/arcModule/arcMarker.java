package mindustry.arcModule;

import arc.*;
import arc.graphics.Color;
import arc.math.geom.*;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.input.DesktopInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mindustry.Vars.*;

public class arcMarker{
    public enum markType{MARK,GATHER,ATTACK,DEFENCE} //,ITEMS,WAVES需要加入吗
    public markType type = markType.MARK;
    /* 应该是int类型 */
    public Vec2 loc = new Vec2(0,0);
    public float time = 0;

    //手机端标记
    public static Boolean mobileMark = false;

    public static ObjectMap<markType,String> markString =  new ObjectMap<>(){{
        put(markType.MARK,"标记");
        put(markType.GATHER,"集合");
        put(markType.ATTACK,"攻击");
        put(markType.DEFENCE,"防御");
    }};
    //感觉有点蠢，但总比跑循环好吧
    public static ObjectMap<String,markType> stringMark =  new ObjectMap<>(){{
        put("标记",markType.MARK);
        put("集合",markType.GATHER);
        put("攻击",markType.ATTACK);
        put("防御",markType.DEFENCE);
    }};

    //储存所有标记点
    public static Seq<arcMarker> markList =  new Seq<>();

    private String msgHander = "<ARC"+arcVersion+">";

    //信息相关
    private static String msgMarkType = "";



    public static arcMarker newMarker(int x,int y){
        arcMarker marker = new arcMarker();
        marker.loc = new Vec2(x,y);
        switch (Core.settings.getInt("markType")){
            case 0: marker.type = markType.MARK;break;
            case 1: marker.type = markType.GATHER;break;
            case 2: marker.type = markType.ATTACK;break;
            case 3: marker.type = markType.DEFENCE;break;
        }
        marker.time = Time.time;
        markList.add(marker);
        return marker;
    }

    public static arcMarker newMarker(int x,int y,markType markerType){
        arcMarker marker = new arcMarker();
        marker.loc = new Vec2(x,y);
        marker.type = markerType;
        marker.time = Time.time;
        markList.add(marker);
        return marker;
    }

    public static void newMsgMarker(String message){
        if (message.contains("<ARC")) {
            int x = 0;
            int y = 0;
            try {
                int strLength = message.length();
                int stopindex = 0;
                for (int i = 0; i < strLength; i++) {
                    if (message.substring(i, i + 1).equals(">")) {
                        msgMarkType = message.substring(i+1,i+3).trim();
                    }
                    if (message.substring(i, i + 1).equals("(")) {
                        stopindex = i;
                    }
                    if (message.substring(i, i + 1).equals(",") && stopindex > 0) {
                        x = Integer.parseInt(message.substring(stopindex + 1, i).trim());
                        y = Integer.parseInt(message.substring(i + 1, strLength - 1).trim());
                        break;
                    }
                }
            } catch (Exception e) {}
            arcMarker.newMarker(x,y,stringMark.get(msgMarkType)).showEffect();

        } else if (message.contains("发起集合")) {
            int strLength = message.length();
            int x = 0;
            int y = 0;
            int initindex = 0;
            int interval = 0;
            try {
                for (int i = 0; i < strLength; i++) {
                    if (message.substring(i, i + 1).equals("(")) {
                        initindex = i;
                    }
                    if (initindex > 0) {
                        if (message.substring(i, i + 1).equals(",")) {
                            interval = i;
                        }
                        if (message.substring(i, i + 1).equals(")")) {
                            x = Integer.parseInt(message.substring(initindex + 6, interval).trim());
                            y = Integer.parseInt(message.substring(interval + 1, i - 7).trim());
                            break;
                        }
                    }
                }
            }catch (Exception e) {}

            arcMarker.newMarker(x,y, arcMarker.markType.GATHER).showEffect();
        }

    }

    public void shareInfo(){
        String location = "(" + (int)loc.x + "," + (int)loc.y + ")";
        if(markList.size<2 || (Time.time - markList.get(markList.size - 2).time)>100f)
            Call.sendChatMessage(msgHander + markString.get(type) +"：[red]"+location);
    }

    public void showEffect(){
        Effect showEffect = Fx.arcMarker;
        if (type == markType.MARK) {
            showEffect = Fx.arcMarker;
        }
        else if(type == markType.GATHER){
            showEffect = Fx.arcGatherMarker;
        }
        else if(type == markType.ATTACK){
            showEffect = Fx.arcAttackMarker;
        }
        else if(type == markType.DEFENCE){
            showEffect = Fx.arcDefenseMarker;
        }
        showEffect.arcCreate(loc.x * tilesize, loc.y * tilesize,0f, Color.red,null);
    }

    public String effectColor(){
        if (type == markType.MARK) return "[#eab678]";
        if (type == markType.GATHER) return Color.red.toString();
        if (type == markType.ATTACK) return Color.cyan.toString();
        if (type == markType.DEFENCE) return Color.acid.toString();
        return "";
    }

    public void reviewEffect(){
        if(control.input instanceof DesktopInput){
            ((DesktopInput) control.input).panning = true;
        }
        Core.camera.position.set(loc.x * tilesize,loc.y * tilesize);
        showEffect();
    }

    public static void arcSendMarkPos(float x, float y){
        if (mobileMark){
            arcMarker.newMarker((int)x/8,(int)y/8).shareInfo();
            mobileMark = false;
        }
    }

    public static void initMobileMark(){
        mobileMark = true;
    }

}
