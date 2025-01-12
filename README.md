![Logo](core/assets-raw/sprites/ui/logo.png)

[![Build Status](https://github.com/Anuken/Mindustry/workflows/Tests/badge.svg?event=push)](https://github.com/Anuken/Mindustry/actions)
[![Discord](https://img.shields.io/discord/391020510269669376.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/mindustry)  

The automation tower defense RTS, written in Java.

_[Trello Board](https://trello.com/b/aE2tcUwF/mindustry-40-plans)_  
_[Wiki](https://mindustrygame.github.io/wiki)_  
_[Javadoc](https://mindustrygame.github.io/docs/)_ 
_[Original](https://github.com/Anuken/Mindustry)_ 

## 学术端介绍
制作团队：Mindustry PVP 学术交流群

更多的工厂显示！从超速范围到工作细节！\
更多的兵种显示！从攻击范围到单位血量！\
更多的辅助功能！从地图属性到蓝图大小！\
更多的地图设置！从更多队伍到强化波次！\
强化核心数据库！给大量工厂和兵种配备详细机制用法讲解！

# 作者声明
关于更详细的游戏说明/机制，欢迎跟我补充。我们希望做一个真正的核心数据库
## 主要负责人
[violet]Lucky_Clover
## 代码
[green]Root[blue]Channel[white]、[blue]MyIndustry2[white]、[blue]xkldklp[white]、[violet]Lucky_Clover、
[yellow]miner[white]、blac8、[yellow]wayzer、[brown]Xor
## 翻译
[lightblue]Somall_dumpling
## 工作器件补充说明
[yellow]Carrot

# 发布|fork|自改端声明
如果fork本端，请在Vars.arcVersion后面加上你自己的子版本号，或者你私仓版本的代号（如3.0.1selfclient) \
如果需要公开发布你自行修改的版本，请联系Lucky Clover \
不鼓励传播到国外！未经授权请不要转载或搬运到任何游戏平台或论坛！允许在Q群内传播 \

# 下面是anuke-mindustry的说明

## Contributing

See [CONTRIBUTING](CONTRIBUTING.md).

## Building

Bleeding-edge builds are generated automatically for every commit. You can see them [here](https://github.com/Anuken/MindustryBuilds/releases).

If you'd rather compile on your own, follow these instructions.
First, make sure you have [JDK 16-17](https://adoptium.net/archive.html?variant=openjdk17&jvmVariant=hotspot) installed. **Other JDK versions will not work.** Open a terminal in the Mindustry directory and run the following commands:

### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`  
_Sprite Packing:_ `gradlew tools:pack`

### Linux/Mac OS

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`  
_Sprite Packing:_ `./gradlew tools:pack`

### Server

Server builds are bundled with each released build (in Releases). If you'd rather compile on your own, replace 'desktop' with 'server', e.g. `gradlew server:dist`.

### Android

1. Install the Android SDK [here.](https://developer.android.com/studio#command-tools) Make sure you're downloading the "Command line tools only", as Android Studio is not required.
2. Set the `ANDROID_HOME` environment variable to point to your unzipped Android SDK directory.
3. Run `gradlew android:assembleDebug` (or `./gradlew` if on linux/mac). This will create an unsigned APK in `android/build/outputs/apk`.

To debug the application on a connected phone, run `gradlew android:installDebug android:run`.

### Troubleshooting

#### Permission Denied

If the terminal returns `Permission denied` or `Command not found` on Mac/Linux, run `chmod +x ./gradlew` before running `./gradlew`. *This is a one-time procedure.*

---

Gradle may take up to several minutes to download files. Be patient. <br>
After building, the output .JAR file should be in `/desktop/build/libs/Mindustry.jar` for desktop builds, and in `/server/build/libs/server-release.jar` for server builds.

## Feature Requests

Post feature requests and feedback [here](https://github.com/Anuken/Mindustry-Suggestions/issues/new/choose).

## Downloads

| [![](https://static.itch.io/images/badge.svg)](https://anuke.itch.io/mindustry)    |    [![](https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png)](https://play.google.com/store/apps/details?id=io.anuke.mindustry)   |    [![](https://fdroid.gitlab.io/artwork/badge/get-it-on.png)](https://f-droid.org/packages/io.anuke.mindustry)	| [![](https://flathub.org/assets/badges/flathub-badge-en.svg)](https://flathub.org/apps/details/com.github.Anuken.Mindustry)  
|---	|---	|---	|---	|
