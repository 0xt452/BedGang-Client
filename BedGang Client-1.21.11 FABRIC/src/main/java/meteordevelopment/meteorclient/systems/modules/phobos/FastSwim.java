package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;

public class FastSwim extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> waterHorizontal = multiplier("water-horizontal", 3);
    private final Setting<Double> waterVertical = multiplier("water-vertical", 3);
    private final Setting<Double> lavaHorizontal = multiplier("lava-horizontal", 4);
    private final Setting<Double> lavaVertical = multiplier("lava-vertical", 4);
    private Setting<Double> multiplier(String name, double value) {
        return sg.add(new DoubleSetting.Builder().name(name).description("Movement multiplier while swimming off the ground.").defaultValue(value).range(1, 20).sliderRange(1, 5).build());
    }
    public FastSwim() { super(Categories.PhobosPort, "fast-swim", "Multiplies horizontal and vertical movement in water and lava."); }
    @EventHandler private void onMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || mc.player.isOnGround() || mc.player.hasVehicle() || mc.player.isGliding()) return;
        boolean lava = mc.player.isInLava();
        if (!lava && !mc.player.isTouchingWater()) return;
        double h = (lava ? lavaHorizontal : waterHorizontal).get();
        double v = (lava ? lavaVertical : waterVertical).get();
        ((IVec3d) event.movement).meteor$set(event.movement.x * h, event.movement.y * v, event.movement.z * h);
    }
}
