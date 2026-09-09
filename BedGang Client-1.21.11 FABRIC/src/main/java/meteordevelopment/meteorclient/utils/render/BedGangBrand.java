package meteordevelopment.meteorclient.utils.render;

import net.minecraft.client.gui.DrawContext;
import static meteordevelopment.meteorclient.MeteorClient.mc;

/** Branding for t4's BedGang edition. Uses native GUI pixels and no external assets. */
public final class BedGangBrand {
    public static final String WATERMARK = "BedGang | t4's edition";
    private BedGangBrand() {}

    public static void title(DrawContext ctx, int screenWidth) {
        int w = Math.min(178, screenWidth - 12);
        ctx.createNewRootLayer();
        ctx.fill(6, 6, 6 + w, 44, 0xe6161824);
        ctx.fill(6, 6, 6 + w, 8, 0xffff427c);
        bed(ctx, 12, 19);
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().translate(39, 13);
        ctx.getMatrices().scale(1.5f, 1.5f);
        ctx.drawText(mc.textRenderer, "BedGang", 0, 0, 0xffff427c, true);
        ctx.getMatrices().popMatrix();
        ctx.drawText(mc.textRenderer, "t4's edition", 40, 30, 0xffeeeeff, true);
    }

    public static void badge(DrawContext ctx, int screenWidth, int screenHeight) {
        String label = mc.textRenderer.trimToWidth(WATERMARK, Math.max(0, screenWidth - 42));
        int w = mc.textRenderer.getWidth(label) + 32;
        int x = Math.max(3, screenWidth - w - 4), y = Math.max(3, screenHeight - 31);
        ctx.createNewRootLayer();
        ctx.fill(x, y, x + w, y + 17, 0xd9161824);
        ctx.fill(x, y, x + 2, y + 17, 0xffff427c);
        bed(ctx, x + 5, y + 3);
        ctx.drawText(mc.textRenderer, label, x + 28, y + 5, 0xffffd4e0, true);
    }

    private static void bed(DrawContext ctx, int x, int y) {
        ctx.fill(x, y + 2, x + 3, y + 13, 0xffe5d3d8);
        ctx.fill(x + 3, y + 5, x + 20, y + 10, 0xffff427c);
        ctx.fill(x + 3, y + 3, x + 8, y + 6, 0xffffffff);
        ctx.fill(x + 18, y + 9, x + 20, y + 13, 0xffe5d3d8);
    }
}
