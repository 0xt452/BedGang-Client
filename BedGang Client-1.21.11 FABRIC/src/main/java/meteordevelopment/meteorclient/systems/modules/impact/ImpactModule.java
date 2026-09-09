package meteordevelopment.meteorclient.systems.modules.impact;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

public abstract class ImpactModule extends Module {
    public final String impactName, group;
    private final String[] conflicts;
    private boolean paused;
    protected final SettingGroup general = settings.getDefaultGroup();
    private final Setting<Boolean> pauseConflicts = general.add(new BoolSetting.Builder().name("pause-on-conflict").description("Pause when a known Meteor/Phobos or Impact controller of the same behavior is active.").defaultValue(true).build());
    protected ImpactModule(String name, String group, String description, String... conflicts) {
        super(ImpactRegistry.CATEGORY, "impact-" + name.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase(java.util.Locale.ROOT), description);
        this.impactName = name; this.group = group; this.conflicts = conflicts;
    }
    public boolean canRun() {
        if (mc.player == null || mc.world == null) return false;
        if (pauseConflicts.get()) for (String name : conflicts) {
            Module module = Modules.get().get(name);
            if (module != null && module != this && module.isActive()) return false;
        }
        return true;
    }
    @EventHandler private void impactTick(TickEvent.Pre event) {
        if (!canRun()) { if (!paused) release(); paused = true; return; }
        paused = false; update();
    }
    protected abstract void update();
    protected void release() {}
    @Override public void onDeactivate() { release(); paused = false; }
    @EventHandler private void left(GameLeftEvent event) { release(); paused = false; }
    @Override public String getInfoString() { return paused ? "Paused: conflict" : null; }
    protected Setting<Double> number(String name, String description, double value, double min, double max) {
        return general.add(new DoubleSetting.Builder().name(name).description(description).defaultValue(value).range(min, max).sliderRange(min, max).build());
    }
}
