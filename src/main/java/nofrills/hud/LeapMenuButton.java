package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import nofrills.features.LeapOverlay;
import nofrills.misc.RenderColor;

import static nofrills.Main.mc;

public class LeapMenuButton implements Drawable {
    public final int slotId;
    public final Text player;
    public final Text dungeonClass;
    private final RenderColor nameColor;
    private final RenderColor classColor;
    private final float offsetX;
    private final float offsetY;
    private final int background;
    private final int backgroundHover;
    private final int border;
    public int minX = 0;
    public int minY = 0;
    public int maxX = 0;
    public int maxY = 0;
    public boolean hovered = false;

    public LeapMenuButton(int slotId, int index, String player, String dungeonClass, RenderColor classColor) {
        this.slotId = slotId;
        this.player = Text.of(player);
        this.dungeonClass = Text.of(dungeonClass);
        this.nameColor = LeapOverlay.nameColor;
        this.classColor = classColor;
        background = ColorHelper.fromFloats(0.67f, 0.0f, 0.0f, 0.0f);
        backgroundHover = ColorHelper.fromFloats(0.67f, this.classColor.r * 0.33f, this.classColor.g * 0.33f, this.classColor.b * 0.33f);
        border = ColorHelper.fromFloats(1.0f, this.classColor.r, this.classColor.g, this.classColor.b);
        offsetX = index == 0 || index == 2 ? 0.25f : 0.55f;
        offsetY = index <= 1 ? 0.25f : 0.55f;
    }

    private int getX(DrawContext context, float xOffset) {
        return (int) Math.floor(context.getScaledWindowWidth() * xOffset);
    }

    private int getY(DrawContext context, float yOffset) {
        return (int) Math.floor(context.getScaledWindowHeight() * yOffset);
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        minX = getX(context, this.offsetX);
        minY = getY(context, this.offsetY);
        maxX = getX(context, this.offsetX + 0.2f);
        maxY = getY(context, this.offsetY + 0.2f);
        context.drawCenteredTextWithShadow(mc.textRenderer, this.player, minX + (maxX - minX) / 2, (int) (minY + (maxY - minY) * 0.35), this.nameColor.hex);
        context.drawCenteredTextWithShadow(mc.textRenderer, this.dungeonClass, minX + (maxX - minX) / 2, (int) (minY + (maxY - minY) * 0.5), this.classColor.hex);
        context.fill(minX, minY, maxX, maxY, hovered ? backgroundHover : background); // for some reason its ARGB rather than RGBA
        if (slotId != -1) {
            context.drawBorder(minX, minY, maxX - minX, maxY - minY, border);
        }
    }
}

