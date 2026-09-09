# BedGang Client t4's edition

A customized Meteor 1.21.11 distribution with the existing Phobos and Impact ports. t4 identifies this custom edition, not authorship of all upstream code.

Client contains all of Meteors modules with the addition of a few rare phobos ports, and the addition of the Impact 3.0 client with a seperate GUI.

## Build

Use Java 21. Extract the ZIP, open a terminal in meteor-client-1.21.11, then run:

```powershell
.\gradlew.bat build
```

The normal remapped client JAR will be named bedgang-t4-…jar in build/libs. Use that file, not the sources/development JAR. Replace the previous Meteor JAR in your Fabric 1.21.11 profile; do not load both. The unchanged meteor-client mod ID keeps the existing configuration location.

Right Alt opens Impact. Your existing Meteor/Phobos GUI binding continues to work. HUDs keep their prior toggle behavior and Impact still defers to Phobos HUD by default. If an existing configuration has a custom title or splash/credit display disabled, change those options in Config. Reset the relevant watermark/default setting if you previously saved your own text and want the new default.

