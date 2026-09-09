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


## Skipped Modules From Impact, and Phobos

“Adapted” can be a deliberately smaller modern implementation; read the details. 

“Shared entry” means broad feature availability through the existing host, not parity with old bypass modes or settings. 

“Not ported” distinguishes outstanding work and obsolete dependencies from an assertion that a feature is impossible. Nested mode helper classes are addressed after the table.

| Original Impact module | Outcome | Details |
|---|---|---|
| combat/Aimbot | Not ported | Not separately ported. SmoothAim is available, but the original instant snap aim and full entity filter need their own implementation. |
| combat/Aura | Shared entry | Uses existing kill-aura. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/AutoArmor | Shared entry | Uses existing auto-armor. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/AutoClicker | Shared entry | Uses existing auto-clicker. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/BowAimbot | Shared entry | Uses existing bow-aimbot. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/Criticals | Shared entry | Uses existing criticals. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/HitBox | Shared entry | Uses existing hitboxes. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| combat/Pot | Not ported | Legacy timed potion selection/use controller omitted; modern inventory timing, hand and potion-component handling need a dedicated port. |
| combat/SmoothAim | Adapted | Visible-player smoothing with range/FOV/speed controls. Player-only; no original jitter, mob/animal options or old antibot heuristics. Default requires attack held. |
| combat/Soup | Not ported | Legacy timed soup-use/hotbar controller omitted; needs a modern item-use and inventory port and applicable server testing. |
| combat/Velocity | Shared entry | Uses existing velocity. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| exploit/AntiHunger | Shared entry | Uses existing anti-hunger. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| exploit/BedGodMode | Not ported | Old sleep/bed desynchronization relies on patched vanilla hooks and historical server behavior; no validated modern equivalent. |
| exploit/Firion | Not ported | Source explicitly targets 1.8 servers and spams movement packets to shorten burning. Not a validated 1.21.11 behavior. |
| exploit/Franky | Not ported | Module shell depends on modified vanilla integration; no standalone behavior that can be registered as a working modern module. |
| exploit/GhostHand | Not ported | Core behavior is in patched Block ray tracing with numeric block IDs. Needs modern raycast/mixin integration and block registry settings. |
| exploit/Ignite | Not ported | Needs modern flint-and-steel target placement, reach/rotation and item-use timing; not replaced by a misleading unrelated fire module. |
| exploit/PingSpoof | Shared entry | Uses existing ping-spoof. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| minigame/CopsAndCrims | Not ported | Legacy minigame item/recoil/aim assumptions require game-specific rewriting and validation. |
| minigame/Minestrike | Not ported | Legacy minigame weapon/item assumptions require game-specific rewriting and validation. |
| minigame/Murder | Not ported | Legacy role detection based on minigame item/state assumptions is not validated for a modern server. |
| minigame/PropHunt | Not ported | Legacy disguised entity/minigame detection requires a separate modern port and server validation. |
| minigame/QuakeCraft | Not ported | Legacy minigame aiming/weapon logic requires a separate modern port and server validation. |
| minigame/SneakyAssassians | Not ported | Legacy minigame role/entity assumptions require a separate modern port and server validation. |
| misc/Animations | Not ported | 1.7/1.8 sword-blocking and shield rendering hook into the old first-person renderer; need modern item render-state/mixin work. |
| misc/AntiBot | Not ported | Old GWEN/Watchdog/AAC heuristics are coupled to old servers and target filters. No claim they identify modern bots reliably. |
| misc/AntiCheat | Not ported | Local movement checks depend on legacy movement/tick assumptions; modern checks and false-positive validation are outstanding. |
| misc/AutoDisconnect | Shared entry | Uses existing auto-log. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| misc/AutoReconnect | Shared entry | Uses existing auto-reconnect. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| misc/CrosshairPlus | Not ported | Independent custom crosshair and vanilla-crosshair suppression need a dedicated rendering integration; not included in the HUD recreation. |
| misc/DeathCoords | Adapted | Local death coordinates and dimension, once per death. |
| misc/Disable | Not ported | Original shuts down Impact managers/hooks. That lifecycle cannot safely be applied to the shared Meteor host; use individual toggles. |
| misc/ItemSaver | Adapted | Moves a nearly broken held item into an empty main-inventory slot. Uses remaining durability, skips containers; cannot save it if inventory is full. |
| misc/MCF | Existing option/system | Use Meteor MiddleClickExtra in AddFriend mode (its default is Pearl), backed by the shared Friends system. No independent Impact toggle. |
| misc/MineplexStaffDetector | Not ported | Historical staff/server detection data and panic handling are not a maintained modern detection system. |
| misc/RainbowEnchant | Not ported | Old fixed-function glint rendering needs a modern render-pipeline/shader port. |
| misc/ScreenshotUploader | Not ported | Old Imgur upload integration and credentials/service handling are not included. Normal local screenshots remain available. |
| misc/SkinBlinker | Not ported | Skin-part cycling is feasible but not implemented; needs saved model-part state and reliable restoration across disconnects. |
| misc/Unpack | Not ported | Numeric-ID log crafting and raw inventory clicks need recipe/component-aware crafting and transaction recovery; not implemented. |
| movement/AutoJump | Adapted | Held jump, released in screens/on disable; uses modern jump handling. |
| movement/AutoWalk | Adapted | Held forward, released in screens/on disable. |
| movement/BoatFly | Shared entry | Uses existing entity-control. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/ElytraPlus | Shared entry | Uses existing elytra-fly. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/FastFall | Shared entry | Uses existing reverse-step. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/FastLadder | Adapted | Original upward ladder motion with horizontal motion stopped. |
| movement/Flight | Shared entry | Uses existing flight. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Glide | Adapted | Constant slow descent; skips vehicles, swimming, ladders and elytra. |
| movement/InventoryMove | Shared entry | Uses existing gui-move. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Jesus | Shared entry | Uses existing jesus. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Jetpack | Adapted | Jump-key vertical acceleration; adds a configurable upward speed cap. |
| movement/LongJump | Shared entry | Uses existing long-jump. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/NoPush | Existing option/system | Use the shared Velocity module and configure its entity/block/liquid push controls; no separate toggle. |
| movement/NoSlowDown | Shared entry | Uses existing no-slow. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Parkour | Adapted | Jump at an unsupported block edge; skips sneaking and vehicles. |
| movement/Phase | Not ported | Legacy Latest/NoClip/SkipClip collision and packet modes are not validated against modern collision and server handling. |
| movement/SafeWalk | Shared entry | Uses existing safe-walk. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/ScaffoldWalk | Shared entry | Uses existing scaffold. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Speed | Shared entry | Uses existing speed. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Spider | Adapted | Normal wall climb only; old NCP mode omitted. |
| movement/Sprint | Shared entry | Uses existing sprint. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| movement/Step | Shared entry | Uses existing step. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/AntiAFK | Shared entry | Uses existing anti-afk. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/AutoEat | Shared entry | Uses existing auto-eat. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/AutoFish | Shared entry | Uses existing auto-fish. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/AutoMine | Adapted | Held attack on the crosshair target; no independent pathfinding. |
| player/AutoRespawn | Adapted | One respawn request per death screen. |
| player/AutoSteal | Shared entry | Uses existing inventory-tweaks. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/AutoTool | Adapted | Raw hotbar mining-speed comparison and swap-back; not Meteor enchantment-aware scoring. |
| player/Blink | Shared entry | Uses existing blink. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/FastBreak | Shared entry | Uses existing speed-mine. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/FastPlace | Shared entry | Uses existing fast-use. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/Freecam | Shared entry | Uses existing freecam. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/LiquidInteract | Shared entry | Uses existing liquid-interact. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/NoFall | Shared entry | Uses existing no-fall. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/NoRotate | Shared entry | Uses existing no-rotate. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| player/Retard | Not ported | Original Headless/Spinny packet rotation modes are not implemented; they would need coordination with other rotation owners. |
| player/Sneak | Adapted | Normal held-sneak only; old packet mode omitted. |
| player/Timer | Shared entry | Uses existing timer. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/AntiBlind | Existing option/system | Use shared NoRender blindness overlay option; not a separate toggle. |
| render/Breadcrumbs | Shared entry | Uses existing breadcrumbs. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Brightness | Shared entry | Uses existing fullbright. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/CameraClip | Shared entry | Uses existing camera-tweaks. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Chams | Shared entry | Uses existing chams. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/ClickGui | Adapted | Separate Impact-style dark/blue category GUI, draggable/collapsible panels, hover ON/OFF badges, settings, search and binds. Native Minecraft font; layout recreated, not pixel-exact. |
| render/ESP | Shared entry | Uses existing esp. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/HUD | Adapted | Blue watermark, Flare/Direkt list spacing, per-module colors, effects, coordinates, stats and tab menu. Native font; no old entity-count section, user badges or font/service backend. |
| render/ItemPhysics | Shared entry | Uses existing item-physics. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/LiquidVision | Existing option/system | Use shared NoRender liquid/fog options as applicable; exact old underwater appearance is not recreated. |
| render/Nametags | Shared entry | Uses existing nametags. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/NoHurtCam | Existing option/system | Use the vanilla damage-tilt strength option; no separate Impact toggle. Modern API: [GameOptions.getDamageTiltStrength](https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/net/minecraft/client/option/GameOptions.html#getDamageTiltStrength()). |
| render/NoRender | Shared entry | Uses existing no-render. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/PCP | Not ported | Old patched OpenGL color behavior needs a modern render-pipeline implementation. |
| render/Radar | Not ported | Original skin/entity radar and Static/Dynamic modes are not recreated. Meteor HUD offers its own radar separately. |
| render/StorageESP | Shared entry | Uses existing storage-esp. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Tracers | Shared entry | Uses existing tracers. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Trajectories | Shared entry | Uses existing trajectories. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Waypoints | Shared entry | Uses existing waypoints. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| render/Wireframe | Not ported | Old polygon-mode rendering is not a drop-in for modern render pipelines; requires a new line-rendering implementation. |
| world/AntiWeather | Existing option/system | Use shared NoRender: weather. This is an option inside NoRender, not an independently toggled Impact module. |
| world/Nuker | Shared entry | Uses existing nuker. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |
| world/Xray | Shared entry | Uses existing xray. Same toggle, settings and keybind in both GUIs; original Impact algorithms/modes are not ported. |


