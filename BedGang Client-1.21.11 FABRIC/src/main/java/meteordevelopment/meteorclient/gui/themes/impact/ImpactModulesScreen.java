package meteordevelopment.meteorclient.gui.themes.impact;

import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.WindowConfig;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.impact.ImpactHud;
import meteordevelopment.meteorclient.systems.modules.impact.ImpactRegistry;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import java.util.*;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.lwjgl.glfw.GLFW.*;

public class ImpactModulesScreen extends TabScreen {
    private static final int ROW = 18, HEADER = 18, TOP = 21;
    private final ImpactGuiTheme look;
    private final List<Panel> panels = new ArrayList<>();
    private final Set<Module> expanded = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Hit> hits = new ArrayList<>();
    private Panel dragging;
    private double dragX, dragY;
    private Hit sliding;
    private Double sliderPreview;
    private Setting<?> editing;
    private Module binding;
    private Setting<Keybind> settingBinding;
    private String buffer = "", search = "", error = "";
    private boolean searchFocused;
    private String tooltip;
    private record Hit(int x, int y, int width, int height, Panel panel, Module module, Setting<?> setting, String kind) {
        boolean contains(double px, double py) { return ImpactPanelLayout.contains(px, py, x, y, width, height); }
    }
    private static class Panel {
        final Category category;
        final WindowConfig config;
        int scroll, total, view;
        Panel(Category category, WindowConfig config) { this.category = category; this.config = config; }
    }

    public ImpactModulesScreen(ImpactGuiTheme theme) { super(theme, Tabs.get().getFirst()); look = theme; }
    @Override public void initWidgets() {
        if (!panels.isEmpty()) return;
        for (Category category : ImpactRegistry.GROUPS) panels.add(new Panel(category, look.getWindowConfig("impact/" + category.name)));
    }
    @Override protected void init() { super.init(); arrange(false); }
    private void arrange(boolean reset) {
        int w = look.panelWidth.get(), columns = ImpactPanelLayout.columns(width, w);
        int rows = ImpactPanelLayout.bands(panels.size(), columns);
        int band = ImpactPanelLayout.bandHeight(height, rows);
        for (int i = 0; i < panels.size(); i++) {
            var p = panels.get(i);
            if (reset || p.config.x < 0 || p.config.y < 0) {
                p.config.x = 4 + i % columns * (w + 3);
                p.config.y = TOP + i / columns * band;
                p.scroll = 0;
                if (reset) p.config.expanded = true;
            }
            p.config.x = Math.clamp(p.config.x, 0, Math.max(0, width - w));
            p.config.y = Math.clamp(p.config.y, TOP, Math.max(TOP, height - 58));
        }
    }

