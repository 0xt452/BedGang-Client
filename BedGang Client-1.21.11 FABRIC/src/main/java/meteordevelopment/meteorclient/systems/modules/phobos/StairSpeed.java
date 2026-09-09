package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class StairSpeed extends Module {
    public StairSpeed() { super(Categories.PhobosPort, "stair-speed", "Jumps while moving forward on fractional-height blocks, as in Phobos."); }
    @EventHandler private void onTick(TickEvent.Pre event) {
        if (mc.currentScreen != null || mc.player.hasVehicle() || mc.player.isSneaking() || mc.player.isTouchingWater() || mc.player.isInLava()) return;
        double fraction = mc.player.getY() - Math.floor(mc.player.getY());
        if (mc.player.isOnGround() && mc.options.forwardKey.isPressed() && fraction > 1e-4 && fraction < 0.9999) mc.player.jump();
    }
}
