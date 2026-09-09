# BedGang — t4's edition

A customized Meteor 1.21.11 distribution with the existing Phobos and Impact ports, branded for BedGang and t4. Upstream code remains credited in LICENSE, UPSTREAM-README.md, source notices, and src/main/resources/bedgang-upstream-notices.txt. The t4 author entry identifies this custom edition, not authorship of all upstream code.

## Branding in this build

- Title screen: BedGang branding card with a pink bed emblem, “t4's edition,” and “BedGang by t4” in the credit area. The title-screen credits setting controls this display. This edition no longer presents upstream commit updates as t4's own updates.
- Splashes: seven BedGang/t4 messages, including “t4's client. BedGang's home.” The existing splash setting still controls them.
- GUIs: BedGang bed emblem and “BedGang | t4's edition” badge in the Meteor screens, Phobos panels, Impact panels, and advanced editors. Classic panels reserve room above the badge for their last rows.
- HUDs: BedGang/t4 branding in the Meteor watermark preset, Phobos watermark and greeting, and Impact watermark. A saved stock “Phobos” watermark also displays the new branding. Other customized saved HUD text remains editable.
- Local client chat prefix: [BedGang | t4]. This does not send messages to other players.
- Mod metadata: BedGang, edition author t4, and a description identifying the Meteor/Phobos/Impact base.
- Window title: “BedGang | t4's edition | Minecraft …” is the new default; custom window titles default on for fresh configuration.
- Personal-name examples, offline-account placeholder, FakePlayer and NameProtect defaults now use t4. Existing configured values and real player/account identities are not overwritten.
- Discord presence: BedGang/t4 default messages and image tooltips; developer avatar references removed. The existing upstream Discord application ID and available logo asset remain, so Discord's application name/icon are not a separately registered BedGang application. Presence is not enabled automatically.
- Build artifact base name: bedgang-t4.

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

IMPACT-INTEGRATION-REPORT.md and the Phobos reports describe the previous integration stages and their feature limitations. This README describes the additional branding changes; earlier statements about unchanged GUI files apply to those earlier stages.
