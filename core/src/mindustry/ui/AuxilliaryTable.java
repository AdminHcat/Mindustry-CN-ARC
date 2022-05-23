package mindustry.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.core.UI;
import mindustry.editor.MapInfoDialog;
import mindustry.editor.WaveInfoDialog;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.input.MobileInput;
import mindustry.net.Packets;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;

import java.util.Objects;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static mindustry.content.UnitTypes.gamma;
import static mindustry.gen.Tex.*;
import static mindustry.gen.Tex.underlineWhite;
import static mindustry.ui.Styles.*;


public class AuxilliaryTable extends Table {
    private boolean show = true;
    private boolean[] showns = {false, false, false, true ,mobile};
    public int waveOffset = 0;
    private float fontScl = 0.6f;
    private float buttonSize = 50f;
    private float imgSize = 33f;


    private ImageButton.ImageButtonStyle imgStyle, imgToggleStyle;
    private TextButton.TextButtonStyle textStyle, textStyle2, textStyle3;

    private MapInfoDialog mapInfoDialog;
    private WaveInfoDialog waveInfoDialog;


    public AuxilliaryTable() {
        textStyle = new TextButton.TextButtonStyle() {{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;

        }};
        imgToggleStyle = new ImageButton.ImageButtonStyle(imgStyle){{
            up = none;
            over = underlineWhite;
            down = underlineWhite;
            checked = underlineWhite;
        }};

        textStyle = new TextButton.TextButtonStyle(logict){{
            up = underlineWhite;
            over = underlineWhite;
            down = underlineWhite;
        }};

        textStyle2 = new TextButton.TextButtonStyle(flatBordert){{
            up = underlineWhite;
            over = accentDrawable;
            down = accentDrawable;
            checked = underlineWhite;
        }};

        textStyle3 = new TextButton.TextButtonStyle(flatBordert){{
            up = none;
            over = accentDrawable;
            down = accentDrawable;
            checked = underlineWhite;
        }};


        toggle();
    }

    public void toggle(){
        show = !show;
        if(show) buildShow();
        else buildHide();
    }

    void buildHide(){
        clear();
        button("[cyan]辅助器", textStyle2, this::toggle).width(50f).height(35);
    }

