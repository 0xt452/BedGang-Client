package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import java.util.*;

/** Modern adaptation of Phobos ArmorMessage. Alerts are local. */
public class ArmorMessage extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Integer> threshold = sg.add(new IntSetting.Builder().name("armor-percent").description("Warn at or below this remaining durability percentage.").defaultValue(20).range(1, 100).sliderRange(1, 100).build());
    private final Setting<Boolean> self = sg.add(new BoolSetting.Builder().name("notify-self").description("Check your own armor.").defaultValue(true).build());
    private final Setting<Boolean> friends = sg.add(new BoolSetting.Builder().name("notify-friends").description("Show local warnings for nearby friends.").defaultValue(true).build());
    private final Set<String> warned = new HashSet<>();
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public ArmorMessage() { super(Categories.PhobosPort, "armor-message", "Warns locally when your or a friend's armor is low."); }
    @Override public void onActivate() { warned.clear(); }
    @Override public void onDeactivate() { warned.clear(); }
    @EventHandler private void onJoin(GameJoinedEvent event) { warned.clear(); }
    @EventHandler private void onTick(TickEvent.Post event) {
        Set<String> low = new HashSet<>();
        for (var player : mc.world.getPlayers()) {
            if (!player.isAlive() || (player == mc.player ? !self.get() : !friends.get() || !Friends.get().isFriend(player))) continue;
            for (var slot : ARMOR) {
                var stack = player.getEquippedStack(slot);
                if (stack.isEmpty() || !stack.isDamageable()) continue;
                double remaining = 100.0 * (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage();
                if (remaining > threshold.get()) continue;
                String key = player.getUuid() + ":" + slot;
                low.add(key);
                if (warned.add(key)) warning("%s: %s has %.0f%% durability left.", player.getName().getString(), stack.getName().getString(), remaining);
            }
        }
        warned.retainAll(low);
    }
}
