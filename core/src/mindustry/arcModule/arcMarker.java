package mindustry.arcModule;

import arc.*;
import arc.func.*;
import arc.graphics.Color;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.type.*;
import mindustry.world.*;

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
            showEffect.at(loc.x, loc.y, Color.red);
        }
        else if(type == markType.GATHER){
            Effect showEffect = Fx.arcGatherMarker;
            showEffect.at(loc.x, loc.y, Color.red);
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