    void buildShow() {

        Table hander = table().fillX().get();

        hander.button("[acid]辅助器", textStyle2, this::toggle).width(50f).height(35);
        hander.button(Icon.map, imgStyle, () -> showns[0] = !showns[0]).size(50f).tooltip("地图信息");
        hander.button(Icon.waves, imgStyle, () -> showns[1] = !showns[1]).size(50f).tooltip("波次信息");
        hander.button(gamma.emoji(), textStyle2, () -> showns[2] = !showns[2]).size(50f).tooltip("玩家AI");
        hander.button(gamma.emoji(), textStyle2, () -> showns[3] = !showns[3]).size(50f).tooltip("控制器");
        hander.button(gamma.emoji(), textStyle2, () -> showns[4] = !showns[4]).size(50f).tooltip("<手机>控制器").visible(mobile);

        Table main = table().fillX().get();

        main.table(body -> {
            body.background(black6);

            /* 地图信息界面 */
            body.collapser(t -> {
                t.button(Icon.map, imgStyle, () -> mapInfoDialog.show()).size(50f).tooltip("地图信息");
            }, () -> showns[0]).left();

            body.row();

            /* 波次信息界面 */
            body.collapser(t -> {
                t.button(Icon.waves, imgStyle, () -> waveInfoDialog.show()).size(50f).tooltip("波次信息");

                t.table(buttons -> {
                    buttons.label(() -> "Wave " + (state.wave + waveOffset)).padLeft(3).get().setFontScale(fontScl);

                    buttons.button("<<", textStyle, () -> {
                        waveOffset -= 10;
                        if(state.wave + waveOffset - 1 < 0) waveOffset = -state.wave + 1;
                    }).size(buttonSize);

                    buttons.button("<", textStyle, () -> {
                        waveOffset -= 1;
                        if(state.wave + waveOffset - 1 < 0) waveOffset = -state.wave + 1;
                    }).size(buttonSize);

                    buttons.button("O", textStyle, () -> {
                        waveOffset = 0;
                    }).size(buttonSize);

                    buttons.button(">", textStyle, () -> {
                        waveOffset += 1;
                    }).size(buttonSize);

                    buttons.button(">>", textStyle, () -> {
                        waveOffset += 10;
                    }).size(buttonSize);

                    buttons.button("Go", textStyle, () -> {
                        state.wave += waveOffset;
                        waveOffset = 0;
                    }).size(buttonSize);

                    buttons.button("RW", textStyle, () -> {
                        for(int rw = waveOffset; rw > 0; rw--){
                            if(net.client() && player.admin){
                                Call.adminRequest(player, Packets.AdminAction.wave);
                            }else{
                                logic.skipWave();
                            }
                            waveOffset = 0;
                        }
                    }).size(buttonSize);

                    buttons.button(Icon.link, imgStyle, imgSize, () -> {
                        String message = arcShareWaveInfo(state.wave + waveOffset);
                        int seperator = 145;
                        for(int i = 0; i < message.length() / (float)seperator; i++){
                            Call.sendChatMessage(message.substring(i * seperator, Math.min(message.length(), (i + 1) * seperator)));
                        }
                    }).size(buttonSize).disabled(!state.rules.waves && !settings.getBool("arcShareWaveInfo"));

                }).left().row();

                float waveImagSize = iconSmall;
                float waveFontScl = 0.9f;

                t.table(waveInfo -> {
                    waveInfo.update(() -> {
                        waveInfo.clear();

                        int curInfoWave = state.wave - 1 + waveOffset;
                        for(SpawnGroup group : state.rules.spawns){
                            int amount = group.getSpawned(curInfoWave);
                            if(amount > 0){
                                float shield = group.getShield(curInfoWave);
                                StatusEffect effect = group.effect;
                                waveInfo.table(groupT -> {
                                    groupT.image(group.type.uiIcon).size(waveImagSize).row();

                                    groupT.add("" + amount, waveFontScl).center();
                                    groupT.row();

                                    if(shield > 0f) groupT.add("" + UI.formatAmount((long)shield), waveFontScl).center();
                                    groupT.row();
                                    if(effect != null && effect != StatusEffects.none) groupT.image(effect.uiIcon).size(waveImagSize);
                                }).padLeft(4).top();
                            }
                        }
                    });
                }).left();
            }, () -> showns[1]).left();

            body.row();

            /* 玩家AI */
            body.collapser(t -> {
            }, () -> showns[2]).left();

            body.row();

            /* 控制器 */
            body.collapser(t -> {
                t.button(Icon.modeAttack, imgToggleStyle, imgSize, () -> {
                    settings.put("autotarget", !settings.getBool("autotarget"));
                }).height(buttonSize).growX().checked(settings.getBool("autotarget")).tooltip("自动瞄准");

            }, () -> showns[3]).left();

            body.row();

            /* <手机>控制器 */
            body.collapser(t -> {

            }, () -> showns[4]).left();

            body.row();

        }).left();

    }

    private String arcShareWaveInfo(int waves){
        if(!state.rules.waves) return " ";
        StringBuilder builder = new StringBuilder();
        builder.append("[ARC").append(arcVersion).append("]");
        builder.append("标记了第").append(waves).append("波");
        if(waves < state.wave){
            builder.append("。");
        }else{
            if(waves > state.wave){
                builder.append("，还有").append(waves - state.wave).append("波");
            }
            int timer = (int)(state.wavetime + (waves - state.wave) * state.rules.waveSpacing);
            builder.append("[").append(fixedTime(timer)).append("]。");
        }

        if(state.rules.attackMode){
            int sum = Math.max(state.teams.present.sum(t -> t.team != player.team() ? t.cores.size : 0), 1) + Vars.spawner.countSpawns();
            builder.append("其包含(×").append(sum).append(")");
        }else{
            builder.append("其包含(×").append(Vars.spawner.countSpawns()).append("):");
        }
        for(SpawnGroup group : state.rules.spawns){
            if(group.getSpawned(waves - 1) > 0){
                builder.append((char)Fonts.getUnicode(group.type.name)).append("(");
                if(group.effect != StatusEffects.invincible && group.effect != StatusEffects.none && group.effect != null){
                    builder.append((char)Fonts.getUnicode(group.effect.name)).append("|");
                }
                if(group.getShield(waves - 1) > 0){
                    builder.append(UI.whiteformatAmount((int)group.getShield(waves - 1))).append("|");
                }
                builder.append(group.getSpawned(waves - 1)).append(")");
            }
        }
        return builder.toString();
    }

    private String fixedTime(int timer){
        StringBuilder str = new StringBuilder();
        int m = timer / 60 / 60;
        int s = timer / 60 % 60;
        int ms = timer % 60;
        if(m > 0){
            str.append(m).append(": ");
            if(s < 10){
                str.append("0");
            }
            str.append(s).append("min");
        }else{
            str.append(s).append(".").append(ms).append('s');
        }
        return str.toString();
    }

}