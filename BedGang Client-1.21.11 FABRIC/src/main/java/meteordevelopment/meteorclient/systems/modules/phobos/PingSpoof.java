package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import java.util.ArrayDeque;

public class PingSpoof extends Module {
    private final Setting<Integer> delay = settings.getDefaultGroup().add(new IntSetting.Builder().name("delay-ms").description("Delay keepalive replies. High values can cause a server timeout.").defaultValue(100).range(0, 5000).sliderRange(0, 1000).build());
    private record Reply(KeepAliveC2SPacket packet, ClientPlayNetworkHandler connection, long due) {}
    private final ArrayDeque<Reply> replies = new ArrayDeque<>();
    private boolean flushing;
    public PingSpoof() { super(Categories.PhobosPort, "ping-spoof", "Delays keepalive replies; does not delay movement or improve latency."); }
    @Override public synchronized void onActivate() { replies.clear(); }
    @Override public synchronized void onDeactivate() { flush(true); }
    @EventHandler private synchronized void onLeave(GameLeftEvent event) { replies.clear(); }
    @EventHandler private synchronized void onSend(PacketEvent.Send event) {
        if (flushing || mc.isInSingleplayer() || mc.getNetworkHandler() == null || delay.get() == 0) return;
        if (event.packet instanceof KeepAliveC2SPacket packet) {
            if (replies.size() >= 64) flush(true);
            replies.addLast(new Reply(packet, mc.getNetworkHandler(), System.nanoTime() + delay.get() * 1_000_000L));
            event.cancel();
        }
    }
    @EventHandler private synchronized void onTick(TickEvent.Post event) { flush(false); }
    private void flush(boolean all) {
        flushing = true;
        try {
            long now = System.nanoTime();
            while (!replies.isEmpty() && (all || replies.peekFirst().due <= now)) {
                Reply reply = replies.removeFirst();
                if (reply.connection == mc.getNetworkHandler() && reply.connection.getConnection().isOpen()) reply.connection.sendPacket(reply.packet);
            }
        } finally { flushing = false; }
    }
}
