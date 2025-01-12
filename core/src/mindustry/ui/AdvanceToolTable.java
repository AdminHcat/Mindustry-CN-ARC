package mindustry.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.ai.types.BuilderAI;
import mindustry.ai.types.MinerAI;
import mindustry.ai.types.RepairAI;
import mindustry.arcModule.arcMarker;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.units.AIController;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Icon;
import mindustry.gen.Payloadc;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.input.DesktopInput;
import mindustry.input.MobileInput;
import mindustry.type.Item;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;

import java.util.Arrays;
import java.util.Objects;

import static mindustry.Vars.*;
import static mindustry.content.UnitTypes.*;
import static mindustry.ui.Styles.*;


public class AdvanceToolTable extends Table {
    private boolean show = false;
    private boolean showGameMode = false, showResTool = false, showTeamChange = false, showUnitStat = false;

    //unitFactory
    private int unitCount = 1;
    private float unitRandDst = 1f;
    private Vec2 unitLoc = new Vec2(0, 0);
    private ObjectMap<StatusEffect, Float> unitStatus = new ObjectMap<>();
    private final float[] statusTime = {10, 30f, 60f, 120f, 180f, 300f, 600f, 900f, 1200f, 1500f, 1800f, 2700f, 3600f, Float.MAX_VALUE};
    private Unit spawnUnit = UnitTypes.emanate.create(Team.sharded);
    private Boolean showUnitSelect = false;
    private Boolean showUnitPro = false;
    private Boolean showStatesEffect = false;
    private Boolean showItems = false;
    private Boolean showPayload = false;
    private boolean showSelectPayload = false;
    private Boolean showPayloadBlock = false;

    public AdvanceToolTable() {
        rebuild();
    }

    void rebuild() {
        clear();
        if (!show) {
            table(t -> {
                t.background(Tex.buttonEdge3);
                t.button("[cyan]工具箱", cleart, () -> {
                    show = !show;
                    rebuild();
                }).left().width(70).expandX();

            }).left();
        } else {
            table(t -> {
                if (mobile) {
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.button("指挥", cleart, () -> {
                            control.input.commandMode = !control.input.commandMode;
                        }).width(80f);

                        tBox.button("取消", cleart, () -> {
                            if (control.input instanceof MobileInput input) input.arcClearPlans();
                        }).width(80f);
                    }).left().row();
                }

                if (showResTool) {
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("资源：").left();
                        tBox.button(Items.copper.emoji() + "[acid]+", cleart, () -> {
                            for (Item item : content.items())
                                player.core().items.set(item, player.core().storageCapacity);
                        }).width(40f).tooltip("[acid]填满核心的所有资源");
                        tBox.button(Items.copper.emoji() + "[red]-", cleart, () -> {
                            for (Item item : content.items()) player.core().items.set(item, 0);
                        }).width(40f).tooltip("[acid]清空核心的所有资源");
                    }).left().row();
                }

