package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.entity.player.AttackEntityEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import java.util.*;

public class AutoGG extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<String> message = sg.add(new StringSetting.Builder().name("message").description("Chat message after a recently attacked player dies. <player> is replaced by their name.").defaultValue("gg <player>").build());
    private final Setting<Integer> reset = sg.add(new IntSetting.Builder().name("target-timeout-seconds").description("Forget a target this long after your last direct attack.").defaultValue(30).range(1, 90).sliderRange(1, 90).build());
    private final Setting<Integer> delay = sg.add(new IntSetting.Builder().name("cooldown-seconds").description("Minimum time between messages.").defaultValue(10).range(1, 60).sliderRange(1, 60).build());
    private final Setting<Boolean> ownDeath = sg.add(new BoolSetting.Builder().name("own-death").description("Also send the message on your own death.").defaultValue(false).build());
    private final Map<UUID, Long> targets = new HashMap<>();
    private long nextMessage;
    private boolean dead;
    public AutoGG() { super(Categories.PhobosPort, "auto-gg", "Sends a configurable GG when a recently directly attacked player dies."); }
    private void clear() { targets.clear(); nextMessage = 0; dead = false; }
    @Override public void onActivate() { clear(); }
    @Override public void onDeactivate() { clear(); }
    @EventHandler private void onJoin(GameJoinedEvent event) { clear(); }
    @EventHandler private void onAttack(AttackEntityEvent event) {
        if (!event.isCancelled() && event.entity instanceof PlayerEntity player && player != mc.player) targets.put(player.getUuid(), System.nanoTime());
    }
    @EventHandler private void onTick(TickEvent.Post event) {
        long now = System.nanoTime();
        var iterator = targets.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var player = mc.world.getPlayerByUuid(entry.getKey());
            if (player == null || now - entry.getValue() > reset.get() * 1_000_000_000L) { iterator.remove(); continue; }
            if (!player.isAlive()) { announce(player, now); iterator.remove(); }
        }
        boolean nowDead = !mc.player.isAlive();
        if (ownDeath.get() && nowDead && !dead) announce(mc.player, now);
        dead = nowDead;
    }
    private void announce(PlayerEntity player, long now) {
        if (now < nextMessage) return;
        String text = message.get().replace("<player>", player.getName().getString()).replaceAll("[\\p{Cntrl}§]", "").strip();
        if (text.isEmpty()) return;
        if (text.length() > 256) text = text.substring(0, 256);
        mc.getNetworkHandler().sendChatMessage(text);
        nextMessage = now + delay.get() * 1_000_000_000L;
    }
}
