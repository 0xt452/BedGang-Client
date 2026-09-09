package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class PhobosGui extends Module {
    public PhobosGui() {
        super(Categories.PhobosPort, "phobos-gui", "Selects the Phobos theme and opens its classic Click GUI. Your normal GUI key will use it too.");
        runInMainMenu = true;
        autoSubscribe = false;
        chatFeedback = false;
    }
    @Override public void onActivate() {
        toggle();
        GuiThemes.select("Phobos");
        Tabs.get().getFirst().openScreen(GuiThemes.get());
    }
}
