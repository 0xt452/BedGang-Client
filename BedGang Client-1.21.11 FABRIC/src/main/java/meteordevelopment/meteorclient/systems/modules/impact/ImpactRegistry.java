package meteordevelopment.meteorclient.systems.modules.impact;

import meteordevelopment.meteorclient.gui.themes.impact.ImpactGuiTheme;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import java.util.*;

/** Shared entries are the actual Meteor modules: no duplicate event subscriptions or settings. */
public final class ImpactRegistry {
    public static final Category CATEGORY = new Category("Impact", Items.DIAMOND.getDefaultStack());
    public static final List<Category> GROUPS = List.of(new Category("Combat"), new Category("Exploit"), new Category("Misc"), new Category("Movement"), new Category("Player"), new Category("Render"), new Category("World"));
    public record Bridge(String group, String label, String target) {}
    public static final List<Bridge> BRIDGES = List.of(
        new Bridge("Combat", "Aura", "kill-aura"),
        new Bridge("Combat", "AutoArmor", "auto-armor"),
        new Bridge("Combat", "AutoClicker", "auto-clicker"),
        new Bridge("Combat", "BowAimbot", "bow-aimbot"),
        new Bridge("Combat", "Criticals", "criticals"),
        new Bridge("Combat", "HitBox", "hitboxes"),
        new Bridge("Combat", "Velocity", "velocity"),
        new Bridge("Exploit", "AntiHunger", "anti-hunger"),
        new Bridge("Exploit", "PingSpoof", "ping-spoof"),
        new Bridge("Misc", "AutoDisconnect", "auto-log"),
        new Bridge("Misc", "AutoReconnect", "auto-reconnect"),
        new Bridge("Movement", "BoatFly", "entity-control"),
        new Bridge("Movement", "ElytraPlus", "elytra-fly"),
        new Bridge("Movement", "FastFall", "reverse-step"),
        new Bridge("Movement", "Flight", "flight"),
        new Bridge("Movement", "InventoryMove", "gui-move"),
        new Bridge("Movement", "Jesus", "jesus"),
        new Bridge("Movement", "LongJump", "long-jump"),
        new Bridge("Movement", "NoSlowDown", "no-slow"),
        new Bridge("Movement", "SafeWalk", "safe-walk"),
        new Bridge("Movement", "ScaffoldWalk", "scaffold"),
        new Bridge("Movement", "Speed", "speed"),
        new Bridge("Movement", "Sprint", "sprint"),
        new Bridge("Movement", "Step", "step"),
        new Bridge("Player", "AntiAFK", "anti-afk"),
        new Bridge("Player", "AutoEat", "auto-eat"),
        new Bridge("Player", "AutoFish", "auto-fish"),
        new Bridge("Player", "AutoSteal", "inventory-tweaks"),
        new Bridge("Player", "Blink", "blink"),
        new Bridge("Player", "FastBreak", "speed-mine"),
        new Bridge("Player", "FastPlace", "fast-use"),
        new Bridge("Player", "Freecam", "freecam"),
        new Bridge("Player", "LiquidInteract", "liquid-interact"),
        new Bridge("Player", "NoFall", "no-fall"),
        new Bridge("Player", "NoRotate", "no-rotate"),
        new Bridge("Player", "Timer", "timer"),
        new Bridge("Render", "Breadcrumbs", "breadcrumbs"),
        new Bridge("Render", "Brightness", "fullbright"),
        new Bridge("Render", "CameraClip", "camera-tweaks"),
        new Bridge("Render", "Chams", "chams"),
        new Bridge("Render", "ESP", "esp"),
        new Bridge("Render", "ItemPhysics", "item-physics"),
        new Bridge("Render", "Nametags", "nametags"),
        new Bridge("Render", "NoRender", "no-render"),
        new Bridge("Render", "StorageESP", "storage-esp"),
        new Bridge("Render", "Tracers", "tracers"),
        new Bridge("Render", "Trajectories", "trajectories"),
        new Bridge("Render", "Waypoints", "waypoints"),
        new Bridge("World", "Nuker", "nuker"),
        new Bridge("World", "Xray", "xray")
    );
    private static ImpactGuiTheme theme;
    private ImpactRegistry() {}
    public static ImpactGuiTheme theme() {
        if (theme == null) { theme = new ImpactGuiTheme(); theme.loadOwn(); }
        return theme;
    }
    public static void register(Modules modules) {
        modules.add(new ImpactPorts.AutoJump()); modules.add(new ImpactPorts.AutoWalk());
        modules.add(new ImpactPorts.AutoMine()); modules.add(new ImpactPorts.Sneak());
        modules.add(new ImpactPorts.Glide()); modules.add(new ImpactPorts.Jetpack());
        modules.add(new ImpactPorts.FastLadder()); modules.add(new ImpactPorts.Spider());
        modules.add(new ImpactPorts.Parkour()); modules.add(new ImpactPorts.AutoRespawn());
        modules.add(new ImpactPorts.DeathCoords()); modules.add(new ImpactPorts.ItemSaver());
        modules.add(new ImpactPorts.AutoTool()); modules.add(new ImpactPorts.SmoothAim());
        modules.add(new ImpactGui()); modules.add(new ImpactHud());
    }
    public static List<Module> group(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : Modules.get().getGroup(CATEGORY)) {
            String group = module instanceof ImpactModule port ? port.group : "Render";
            if (group.equals(category.name)) result.add(module);
        }
        for (Bridge bridge : BRIDGES) if (bridge.group.equals(category.name)) {
            Module module = Modules.get().get(bridge.target);
            if (module != null && !result.contains(module)) result.add(module);
        }
        return result;
    }
    public static String displayName(Module module) {
        if (module instanceof ImpactModule port) return port.impactName;
        if (module instanceof ImpactGui) return "ClickGui";
        if (module instanceof ImpactHud) return "HUD";
        for (Bridge bridge : BRIDGES) if (bridge.target.equals(module.name)) return bridge.label + (module.category == meteordevelopment.meteorclient.systems.modules.Categories.PhobosPort ? " [P]" : " [M]");
        return module.title;
    }
    public static String description(Module module) {
        return module.category == CATEGORY ? "Impact adaptation: " + module.description : "Shared " + module.title + ": toggles, settings and binds are shared with its existing module. " + module.description;
    }
}
