package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AntiVanish extends Module {
    private final Queue<UUID> pending = new ConcurrentLinkedQueue<>();
    private final Set<UUID> warned = new HashSet<>();
    public AntiVanish() { super(Categories.PhobosPort, "anti-vanish", "Flags latency updates for unknown players. A heuristic, not proof of vanish."); }
    private void clear() { pending.clear(); warned.clear(); }
    @Override public void onActivate() { clear(); }
    @Override public void onDeactivate() { clear(); }
    @EventHandler private void onJoin(GameJoinedEvent event) { clear(); }
    @EventHandler private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket packet
            && packet.getActions().contains(PlayerListS2CPacket.Action.UPDATE_LATENCY)
            && !packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            for (var entry : packet.getEntries()) pending.add(entry.profileId());
        }
    }
    @EventHandler private void onTick(TickEvent.Post event) {
        warned.removeIf(id -> mc.getNetworkHandler().getPlayerListEntry(id) != null);
        UUID id;
        while ((id = pending.poll()) != null) {
            if (mc.getNetworkHandler().getPlayerListEntry(id) == null && warned.size() < 4096 && warned.add(id)) {
                warning("Possible vanished player: %s (unknown-player latency update).", id);
            }
        }
    }
}
