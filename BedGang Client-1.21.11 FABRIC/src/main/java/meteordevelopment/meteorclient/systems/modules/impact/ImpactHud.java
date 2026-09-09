package meteordevelopment.meteorclient.systems.modules.impact;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.phobos.PhobosHud;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.world.World;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

/** Impact 3.0's corner HUD and Flare/Direkt list movement, adapted to native text. */
public final class ImpactHud extends Module {
    public enum Mode { Flare, Direkt }
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Mode> mode = sg.add(new EnumSetting.Builder<Mode>().name("mode").description("Flare animates row spacing; Direkt keeps full row spacing.").defaultValue(Mode.Flare).build());
    private final Setting<Boolean> replace = flag("replace-meteor-hud", "Hide Meteor HUD while this HUD is drawing; its layout is preserved.", true);
    private final Setting<Boolean> defer = flag("defer-to-phobos", "Pause this HUD while Phobos HUD is active, avoiding overlapping corner text.", true);
    private final Setting<Boolean> watermark = flag("watermark", "Show the blue Impact watermark.", true);
    private final Setting<Boolean> list = flag("array-list", "Show active Impact entries, including shared entries enabled in either GUI.", true);
    private final Setting<Boolean> all = flag("all-modules", "Include the rest of Meteor and Phobos in the list.", false);
    private final Setting<Boolean> effects = flag("effects", "Show effects at the bottom-right.", true);
    private final Setting<Boolean> coords = flag("coordinates", "Show X/Y/Z at the bottom-right.", false);
    private final Setting<Boolean> nether = flag("nether-coordinates", "Convert Nether coordinates to Overworld coordinates.", false);
    private final Setting<Boolean> info = flag("information", "FPS, ping and TPS below the watermark/tab menu.", false);
    private final Setting<Boolean> tab = flag("tab-gui", "Category menu. Hold Left Alt and use arrow keys to navigate/toggle.", true);
    private final Map<Module, Double> transitions = new IdentityHashMap<>();
    private long frame;
    private int category, selection;
    private boolean opened;
    public ImpactHud() { super(ImpactRegistry.CATEGORY, "impact-hud", "Impact's blue watermark, Flare/Direkt array list, tab menu, effects and corner coordinates."); }
    private Setting<Boolean> flag(String name, String description, boolean value) { return sg.add(new BoolSetting.Builder().name(name).description(description).defaultValue(value).build()); }
    @Override public void onActivate() { transitions.clear(); frame = 0; }
    @Override public void onDeactivate() { transitions.clear(); frame = 0; }
    private boolean visible() {
        var phobos = Modules.get().get(PhobosHud.class);
        return isActive() && Utils.canUpdate() && !mc.options.hudHidden && !mc.debugHudEntryList.isF3Enabled()
            && !meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen.isOpen()
            && !(defer.get() && phobos != null && phobos.isActive());
    }
    public boolean replacesMeteorHud() { return replace.get() && visible(); }
    private List<Module> selectedModules() {
        var modules = ImpactRegistry.group(ImpactRegistry.GROUPS.get(category));
        modules.sort(Comparator.comparing(ImpactRegistry::displayName));
        return modules;
    }
    @EventHandler private void key(KeyEvent event) {
        if (!visible() || !tab.get() || mc.currentScreen != null || event.action != KeyAction.Press || !Input.isKeyPressed(GLFW_KEY_LEFT_ALT)) return;
        var modules = selectedModules();
        switch (event.key()) {
            case GLFW_KEY_UP, GLFW_KEY_DOWN -> {
                int step = event.key() == GLFW_KEY_UP ? -1 : 1;
                if (opened && !modules.isEmpty()) selection = Math.floorMod(selection + step, modules.size());
                else { category = Math.floorMod(category + step, ImpactRegistry.GROUPS.size()); selection = 0; }
            }
            case GLFW_KEY_LEFT -> opened = false;
            case GLFW_KEY_RIGHT -> { if (!opened) opened = true; else if (!modules.isEmpty()) modules.get(Math.min(selection, modules.size() - 1)).toggle(); }
        }
    }
    @EventHandler private void render(Render2DEvent event) {
        if (!visible()) { frame = 0; return; }
        var ctx = event.drawContext;
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        int y = 2;
        if (watermark.get()) { draw(ctx, "\u00a7dBedGang \u00a7f| t4's edition", 2, y, 0xffffffff); y += 13; }
        if (tab.get()) { drawTab(ctx, y, h); y += ImpactRegistry.GROUPS.size() * 12 + 4; }
        if (info.get()) {
            var player = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            draw(ctx, "\u00a79FPS: \u00a7f" + mc.getCurrentFps(), 2, y, -1); y += 10;
            draw(ctx, "\u00a79Ping: \u00a7f" + (player == null ? "--" : player.getLatency()), 2, y, -1); y += 10;
            draw(ctx, String.format(Locale.ROOT, "\u00a79TPS: \u00a7f%.1f", TickRate.INSTANCE.getTickRate()), 2, y, -1);
        }
        int bottom = h - 12 - (mc.currentScreen instanceof ChatScreen ? 14 : 0);
        if (effects.get()) for (var effect : mc.player.getStatusEffects()) {
            int seconds = effect.getDuration() / 20;
            String value = effect.getEffectType().value().getName().getString() + " " + (effect.getAmplifier() + 1) + " \u00a77(" + (effect.isInfinite() ? "**:**" : String.format(Locale.ROOT, "%d:%02d", seconds / 60, seconds % 60)) + ")";
            right(ctx, value, w, bottom, -1); bottom -= 10;
            if (bottom < h / 2) break;
        }
        if (coords.get()) {
            double factor = nether.get() && mc.world.getRegistryKey().equals(World.NETHER) ? 8 : 1;
            right(ctx, String.format(Locale.ROOT, "\u00a79Z: \u00a7f%.1f", mc.player.getZ() * factor), w, bottom, -1); bottom -= 10;
            right(ctx, String.format(Locale.ROOT, "\u00a79Y: \u00a7f%.1f", mc.player.getY()), w, bottom, -1); bottom -= 10;
            right(ctx, String.format(Locale.ROOT, "\u00a79X: \u00a7f%.1f", mc.player.getX() * factor), w, bottom, -1);
        }
        if (list.get()) drawList(ctx, w, h); else { transitions.clear(); frame = 0; }
    }
    private void drawTab(DrawContext ctx, int y, int h) {
        for (int i = 0; i < ImpactRegistry.GROUPS.size(); i++) {
            ctx.fill(2, y + i * 12, 79, y + i * 12 + 12, i == category ? 0xcc5555ff : 0xaa1e1e1e);
            draw(ctx, ImpactRegistry.GROUPS.get(i).name, 5, y + i * 12 + 2, -1);
        }
        if (!opened) return;
        var modules = selectedModules();
        if (modules.isEmpty()) return;
        selection = Math.clamp(selection, 0, modules.size() - 1);
        int count = Math.max(1, (h - y - 14) / 12), first = Math.max(0, selection - count + 1);
        for (int i = first; i < Math.min(modules.size(), first + count); i++) {
            Module module = modules.get(i); int row = y + (i - first) * 12;
            ctx.fill(81, row, 231, row + 12, i == selection ? 0xcc5555ff : 0xaa1e1e1e);
            draw(ctx, mc.textRenderer.trimToWidth(ImpactRegistry.displayName(module), 143), 84, row + 2, module.isActive() ? -1 : 0xffaaaaaa);
        }
    }
    private void drawList(DrawContext ctx, int w, int h) {
        Set<Module> eligible = Collections.newSetFromMap(new IdentityHashMap<>());
        if (all.get()) eligible.addAll(Modules.get().getAll());
        else for (var group : ImpactRegistry.GROUPS) eligible.addAll(ImpactRegistry.group(group));
        eligible.remove(this); eligible.remove(Modules.get().get(ImpactGui.class));
        long now = System.nanoTime(); double step = frame == 0 ? 1 : Math.min(.1, (now - frame) / 1e9) * 5; frame = now;
        for (Module module : eligible) if (module.isActive()) transitions.putIfAbsent(module, 0.0);
        var it = transitions.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next(); boolean enabled = entry.getKey().isActive() && eligible.contains(entry.getKey());
            double value = Math.clamp(entry.getValue() + (enabled ? step : -step), 0, 1);
            if (!enabled && value == 0) it.remove(); else entry.setValue(value);
        }
        var modules = new ArrayList<>(transitions.keySet());
        modules.sort(Comparator.<Module>comparingInt(m -> mc.textRenderer.getWidth(ImpactRegistry.displayName(m))).reversed().thenComparing(m -> m.name));
        double y = mc.player.getStatusEffects().isEmpty() ? 2 : 55;
        for (Module module : modules) {
            if (y > h / 2) break;
            String name = ImpactRegistry.displayName(module); double progress = transitions.get(module);
            int length = mc.textRenderer.getWidth(name);
            int color = 0xff000000 | (java.awt.Color.HSBtoRGB((module.name.hashCode() & 0xffff) / 65535f, .65f, 1) & 0xffffff);
            draw(ctx, name, w - 4 - (int) (length * progress), (int) y, color);
            y += 10 * (mode.get() == Mode.Flare ? progress : 1);
        }
    }
    private void draw(DrawContext ctx, String text, int x, int y, int color) { ctx.drawText(mc.textRenderer, text, x, y, color, true); }
    private void right(DrawContext ctx, String text, int w, int y, int color) { draw(ctx, text, w - 4 - mc.textRenderer.getWidth(text), y, color); }
}
