package nofrills.misc;

import net.minecraft.util.math.ColorHelper;

import java.awt.*;

public class RenderColor {
    public float r;
    public float g;
    public float b;
    public float a;
    public int hex;
    public int argb;

    public RenderColor(int r, int g, int b, int a) {
        this.r = (float) Math.clamp(r, 0, 255) / 255;
        this.g = (float) Math.clamp(g, 0, 255) / 255;
        this.b = (float) Math.clamp(b, 0, 255) / 255;
        this.a = (float) Math.clamp(a, 0, 255) / 255;
        this.hex = (Math.clamp(r, 0, 255) << 16) + (Math.clamp(g, 0, 255) << 8) + Math.clamp(b, 0, 255);
        this.argb = ColorHelper.getArgb(Math.clamp(a, 0, 255), Math.clamp(r, 0, 255), Math.clamp(g, 0, 255), Math.clamp(b, 0, 255));
    }

    public static RenderColor fromHex(int hex) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, 255);
    }

    public static RenderColor fromHex(int hex, float alpha) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (int) (255 * alpha));
    }

    public static RenderColor fromColor(Color color) {
        return new RenderColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static RenderColor fromFloat(float r, float g, float b, float a) {
        return new RenderColor((int) (255 * r), (int) (255 * g), (int) (255 * b), (int) (255 * a));
    }
}