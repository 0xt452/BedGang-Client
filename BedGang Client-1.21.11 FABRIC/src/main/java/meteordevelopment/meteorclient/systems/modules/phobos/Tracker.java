package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import java.util.Comparator;
import java.util.UUID;

public class Tracker extends Module {
    private final Setting<Double> range = settings.getDefaultGroup().add(new DoubleSetting.Builder().name("target-range").description("Maximum range for acquiring the nearest opponent.").defaultValue(32).range(1, 128).sliderRange(1, 64).build());
    private UUID target;
    private String targetName;
    private int exp, crystals;
    public Tracker() { super(Categories.PhobosPort, "tracker", "Estimates a duel opponent's EXP and crystal usage from nearby spawns. Attribution is approximate."); }
    private void reset() { target = null; targetName = null; exp = crystals = 0; }
    @Override public void onActivate() { reset(); }
    @Override public void onDeactivate() { reset(); }
    @EventHandler private void onJoin(GameJoinedEvent event) { reset(); }
    @EventHandler private void onTick(TickEvent.Post event) {
        if (target == null) {
            var player = mc.world.getPlayers().stream().filter(p -> p != mc.player && p.isAlive() && !Friends.get().isFriend(p) && mc.player.squaredDistanceTo(p) <= range.get() * range.get())
                .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p))).orElse(null);
            if (player != null) { target = player.getUuid(); targetName = player.getName().getString(); info("Tracking %s. Counts are estimates based on nearby spawns.", targetName); }
        } else {
            var player = mc.world.getPlayerByUuid(target);
            if (player == null || !player.isAlive() || !mc.player.isAlive()) {
                info("%s: estimated %d EXP bottles, %d crystals. Tracking ended.", targetName, exp, crystals);
                toggle();
            }
        }
    }
    @EventHandler private void onSpawn(EntityAddedEvent event) {
        if (target == null || !(event.entity instanceof ExperienceBottleEntity || event.entity instanceof EndCrystalEntity)) return;
        var nearest = mc.world.getPlayers().stream().filter(p -> p.isAlive() && p.squaredDistanceTo(event.entity) <= 9)
            .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(event.entity))).orElse(null);
        if (nearest == null || !nearest.getUuid().equals(target)) return;
        if (event.entity instanceof ExperienceBottleEntity) {
            if (++exp % 64 == 0) info("%s: estimated %d stacks of EXP.", targetName, exp / 64);
        } else if (++crystals % 64 == 0) info("%s: estimated %d stacks of crystals.", targetName, crystals / 64);
    }
    @Override public String getInfoString() { return targetName == null ? "Searching" : targetName + " E:" + exp + " C:" + crystals; }
}