                if (showUnitStat) {
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("单位：").left();
                        tBox.button(UnitTypes.gamma.emoji() + "[acid]+", cleart, () -> {
                            Unit cloneUnit = cloneExactUnit(player.unit());
                            cloneUnit.set(player.x + Mathf.range(8f), player.y + Mathf.range(8f));
                            cloneUnit.add();
                        }).width(40f).tooltip("[acid]克隆");
                        tBox.button(UnitTypes.gamma.emoji() + "[red]×", cleart, () -> player.unit().kill()).width(40f).tooltip("[red]自杀");
                        tBox.button(Icon.waves, clearNonei, this::unitSpawnMenu).width(40f).tooltip("[acid]单位工厂-ARC");
                    }).left().row();
                }

                if (showTeamChange) {
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("队伍：").left();
                        for (Team team : Team.baseTeams) {
                            tBox.button(String.format("[#%s]%s", team.color, team.localized()), flatToggleMenut, () -> player.team(team))
                                    .checked(b -> player.team() == team).size(30f, 30f);
                        }
                        tBox.button("[violet]+", flatToggleMenut, this::teamChangeMenu).checked(b -> {
                            return !Arrays.asList(Team.baseTeams).contains(player.team());
                        }).tooltip("[acid]更多队伍选择").size(30f, 30f);

                    }).left().row();
                }

                if (showGameMode) {
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("规则：").left();

                        tBox.button("解禁", cleart, () -> {
                            state.rules.bannedBlocks.clear();
                        }).tooltip("[acid]解除所有被禁建筑(不可还原，服务器可以sync)").width(50f);

                        tBox.button("无限火力", flatToggleMenut, () -> {
                            player.team().rules().cheat = !player.team().rules().cheat;
                        }).checked(b -> player.team().rules().cheat).tooltip("[acid]开关自己队的无限火力").size(90f, 30f);

                        tBox.button("编辑器", flatToggleMenut, () -> {
                            state.rules.editor = !state.rules.editor;
                        }).checked(b -> state.rules.editor).size(70f, 30f);

                        tBox.button("沙盒", flatToggleMenut, () -> {
                            state.rules.infiniteResources = !state.rules.infiniteResources;
                        }).checked(b -> state.rules.infiniteResources).size(50f, 30f);

                        if (Core.settings.getBool("developmode")) {
                            tBox.button("创世神", flatToggleMenut, () -> {
                                Core.settings.put("worldCreator", !Core.settings.getBool("worldCreator"));
                            }).checked(b -> Core.settings.getBool("worldCreator")).size(70f, 30f);
                        }
                    }).left().row();
                }

                t.row();
                t.table(mainBox -> {
                    mainBox.background(Tex.buttonEdge3);
                    mainBox.button("[red]工具箱", cleart, () -> {
                        show = !show;
                        rebuild();
                    }).width(70f);
                    mainBox.button((showResTool ? "[cyan]" : "[acid]") + "资源", cleart, () -> {
                        showResTool = !showResTool;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showUnitStat ? "[cyan]" : "[acid]") + "单位", cleart, () -> {
                        showUnitStat = !showUnitStat;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showGameMode ? "[cyan]" : "[acid]") + "规则", cleart, () -> {
                        showGameMode = !showGameMode;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showTeamChange ? "[cyan]" : "[acid]") + "队伍", cleart, () -> {
                        showTeamChange = !showTeamChange;
                        rebuild();
                    }).width(50f);

                }).left();


            }).left();
        }
    }

    private void teamChangeMenu() {
        BaseDialog dialog = new BaseDialog("队伍选择器");
        Table selectTeam = new Table().top();

        dialog.cont.pane(td -> {
            for (Team team : Team.all) {
                if (team.id % 10 == 6) {
                    td.row();
                    td.add("队伍：" + team.id + "~" + (team.id + 10));
                }
                ImageButton button = new ImageButton(Tex.whiteui, Styles.clearTogglei);
                button.getStyle().imageUpColor = team.color;
                button.margin(10f);
                button.resizeImage(40f);
                button.clicked(() -> {
                    player.team(team);
                    dialog.hide();
                });
                button.update(() -> button.setChecked(player.team() == team));
                td.add(button);
            }
        });

        dialog.add(selectTeam).center();
        dialog.row();
        dialog.addCloseButton();
        dialog.show();
    }

    private void unitSpawnMenu() {
        BaseDialog unitFactory = new BaseDialog("单位工厂-ARC");

        Table table = unitFactory.cont;
        Runnable[] rebuild = {null};
        rebuild[0] = () -> {
            table.clear();
            /* Unit */
            table.label(() -> "目标单位：" + spawnUnit.type.emoji() + spawnUnit.type.localizedName);

            table.row();

            Table r = table.table().center().bottom().get();
            r.add("数量：");
            r.field("" + unitCount, text -> {
                unitCount = Integer.parseInt(text);
            }).valid(Strings::canParsePositiveInt).maxTextLength(4).get();

            table.row();

            r.add("生成范围：");
            r.field(Strings.autoFixed(unitRandDst, 3), text -> {
                unitRandDst = Float.parseFloat(text);
            }).valid(Strings::canParsePositiveFloat).tooltip("在目标点附近的这个范围内随机生成").maxTextLength(8).get();
            r.add("格");
            table.row();

            table.table(t -> {
                t.add("坐标：x= ");
                t.field(Strings.autoFixed(unitLoc.x, 2), text -> {
                    unitLoc.x = Float.parseFloat(text);
                }).valid(Strings::canParseFloat).maxTextLength(8).get();
                t.add("  ,y= ");
                t.field(Strings.autoFixed(unitLoc.y, 2), text -> {
                    unitLoc.y = Float.parseFloat(text);
                }).valid(Strings::canParseFloat).maxTextLength(8).get();
                t.button(gamma.emoji(), () -> {
                    unitLoc.x = player.x / tilesize;
                    unitLoc.y = player.y / tilesize;
                    rebuild[0].run();
                }).tooltip("选择玩家当前位置：" + Strings.autoFixed(player.x / tilesize, 2) + "," + Strings.autoFixed(player.y / tilesize, 2)).height(50f);
                t.button(StatusEffects.blasted.emoji(), () -> {
                    if(arcMarker.markList.size==0) return;
                    unitLoc.x = arcMarker.markList.peek().loc.x;
                    unitLoc.y = arcMarker.markList.peek().loc.y;
                    rebuild[0].run();
                }).tooltip(arcMarker.markList.size==0?"[red]未标记":"选择上个标记点：" + arcMarker.markList.peek().loc.x + "," + arcMarker.markList.peek().loc.y).height(50f);
            });

            table.row();

            table.button(Blocks.tetrativeReconstructor.emoji() + "[cyan]单位状态重构厂", () -> unitFabricator(spawnUnit)).fillX();

            table.row();

            table.button("[orange]生成！", Icon.modeAttack, () -> {
                for (var n = 0; n < unitCount; n++) {
                    Tmp.v1.rnd(Mathf.random(unitRandDst * tilesize));
                    Unit unit = cloneUnit(spawnUnit);
                    unit.set(unitLoc.x * tilesize + Tmp.v1.x, unitLoc.y * tilesize + Tmp.v1.y);
                    unitStatus.each((status, statusDuration) -> {
                        unit.apply(status, statusDuration * 60f);
                    });
                    unit.add();
                }
                if (control.input instanceof DesktopInput input) {
                    input.panning = true;
                }
                Core.camera.position.set(unitLoc.x * tilesize, unitLoc.y * tilesize);
                unitFactory.closeOnBack();
            }).fillX();
        };
        rebuild[0].run();

        unitFactory.addCloseButton();
        unitFactory.show();
    }

    private void unitFabricator(Unit unit) {
        BaseDialog rebuildFabricatorTable = new BaseDialog("单位加工车间");
        Table table = new Table();

        ScrollPane pane = new ScrollPane(table);

        Runnable[] rebuildTable = {null};
        rebuildTable[0] = () -> {
            table.clear();

            table.button("加工单位：" + unit.type.emoji(), showUnitSelect ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                showUnitSelect = !showUnitSelect;
                rebuildTable[0].run();
            }).fillX().minWidth(400f).row();

            if (showUnitSelect) {
                table.table(list -> {
                    int i = 0;
                    for (UnitType units : content.units()) {
                        if (i++ % 8 == 0) list.row();
                        list.button(units.emoji(), cleart, () -> {
                            if (unit.type != units) {
                                changeUnitType(unit, units);
                                rebuildTable[0].run();
                            }
                            showUnitSelect = !showUnitSelect;
                            rebuildTable[0].run();
                        }).tooltip(units.localizedName).size(50f);
                    }
                }).row();
            }

            table.button("[#" + unit.team.color + "]单位属性", showUnitPro ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                showUnitPro = !showUnitPro;
                rebuildTable[0].run();
            }).fillX().row();
            if (showUnitPro) {
                table.table(t -> {
                    t.table(tt -> {
                        tt.add("[red]血：");
                        tt.field(Strings.autoFixed(unit.health, 1), text -> unit.health = Float.parseFloat(text)).valid(Strings::canParsePositiveFloat);
                        tt.add("[yellow]盾：");
                        tt.field(Strings.autoFixed(unit.shield, 1), text -> unit.shield = Float.parseFloat(text)).valid(Strings::canParsePositiveFloat);
                    }).row();
                    t.table(tt -> {
                        tt.add("队伍：");
                        tt.field(String.valueOf(unit.team.id), text -> {
                            unit.team = Team.get(Integer.parseInt(text));
                        }).valid(text -> {
                            return Strings.canParsePositiveInt(text) && Integer.parseInt(text) < Team.all.length;
                        }).maxTextLength(4).get();
                        for (Team team : Team.baseTeams) {
                            tt.button("[#" + team.color + "]" + team.localized(), cleart, () -> {
                                unit.team = team;
                                rebuildTable[0].run();
                            }).size(30, 30).color(team.color);
                        }
                        tt.button("[violet]+", cleart, () -> {
                            BaseDialog selectTeamDialog = new BaseDialog("队伍选择器");
                            Table selectTeam = new Table().top();
                            selectTeamDialog.cont.pane(td -> {
                                for (Team team : Team.all) {
                                    if (team.id % 10 == 6) {
                                        td.row();
                                        td.add("队伍：" + team.id + "~" + (team.id + 9));
                                    }
                                    ImageButton button = new ImageButton(Tex.whiteui, Styles.clearTogglei);
                                    button.getStyle().imageUpColor = team.color;
                                    button.margin(10f);
                                    button.resizeImage(40f);
                                    button.clicked(() -> {
                                        unit.team = team;
                                        selectTeamDialog.hide();
                                        rebuildTable[0].run();
                                    });
                                    button.update(() -> button.setChecked(unit.team == team));
                                    td.add(button);
                                }
                            });

                            selectTeamDialog.add(selectTeam).center();
                            selectTeamDialog.row();
                            selectTeamDialog.addCloseButton();
                            selectTeamDialog.show();
                        }).tooltip("[acid]更多队伍选择").center().width(50f).row();
                    });
                }).row();
            }

            StringBuilder unitStatusText = new StringBuilder("单位状态 ");
            for (StatusEffect effects : content.statusEffects()) {
                if (unitStatus.containsKey(effects)) unitStatusText.append(effects.emoji());
            }
            table.button(unitStatusText.toString(), showStatesEffect ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                showStatesEffect = !showStatesEffect;
                rebuildTable[0].run();
            }).fillX().row();

            if (showStatesEffect) {
                table.table(t -> {
                    t.table(list -> {
                        int i = 0;
                        for (StatusEffect effects : content.statusEffects()) {
                            if (i++ % 8 == 0) list.row();
                            list.button(effects.emoji(), squareTogglet, () -> {
                                if (unitStatus.get(effects) == null) unitStatus.put(effects, 600f);
                                else unitStatus.remove(effects);
                                rebuildTable[0].run();
                            }).size(50f).color(unitStatus.get(effects) == null ? Color.gray : Color.white).tooltip(effects.localizedName);
                        }
                    }).top().center();

                    t.row();

                    t.table(list -> {
                        for (StatusEffect effects : content.statusEffects()) {
                            if (!unitStatus.containsKey(effects)) continue;
                            list.add(effects.emoji() + effects.localizedName + " ");
                            if (effects.permanent) {
                                list.add("<永久buff>");
                            } else {
                                TextField sField = list.field(checkInf(unitStatus.get(effects)), text -> {
                                    unitStatus.remove(effects);
                                    if (Objects.equals(text, "Inf")) {
                                        unitStatus.put(effects, Float.MAX_VALUE);
                                    } else unitStatus.put(effects, Float.parseFloat(text));
                                }).valid(text -> {
                                    return Objects.equals(text, "Inf") || Strings.canParsePositiveFloat(text);
                                }).tooltip("buff持续时间(单位：秒)").maxTextLength(10).get();
                                list.add("秒");
                                Slider sSlider = list.slider(0f, statusTime.length - 1f, 1f, statusTimeIndex(unitStatus.get(effects)), n -> {
                                    if (statusTimeIndex(unitStatus.get(effects)) != n) {//用一种神奇的方式阻止了反复更新
                                        sField.setText(checkInf(statusTime[(int) n]));
                                    }
                                    unitStatus.remove(effects);
                                    unitStatus.put(effects, statusTime[(int) n]);
                                }).get();
                                sField.update(() -> sSlider.setValue(statusTimeIndex(unitStatus.get(effects))));
                            }
                            list.row();
                        }
                    });
                }).fillX().row();
            }

            String unitItemText = "单位物品 ";
            if (unit.stack.amount > 0 && !showItems) {
                unitItemText += unit.stack.item.emoji() + " " + unit.stack.amount;
            }
            table.button(unitItemText, showItems ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                showItems = !showItems;
                rebuildTable[0].run();
            }).fillX().row();
            if (showItems) {
                table.table(pt -> {
                    pt.table(ptt -> {
                        int i = 0;
                        for (Item item : content.items()) {
                            ptt.button(item.emoji(), cleart, () -> {
                                unit.stack.item = item;
                                if (unit.stack.amount == 0) {
                                    unit.stack.amount = unit.itemCapacity();
                                }
                                rebuildTable[0].run();
                            }).size(50f).left().tooltip(item.localizedName);
                            if (++i % 6 == 0) ptt.row();
                        }
                    });
                    if (unit.stack.amount > 0) {
                        pt.row();
                        pt.table(ptt -> {
                            ptt.add(unit.stack.item.emoji() + " 数量：");
                            ptt.field(String.valueOf(unit.stack.amount), text -> {
                                unit.stack.amount = Integer.parseInt(text);
                            }).valid(value -> {
                                if (!Strings.canParsePositiveInt(value)) return false;
                                int val = Integer.parseInt(value);
                                return 0 < val && val <= unit.type.itemCapacity;
                            }).maxTextLength(4);
                            ptt.add("/ " + unit.type.itemCapacity + " ");
                            ptt.button(Icon.up, cleari, () -> {
                                unit.stack.amount = unit.type.itemCapacity;
                                rebuildTable[0].run();
                            }).tooltip("设置物品数量为单位最大容量");
                            ptt.button(Icon.down, cleari, () -> {
                                unit.stack.amount = 0;
                                rebuildTable[0].run();
                            }).tooltip("清空单位物品");
                        });
                    }
                }).row();
            }

            if (unit instanceof Payloadc pay) {
                StringBuilder unitPayloadText = new StringBuilder("单位背包 ");
                for (Payload payload : pay.payloads()) {
                    unitPayloadText.append(payload.content().emoji());
                }
                table.button(unitPayloadText.toString(), showPayload ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                    showPayload = !showPayload;
                    rebuildTable[0].run();
                }).fillX().checked(showPayload).row();
            }

            if (showPayload) {
                table.table(p -> {
                    if (unit instanceof Payloadc pay) {
                        p.table(pt -> {
                            pay.payloads().each(payload -> {
                                if (payload instanceof Payloadc payloadUnit) {
                                    pt.button(payload.content().emoji() + "[red]*", squareTogglet, () -> {
                                        pay.payloads().remove(payload);
                                        rebuildTable[0].run();
                                    }).color(payloadUnit.team().color).size(50f).left();
                                } else {
                                    pt.button(payload.content().emoji(), squareTogglet, () -> {
                                        pay.payloads().remove(payload);
                                        rebuildTable[0].run();
                                    }).size(50f).left();
                                }
                                if (pay.payloads().indexOf(payload) % 8 == 7) pt.row();
                            });
                        }).row();

                        p.button("载入单位 " + UnitTypes.mono.emoji(), showSelectPayload ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                            showSelectPayload = !showSelectPayload;
                            rebuildTable[0].run();
                        }).width(300f).row();

                        if (showSelectPayload) {
                            p.table(list -> {
                                int i = 0;
                                for (UnitType units : content.units()) {
                                    list.button(units.emoji(), () -> {
                                        pay.addPayload(new UnitPayload(units.create(unit.team)));
                                        rebuildTable[0].run();
                                    }).size(50f).tooltip(units.localizedName);
                                    if (++i % 8 == 0) list.row();
                                }
                            });
                            p.row();
                            p.table(pt -> {
                                pt.button("[cyan]自递归", () -> {
                                    pay.pickup(cloneUnit(unit));
                                    rebuildTable[0].run();
                                }).width(200f);
                                pt.button("?", () -> ui.showInfo("""
                                        使用说明：携带的单位存在一个序列，每个单位可以具备特定的属性。
                                        [cyan]自递归[white]是指根据当前的配置生成一个单位，并储存到载荷序列上
                                        这一单位具备所有目前设置的属性，包括buff、物品和载荷。
                                        合理使用自递归可以发掘无限的可能性
                                        [orange][警告]可能导致地图损坏！请备份地图后再使用！""")).size(50f);
                            }).row();
                        }

                        p.button("载入建筑 " + Blocks.surgeWallLarge.emoji(), showPayloadBlock ? Icon.upOpen : Icon.downOpen, Styles.togglet, () -> {
                            showPayloadBlock = !showPayloadBlock;
                            rebuildTable[0].run();
                        }).width(300f).row();

                        if (showPayloadBlock) {
                            p.table(list -> {
                                int i = 0;
                                for (Block payBlock : content.blocks()) {
                                    if (!payBlock.isVisible() || !payBlock.isAccessible() || payBlock.isFloor())
                                        continue;
                                    list.button(payBlock.emoji(), () -> {
                                        pay.addPayload(new BuildPayload(payBlock, unit.team));
                                        rebuildTable[0].run();
                                    }).size(50f).tooltip(payBlock.localizedName);
                                    if (++i % 8 == 0) list.row();
                                }
                            });
                        }
                    }
                }).fillX().row();
            }

            table.row();
            table.button("[red]重置出厂状态", () -> {
                resetUnitType(unit, unit.type);
                rebuildFabricatorTable.closeOnBack();
            }).fillX().row();
            //table.add("[orange]单位加工车间。 [white]Made by [violet]Lucky Clover\n").width(400f);
        };


        rebuildTable[0].run();
        rebuildFabricatorTable.cont.add(pane);
        rebuildFabricatorTable.addCloseButton();
        rebuildFabricatorTable.show();
    }

    private Unit cloneExactUnit(Unit unit) {
        Unit reUnit = unit.type.create(unit.team);
        reUnit.health = unit.health;
        reUnit.shield = unit.shield;
        reUnit.stack = unit.stack;

        for (StatusEffect effects : content.statusEffects()) {
            if (unit.getDuration(effects) > 0f) reUnit.apply(effects, unit.getDuration(effects));
        }

        if (unit instanceof Payloadc pay && reUnit instanceof Payloadc rePay) {
            pay.payloads().each(rePay::addPayload);
        }
        return reUnit;
    }

    private Unit cloneUnit(Unit unit) {
        Unit reUnit = unit.type.create(unit.team);
        reUnit.health = unit.health;
        reUnit.shield = unit.shield;
        reUnit.stack = unit.stack;

        if (unit instanceof Payloadc pay && reUnit instanceof Payloadc rePay) {
            pay.payloads().each(rePay::addPayload);
        }
        return reUnit;
    }

    private void resetUnitType(Unit unit, UnitType unitType) {
        unit.type = unitType;
        unit.health = unitType.health;
        unit.shield = 0;
        unit.stack.amount = 0;
        if (unit instanceof Payloadc pay) {
            pay.payloads().clear();
        }
        unitStatus.clear();
    }

    private void changeUnitType(Unit unit, UnitType unitType) {
        unit.type = unitType;
        unit.health = unitType.health;
        unit.shield = 0;
        if (unit.stack.amount > unit.itemCapacity()) {
            unit.stack.amount = unit.itemCapacity();
        }
        unitStatus.clear();
    }

    private String checkInf(float value) {
        if (value == Float.MAX_VALUE) {
            return "Inf";
        }
        return Strings.autoFixed(value, 1);
    }

    private int statusTimeIndex(float time) {
        for (int i = statusTime.length - 1; i >= 0; i--) {
            if (statusTime[i] <= time) {
                return i;
            }
        }
        return 0;
    }
    //已废弃，但可以用在其他地方
    /*
    private void unitStatusMenu(){
        BaseDialog unitStatusTable = new BaseDialog("单位状态重构厂");
        Table table = unitStatusTable.cont;

        Runnable[] rebuildStatus = {null};
        rebuildStatus[0] = () -> {
            table.clear();

            table.row();

            table.pane(list -> {
                int i = 0;
                for (StatusEffect effects : content.statusEffects()) {
                    if (i++ % 8 == 0) list.row();
                    list.button(effects.emoji(), () -> {
                        if(unitStatus.get(effects) == null)
                            unitStatus.put(effects, 10f);
                        else unitStatus.remove(effects);
                        rebuildStatus[0].run();
                    }).size(50f).color(unitStatus.get(effects) == null ? Color.red:Color.acid);
                }
            }).top().center();

            table.row();

            table.table(t-> {
                if(unitStatus.size > 0) {
                    unitStatus.each((status,statusDuration)->{
                        t.add(status.emoji() + status.localizedName+" ");
                        if(!status.permanent){
                            TextField sField;
                            sField = t.field(String.valueOf(statusDuration), text -> {
                                unitStatus.remove(status);
                                unitStatus.put(status, Float.parseFloat(text));
                            }).maxTextLength(10).valid(value -> Strings.canParsePositiveFloat(value)).get();

                            t.slider(0.125f, 10F, 0.125f, (float) Math.log10(statusDuration), n -> {
                                unitStatus.remove(status);
                                unitStatus.put(status, (float) Math.pow(10,n));
                                sField.setText(Strings.autoFixed((float) Math.pow(10,n),2));
                            });
                        }
                        t.row();
                    });
                }
            });

            table.row();
            table.button("[red]重置出厂状态",()->{unitStatus.clear();}).fillX();
        };
        rebuildStatus[0].run();
        unitStatusTable.addCloseButton();
        unitStatusTable.show();
    }

    private void showItems(){
        BaseDialog itemDialog = new BaseDialog("单位的百宝袋");
        Table table = itemDialog.cont;
        Runnable[] rebuildItems = {null};

        rebuildItems[0] = () -> {
            table.clear();
            table.table(p -> {

                p.table(pt->{
                    pt.button(Items.copper.emoji(),() -> {showSelectItems = !showSelectItems;rebuildItems[0].run();}).size(50f);
                    pt.add("数量：");
                    pt.field(String.valueOf(unitItemAmount), text -> {
                        unitItemAmount = Integer.parseInt(text);
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveInt(value)).get();
                    pt.add("/ " + spawning.itemCapacity+" ");
                    pt.add(unitItems==null?"":unitItems.emoji());
                });

                p.row();

                if(showSelectItems){
                    p.table(pt->{
                        int i = 0;
                        for (Item item : content.items()) {
                            pt.button(item.emoji(), () -> {
                                unitItems = item;
                                rebuildItems[0].run();
                            }).size(50f).left();
                            if (++i % 6 == 0) pt.row();
                        }
                        pt.button("[red]×", () -> {
                            unitItems = null;
                            rebuildItems[0].run();
                        }).size(50f).left();
                        pt.row();
                    }).fillX().row();
                }

                p.table(pt->{
                    int i = 0;
                    for (Unit payload:unitPayload){
                        if((payload instanceof Payloadc pay && pay.hasPayload())|| payload.hasItem()){
                            pt.button(payload.type.emoji() + "[red]*",
                                    () -> {unitPayload.remove(payload);
                                        rebuildItems[0].run();
                                    }).size(50f).left();
                        }
                        else{
                            pt.button(payload.type.emoji(),
                                    () -> {unitPayload.remove(payload);
                                        rebuildItems[0].run();
                                    }).size(50f).left();
                        }

                        if (++i % 8 == 0) pt.row();
                    }
                }).row();

                p.button(UnitTypes.mono.emoji(),cleart,() -> {showSelectPayload = !showSelectPayload;rebuildItems[0].run();}).size(50f);
                p.row();

                if (showSelectPayload){
                    p.pane(list -> {
                        int i = 0;
                        for (UnitType units : content.units()) {
                            list.button(units.emoji(), () -> {
                                unitPayload.add(units.create(unitTeam));
                                rebuildItems[0].run();
                            }).size(50f);
                            if (++i % 8 == 0) list.row();
                        }
                    });
                    p.row();
                    p.table(pt->{
                        pt.button("[cyan]自递归",()->{
                            Unit unit = spawning.create(unitTeam);
                            unit.set(unitLoc.x * tilesize + Tmp.v1.x, unitLoc.y * tilesize + Tmp.v1.y);
                            unitStatus.each((status,statusDuration)->{
                                unit.apply(status,statusDuration * 60f);
                            });
                            if (unitItems!=null) unit.addItem(unitItems,unitItemAmount);

                            if (unit instanceof Payloadc pay){
                                unitPayload.each(payload ->{
                                    pay.addPayload(new UnitPayload(payload));
                                });
                            }



                            rebuildItems[0].run();
                        });
                        pt.button("?",()->ui.showInfo("使用说明：携带的单位存在一个序列，每个单位可以具备特定的属性。\n[cyan]自递归[white]是指根据当前的配置生成一个单位，并储存到载荷序列上"
                                +"\n这一单位具备所有目前设置的属性，包括buff、物品和载荷。\n合理使用自递归可以发掘无限的可能性"+
                                "\n[orange][警告]尚不清楚连续套娃是否会对游戏产生影响")).size(50f);
                    });
                }
            });
        };

        rebuildItems[0].run();
        itemDialog.addCloseButton();
        itemDialog.show();
    }
    */
}