package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import org.joml.Vector3d;

public class OffscreenESP extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> offscreen = sg.add(new BoolSetting.Builder().name("offscreen-only").description("Hide arrows for player centers visible on screen.").defaultValue(true).build());
    private final Setting<Boolean> friends = sg.add(new BoolSetting.Builder().name("show-friends").description("Include friends.").defaultValue(true).build());
    private final Setting<Double> radius = sg.add(new DoubleSetting.Builder().name("radius").description("Arrow ring radius as a fraction of half the screen size.").defaultValue(0.65).range(0.1, 0.9).sliderRange(0.1, 0.9).build());
    private final Setting<Integer> size = sg.add(new IntSetting.Builder().name("size").description("Arrow size in framebuffer pixels.").defaultValue(12).range(4, 40).sliderRange(4, 40).build());
    private final Setting<SettingColor> color = sg.add(new ColorSetting.Builder().name("color").description("Player arrow color.").defaultValue(new SettingColor(255, 80, 200, 220)).build());
    private final Setting<SettingColor> friendColor = sg.add(new ColorSetting.Builder().name("friend-color").description("Friend arrow color.").defaultValue(new SettingColor(80, 180, 255, 220)).build());
    public OffscreenESP() { super(Categories.PhobosPort, "offscreen-esp", "Shows directional player arrows around the screen center (Phobos ArrowESP)."); }
    @EventHandler private void onRender(Render2DEvent event) {
        if (mc.options.hudHidden) return;
        double width = mc.getWindow().getFramebufferWidth(), height = mc.getWindow().getFramebufferHeight();
        double cx = width / 2, cy = height / 2, r = Math.min(cx, cy) * radius.get();
        var camera = mc.gameRenderer.getCamera();
        Renderer2D renderer = Renderer2D.COLOR;
        renderer.begin();
        for (var player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            boolean friend = Friends.get().isFriend(player);
            if (friend && !friends.get()) continue;
            double x = player.lastX + (player.getX() - player.lastX) * event.tickDelta;
            double y = player.lastY + (player.getY() - player.lastY) * event.tickDelta + player.getHeight() / 2;
            double z = player.lastZ + (player.getZ() - player.lastZ) * event.tickDelta;
            Vector3d screen = new Vector3d(x, y, z);
            if (offscreen.get() && NametagUtils.to2D(screen, 1, false) && screen.x >= 0 && screen.x <= width && screen.y >= 0 && screen.y <= height) continue;
            double dx = x - camera.getCameraPos().x, dz = z - camera.getCameraPos().z;
            if (dx * dx + dz * dz < 1e-8) continue;
            double angle = Math.atan2(-dx, dz) - Math.toRadians(camera.getYaw());
            double ux = Math.sin(angle), uy = -Math.cos(angle), s = size.get();
            double tx = cx + ux * r, ty = cy + uy * r;
            renderer.triangle(tx + ux * s, ty + uy * s,
                tx - ux * s * 0.6 - uy * s * 0.6, ty - uy * s * 0.6 + ux * s * 0.6,
                tx - ux * s * 0.6 + uy * s * 0.6, ty - uy * s * 0.6 - ux * s * 0.6,
                friend ? friendColor.get() : color.get());
        }
        renderer.render();
    }
}
