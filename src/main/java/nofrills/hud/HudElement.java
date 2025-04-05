package nofrills.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;

public class HudElement implements Drawable {
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public double minX = 0;
    public double minY = 0;
    public double maxX = 0;
    public double maxY = 0;
    public double snapX = 1;
    public double snapY = 1;

    public HudElement(double posX, double posY, double sizeX, double sizeY) {
        this.posX = posX;
        this.posY = posY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        calculateDimensions(context);
    }

    public void move(DrawContext context, int x, int y, boolean snap) {
        double newX = snap ? x - (x % snapX) : x;
        double newY = snap ? y - (y % snapY) : y;
        if (!isInBounds(context, newX, newY)) {
            newX = Math.clamp(newX, 0, context.getScaledWindowWidth());
            newY = Math.clamp(newY, 0, context.getScaledWindowHeight());
        }
        double newMaxX = newX + (maxX - minX);
        double newMaxY = newY + (maxY - minY);
        if (!isInBounds(context, newMaxX, newMaxY)) {
            newX -= newMaxX - Math.clamp(newMaxX, 0, context.getScaledWindowWidth());
            newY -= newMaxY - Math.clamp(newMaxY, 0, context.getScaledWindowHeight());
        }
        posX = getOffsetX(context, newX);
        posY = getOffsetY(context, newY);
    }

    public void calculateDimensions(DrawContext context) {
        int resX = context.getScaledWindowWidth();
        int resY = context.getScaledWindowHeight();
        minX = resX * posX;
        maxX = resX * (posX + sizeX);
        minY = resY * posY;
        maxY = resY * (posY + sizeY);
        snapX = resX * 0.01;
        snapY = resY * 0.01;
    }

    public double getX(DrawContext context, double offset) {
        return context.getScaledWindowWidth() * offset;
    }

    public double getY(DrawContext context, double offset) {
        return context.getScaledWindowHeight() * offset;
    }

    public double getOffsetX(DrawContext context, double x) {
        return x / context.getScaledWindowWidth();
    }

    public double getOffsetY(DrawContext context, double y) {
        return y / context.getScaledWindowHeight();
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY <= maxY;
    }

    public boolean isInBounds(DrawContext context, double x, double y) {
        return x >= 0 && x <= context.getScaledWindowWidth() && y >= 0 && y <= context.getScaledWindowHeight();
    }
}

