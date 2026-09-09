# BedGang — t4's edition

A customized Meteor 1.21.11 distribution with the existing Phobos and Impact ports. Upstream code remains credited in LICENSE, UPSTREAM-README.md, source notices, and src/main/resources/bedgang-upstream-notices.txt. The t4 author entry identifies this custom edition, not authorship of all upstream code.


The Meteor, Phobos and Impact GUI names, category IDs, module IDs, Java packages, resource namespaces and config paths remain compatible with the prior build. Functional dependency coordinates, API URLs and historical copyright/attribution references retain their real names. Original personal attribution comments are preserved rather than relabeled as t4's work. The main README now describes BedGang; the original README is kept as UPSTREAM-README.md.

## Build

Use Java 21. Extract the ZIP, open a terminal in meteor-client-1.21.11, then run:

```powershell
.\gradlew.bat build
```

The normal remapped client JAR will be named bedgang-t4-…jar in build/libs. Use that file, not the sources/development JAR. Replace the previous custom Meteor JAR in your Fabric 1.21.11 profile; do not load both. The unchanged meteor-client mod ID keeps the existing configuration location.

Right Alt opens Impact. Your existing Meteor/Phobos GUI binding continues to work. HUDs keep their prior toggle behavior and Impact still defers to Phobos HUD by default. If an existing configuration has a custom title or splash/credit display disabled, change those options in Config. Reset the relevant watermark/default setting if you previously saved your own text and want the new default.

## Verification

Java 21 syntax parsing passed for all 983 Java source files. The audited personal handles no longer appear in Java string literals; their historical source attribution comments remain. Fabric metadata parses with BedGang as the display name, t4 as the edition author, and the original mod ID/entrypoints. The packaged source ZIP is read back after creation.

A fresh full Gradle build attempt again fails before client compilation: version-catalog generation encounters AccessDeniedException on gradle-logging-9.2.0.jar in this workspace. The supplied bedgang-build.log records that failure. No compiled JAR, type-check success, in-game visual verification or launch success is claimed for this version. No installed Minecraft files were modified.

