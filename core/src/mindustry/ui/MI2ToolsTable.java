package mindustry.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.net.*;
import mindustry.type.*;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static mindustry.content.UnitTypes.gamma;
import static mindustry.content.UnitTypes.poly;
import static mindustry.gen.Tex.underlineWhite;
import static mindustry.ui.Styles.*;


public class MI2ToolsTable extends Table{
    public int waveOffset = 0;
    private float fontScl = 0.6f;
    private boolean shown = true;
    private boolean[] showns = {true, true, true};

    private Table buttons, rulesTable, scriptButtons;

    private ImageButton.ImageButtonStyle imgStyle, imgToggleStyle;
    private TextButton.TextButtonStyle textStyle, textStyle2, textStyle3;

    private MapInfoDialog customrules;

    public MI2ToolsTable(){
        customrules = new MapInfoDialog();

        imgStyle = clearNonei;

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

        rebuild();
    }

    public void toggle(){
        shown = !shown;
        rebuild();
    }

    public void rebuild(){
        clear();

        float buttonSize = 72 * fontScl;
        float imgSize = buttonSize / 3f * 2f;

        button((shown ? "MI2Tools" : "MI2"), textStyle2, this::toggle).minWidth(shown ? (65f * 2f) : 65f).height(35).fillX();

        if(!shown) return;

        row();

        Table main = table().fillX().get();

        main.table(black6, buttons -> {
            int i = 0;
            addButton(buttons, Iconc.map, i++);
            addButton(buttons, Iconc.blockSpawn, i++);
            addButton(buttons, Iconc.edit, i++);
        }).fillX().row();

        main.table(body -> {
            body.background(black6);

            /* map rules 1 / game stats */
            body.collapser(t -> {
                Rules rules = state.rules;
                GameStats stats = state.stats;

                t.table(rulesStats -> {
                    rulesStats.table(rulesT -> {
                        addRule(rulesT, Iconc.statusBurning, rules.fire);
                        addRule(rulesT, Iconc.itemBlastCompound, rules.damageExplosions);
                        addRule(rulesT, Iconc.blockThoriumReactor, rules.reactorExplosions);
                        addRule(rulesT, Iconc.itemCopper, rules.unitAmmo);
                        addRule(rulesT, Iconc.blockMicroProcessor, rules.logicUnitBuild);

                        rulesT.row();

                        addRule(rulesT, Iconc.blockCoreShard, rules.unitCapVariable);
                        addRule(rulesT, Iconc.blockIlluminator, rules.lighting);
                        addRule(rulesT, Iconc.blockIncinerator, rules.coreIncinerates);
                        addRule(rulesT, Iconc.paste, rules.schematicsAllowed);
                    });

                    rulesStats.table(statsT -> {
                        Table t1 = statsT.table().left().fillX().get();
                        statsT.row();
                        Table t2 = statsT.table().left().fillX().get();

                        addStat(t1, "Time", () -> {
                            Saves.SaveSlot save = control.saves.getCurrent();
                            return save != null ? "" + save.getPlayTime() : "";
                        });

                        Table table1 = t2.table().fill().get();
                        Table table2 = t2.table().padLeft(4).fill().get();

                        addStat(table1, "Kill", () -> "" + stats.enemyUnitsDestroyed).row();
                        addStat(table1, "Builds", () -> "" + stats.buildingsBuilt).row();
                        addStat(table1, "Descons", () -> "" + stats.buildingsDeconstructed).row();

                        addStat(table2, "Destroy", () -> "" + stats.buildingsDestroyed).row();

                        addStat(table2, "UnitCap", () -> "" + rules.unitCap).row();
                        addStat(table2, "World", () -> {
                            if(world != null) return world.width() + "x" + world.height();
                            return "";
                        }).row();
                    }).padLeft(2).left().fillX();
                }).left();
            }, () -> showns[0]).left();

            body.row();

            /* wave info */
            body.collapser(t -> {
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

                float waveImagSize = iconSmall * fontScl;
                float waveFontScl = 0.9f * fontScl;

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
            body.image().height(3).fillX().update(i -> i.setColor(player.team().color));
            body.row();

            /* Script buttons */
            body.collapser(t -> {
                t.button("+", textStyle, () -> {
                    fontScl += 0.035;
                    rebuild();
                }).height(buttonSize).growX();

                t.button(Icon.refreshSmall, imgStyle, () -> {
                    Call.sendChatMessage("/sync");
                }).height(buttonSize).growX();

                t.button(Icon.map, imgStyle, imgSize, () -> {
                    customrules.show();
                }).height(buttonSize).growX();

                t.button(new TextureRegionDrawable(poly.uiIcon), imgStyle, imgSize, () -> {
                    player.buildDestroyedBlocks();
                }).height(buttonSize).growX();

                t.button(new TextureRegionDrawable(gamma.uiIcon), imgStyle, imgSize, () -> {
                    if(control.input instanceof DesktopInput){
                        ((DesktopInput) control.input).panning = true;
                    }
                    Core.camera.position.set(ui.chatfrag.getArcMarkerX(),ui.chatfrag.getArcMarkerY());

                }).height(buttonSize).growX();

                t.button(Icon.modeAttack, imgToggleStyle, imgSize, () -> {
                    boolean at = settings.getBool("autotarget");
                    settings.put("autotarget", !at);
                }).height(buttonSize).growX().checked(settings.getBool("autotarget"));

                if(mobile){
                    t.button(Icon.eyeSmall, imgToggleStyle, () -> {
                        boolean view = settings.getBool("viewMode");
                        if(view) Core.camera.position.set(player);
                        settings.put("viewMode", !view);
                    }).height(buttonSize).growX().checked(settings.getBool("viewMode"));
                }

                t.button("-", textStyle, () -> {
                    fontScl -= 0.035;
                    rebuild();
                }).height(buttonSize).growX();
            }, () -> showns[2]).left().fillX();

        }).left();

    }

    private void addButton(Table buttons, char iconc, int index){
        buttons.button("" + iconc, textStyle3, () -> {
            showns[index] = !showns[index];
        }).growX().checked(showns[index]);
    }

    private Table addRule(Table rulesTable, char iconc, Boolean enable){
        rulesTable.add("" + iconc).left().padLeft(2).get().setColor(enable ? Color.white : Color.red);
        return rulesTable;
    }

    private Table addRules2(Table rulesTable, String l, Prov<CharSequence> getNum){
        rulesTable.table(ruleT -> {
            ruleT.add(l, fontScl).row();
            ruleT.label(getNum).center().get().setFontScale(fontScl);
        }).padLeft(3);

        return rulesTable;
    }

    private Table addStat(Table statsTable, String label, Prov<CharSequence> getNum){
        statsTable.add(label + ": ", fontScl).left();
        statsTable.label(getNum).left().get().setFontScale(fontScl);
        return statsTable;
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
