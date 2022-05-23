package mindustry.ui.dialogs;

import arc.Core;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;

import static mindustry.Vars.*;
import static mindustry.ui.Styles.cleart;

public class AboutCN_ARCDialog extends BaseDialog{
    Seq<String> changelogs = new Seq<>();

    public AboutCN_ARCDialog() {
        super("@aboutcn_arc.button");

        shown(() -> {
            changelogs = Seq.with(Core.files.internal("arcchangelog").readString("UTF-8").split("\n"));
            Core.app.post(this::setup);
        });

        shown(this::setup);
        onResize(this::setup);

    }

    void setup(){
        cont.clear();
        buttons.clear();

        Table about = new Table();
        ScrollPane pane = new ScrollPane(about);

        cont.add("[cyan]CN-ARC Client by PVP学术交流群");
        cont.row();
        cont.row();
        cont.add("共同制作:");
        cont.row();
        cont.add("[green]Root[blue]Channel[white]、[blue]MyIndustry2[white]、[blue]xkldklp[white]、[violet]Lucky_Clover、[yellow]miner[white]、blac8、[yellow]wayzer、[brown]Xor");
        cont.row();
        cont.add("主负责人：[violet]Lucky_Clover").row();
        cont.add("公开发行版本："+arcVersion).row();
        cont.add("获取最新版本，请加入[yellow]猫猫的数据库[white]").row();
        cont.add("讨论学术端更新|提议源码|共同编辑，请加入[yellow]mindustry PVP交流群[white]:931790051[orange]不欢迎萌新，仅限大佬加入").row();
        cont.row();
        cont.table(t->{
            t.add("相关链接").height(30f);
            t.button("[cyan]更新日志",cleart,()->{
                if(!Core.app.openURI("https://github.com/CN-ARC/Mindustry-CN-ARC")){
                    ui.showErrorMessage("@linkfail");
                    Core.app.setClipboardText("https://github.com/CN-ARC/Mindustry-CN-ARC");
                }
            }).width(100f).height(30f);
            t.button("[cyan]数据库1群",cleart,()->{
                if(!Core.app.openURI("https://jq.qq.com/?_wv=1027&k=SIlwZafb")){
                    ui.showErrorMessage("@linkfail");
                    Core.app.setClipboardText("https://jq.qq.com/?_wv=1027&k=SIlwZafb");
                }
            }).width(100f).height(30f);
            t.button("[cyan]数据库2群",cleart,()->{
                if(!Core.app.openURI("https://jq.qq.com/?_wv=1027&k=OTtencaZ")){
                    ui.showErrorMessage("@linkfail");
                    Core.app.setClipboardText("https://jq.qq.com/?_wv=1027&k=OTtencaZ");
                }
            }).width(100f).height(30f);
        });

        cont.row();
        for(String log : changelogs){
            if(!log.startsWith("[cyan]请仔细阅读学术端")){
                about.add(log).left();
            }else{
                about.add(log+changeLogRead).left();
            }
            about.row();
        }
        cont.add(pane).growX();
        cont.row();


        addCloseButton();
    }
}
