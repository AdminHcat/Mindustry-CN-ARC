package mindustry.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
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

import mindustry.net.Packets;

import mindustry.type.StatusEffect;


import static arc.Core.settings;
import static mindustry.Vars.*;
import static mindustry.content.UnitTypes.gamma;
import static mindustry.content.UnitTypes.poly;
import static mindustry.gen.Tex.*;
import static mindustry.gen.Tex.underlineWhite;
import static mindustry.ui.Styles.*;


public class AuxilliaryTable extends Table {
    private boolean show = true;
    private boolean[] showns = {false, false, false, false ,mobile};
    public int waveOffset = 0;
    private float fontScl = 0.6f;
    private float buttonSize = 50f;
    private float imgSize = 33f;

    private float handerSize = 40f;
    private float tableSize = 30f;

    private ImageButton.ImageButtonStyle imgStyle, imgToggleStyle;
    private TextButton.TextButtonStyle textStyle, textStyle2, textStyle3;

    private ImageButton.ImageButtonStyle ImageHander;
    private TextButton.TextButtonStyle textHander;

    private MapInfoDialog mapInfoDialog = new MapInfoDialog();
    private WaveInfoDialog waveInfoDialog = new WaveInfoDialog();


    public AuxilliaryTable() {

        textHander = new TextButton.TextButtonStyle(fullTogglet){{
            up = none;
            over = accentDrawable;
            down = underlineWhite;
            checked = underlineWhite;
        }};

        ImageHander = new ImageButton.ImageButtonStyle(clearNonei){{
            up = none;
            over = accentDrawable;
            down = none;
            checked = underlineWhite;
        }};

        imgStyle = clearNonei;

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
        button("[cyan]辅助器", textHander, this::toggle).width(80f).height(handerSize).tooltip("开启辅助器");
    }

    void buildShow() {
        clear();
        Table hander = table().fillX().get();

        hander.button("[acid]辅助器", textHander, this::toggle).width(6f).height(handerSize).tooltip("关闭辅助器");
        hander.button(Icon.map, ImageHander, () -> showns[0] = !showns[0]).size(handerSize).tooltip("地图信息");
        hander.button(Icon.waves, ImageHander, () -> showns[1] = !showns[1]).size(handerSize).tooltip("波次信息");
        hander.button(gamma.emoji(), textHander, () -> showns[2] = !showns[2]).size(handerSize).tooltip("玩家AI");
        hander.button(gamma.emoji(), textHander, () -> showns[3] = !showns[3]).size(handerSize).tooltip("控制器");
        hander.button(gamma.emoji(), textHander, () -> showns[4] = !showns[4]).size(handerSize).tooltip("<手机>控制器").visible(mobile);

        row();

        Table main = table().fillX().get();

        main.table(body -> {
            //body.background(black6);

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

                    buttons.button("Go", textStyle, () -> {
                        state.wave += waveOffset;
                        waveOffset = 0;
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

                                StringBuilder waveUI = new StringBuilder();
                                waveUI.append("[cyan]单位：[white]").append(group.type.localizedName).append(group.type.uiIcon).append("\n");
                                waveUI.append("[cyan]波次：[white]").append(group.begin).append("+").append(group.spacing).append("×n").append("->").append(group.end).append("\n");
                                waveUI.append("[cyan]数量：[white]").append(group.unitAmount).append("+").append(group.unitScaling).append("×n").append("->").append(group.max).append("\n");
                                waveUI.append("[cyan]护盾：[white]").append(group.shields).append("+").append(group.shieldScaling).append("×n").append("\n");
                                if(group.effect!=null ||group.items!=null){
                                    waveUI.append("[cyan]属性：[white]");
                                    if (group.effect!=null) waveUI.append(group.effect.uiIcon);
                                    if (group.items!=null) waveUI.append(group.items.item.uiIcon).append(":").append(group.items.amount);
                                    waveUI.append("\n");
                                }
                                if(group.payloads!=null){
                                    waveUI.append("[cyan]载荷：[white]");
                                    group.payloads.each(payload-> waveUI.append(payload.uiIcon));
                                    waveUI.append("\n");
                                }

                                waveInfo.table(groupT -> {
                                    groupT.image(group.type.uiIcon).size(tableSize).row();

                                    groupT.add("" + amount, tableSize).center();
                                    groupT.row();

                                    if(shield > 0f) groupT.add("" + UI.formatAmount((long)shield), waveFontScl).center();
                                    groupT.row();
                                    if(effect != null && effect != StatusEffects.none) groupT.image(effect.uiIcon).size(tableSize);
                                }).padLeft(4).top().tooltip(waveUI.toString());

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
                t.button(Icon.refreshSmall, imgStyle, () -> {
                    Call.sendChatMessage("/sync");
                }).height(buttonSize).growX();

                t.button(new TextureRegionDrawable(poly.uiIcon), imgStyle, imgSize, () -> {
                    player.buildDestroyedBlocks();
                }).height(buttonSize).growX();

                t.button(new TextureRegionDrawable(gamma.uiIcon), imgStyle, imgSize, () -> {
                    if (ui.chatfrag.marker.size>0) ui.chatfrag.marker.getFrac(ui.chatfrag.marker.size-1).reviewEffect();
                }).height(buttonSize).growX();

                t.button(Icon.modeAttack, imgToggleStyle, imgSize, () -> {
                    boolean at = settings.getBool("autotarget");
                    settings.put("autotarget", !at);
                }).height(buttonSize).growX().checked(settings.getBool("autotarget"));

            }, () -> showns[3]).left();

            body.row();

            /* <手机>控制器 */
            body.collapser(t -> {
                t.button(Icon.eyeSmall, imgToggleStyle, () -> {
                    boolean view = settings.getBool("viewMode");
                    if(view) Core.camera.position.set(player);
                    settings.put("viewMode", !view);
                }).height(buttonSize).growX().checked(settings.getBool("viewMode"));

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