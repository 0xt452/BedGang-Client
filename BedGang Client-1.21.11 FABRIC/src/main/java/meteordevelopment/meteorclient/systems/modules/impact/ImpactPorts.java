package meteordevelopment.meteorclient.systems.modules.impact;

import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.screen.slot.SlotActionType;

/** Rewritten from the uploaded Impact 3.0 behaviors for modern Minecraft APIs. */
public final class ImpactPorts {
    private ImpactPorts() {}
    private abstract static class HoldKey extends ImpactModule {
        private boolean holding;
        HoldKey(String name, String group, String description, String... conflicts) { super(name, group, description, conflicts); }
        protected abstract KeyBinding key();
        @Override protected void update() {
            if (mc.currentScreen != null) { release(); return; }
            key().setPressed(true); holding = true;
        }
        @Override protected void release() {
            if (!holding) return;
            var bound = ((KeyBindingAccessor) key()).meteor$getKey();
            boolean down = bound.getCategory() == InputUtil.Type.MOUSE ? Input.isButtonPressed(bound.getCode()) : bound.getCategory() == InputUtil.Type.KEYSYM && Input.isKeyPressed(bound.getCode());
            key().setPressed(down); holding = false;
        }
    }
    public static final class AutoJump extends HoldKey {
        public AutoJump() { super("AutoJump", "Movement", "Impact's held-jump behavior.", "auto-jump", "parkour", "impact-parkour", "impact-jetpack"); }
        protected KeyBinding key() { return mc.options.jumpKey; }
    }
    public static final class AutoWalk extends HoldKey {
        public AutoWalk() { super("AutoWalk", "Movement", "Impact's held-forward behavior.", "auto-walk"); }
        protected KeyBinding key() { return mc.options.forwardKey; }
    }
    public static final class AutoMine extends HoldKey {
        public AutoMine() { super("AutoMine", "Player", "Holds attack to mine the block under the crosshair.", "auto-clicker", "nuker", "packet-mine"); }
        protected KeyBinding key() { return mc.options.attackKey; }
    }
    public static final class Sneak extends HoldKey {
        public Sneak() { super("Sneak", "Movement", "Impact's normal held-sneak mode. Legacy packet mode is omitted.", "sneak"); }
        protected KeyBinding key() { return mc.options.sneakKey; }
    }
    public static final class Glide extends ImpactModule {
        private final Setting<Double> fall = number("fall-speed", "Downward velocity in blocks/tick.", 0.125, 0.01, 1);
        public Glide() { super("Glide", "Movement", "Impact's constant slow descent.", "flight", "elytra-fly", "reverse-step", "impact-jetpack", "fast-swim"); }
        protected void update() {
            if (!mc.player.isOnGround() && !mc.player.hasVehicle() && !mc.player.isTouchingWater() && !mc.player.isInLava() && !mc.player.isClimbing() && !mc.player.isGliding() && mc.player.getVelocity().y < 0) mc.player.setVelocity(mc.player.getVelocity().x, -fall.get(), mc.player.getVelocity().z);
        }
    }
    public static final class Jetpack extends ImpactModule {
        private final Setting<Double> acceleration = number("acceleration", "Upward acceleration while jump is held.", 0.3, 0.01, 1);
        private final Setting<Double> cap = number("max-rise", "Bound upward speed to avoid runaway acceleration.", 1.5, 0.1, 5);
        public Jetpack() { super("Jetpack", "Movement", "Impact's jump-key vertical thrust with a modern speed cap.", "flight", "elytra-fly", "impact-glide", "impact-auto-jump"); }
        protected void update() {
            if (mc.currentScreen == null && !mc.player.hasVehicle() && !mc.player.isGliding() && mc.options.jumpKey.isPressed()) mc.player.setVelocity(mc.player.getVelocity().x, Math.min(cap.get(), mc.player.getVelocity().y + acceleration.get()), mc.player.getVelocity().z);
        }
    }
    public static final class FastLadder extends ImpactModule {
        private final Setting<Double> speed = number("speed", "Vertical ladder speed.", 0.12, 0.01, 1);
        public FastLadder() { super("FastLadder", "Movement", "Impact's ladder ascent with horizontal motion stopped.", "fast-climb", "impact-spider"); }
        protected void update() { if (mc.currentScreen == null && mc.player.isClimbing() && !mc.player.isSneaking()) mc.player.setVelocity(0, speed.get(), 0); }
    }
    public static final class Spider extends ImpactModule {
        private final Setting<Double> speed = number("speed", "Upward speed against walls.", 0.2, 0.01, 1);
        public Spider() { super("Spider", "Movement", "Impact's normal wall-climb mode; old NCP packet mode is omitted.", "spider", "flight", "impact-fast-ladder"); }
        protected void update() { if (mc.currentScreen == null && !mc.player.hasVehicle() && !mc.player.isSneaking() && mc.player.horizontalCollision) mc.player.setVelocity(mc.player.getVelocity().x, speed.get(), mc.player.getVelocity().z); }
    }
    public static final class Parkour extends ImpactModule {
        public Parkour() { super("Parkour", "Movement", "Impact's automatic jump at block edges.", "parkour", "auto-jump", "impact-auto-jump"); }
        protected void update() {
            if (mc.currentScreen != null || !mc.player.isOnGround() || mc.player.isSneaking() || mc.player.hasVehicle()) return;
            var box = mc.player.getBoundingBox().offset(0, -0.5, 0).expand(-0.001, 0, -0.001);
            if (!mc.world.getBlockCollisions(mc.player, box).iterator().hasNext()) mc.player.jump();
        }
    }
    public static final class AutoRespawn extends ImpactModule {
        private boolean requested;
        public AutoRespawn() { super("AutoRespawn", "Player", "Respawns after death without repeatedly requesting it.", "auto-respawn"); }
        protected void update() { if (mc.player.isAlive()) requested = false; else if (!requested && mc.currentScreen instanceof net.minecraft.client.gui.screen.DeathScreen) { requested = true; mc.player.requestRespawn(); } }
        protected void release() { requested = false; }
    }
    public static final class DeathCoords extends ImpactModule {
        private boolean dead;
        public DeathCoords() { super("DeathCoords", "Misc", "Locally reports death coordinates and dimension."); }
        protected void update() {
            if (!mc.player.isAlive() && !dead) info("You died at X: %d Y: %d Z: %d (%s)", mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), mc.world.getRegistryKey().getValue());
            dead = !mc.player.isAlive();
        }
        protected void release() { dead = false; }
    }
    public static final class ItemSaver extends ImpactModule {
        private final Setting<Double> threshold = number("remaining-durability", "Move a near-broken held item into an empty main-inventory slot.", 1, 1, 100);
        public ItemSaver() { super("ItemSaver", "Misc", "Moves nearly broken main/offhand items out of use; skips open containers.", "auto-tool", "auto-replenish", "impact-auto-tool"); }
        protected void update() {
            if (mc.currentScreen != null || mc.player.currentScreenHandler != mc.player.playerScreenHandler) return;
            int empty = -1;
            for (int i = 9; i < 36; i++) if (mc.player.getInventory().getStack(i).isEmpty()) { empty = i; break; }
            if (empty == -1) return;
            var main = mc.player.getMainHandStack(); var off = mc.player.getOffHandStack();
            int slot = main.isDamageable() && main.getMaxDamage() - main.getDamage() <= threshold.get() ? mc.player.getInventory().getSelectedSlot()
                : off.isDamageable() && off.getMaxDamage() - off.getDamage() <= threshold.get() ? 40 : -1;
            if (slot != -1) mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, empty, slot, SlotActionType.SWAP, mc.player);
        }
    }
    public static final class AutoTool extends ImpactModule {
        private int previous = -1, selected = -1;
        public AutoTool() { super("AutoTool", "Player", "Impact's hotbar mining-tool choice with swap-back on release.", "auto-tool", "auto-weapon", "impact-item-saver"); }
        protected void update() {
            if (mc.currentScreen != null || !mc.options.attackKey.isPressed() || !(mc.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) { release(); return; }
            var state = mc.world.getBlockState(hit.getBlockPos());
            int best = -1; float speed = 1;
            for (int i = 0; i < 9; i++) { float value = mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(state); if (value > speed) { speed = value; best = i; } }
            if (best != -1) { if (previous == -1) previous = mc.player.getInventory().getSelectedSlot(); selected = best; InvUtils.swap(best, false); }
        }
        protected void release() { if (mc.player != null && mc.interactionManager != null && mc.getNetworkHandler() != null && previous != -1 && mc.player.getInventory().getSelectedSlot() == selected) InvUtils.swap(previous, false); previous = selected = -1; }
    }
    public static final class SmoothAim extends ImpactModule {
        private final Setting<Double> range = number("range", "Maximum player target distance.", 4.75, 1, 8);
        private final Setting<Double> fov = number("fov", "Target cone in degrees.", 90, 10, 360);
        private final Setting<Double> smooth = number("smoothness", "Angular error divisor.", 2.75, 1, 10);
        private final Setting<Double> base = number("base-speed", "Minimum angular step in degrees.", 3, 0, 10);
        private final Setting<Boolean> held = general.add(new BoolSetting.Builder().name("while-attacking").description("Only aim while attack is held.").defaultValue(true).build());
        public SmoothAim() { super("SmoothAim", "Combat", "Impact-style smoothed visible-player aim; no jitter or obsolete antibot heuristics.", "kill-aura", "bow-aimbot", "rotation", "crystal-aura"); }
        protected void update() {
            if (mc.currentScreen != null || (held.get() && !mc.options.attackKey.isPressed())) return;
            net.minecraft.entity.player.PlayerEntity target = null; double distance = range.get() * range.get();
            for (var p : mc.world.getPlayers()) {
                double d = mc.player.squaredDistanceTo(p);
                float yaw = (float) Math.toDegrees(Math.atan2(p.getZ() - mc.player.getZ(), p.getX() - mc.player.getX())) - 90;
                if (p != mc.player && p.isAlive() && !p.isSpectator() && !p.isInvisible() && !Friends.get().isFriend(p) && mc.player.canSee(p) && d < distance && Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw())) <= fov.get() / 2) { distance = d; target = p; }
            }
            if (target == null) return;
            var delta = target.getEyePos().subtract(mc.player.getEyePos());
            float yaw = (float) Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90;
            float pitch = (float) -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
            mc.player.setYaw(approach(mc.player.getYaw(), yaw)); mc.player.setPitch(Math.clamp(approach(mc.player.getPitch(), pitch), -90, 90));
        }
        private float approach(float value, float target) { float diff = MathHelper.wrapDegrees(target - value); float max = (float) (Math.abs(diff) / smooth.get() + base.get()); return value + Math.clamp(diff, -max, max); }
    }
}
