package nofrills.misc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.text.Text;
import nofrills.features.LeapOverlay;

import static nofrills.Main.mc;

public class LeapMenuButton implements Drawable {
    public final int slotId;
    public final Text player;
    public final Text dungeonClass;
    private final int nameColor;
    private final int classColor;
    private final float offsetX;
    private final float offsetY;
    public int minX = 0;
    public int minY = 0;
    public int maxX = 0;
    public int maxY = 0;

    public LeapMenuButton(int slotId, int index, String player, String dungeonClass, RenderColor classColor) {
        this.slotId = slotId;
        this.player = Text.of(player);
        this.dungeonClass = Text.of(dungeonClass);
        this.nameColor = LeapOverlay.nameColor.hex;
        this.classColor = classColor.hex;
        if (index == 0 || index == 2) {
            offsetX = 0.2f;
        } else {
            offsetX = 0.6f;
        }
        if (index <= 1) {
            offsetY = 0.2f;
        } else {
            offsetY = 0.6f;
        }
    }

    private int getX(DrawContext context, float xOffset) {
        return (int) Math.floor(context.getScaledWindowWidth() * xOffset);
    }

    private int getY(DrawContext context, float yOffset) {
        return (int) Math.floor(context.getScaledWindowHeight() * yOffset);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        minX = getX(context, this.offsetX);
        minY = getY(context, this.offsetY);
        maxX = getX(context, this.offsetX + 0.2f);
        maxY = getY(context, this.offsetY + 0.2f);
        int textX = getX(context, this.offsetX + 0.01f);
        int textY = getY(context, this.offsetY + 0.01f);
        context.drawTextWithShadow(mc.textRenderer, this.player, textX, textY, this.nameColor);
        context.drawTextWithShadow(mc.textRenderer, this.dungeonClass, textX, textY + 2 + mc.textRenderer.fontHeight, this.classColor);
        context.fill(minX, minY, maxX, maxY, 0xaa000000); // for some reason its ARGB rather than RGBA
    }
}

