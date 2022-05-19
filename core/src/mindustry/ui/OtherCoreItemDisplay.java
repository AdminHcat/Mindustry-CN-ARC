package mindustry.ui;

import arc.graphics.Color;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.core.*;
import mindustry.game.Teams;
import mindustry.type.Item;
import mindustry.type.UnitType;

import static mindustry.Vars.content;
import static mindustry.Vars.state;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.clearNonei;
import static mindustry.ui.Styles.*;


//code by MI2 now
public class OtherCoreItemDisplay extends Table {
    private Seq<Teams.TeamData> teams = null, teams0 = null;

    private float lastUpd = 0f, fontScl = 0.8f;
    private int showTeams = 6, showStart = 0;
    private boolean show = false, showStat = true, showItem = true, showUnit = true;
    //TODO Stats

    private Table teamsTable;

    private TextButton.TextButtonStyle textStyle;


    public OtherCoreItemDisplay() {

        textStyle = new TextButton.TextButtonStyle(){{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};

        teamsTable = new Table();
        rebuild();
    }

    void rebuild(){
        clear();
        if(!show){
        button("[red]+",textStyle, () -> {
            show = !show;
            rebuild();
        }).left().width(40f).fillY().get().left();}
        else{
            table(t -> {
                t.table(buttons -> {
                    buttons.button("[red]×",textStyle, () -> {
                        show = !show;
                        rebuild();
                    }).size(40f).row();

                    buttons.button("+",textStyle, () -> {
                        if(showTeams > teams.size) return;
                        showTeams += 1;
                        if(showTeams > 15) showTeams = 15;
                    }).size(40f).row();
    
                    buttons.button("-",textStyle, () -> {
                        showTeams -= 1;
                        if(showTeams <= 0) showTeams = 1;
                    }).size(40f).row();
    
                    buttons.button(">",textStyle, () -> {
                        showStart += 1;
                        if(showStart + showTeams > teams0.size) showStart = teams0.size - showTeams;
                    }).size(40f).row();
    
                    buttons.button("<",textStyle, () -> {
                        showStart -= 1;
                        if(showStart < 0) showStart = 0;
                    }).size(40f).row();

                    buttons.button(Blocks.worldProcessor.emoji(),textStyle, () -> {
                        showStat = !showStat;
                    }).size(40f).row();

                    buttons.button(content.items().get(0).emoji(),textStyle, () -> {
                        showItem = !showItem;
                    }).size(40f).row();
    
                    buttons.button(UnitTypes.mono.emoji(),textStyle, () -> {
                        showUnit = !showUnit;
                    }).size(40f);
                }).left();
        
                teamsRebuild();
    
                t.add(teamsTable).left();
    
            }).left();
        }



    }

    private void teamsRebuild(){
        teamsTable.clear();
        teamsTable.background(Styles.black6);
        teamsTable.update(() -> {
            if (Time.time - lastUpd > 120f){
                lastUpd = Time.time;
                teamsRebuild();
                return;
            }

            if (teams != state.teams.getActive()) {
                teamsRebuild();
                return;
            }
        });

        teamsTable.label(() -> "").get().setFontScale(fontScl);
        teams0 = Vars.state.teams.getActive();
        teams0.sort(team -> -team.cores.size);

        teams = new Seq<>();
        for(int ii = 0; ii < showTeams; ii++){
            if(ii + showStart < 0 || ii + showStart >= teams0.size) break;
            teams.add(teams0.get(ii + showStart));
        }

        /**name + cores + units */
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> "[#" + team.team.color + "]" + team.team.localized()).get().setFontScale(fontScl);
        }
        teamsTable.row();
        teamsTable.label(() -> Blocks.coreNucleus.emoji()).get().setFontScale(fontScl);
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> "[#" + team.team.color + "]" + UI.formatAmount(team.cores.size)).padRight(1).get().setFontScale(fontScl);
        }
        teamsTable.row();
        teamsTable.label(() -> UnitTypes.mono.emoji()).get().setFontScale(fontScl);
        for (Teams.TeamData team : teams) {
            teamsTable.label(() -> "[#" + team.team.color + "]" + UI.formatAmount(team.units.size)).padRight(1).get().setFontScale(fontScl);
        }
        teamsTable.row();

        if(showStat){
            teamsTable.image(Blocks.siliconSmelter.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + (state.rules.teams.get(team.team).cheat? "[green]+":"[red]×")).get().setFontScale(fontScl);
            }
            teamsTable.row();
            teamsTable.image(Blocks.arc.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + Strings.autoFixed(state.rules.teams.get(team.team).blockDamageMultiplier * state.rules.blockDamageMultiplier,2)).get().setFontScale(fontScl);
            }
            teamsTable.row();
            teamsTable.image(Blocks.titaniumWall.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + Strings.autoFixed(state.rules.teams.get(team.team).blockHealthMultiplier * state.rules.blockHealthMultiplier,2)).get().setFontScale(fontScl);
            }
            teamsTable.row();
            teamsTable.image(UnitTypes.corvus.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + Strings.autoFixed(state.rules.teams.get(team.team).unitDamageMultiplier * state.rules.unitDamageMultiplier,2)).get().setFontScale(fontScl);
            }
            teamsTable.row();
            teamsTable.image(UnitTypes.poly.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + Strings.autoFixed(state.rules.teams.get(team.team).buildSpeedMultiplier * state.rules.buildSpeedMultiplier,2)).get().setFontScale(fontScl);
            }
            teamsTable.row();
            teamsTable.image(Blocks.tetrativeReconstructor.uiIcon).size(15,15).left().get();
            for(Teams.TeamData team : teams){
                teamsTable.label(() -> "[#" + team.team.color + "]" + Strings.autoFixed(state.rules.teams.get(team.team).unitBuildSpeedMultiplier * state.rules.unitBuildSpeedMultiplier,2)).get().setFontScale(fontScl);
            }
            teamsTable.row();


        }

        if(showItem){
            boolean[] dispItems = new boolean[content.items().size];
            for(Item item : content.items()){            
                for(Teams.TeamData team : teams){
                    if(team.hasCore() && team.core().items.get(item) > 0) dispItems[content.items().indexOf(item)] = true;
                }
            }

            for(Item item : content.items()){            
                if(dispItems[content.items().indexOf(item)]){

                    //teamsTable.label(() -> item.emoji()).padRight(5f).left().get().setFontScale(fontScl);
                    teamsTable.image(item.uiIcon).size(15,15).left().get(); 
                    for(Teams.TeamData team : teams){
                        teamsTable.label(() -> "[#" + team.team.color + "]" + ((team.hasCore() && team.core().items.get(item) > 0) ? UI.formatAmount(team.core().items.get(item)) : "-")).get().setFontScale(fontScl);
                    }
                    teamsTable.row();

                }
            }
        }

        if(showUnit){
            boolean[] dispUnits = new boolean[content.units().size];
            for(UnitType unit : content.units()){            
                for(Teams.TeamData team : teams){
                    if(team.countType(unit) > 0) dispUnits[content.units().indexOf(unit)] = true;
                }
            }

            for(UnitType unit : content.units()){            
                if(dispUnits[content.units().indexOf(unit)]){

                    //teamsTable.label(() -> unit.emoji()).padRight(5f).left().get().setFontScale(fontScl);
                    teamsTable.image(unit.uiIcon).size(15,15).left().get(); 
                    for(Teams.TeamData team : teams){
                        teamsTable.label(() -> "[#" + team.team.color + "]" + (team.countType(unit) > 0 ? team.countType(unit) : "-")).get().setFontScale(fontScl);
                    }
                    teamsTable.row();

                }
            }
        }
    }

    public void updateTeamList() {
        teams0 = Vars.state.teams.getActive();
        showTeams = teams0.size;
        teams0.sort(team -> -team.cores.size);
        teams = new Seq<>();
        for(int ii = 0; ii < showTeams; ii++){
            if(ii + showStart < 0 || ii + showStart >= teams0.size) break;
            teams.add(teams0.get(ii + showStart));
        }
    }

}