    // Meteor calls this after vanilla GUI rendering. DrawContext keeps native GUI scaling.
    @Override public void renderCustom(DrawContext ctx, int mx, int my, float delta) {
        GuiKeyEvents.canUseKeys = editing == null && binding == null && settingBinding == null && !searchFocused;
        if (dragging != null) {
            dragging.config.x = Math.clamp(mx - dragX, 0, Math.max(0, width - look.panelWidth.get()));
            dragging.config.y = Math.clamp(my - dragY, TOP, Math.max(TOP, height - 58));
        }
        if (sliding != null) slide(sliding, mx);
        arrange(false);
        hits.clear(); tooltip = null;
        ctx.fill(0, 0, width, height, 0x55000000);
        // This screen owns its palette and layout; the global selected theme is untouched.
        int tx = 4;
        for (String action : List.of("Back", "HUD", "Style", "Reset")) {
            int tw = mc.textRenderer.getWidth(action) + 8;
            boolean hover = mx >= tx && mx < tx + tw && my >= 3 && my < 17;
            ctx.fill(tx, 3, tx + tw, 17, hover ? 0x99777777 : 0x77333333);
            text(ctx, action, tx + 4, 6, 0xffeeeeee);
            hits.add(new Hit(tx, 3, tw, 14, null, null, null, action));
            tx += tw + 3;
        }
        int searchX = tx + 2;
        if (width > searchX + 35) {
            ctx.fill(searchX, 3, width - 4, 17, 0x99222222);
            text(ctx, trim((searchFocused ? "> " : "Search: ") + search + (searchFocused ? "_" : ""), width - searchX - 10), searchX + 3, 6, 0xffbbbbbb);
            hits.add(new Hit(searchX, 3, width - searchX - 4, 14, null, null, null, "search"));
        }
        for (Panel panel : panels) drawPanel(ctx, panel, mx, my);
        String help = error.isEmpty() ? "[M] shared Meteor | [P] shared Phobos | LMB toggle | RMB settings | MMB bind | Ctrl+F search" : error;
        text(ctx, trim(help, width - 8), 4, height - 11, error.isEmpty() ? 0xffaaaaaa : 0xffff7777);
        if (tooltip != null && dragging == null && sliding == null && editing == null) {
            String tip = trim(tooltip, width - 16);
            int tw = mc.textRenderer.getWidth(tip) + 8, x = Math.clamp(mx + 8, 2, Math.max(2, width - tw - 2));
            int y = Math.min(my + 15, height - 27);
            ctx.createNewRootLayer();
            ctx.fill(x, y, x + tw, y + 13, 0xee171717);
            text(ctx, tip, x + 4, y + 3, 0xffdddddd);
        }
        if (binding != null || settingBinding != null) {
            String prompt = "Press a key / mouse button. Delete clears; Escape cancels.";
            ctx.createNewRootLayer();
            ctx.fill(0, height / 2 - 11, width, height / 2 + 12, 0xee111111);
            text(ctx, trim(prompt, width - 8), 4, height / 2 - 4, 0xffffffff);
        }
        meteordevelopment.meteorclient.utils.render.BedGangBrand.badge(ctx, width, height);
        runAfterRenderTasks();
    }
    private void drawPanel(DrawContext ctx, Panel panel, int mx, int my) {
        ctx.createNewRootLayer();
        int x = (int) panel.config.x, y = (int) panel.config.y, w = look.panelWidth.get();
        ctx.fill(x, y, x + w, y + HEADER, look.header.get().getPacked());
        text(ctx, trim(panel.category.name, w - 15), x + (w - mc.textRenderer.getWidth(panel.category.name)) / 2, y + 5, 0xffffffff);
        text(ctx, panel.config.expanded ? "-" : "+", x + w - 9, y + 5, 0xffffffff);
        hits.add(new Hit(x, y, w, HEADER, panel, null, null, "header"));
        if (!panel.config.expanded) return;
        List<Module> modules = new ArrayList<>(ImpactRegistry.group(panel.category));
        modules.removeIf(m -> !search.isEmpty() && !(ImpactRegistry.displayName(m) + " " + m.name).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)));
        modules.sort(Comparator.comparing(ImpactRegistry::displayName));
        List<Hit> rows = new ArrayList<>();
        int offset = 0;
        for (Module module : modules) {
            rows.add(new Hit(x + 2, offset, w - 4, ROW, panel, module, null, "module")); offset += ROW;
            if (!expanded.contains(module)) continue;
            for (SettingGroup group : module.settings) for (Setting<?> setting : group) {
                if (!setting.isVisible()) continue;
                rows.add(new Hit(x + 4, offset, w - 9, ROW, panel, module, setting, "setting")); offset += ROW;
            }
            rows.add(new Hit(x + 4, offset, w - 9, ROW, panel, module, null, "bind")); offset += ROW;
            rows.add(new Hit(x + 4, offset, w - 9, ROW, panel, module, null, "full")); offset += ROW;
        }
        panel.total = offset;
        int columns = ImpactPanelLayout.columns(width, w);
        int bands = ImpactPanelLayout.bands(panels.size(), columns);
        int cap = ImpactPanelLayout.bandHeight(height, bands) - HEADER - 4;
        panel.view = Math.min(offset, Math.min(cap, Math.max(0, height - y - HEADER - 36)));
        panel.scroll = ImpactPanelLayout.scroll(panel.scroll, offset, panel.view);
        int top = y + HEADER + 2, bottom = top + panel.view;
        ctx.fill(x, y + HEADER, x + w, bottom + 2, 0xc81e1e1e);
        hits.add(new Hit(x, y + HEADER, w, panel.view + 4, panel, null, null, "body"));
        ctx.enableScissor(x, top, x + w, bottom);
        for (Hit row : rows) {
            int ry = top + row.y - panel.scroll;
            if (ry + ROW <= top || ry >= bottom) continue;
            Hit hit = new Hit(row.x, ry, row.width, ROW, panel, row.module, row.setting, row.kind);
            boolean hover = mx >= x && mx < x + w && my >= top && my < bottom && hit.contains(mx, my);
            boolean on = row.kind.equals("module") ? row.module.isActive() : row.setting instanceof BoolSetting b && b.get();
            ctx.fill(hit.x, ry, hit.x + hit.width, ry + ROW - 1, on ? (hover ? 0xff555555 : 0xff414141) : hover ? 0xff333333 : 0x001e1e1e);
            if (row.setting instanceof IntSetting || row.setting instanceof DoubleSetting) {
                double fraction = fraction(row.setting);
                ctx.fill(hit.x, ry, hit.x + (int) (hit.width * fraction), ry + ROW - 1, look.accent(ry, hover));
            }
            String label;
            if (row.kind.equals("module")) label = ImpactRegistry.displayName(row.module);
            else if (row.kind.equals("bind")) label = "Bind " + row.module.keybind;
            else if (row.kind.equals("full")) label = "Full settings...";
            else label = row.setting == editing ? row.setting.title + " " + buffer + "_" : settingLabel(row.setting);
            if (row.kind.equals("module")) {
                text(ctx, expanded.contains(row.module) ? "v" : ">", hit.x + 4, ry + 5, 0xffaaaaaa);
                text(ctx, trim(label, hit.width - (hover ? 42 : 17)), hit.x + 14, ry + 5, on ? 0xffffffff : 0xffaaaaaa);
                if (hover) {
                    String badge = on ? "ON" : "OFF";
                    int bx = hit.x + hit.width - 24;
                    ctx.fill(bx, ry + 3, hit.x + hit.width - 2, ry + ROW - 3, on ? 0xff5bc94f : 0xffc23030);
                    text(ctx, badge, bx + 2, ry + 5, 0xffffffff);
                }
            } else {
                ctx.fill(hit.x + 3, ry, hit.x + 4, ry + ROW, 0xff777777);
                if (row.setting instanceof BoolSetting) {
                    ctx.fill(hit.x + 8, ry + 5, hit.x + 15, ry + 12, on ? look.accent(ry, hover) : 0xff555555);
                    text(ctx, trim(label, hit.width - 20), hit.x + 18, ry + 5, 0xffdddddd);
                } else text(ctx, trim(label, hit.width - 12), hit.x + 9, ry + 5, 0xffdddddd);
            }
            // Clip hit boxes as well as rendering so hidden rows cannot receive clicks.
            int hy = Math.max(top, ry), hh = Math.min(bottom, ry + ROW) - hy;
            hits.add(new Hit(hit.x, hy, hit.width, hh, panel, hit.module, hit.setting, hit.kind));
            if (hover) tooltip = row.setting == null ? ImpactRegistry.description(row.module) : row.setting.description;
        }
        ctx.disableScissor();
        if (offset > panel.view && panel.view > 0) {
            int thumb = Math.max(5, panel.view * panel.view / offset);
            int sy = top + (panel.view - thumb) * panel.scroll / Math.max(1, offset - panel.view);
            ctx.fill(x + w - 2, sy, x + w - 1, sy + thumb, look.accent(sy, false));
        }
        if (look.outline.get()) {
            int c = look.accent(y, false);
            ctx.fill(x, y, x + w, y + 1, c); ctx.fill(x, bottom + 1, x + w, bottom + 2, c);
            ctx.fill(x, y, x + 1, bottom + 2, c); ctx.fill(x + w - 1, y, x + w, bottom + 2, c);
        }
    }
    private String settingLabel(Setting<?> setting) {
        if (setting instanceof BoolSetting) return setting.title;
        if (setting instanceof DoubleSetting d) return setting.title + " " + String.format(Locale.ROOT, "%." + d.decimalPlaces + "f", sliding != null && sliding.setting == d && sliderPreview != null ? sliderPreview : d.get());
        if (setting instanceof IntSetting || setting instanceof StringSetting || setting instanceof EnumSetting<?> || setting instanceof KeybindSetting) return setting.title + " " + setting.get();
        return setting.title + " ...";
    }
    private void text(DrawContext ctx, String text, int x, int y, int color) { ctx.drawText(mc.textRenderer, text, x, y, color, look.shadow.get()); }
    private String trim(String text, int width) { return mc.textRenderer.trimToWidth(text, Math.max(0, width)); }
    private Hit hit(double x, double y) {
        for (int i = hits.size() - 1; i >= 0; i--) if (hits.get(i).contains(x, y)) return hits.get(i);
        return null;
    }
    @Override public boolean mouseClicked(Click click, boolean doubled) {
        if (binding != null || settingBinding != null) {
            if (click.button() > 1) finishBind(Keybind.fromButton(click.button()));
            return true;
        }
        Hit hit = hit(click.x(), click.y());
        if (editing != null && (hit == null || hit.setting != editing)) { if (!commit()) return true; }
        searchFocused = false; error = "";
        if (hit == null) return true;
        switch (hit.kind) {
            case "Back" -> close();
            case "HUD" -> {
                var hud = Modules.get().get(ImpactHud.class);
                if (click.button() == 1) mc.setScreen(look.moduleScreen(hud)); else hud.toggle();
            }
            case "Reset" -> arrange(true);
            case "Style" -> mc.setScreen(new meteordevelopment.meteorclient.gui.WindowScreen(look, "Impact Style") { public void initWidgets() { add(look.settings(look.settings)).expandX(); } public void removed() { look.saveOwn(); super.removed(); } });
            case "search" -> searchFocused = true;
            case "header" -> {
                if (click.button() == 1) hit.panel.config.expanded = !hit.panel.config.expanded;
                else if (click.button() == 0) { dragging = hit.panel; dragX = click.x() - dragging.config.x; dragY = click.y() - dragging.config.y; panels.remove(dragging); panels.add(dragging); }
            }
            case "module" -> {
                if (click.button() == 0) hit.module.toggle();
                else if (click.button() == 2) binding = hit.module;
                else if (click.button() == 1 && shift()) mc.setScreen(look.moduleScreen(hit.module));
                else if (click.button() == 1) { if (!expanded.remove(hit.module)) expanded.add(hit.module); }
            }
            case "bind" -> binding = hit.module;
            case "full" -> mc.setScreen(look.moduleScreen(hit.module));
            case "setting" -> change(hit, click);
        }
        GuiKeyEvents.canUseKeys = editing == null && binding == null && settingBinding == null && !searchFocused;
        return true;
    }
    private boolean shift() { return meteordevelopment.meteorclient.utils.misc.input.Input.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || meteordevelopment.meteorclient.utils.misc.input.Input.isKeyPressed(GLFW_KEY_RIGHT_SHIFT); }
    @SuppressWarnings({"rawtypes", "unchecked"}) private void change(Hit hit, Click click) {
        Setting setting = hit.setting;
        if (setting instanceof BoolSetting b) b.set(!b.get());
        else if (setting instanceof EnumSetting<?>) {
            Enum value = (Enum) setting.get(); Object[] options = value.getDeclaringClass().getEnumConstants();
            setting.set(options[Math.floorMod(value.ordinal() + (click.button() == 1 ? -1 : 1), options.length)]);
        } else if (setting instanceof IntSetting i && !i.noSlider && click.button() == 0) { sliding = hit; slide(hit, click.x()); }
        else if (setting instanceof DoubleSetting d && !d.noSlider && click.button() == 0) { sliding = hit; slide(hit, click.x()); }
        else if (setting instanceof StringSetting || setting instanceof IntSetting || setting instanceof DoubleSetting) { editing = setting; buffer = setting.get().toString(); }
        else if (setting instanceof KeybindSetting) settingBinding = (Setting<Keybind>) setting;
        else mc.setScreen(look.moduleScreen(hit.module));
    }
    private double fraction(Setting<?> s) {
        double min = s instanceof IntSetting i ? Math.max(i.min, i.sliderMin) : Math.max(((DoubleSetting) s).min, ((DoubleSetting) s).sliderMin);
        double max = s instanceof IntSetting i ? Math.min(i.max, i.sliderMax) : Math.min(((DoubleSetting) s).max, ((DoubleSetting) s).sliderMax);
        double value = sliding != null && sliding.setting == s && sliderPreview != null ? sliderPreview : ((Number) s.get()).doubleValue();
        return max > min ? Math.clamp((value - min) / (max - min), 0, 1) : 0;
    }
    private void slide(Hit hit, double x) {
        double f = Math.clamp((x - hit.x) / hit.width, 0, 1);
        if (hit.setting instanceof IntSetting i) {
            double min = Math.max(i.min, i.sliderMin), max = Math.min(i.max, i.sliderMax);
            if (max >= min) i.set((int) Math.round(min + f * (max - min)));
        } else if (hit.setting instanceof DoubleSetting d) {
            double min = Math.max(d.min, d.sliderMin), max = Math.min(d.max, d.sliderMax), factor = Math.pow(10, d.decimalPlaces);
            if (max >= min) {
                double value = Math.clamp(Math.round((min + f * (max - min)) * factor) / factor, min, max);
                if (d.onSliderRelease) sliderPreview = value; else d.set(value);
            }
        }
    }
    @Override public boolean mouseReleased(Click click) {
        if (sliding != null && sliding.setting instanceof DoubleSetting d && sliderPreview != null) d.set(sliderPreview);
        dragging = null; sliding = null; sliderPreview = null; return true;
    }
    @Override public void mouseMoved(double x, double y) { if (sliding != null) slide(sliding, x); }
    @Override public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        Hit target = hit(x, y);
        if (target != null && target.panel != null) target.panel.scroll = ImpactPanelLayout.scroll(target.panel.scroll - (int) (vertical * ROW * 2), target.panel.total, target.panel.view);
        return true;
    }
    @Override public boolean keyPressed(KeyInput key) {
        if (binding != null || settingBinding != null) {
            if (key.key() == GLFW_KEY_ESCAPE) { binding = null; settingBinding = null; }
            else if (key.key() == GLFW_KEY_DELETE || key.key() == GLFW_KEY_BACKSPACE) finishBind(Keybind.none());
            else if (Keybind.none().canBindTo(true, key.key(), key.modifiers())) finishBind(Keybind.fromKeys(key.key(), key.modifiers()));
            return true;
        }
        if (key.key() == GLFW_KEY_ESCAPE) {
            if (editing != null) { editing = null; error = ""; }
            else if (searchFocused || !search.isEmpty()) { searchFocused = false; search = ""; }
            else close();
            return true;
        }
        if ((key.modifiers() & GLFW_MOD_CONTROL) != 0 && key.key() == GLFW_KEY_F) { searchFocused = true; return true; }
        if (editing != null || searchFocused) {
            if (key.key() == GLFW_KEY_ENTER || key.key() == GLFW_KEY_KP_ENTER) { if (editing != null) commit(); searchFocused = false; }
            else if (key.key() == GLFW_KEY_BACKSPACE) { if (editing != null) buffer = backspace(buffer); else search = backspace(search); }
            else if ((key.modifiers() & GLFW_MOD_CONTROL) != 0 && key.key() == GLFW_KEY_A) { if (editing != null) buffer = ""; else search = ""; }
            return true;
        }
        return super.keyPressed(key);
    }
    private String backspace(String s) { return s.isEmpty() ? s : s.substring(0, s.offsetByCodePoints(s.length(), -1)); }
    @Override public void keyRepeated(KeyInput key) { keyPressed(key); }
    @Override public boolean charTyped(CharInput input) {
        String text = new String(Character.toChars(input.codepoint()));
        if (Character.isISOControl(input.codepoint())) return false;
        if (editing != null && buffer.length() < 512) buffer += text;
        else if (searchFocused && search.length() < 80) search += text;
        return editing != null || searchFocused;
    }
    private boolean commit() {
        try {
            boolean ok;
            if (editing instanceof IntSetting i) ok = i.set(Integer.parseInt(buffer.trim()));
            else if (editing instanceof DoubleSetting d) { double v = Double.parseDouble(buffer.trim()); ok = Double.isFinite(v) && d.set(v); }
            else if (editing instanceof StringSetting s) ok = s.set(buffer);
            else ok = false;
            if (!ok) { error = "Value is outside this setting's allowed range."; return false; }
            editing = null; error = ""; return true;
        } catch (NumberFormatException e) { error = "Enter a valid number; Escape cancels."; return false; }
    }
    private void finishBind(Keybind value) {
        if (binding != null) {
            binding.keybind.set(value);
            meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.post(meteordevelopment.meteorclient.events.meteor.ModuleBindChangedEvent.get(binding));
        }
        if (settingBinding != null) settingBinding.set(value);
        binding = null; settingBinding = null; GuiKeyEvents.canUseKeys = true;
    }
    @Override public void close() { editing = null; binding = null; settingBinding = null; mc.setScreen(parent); }
    @Override public void removed() { dragging = null; sliding = null; GuiKeyEvents.canUseKeys = true; look.saveOwn(); super.removed(); }
}

