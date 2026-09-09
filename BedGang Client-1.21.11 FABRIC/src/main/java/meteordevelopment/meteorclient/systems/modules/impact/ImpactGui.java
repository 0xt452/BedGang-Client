package meteordevelopment.meteorclient.systems.modules.impact;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;

public final class ImpactGui extends Module {
    public ImpactGui() {
        super(ImpactRegistry.CATEGORY, "impact-gui", "Opens the separate Impact GUI without changing your Meteor/Phobos theme.");
        runInMainMenu = true; autoSubscribe = false; chatFeedback = false;
        keybind.set(Keybind.fromKey(GLFW_KEY_RIGHT_ALT));
    }
    @Override public void onActivate() { toggle(); mc.setScreen(ImpactRegistry.theme().modulesScreen()); }
}
