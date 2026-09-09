package meteordevelopment.meteorclient.gui.themes.phobos;

import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.MeteorGuiTheme;
import meteordevelopment.meteorclient.gui.utils.AlignmentX;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.gui.screen.Screen;

/** Phobos 1.9-style panels, with Meteor's full setting editors available as a fallback. */
public class PhobosGuiTheme extends MeteorGuiTheme {
    private final SettingGroup classic = settings.createGroup("Phobos Panels");
    public final Setting<Integer> panelWidth = classic.add(new IntSetting.Builder().name("panel-width").description("Classic Phobos used 88 pixels; increase for long modern module names.").defaultValue(88).range(88, 200).sliderRange(88, 160).build());
    public final Setting<SettingColor> header = classic.add(new ColorSetting.Builder().name("header").description("Classic category header color.").defaultValue(new SettingColor(136, 136, 136)).build());
    public final Setting<Boolean> outline = classic.add(new BoolSetting.Builder().name("panel-outline").description("Draw a thin accent outline around each category.").defaultValue(false).build());
    public final Setting<Boolean> rainbow = classic.add(new BoolSetting.Builder().name("rolling-rainbow").description("Roll the accent hue down the panels.").defaultValue(false).build());
    public final Setting<Boolean> shadow = classic.add(new BoolSetting.Builder().name("text-shadow").description("Shadow the Minecraft font, like the original Click GUI.").defaultValue(true).build());

    public PhobosGuiTheme() {
        super("Phobos");
        accentColor.getDefaultValue().set(255, 0, 0, 180);
        accentColor.reset();
        checkboxColor.getDefaultValue().set(255, 0, 0, 180);
        checkboxColor.reset();
        moduleAlignment.set(AlignmentX.Left);
        moduleBackground.getDefaultValue().set(255, 0, 0, 180);
        moduleBackground.reset();
        textSecondaryColor.set(new SettingColor(170, 170, 170));
    }

    public int accent(double y, boolean hover) {
        int rgb = rainbow.get()
            ? java.awt.Color.HSBtoRGB((float) ((System.currentTimeMillis() % 6000L) / 6000.0 + y / 900.0), 1, 1) & 0xffffff
            : accentColor.get().getPacked() & 0xffffff;
        return ((hover ? 240 : accentColor.get().a) << 24) | rgb;
    }

    @Override public TabScreen modulesScreen() { return new PhobosModulesScreen(this); }
    @Override public boolean isModulesScreen(Screen screen) { return screen instanceof PhobosModulesScreen; }
}
