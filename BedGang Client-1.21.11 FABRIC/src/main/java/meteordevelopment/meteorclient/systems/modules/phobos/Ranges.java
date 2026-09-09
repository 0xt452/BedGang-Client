package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;

public class Ranges extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Double> radius = sg.add(new DoubleSetting.Builder().name("radius").description("Range radius in blocks.").defaultValue(4.5).range(0.1, 8).sliderRange(0.1, 8).build());
    private final Setting<Boolean> circle = sg.add(new BoolSetting.Builder().name("circle").description("Draw your horizontal range circle.").defaultValue(true).build());
    private final Setting<Boolean> spheres = sg.add(new BoolSetting.Builder().name("hit-spheres").description("Draw wireframe range spheres around nearby players.").defaultValue(false).build());
    private final Setting<Boolean> own = sg.add(new BoolSetting.Builder().name("own-sphere").description("Include yourself in range spheres.").defaultValue(false).build());
    private final Setting<SettingColor> color = sg.add(new ColorSetting.Builder().name("color").description("Range line color.").defaultValue(new SettingColor(180, 70, 255, 200)).build());
    public Ranges() { super(Categories.PhobosPort, "ranges", "Draws a range circle and player range spheres."); }
    @EventHandler private void onRender(Render3DEvent event) {
        if (circle.get()) ring(event, mc.player, radius.get(), 0.1, 0);
        if (!spheres.get()) return;
        for (var player : mc.world.getPlayers()) {
            if (!player.isAlive() || (player == mc.player && !own.get()) || mc.player.squaredDistanceTo(player) > 4096) continue;
            for (int latitude = -3; latitude <= 3; latitude++) {
                double angle = latitude * Math.PI / 8;
                ring(event, player, radius.get() * Math.cos(angle), radius.get() * Math.sin(angle), 0);
            }
            ring(event, player, radius.get(), 0, 1);
            ring(event, player, radius.get(), 0, 2);
        }
    }
    private void ring(Render3DEvent event, PlayerEntity player, double r, double offset, int plane) {
        double x = player.lastX + (player.getX() - player.lastX) * event.tickDelta;
        double y = player.lastY + (player.getY() - player.lastY) * event.tickDelta;
        double z = player.lastZ + (player.getZ() - player.lastZ) * event.tickDelta;
        for (int i = 0; i < 64; i++) {
            double a = i * Math.PI / 32, b = (i + 1) * Math.PI / 32;
            double ac = Math.cos(a) * r, as = Math.sin(a) * r, bc = Math.cos(b) * r, bs = Math.sin(b) * r;
            if (plane == 0) event.renderer.line(x + ac, y + offset, z + as, x + bc, y + offset, z + bs, color.get());
            else if (plane == 1) event.renderer.line(x + ac, y + as, z, x + bc, y + bs, z, color.get());
            else event.renderer.line(x, y + as, z + ac, x, y + bs, z + bc, color.get());
        }
    }
}
