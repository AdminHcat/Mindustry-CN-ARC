package mindustry.ui;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.ClickListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.event.InputEvent;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.ctype.Content;
import mindustry.game.SpawnGroup;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.*;
import mindustry.input.DesktopInput;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;

import static mindustry.Vars.*;
import static mindustry.Vars.world;
import static mindustry.content.UnitTypes.gamma;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.clearNonei;
import static mindustry.ui.Styles.*;


public class AdvanceToolTable extends Table {
    private boolean show = false;
    private boolean showGameMode = false,showResTool = false,showTeamChange = false, showUnitStat = false;

    //unitFactory
    private UnitType spawning = UnitTypes.dagger;
    private int unitCount = 1;
    private Vec2 unitLoc = new Vec2(0, 0);
    private Team unitTeam = Team.sharded;
    private ObjectMap<StatusEffect, Float> unitStatus = new ObjectMap();

    private Item unitItems = null;
    private int unitItemAmount = 0;
    private boolean showSelectItems = false,showSelectPayload = false;
    /** Seq of payloads that this unit will spawn with. */
    private Seq<Unit> unitPayload = Seq.with();

    private ObjectMap<String,Float> unitProperty =  new ObjectMap();
    private StatusEffect status = StatusEffects.none;
    private float statusDuration = 0f;


    private TextButton.TextButtonStyle textStyle;

    public AdvanceToolTable() {
        textStyle = new TextButton.TextButtonStyle(){{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        rebuild();
    }

    void rebuild(){
        clear();
        if(!show) {
            table(t -> {
                t.background(Tex.buttonEdge3);
                t.button("[cyan]工具箱", cleart, () -> {
                    show = !show;
                    rebuild();
                }).left().width(70).expandX();

            }).left();
        }
        else{
            table(t -> {
                if (showGameMode){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("规则：").left();
                        tBox.button("沙盒",cleart, () -> {
                            state.rules.editor = !state.rules.editor;
                        }).width(40f);
                    }).left().row();
                }
                if (showResTool){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("资源：").left();
                        tBox.button(Items.copper.emoji()+"[acid]+",cleart, () -> {
                            for(Item item :content.items())  player.core().items.set(item,player.core().storageCapacity);
                        }).width(40f);
                        tBox.button(Items.copper.emoji()+"[red]-",cleart, () -> {
                            for(Item item :content.items())  player.core().items.set(item,0);
                        }).width(40f);
                    }).left().row();
                }
                if (showTeamChange){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("队伍：").left();
                        for(Team team:Team.baseTeams){
                            tBox.button("[#" + team.color +  "]" + team.localized(),cleart,()->player.team(team)).width(40f);
                        }
                        tBox.button("[#" + Color.violet +  "]+",cleart,()->teamChangeMenu()).width(40f);

                    }).left().row();
                }
                if(showUnitStat){
                    t.table(tBox -> {
                        tBox.background(Tex.buttonEdge3);
                        tBox.add("单位：").left();
                        tBox.button(UnitTypes.gamma.emoji()+"[acid]+",cleart, () -> player.unit().type().spawn(player.team(),player.x,player.y)).width(40f).tooltip("[acid]克隆");
                        tBox.button(UnitTypes.gamma.emoji()+"[red]×",cleart, () -> player.unit().kill()).width(40f).tooltip("[red]自杀");
                        tBox.button(Icon.waves,clearNonei, () -> {
                            unitSpawnMenu();
                        }).width(40f).tooltip("[acid]单位");
                    }).left().row();
                }


