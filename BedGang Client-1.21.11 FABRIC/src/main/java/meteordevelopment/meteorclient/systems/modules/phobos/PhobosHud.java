package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.themes.phobos.PhobosGuiTheme;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Classic fixed-corner HUD. Uses native text/items, never owns or replaces the Meteor HUD config. */
public class PhobosHud extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final SettingGroup colors = settings.createGroup("Colors");
    private final SettingGroup list = settings.createGroup("Active Modules");
    private final SettingGroup stats = settings.createGroup("Information");
    private final SettingGroup combat = settings.createGroup("Combat");
    private final Setting<Boolean> replace = bool(sg, "replace-meteor-hud", "Temporarily hide Meteor HUD elements while this HUD is active. Keeps their layout and enabled state.", true);
    private final Setting<Boolean> hideMenus = bool(sg, "hide-in-menus", "Hide in screens other than chat.", false);
    private final Setting<Double> scale = sg.add(new DoubleSetting.Builder().name("hud-scale").description("Scale relative to Minecraft GUI scale.").defaultValue(1).range(0.5, 2).sliderRange(0.5, 2).build());
    private final Setting<Integer> margin = sg.add(new IntSetting.Builder().name("margin").description("Classic HUD edge inset.").defaultValue(2).range(0, 30).sliderRange(0, 15).build());
    private final Setting<Boolean> shadow = bool(sg, "shadow", "Draw classic text shadows.", true);
    private final Setting<String> watermark = sg.add(new StringSetting.Builder().name("watermark").description("Top-left watermark. Empty hides it.").defaultValue("BedGang | t4's edition").build());
    private final Setting<Boolean> version = bool(sg, "version", "Append the port's Minecraft version.", true);
    private final Setting<Boolean> greeter = bool(sg, "greeter", "Show a centered greeting.", false);
    private final Setting<String> greeting = sg.add(new StringSetting.Builder().name("greeting").description("Use <player> for your name.").defaultValue("Welcome to t4's BedGang edition, <player>!").build());
    private final Setting<SettingColor> color = colors.add(new ColorSetting.Builder().name("color").description("Classic HUD accent.").defaultValue(new SettingColor(255, 0, 0)).build());
    private final Setting<Boolean> sync = bool(colors, "sync-gui", "Use the Phobos GUI accent when that theme is selected.", true);
    private final Setting<Boolean> rainbow = bool(colors, "rainbow", "Cycle the HUD hue.", false);
    private final Setting<Boolean> rolling = bool(colors, "rolling", "Offset rainbow hue for each line.", true);
    private final Setting<Double> rainbowSeconds = colors.add(new DoubleSetting.Builder().name("rainbow-seconds").description("Seconds per hue cycle.").defaultValue(6).range(1, 30).sliderRange(1, 15).build());
    private final Setting<Boolean> grayLabels = bool(colors, "gray-labels", "Gray information labels with white values, like Phobos FutureColour.", true);
    private final Setting<Boolean> arrayList = bool(list, "active-modules", "Show the right-aligned enabled module list.", true);
    private final Setting<Boolean> topList = bool(list, "rendering-up", "Place modules at the top-right and information at the bottom-right. Off matches the original bottom module list.", false);
    private final Setting<Boolean> alphabetical = bool(list, "alphabetical", "Sort alphabetically instead of by text width.", false);
    private final Setting<Boolean> info = bool(list, "module-info", "Show module info in gray brackets.", true);
    private final Setting<Integer> animation = list.add(new IntSetting.Builder().name("animation-ms").description("Module entry/exit animation time; zero is instant.").defaultValue(250).range(0, 1000).sliderRange(0, 500).build());
    private final Setting<List<Module>> hidden = list.add(new ModuleListSetting.Builder().name("hidden-modules").description("Modules omitted from the classic array list.").build());
    private final Setting<Boolean> coords = bool(stats, "coordinates", "Bottom-left XYZ and Overworld/Nether equivalent coordinates.", true);
    private final Setting<Boolean> direction = bool(stats, "direction", "Cardinal direction above coordinates.", true);
    private final Setting<Boolean> fps = bool(stats, "fps", "Show frames per second.", true);
    private final Setting<Boolean> ping = bool(stats, "ping", "Show server latency.", true);
    private final Setting<Boolean> tps = bool(stats, "tps", "Show measured server ticks per second.", true);
    private final Setting<Boolean> speed = bool(stats, "speed", "Show horizontal speed in km/h.", true);
    private final Setting<Boolean> time = bool(stats, "time", "Show local clock time.", false);
    private final Setting<Boolean> brand = bool(stats, "server-brand", "Show the server's reported brand.", false);
    private final Setting<Boolean> potions = bool(stats, "potions", "List active effects, levels and remaining duration.", true);
    private final Setting<Boolean> heldDurability = bool(stats, "held-durability", "Show remaining durability for the held item.", false);
    private final Setting<Boolean> lag = bool(stats, "lag-notifier", "Warn after a gap of more than one second without a server packet.", true);
    private final Setting<Boolean> armor = bool(combat, "armor", "Armor icons above the hotbar, in Phobos's original order.", true);
    private final Setting<Boolean> percent = bool(combat, "armor-percent", "Green-to-red remaining durability above armor icons.", true);
    private final Setting<Boolean> totems = bool(combat, "totems", "Totem icon and inventory count above the hotbar.", true);
    private final Setting<Boolean> radar = bool(combat, "text-radar", "Nearby players, health and distance beneath the watermark.", false);
    private final Setting<Integer> radarLimit = combat.add(new IntSetting.Builder().name("radar-limit").description("Maximum radar lines.").defaultValue(8).range(1, 30).sliderRange(1, 15).build());
    private final Setting<Boolean> hitMarkers = bool(combat, "hit-markers", "Brief center marker on your attack action. This is not server hit confirmation.", true);
    private final Map<Module, Double> transitions = new IdentityHashMap<>();
    private volatile long lastPacket;
    private long lastFrame, attackUntil;
    private double kmh;
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public PhobosHud() { super(Categories.PhobosPort, "phobos-hud", "Classic Phobos watermark, array list, corner stats, coordinates, armor and totems."); }
    private Setting<Boolean> bool(SettingGroup group, String name, String description, boolean value) { return group.add(new BoolSetting.Builder().name(name).description(description).defaultValue(value).build()); }
    private void reset() { transitions.clear(); lastPacket = System.nanoTime(); lastFrame = 0; attackUntil = 0; kmh = 0; }
    @Override public void onActivate() { reset(); }
    @Override public void onDeactivate() { reset(); }
    @EventHandler private void onJoin(GameJoinedEvent event) { reset(); }
    @EventHandler private void onPacket(PacketEvent.Receive event) { lastPacket = System.nanoTime(); }
    @EventHandler private void onAttack(AttackEntityEvent event) { if (!event.isCancelled()) attackUntil = System.nanoTime() + 250_000_000L; }
    @EventHandler private void onTick(TickEvent.Post event) { kmh = Math.hypot(mc.player.getX() - mc.player.lastX, mc.player.getZ() - mc.player.lastZ) * 72; }
    public boolean replacesMeteorHud() { return isActive() && replace.get(); }

    @EventHandler private void onRender(Render2DEvent event) {
        if (!Utils.canUpdate() || mc.options.hudHidden || mc.debugHudEntryList.isF3Enabled()) return;
        if (meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen.isOpen()) return;
        if (hideMenus.get() && mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) return;
        DrawContext ctx = event.drawContext;
        float s = scale.get().floatValue();
        int w = (int) (mc.getWindow().getScaledWidth() / s), h = (int) (mc.getWindow().getScaledHeight() / s), m = margin.get();
        int chat = mc.currentScreen instanceof ChatScreen ? (int) (14 / s) : 0;
        ctx.createNewRootLayer();
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(s, s);
        try {
            if (!watermark.get().isEmpty()) draw(ctx, (watermark.get().equals("Phobos") ? "BedGang | t4's edition" : watermark.get()) + (version.get() ? " 1.21.11" : ""), m, m, accent(m));
            if (greeter.get()) center(ctx, greeting.get().replace("Welcome to Phobos <player> :^)", "Welcome to t4's BedGang edition, <player>!").replace("<player>", mc.player.getName().getString()), w, m, accent(m));
            if (radar.get()) drawRadar(ctx, m, watermark.get().isEmpty() ? m : m + 14, w, h);
            drawInformation(ctx, w, h, m, chat);
            drawModules(ctx, w, h, m, chat);
            int bottom = h - m - 9 - chat;
            if (coords.get()) {
                String equivalent = "";
                if (mc.world.getRegistryKey().equals(World.NETHER)) equivalent = String.format(Locale.ROOT, " \u00a77[\u00a7f%.0f, %.0f\u00a77]", mc.player.getX() * 8, mc.player.getZ() * 8);
                else if (mc.world.getRegistryKey().equals(World.OVERWORLD)) equivalent = String.format(Locale.ROOT, " \u00a77[\u00a7f%.0f, %.0f\u00a77]", mc.player.getX() / 8, mc.player.getZ() / 8);
                draw(ctx, label("XYZ ") + String.format(Locale.ROOT, "%.0f, %.0f, %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()) + equivalent, m, bottom, accent(bottom));
                bottom -= 10;
            }
            if (direction.get()) {
                String cardinal = mc.player.getHorizontalFacing().asString();
                String axis = switch (cardinal) { case "north" -> " [-Z]"; case "south" -> " [+Z]"; case "east" -> " [+X]"; default -> " [-X]"; };
                draw(ctx, cardinal.substring(0, 1).toUpperCase(Locale.ROOT) + cardinal.substring(1) + axis, m, bottom, accent(bottom));
            }
            drawCombat(ctx, w, h, chat);
            double seconds = (System.nanoTime() - lastPacket) / 1e9;
            if (lag.get() && !mc.isInSingleplayer() && seconds > 1) center(ctx, String.format(Locale.ROOT, "Server not responding: %.1fs.", seconds), w, m + 18, 0xffaaaaaa);
            if (hitMarkers.get() && System.nanoTime() < attackUntil) {
                for (int i = 4; i <= 8; i++) for (int dx : new int[]{-1, 1}) for (int dy : new int[]{-1, 1}) ctx.fill(w / 2 + dx * i, h / 2 + dy * i, w / 2 + dx * i + 1, h / 2 + dy * i + 1, 0xffffffff);
            }
        } finally { ctx.getMatrices().popMatrix(); }
    }
    private void drawModules(DrawContext ctx, int w, int h, int m, int chat) {
        if (!arrayList.get()) { transitions.clear(); return; }
        long now = System.nanoTime();
        double step = animation.get() == 0 || lastFrame == 0 ? 1 : Math.min(0.1, (now - lastFrame) / 1e9) * 1000 / animation.get();
        lastFrame = now;
        for (Module module : Modules.get().getActive()) if (module != this && !(module instanceof PhobosGui) && !hidden.get().contains(module)) transitions.putIfAbsent(module, 0.0);
        var iterator = transitions.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            boolean active = entry.getKey().isActive() && !hidden.get().contains(entry.getKey());
            double value = Math.clamp(entry.getValue() + (active ? step : -step), 0, 1);
            if (!active && value == 0) iterator.remove(); else entry.setValue(value);
        }
        List<Module> modules = new ArrayList<>(transitions.keySet());
        modules.sort(alphabetical.get() ? Comparator.comparing(module -> module.title) : Comparator.<Module>comparingInt(module -> mc.textRenderer.getWidth(moduleText(module))).reversed().thenComparing(module -> module.title));
        double offset = 0;
        for (Module module : modules) {
            String text = moduleText(module);
            double progress = transitions.get(module);
            int tw = mc.textRenderer.getWidth(text);
            int y = topList.get() ? m + (int) offset : h - m - chat - 9 - (int) offset;
            if (topList.get() ? y > h / 2 : y < h / 2) break;
            draw(ctx, text, w - m - tw + (int) ((1 - progress) * (tw + m)), y, accent(y));
            offset += 10 * progress;
        }
    }
    private String moduleText(Module module) {
        String value = info.get() ? module.getInfoString() : null;
        return module.title + (value == null || value.isEmpty() ? "" : " \u00a77[\u00a7f" + value + "\u00a77]");
    }
    private void drawInformation(DrawContext ctx, int w, int h, int m, int chat) {
        List<String> lines = new ArrayList<>();
        if (brand.get()) lines.add(label("Server brand ") + Objects.toString(mc.getNetworkHandler().getBrand(), "Unknown"));
        if (potions.get()) for (var effect : mc.player.getStatusEffects()) {
            int seconds = effect.getDuration() / 20;
            lines.add(effect.getEffectType().value().getName().getString() + " " + (effect.getAmplifier() + 1) + " \u00a7f" + (effect.isInfinite() ? "**:**" : String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)));
        }
        if (speed.get()) lines.add(label("Speed ") + String.format(Locale.ROOT, "%.1f km/h", kmh));
        if (time.get()) lines.add(label("Time ") + LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
        if (heldDurability.get() && mc.player.getMainHandStack().isDamageable()) lines.add(label("Durability ") + (mc.player.getMainHandStack().getMaxDamage() - mc.player.getMainHandStack().getDamage()));
        if (tps.get()) { float value = TickRate.INSTANCE.getTickRate(); lines.add(label("TPS ") + (Float.isFinite(value) ? String.format(Locale.ROOT, "%.1f", value) : "--")); }
        if (ping.get()) { var player = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()); lines.add(label("Ping ") + (player == null ? "--" : player.getLatency()) + " ms"); }
        if (fps.get()) lines.add(label("FPS ") + mc.getCurrentFps());
        int index = 0;
        for (String text : lines) {
            int y = topList.get() ? h - m - chat - 9 - index * 10 : m + index * 10;
            if (topList.get() ? y < h / 2 : y > h / 2) break;
            draw(ctx, text, w - m - mc.textRenderer.getWidth(text), y, accent(y)); index++;
        }
    }
    private void drawCombat(DrawContext ctx, int w, int h, int chat) {
        int y = h - 55 - (mc.player.isSubmergedInWater() ? 10 : 0) - chat;
        if (armor.get()) for (int i = 0; i < ARMOR.length; i++) {
            var stack = mc.player.getEquippedStack(ARMOR[i]);
            if (stack.isEmpty()) continue;
            int x = w / 2 - 90 + (8 - i) * 20 + 2;
            ctx.drawItem(stack, x, y);
            ctx.drawStackOverlay(mc.textRenderer, stack, x, y, stack.getCount() > 1 ? Integer.toString(stack.getCount()) : "");
            if (percent.get() && stack.isDamageable()) {
                double remaining = Math.clamp((stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage(), 0, 1);
                String value = Integer.toString((int) Math.ceil(remaining * 100));
                int c = 0xff000000 | ((int) ((1 - remaining) * 255) << 16) | ((int) (remaining * 255) << 8);
                draw(ctx, value, x + 8 - mc.textRenderer.getWidth(value) / 2, y - 10, c);
            }
        }
        if (totems.get()) {
            int count = 0;
            for (int slot = 0; slot < 36; slot++) { var stack = mc.player.getInventory().getStack(slot); if (stack.isOf(Items.TOTEM_OF_UNDYING)) count += stack.getCount(); }
            if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) count += mc.player.getOffHandStack().getCount();
            if (count > 0) { int x = w / 2 - 7; var stack = Items.TOTEM_OF_UNDYING.getDefaultStack(); ctx.drawItem(stack, x, y); ctx.drawStackOverlay(mc.textRenderer, stack, x, y, Integer.toString(count)); }
        }
    }
    private void drawRadar(DrawContext ctx, int x, int y, int w, int h) {
        var players = new ArrayList<>(mc.world.getPlayers());
        players.removeIf(p -> p == mc.player || !p.isAlive() || p.isSpectator());
        players.sort(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p)));
        int count = 0;
        for (var p : players) {
            if (count++ >= radarLimit.get() || y >= h / 2) break;
            String hp = String.format(Locale.ROOT, "%.0f", p.getHealth() + p.getAbsorptionAmount());
            String text = "\u00a7a" + hp + " " + (Friends.get().isFriend(p) ? "\u00a7b" : "\u00a7f") + p.getName().getString() + " \u00a77" + (int) mc.player.distanceTo(p) + "m";
            draw(ctx, mc.textRenderer.trimToWidth(text, w / 2 - x), x, y, accent(y)); y += 10;
        }
    }
    private String label(String label) { return (grayLabels.get() ? "\u00a77" : "") + label + "\u00a7f"; }
    private int accent(int y) {
        if (rainbow.get()) return 0xff000000 | (java.awt.Color.HSBtoRGB((float) ((System.currentTimeMillis() % (long) (rainbowSeconds.get() * 1000)) / (rainbowSeconds.get() * 1000) + (rolling.get() ? y / 900.0 : 0)), 1, 1) & 0xffffff);
        if (sync.get() && GuiThemes.get() instanceof PhobosGuiTheme look) return 0xff000000 | (look.accent(y, false) & 0xffffff);
        return color.get().getPacked();
    }
    private void draw(DrawContext ctx, String text, int x, int y, int color) { ctx.drawText(mc.textRenderer, text, x, y, color, shadow.get()); }
    private void center(DrawContext ctx, String text, int width, int y, int color) { draw(ctx, text, width / 2 - mc.textRenderer.getWidth(text) / 2, y, color); }
}
