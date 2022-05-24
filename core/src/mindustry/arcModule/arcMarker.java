package mindustry.arcModule;

import arc.*;
import arc.graphics.Color;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.input.DesktopInput;

import static mindustry.Vars.*;

public class arcMarker{
    public enum markType{MARK,GATHER,ATTACK,DEFENCE,ITEMS,WAVES}
    public markType type = markType.MARK;
    public Vec2 loc = new Vec2(0,0);
    public float time = 0;

    private String msgHander = "[ARC"+arcVersion+"]";

    public void showEffect(){
        if (type == markType.MARK) {
            Effect showEffect = Fx.arcMarker;
            showEffect.arcCreate(loc.x, loc.y,0f, Color.red,null);
        }
        else if(type == markType.GATHER){
            Effect showEffect = Fx.arcGatherMarker;
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

    public void shareInfo(){
        if (type == markType.MARK) {
            Call.sendChatMessage(msgHander+"标记了一处地点[red]("+loc.x+","+loc.y+")");
        }
    }

}
