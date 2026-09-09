package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;

public class Companion extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<String> pop = sg.add(new StringSetting.Builder().name("pop-message").description("Spoken after your totem pops. Use <player> for your name.").defaultValue("<player>, watch out, your totem popped.").build());
    private final Setting<String> death = sg.add(new StringSetting.Builder().name("death-message").description("Spoken after your death.").defaultValue("<player>, you died.").build());
    private boolean dead;
    public Companion() { super(Categories.PhobosPort, "companion", "Speaks local totem-pop and death alerts using Minecraft's narrator."); }
    @Override public void onActivate() { dead = false; }
    @EventHandler private void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet && packet.getStatus() == EntityStatuses.USE_TOTEM_OF_UNDYING) {
            mc.execute(() -> {
                if (isActive() && mc.player != null && mc.world != null && packet.getEntity(mc.world) == mc.player) speak(pop.get());
            });
        }
    }
    @EventHandler private void onTick(TickEvent.Post event) {
        boolean nowDead = !mc.player.isAlive();
        if (nowDead && !dead) speak(death.get());
        dead = nowDead;
    }
    private void speak(String message) {
        mc.getNarratorManager().narrate(Text.literal(message.replace("<player>", mc.player.getName().getString())));
    }
}
