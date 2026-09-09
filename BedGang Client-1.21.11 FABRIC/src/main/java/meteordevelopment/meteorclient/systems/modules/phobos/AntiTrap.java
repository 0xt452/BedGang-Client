package meteordevelopment.meteorclient.systems.modules.phobos;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import java.util.*;

public class AntiTrap extends Module {
    private final SettingGroup sg = settings.getDefaultGroup();
    private final Setting<Boolean> rotate = sg.add(new BoolSetting.Builder().name("rotate").description("Rotate toward the crystal base.").defaultValue(true).build());
    private final Setting<Boolean> sortY = sg.add(new BoolSetting.Builder().name("sort-y").description("Prefer lower bases before distance from the nearest enemy.").defaultValue(true).build());
    private final Setting<Integer> timeout = sg.add(new IntSetting.Builder().name("timeout").description("Disable after this many ticks without a suitable base.").defaultValue(20).range(1, 100).sliderRange(1, 100).build());
    private int ticks;
    private boolean pending;
    private int generation;
    public AntiTrap() { super(Categories.PhobosPort, "anti-trap", "Places one nearby crystal to obstruct trapping, then disables. Does not detonate it."); }
    @Override public void onActivate() { ticks = 0; pending = false; generation++; }
    @Override public void onDeactivate() { pending = false; generation++; }
    @EventHandler private void onTick(TickEvent.Pre event) {
        if (++ticks > timeout.get()) { warning("No placement completed before timeout."); toggle(); return; }
        if (pending) return;
        if (!InvUtils.findInHotbar(Items.END_CRYSTAL).found()) { warning("No end crystals in hotbar or offhand."); toggle(); return; }
        BlockPos origin = mc.player.getBlockPos();
        List<BlockPos> candidates = new ArrayList<>();
        for (int y = 0; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (x == 0 && z == 0) continue;
            BlockPos pos = origin.add(x, y, z);
            if (canPlace(pos)) candidates.add(pos);
        }
        PlayerEntity enemy = mc.world.getPlayers().stream().filter(p -> p != mc.player && p.isAlive() && !Friends.get().isFriend(p) && mc.player.squaredDistanceTo(p) <= 36)
            .min(Comparator.comparingDouble(p -> mc.player.squaredDistanceTo(p))).orElse(null);
        if (enemy != null) candidates.sort(Comparator.comparingDouble((BlockPos p) -> enemy.squaredDistanceTo(Vec3d.ofCenter(p))).reversed());
        if (sortY.get()) candidates.sort(Comparator.comparingInt(BlockPos::getY));
        if (candidates.isEmpty()) return;
        BlockPos pos = candidates.getFirst();
        pending = true;
        int token = generation;
        if (rotate.get()) {
            Vec3d hit = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            Rotations.rotate(Rotations.getYaw(hit), Rotations.getPitch(hit), 50, () -> place(pos, token));
        } else place(pos, token);
    }
    private boolean canPlace(BlockPos pos) {
        var state = mc.world.getBlockState(pos);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) return false;
        if (!mc.world.isAir(pos.up())) return false;
        Vec3d hit = Vec3d.ofCenter(pos).add(0, 0.5, 0);
        if (mc.player.getEyePos().squaredDistanceTo(hit) > mc.player.getBlockInteractionRange() * mc.player.getBlockInteractionRange()) return false;
        return mc.world.getOtherEntities(null, new Box(pos.up()).stretch(0, 1, 0)).isEmpty();
    }
    private void place(BlockPos pos, int token) {
        if (!isActive() || token != generation || mc.world == null || mc.player == null || mc.interactionManager == null) return;
        pending = false;
        if (!canPlace(pos)) return;
        var crystals = InvUtils.findInHotbar(Items.END_CRYSTAL);
        if (!crystals.found()) return;
        int previous = mc.player.getInventory().getSelectedSlot();
        boolean offhand = crystals.isOffhand();
        if (!offhand && !InvUtils.swap(crystals.slot(), false)) return;
        try {
            Hand hand = offhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            var result = mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos, false));
            if (result.isAccepted()) { mc.player.swingHand(hand); toggle(); }
        } finally {
            if (!offhand) InvUtils.swap(previous, false);
        }
    }
}
