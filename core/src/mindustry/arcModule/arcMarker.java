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
    public Vec2 loc = new Vec2(0,0);
    public float time = 0;

    public static ObjectMap<markType,String> markString =  new ObjectMap<>(){{
        put(markType.MARK,"标记");
        put(markType.GATHER,"集合");
        put(markType.ATTACK,"攻击");
        put(markType.DEFENCE,"防御");
    }};


    //储存所有标记点
    public static Seq<arcMarker> markList =  new Seq<>();

    private String msgHander = "[ARC"+arcVersion+"]";

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

    public void shareInfo(){
        String location = "(" + (int)loc.x + "," + (int)loc.y + ")";
        Call.sendChatMessage(msgHander + markString.get(type) +"：[red]"+location);
    }

    public void showEffect(){
        if (type == markType.MARK) {
            Effect showEffect = Fx.arcMarker;
            showEffect.arcCreate(loc.x, loc.y,0f, Color.red,null);
        }
        else if(type == markType.GATHER){
            Effect showEffect = Fx.arcGatherMarker;
            showEffect.arcCreate(loc.x, loc.y,0f, Color.red,null);
        }
        else if(type == markType.ATTACK){
            Effect showEffect = Fx.arcAttackMarker;
            showEffect.arcCreate(loc.x, loc.y,0f, Color.red,null);
        }
    }

    public void reviewEffect(){
        if(control.input instanceof DesktopInput){
            ((DesktopInput) control.input).panning = true;
        }
        Core.camera.position.set(loc.x,loc.y);
        showEffect();
    }

}
