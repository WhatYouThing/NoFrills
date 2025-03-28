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
        double newX = snap && x != 0 ? minX + Math.clamp(x, -1, 1) : minX + x;
        double newY = snap && y != 0 ? minY + Math.clamp(y, -1, 1) : minY + y;
        if (isInBounds(context, newX, newY) && isInBounds(context, maxX + x, maxY + y)) {
            double newPosX = getOffsetX(context, newX);
            double newPosY = getOffsetY(context, newY);
            if (snap) {
                double differenceX = newPosX % 0.005;
                double differenceY = newPosY % 0.005;
                if (differenceX != 0) {
                    newPosX = Math.min(newPosX, newPosX - differenceX);
                }
                if (differenceY != 0) {
                    newPosY = Math.min(newPosY, newPosY - differenceY);
                }
            }
            posX = newPosX;
            posY = newPosY;
        }
    }

    public void calculateDimensions(DrawContext context) {
        int resX = context.getScaledWindowWidth();
        int resY = context.getScaledWindowHeight();
        minX = resX * posX;
        maxX = resX * (posX + sizeX);
        minY = resY * posY;
        maxY = resY * (posY + sizeY);
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