                t.row();
                t.table(mainBox -> {
                    mainBox.background(Tex.buttonEdge3);
                    mainBox.button("[red]工具箱",cleart, () -> {
                        show = !show;
                        rebuild();
                    }).width(70f);
                    mainBox.button((showGameMode?"[cyan]":"[acid]")+"规则",cleart, () -> {
                        showGameMode = !showGameMode;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showResTool?"[cyan]":"[acid]")+"资源",cleart, () -> {
                        showResTool = !showResTool;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showResTool?"[cyan]":"[acid]")+"队伍",cleart, () -> {
                        showTeamChange = !showTeamChange;
                        rebuild();
                    }).width(50f);
                    mainBox.button((showUnitStat?"[cyan]":"[acid]")+"单位",cleart, () -> {
                        showUnitStat = !showUnitStat;
                        rebuild();
                    }).width(50f);

                }).left();


            }).left();
        }
    }

    private void teamChangeMenu(){
        BaseDialog dialog = new BaseDialog("队伍选择器");
        Table selectTeam = new Table().top();

        dialog.cont.pane(td->{
            int j = 0;
            for(Team team : Team.all){
                ImageButton button = new ImageButton(Tex.whiteui, Styles.clearTogglei);
                button.getStyle().imageUpColor = team.color;
                button.margin(10f);
                button.resizeImage(40f);
                button.clicked(() -> {player.team(team);dialog.hide();});
                button.update(() -> button.setChecked(player.team() == team));
                td.add(button);
                j++;
                if(j==5) {td.row();td.add("队伍："+j+"~"+(j+10));}
                else if((j-5)%10==0) {td.row();td.add("队伍："+j+"~"+(j+10));}
            }
        }
        );

        dialog.add(selectTeam).center();
        dialog.row();

        dialog.addCloseButton();

        dialog.show();
    }

    private void unitSpawnMenu(){
        BaseDialog unitFactory = new BaseDialog("单位工厂-ARC");

        Table table = unitFactory.cont;
        Runnable[] rebuild = {null};
        rebuild[0] = () -> {
            table.clear();
            /* Unit */
            table.label(() -> spawning.localizedName);
            table.row();

            table.pane(list -> {
                int i = 0;
                for (UnitType units : content.units()) {
                    if (i++ % 8 == 0) list.row();
                    list.button(units.emoji(), () -> {
                        spawning = units;
                    }).size(50f);
                }
            }).top().center();

            table.row();

            Table r = table.table().center().bottom().get();
            r.add("数量：");
            r.field("" + unitCount, text -> {
                unitCount = Integer.parseInt(text);
            }).maxTextLength(4).valid(value -> Strings.canParsePositiveInt(value)).get();

            table.row();

            table.table(t -> {
                    t.add("坐标：x= ");
                    t.field(String.valueOf((int) unitLoc.x), text -> {
                        unitLoc.x = Integer.parseInt(text);
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveFloat(value)).get();
                    t.add("  ,y= ");
                    t.field(String.valueOf((int) unitLoc.y), text -> {
                        unitLoc.y = Integer.parseInt(text);
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveFloat(value)).get();
                    t.button(gamma.emoji(),()->{
                        unitLoc.x = ui.chatfrag.getArcMarkerX() / tilesize;
                        unitLoc.y = ui.chatfrag.getArcMarkerY() / tilesize;
                        rebuild[0].run();
                    }).tooltip("选择上个标记点：" + (int)  (ui.chatfrag.getArcMarkerX() / tilesize) + "," + (int) (ui.chatfrag.getArcMarkerY() / tilesize)).height(50f);
                }
            );

            table.row();

            table.table(t -> {
                    t.add("队伍：");
                    t.field(String.valueOf(unitTeam.id), text -> {
                        unitTeam = Team.get(Integer.parseInt(text));
                    }).maxTextLength(4).valid(value -> Strings.canParsePositiveInt(value)).get();
                    for(Team team:Team.baseTeams){
                        t.button("[#" + team.color +  "]" + team.localized(),cleart,()->{unitTeam = team;rebuild[0].run();}).size(30,30).color(team.color);
                    }
                    t.button("更多", cleart ,() -> {
                        BaseDialog selectTeamDialog = new BaseDialog("队伍选择器");
                        Table selectTeam = new Table().top();
                        selectTeamDialog.cont.pane(td->{
                                int j = 0;
                                for(Team team : Team.all){
                                    ImageButton button = new ImageButton(Tex.whiteui, Styles.clearTogglei);
                                    button.getStyle().imageUpColor = team.color;
                                    button.margin(10f);
                                    button.resizeImage(40f);
                                    button.clicked(() -> {
                                        Call.setPlayerTeamEditor(player, team);selectTeamDialog.hide();rebuild[0].run();});
                                    button.update(() -> button.setChecked(player.team() == team));
                                    td.add(button);
                                    j++;
                                    if(j==5) {td.row();td.add("队伍："+j+"~"+(j+10));}
                                    else if((j-5)%10==0) {td.row();td.add("队伍："+j+"~"+(j+10));}
                                }
                            }
                        );

                        selectTeamDialog.add(selectTeam).center();
                        selectTeamDialog.row();

                        selectTeamDialog.addCloseButton();
                        selectTeamDialog.show();
                    }).center().width(50f).row();
                }
            );

            table.row();

            table.button(StatusEffects.electrified.emoji() + "[cyan]单位状态重构厂",()->unitStatusMenu()).fillX();

            table.row();

            table.button(Items.copper.emoji() +"|" + UnitTypes.mono.emoji() + "  [cyan]单位的百宝袋",()->showItems()).fillX();

            table.row();

            table.button("[orange]生成！", Icon.modeAttack, () -> {
                for (var n = 0; n < unitCount; n++) {
                    Tmp.v1.rnd(Mathf.random(5f * tilesize));
                    Unit unit = spawning.create(unitTeam);
                    unit.set(unitLoc.x * tilesize + Tmp.v1.x, unitLoc.y * tilesize + Tmp.v1.y);
                    unitStatus.each((status,statusDuration)->{
                        unit.apply(status,statusDuration * 60f);
                    });
                    if (unitItems!=null) unit.addItem(unitItems,unitItemAmount);

                    if (unit instanceof Payloadc pay){
                        unitPayload.each(payload ->{
                            pay.pickup(payload);
                        });
                    }
                    unit.add();
                }
                if (control.input instanceof DesktopInput) {
                    ((DesktopInput) control.input).panning = true;
                }
                Core.camera.position.set(unitLoc.x * tilesize,unitLoc.y * tilesize);
                unitFactory.closeOnBack();
            }).fillX();
        };
        rebuild[0].run();

        unitFactory.addCloseButton();
        unitFactory.show();
    }

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
                                //rebuildStatus[0].run();
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
                    pt.button(Items.copper.emoji(),cleart,() -> {showSelectItems = !showSelectItems;rebuildItems[0].run();}).size(50f);
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


}