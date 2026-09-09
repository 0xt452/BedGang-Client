package meteordevelopment.meteorclient.gui.themes.impact;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtIo;
import java.io.File;
import java.io.IOException;

/** Independently owned and persisted; opening it never selects a global GUI theme. */
public final class ImpactGuiTheme extends MeteorGuiTheme {
    private final SettingGroup panels = settings.createGroup("Impact Panels");
    public final Setting<Integer> panelWidth = panels.add(new IntSetting.Builder().name("panel-width").description("Width in Minecraft GUI pixels.").defaultValue(130).range(110, 240).sliderRange(110, 200).build());
    public final Setting<SettingColor> header = panels.add(new ColorSetting.Builder().name("header").description("Impact's dark gray headers.").defaultValue(new SettingColor(30, 30, 30, 230)).build());
    public final Setting<Boolean> shadow = panels.add(new BoolSetting.Builder().name("text-shadow").description("Draw text shadows.").defaultValue(true).build());
    public final Setting<Boolean> outline = panels.add(new BoolSetting.Builder().name("outline").description("Blue category borders.").defaultValue(true).build());
    public ImpactGuiTheme() {
        super("Impact");
        accentColor.getDefaultValue().set(85, 85, 255, 255); accentColor.reset();
        checkboxColor.getDefaultValue().set(85, 85, 255, 255); checkboxColor.reset();
        moduleBackground.getDefaultValue().set(70, 70, 70, 220); moduleBackground.reset();
    }
    public int accent(double y, boolean hover) { return accentColor.get().getPacked(); }
    private File file() { return new File(MeteorClient.FOLDER, "gui/impact-port.nbt"); }
    public void loadOwn() {
        if (!file().exists()) return;
        try { var tag = NbtIo.read(file().toPath()); if (tag != null) fromTag(tag); }
        catch (IOException e) { MeteorClient.LOG.error("Could not load Impact GUI settings", e); }
    }
    public void saveOwn() {
        try { file().getParentFile().mkdirs(); NbtIo.write(toTag(), file().toPath()); }
        catch (IOException e) { MeteorClient.LOG.error("Could not save Impact GUI settings", e); }
    }
    @Override public TabScreen modulesScreen() { return new ImpactModulesScreen(this); }
    @Override public boolean isModulesScreen(Screen screen) { return screen instanceof ImpactModulesScreen; }
}
