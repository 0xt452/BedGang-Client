package meteordevelopment.meteorclient.gui.themes.phobos;

/** Shared, independent panel geometry; coordinates use Minecraft GUI pixels. */
public final class PhobosPanelLayout {
    private PhobosPanelLayout() {}
    public static int columns(int screenWidth, int panelWidth) { return Math.max(1, (screenWidth - 8) / (panelWidth + 3)); }
    public static int bands(int count, int columns) { return Math.max(1, (count + columns - 1) / columns); }
    public static int bandHeight(int screenHeight, int bands) { return Math.max(33, (screenHeight - 46) / bands); }
    public static int scroll(int requested, int content, int view) { return Math.clamp(requested, 0, Math.max(0, content - view)); }
    public static boolean contains(double x, double y, int left, int top, int width, int height) {
        return width > 0 && height > 0 && x >= left && x < left + width && y >= top && y < top + height;
    }
}
