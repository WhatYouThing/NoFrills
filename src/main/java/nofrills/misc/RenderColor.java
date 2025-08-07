package nofrills.misc;

import net.minecraft.util.math.ColorHelper;

public class RenderColor {
    public static final RenderColor white = RenderColor.fromHex(0xffffff);
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

    public RenderColor(float r, float g, float b, float a) {
        this.r = Math.clamp(r, 0.0f, 1.0f);
        this.g = Math.clamp(g, 0.0f, 1.0f);
        this.b = Math.clamp(b, 0.0f, 1.0f);
        this.a = Math.clamp(a, 0.0f, 1.0f);
        this.hex = (((int) this.r * 255) << 16) + (((int) this.g * 255) << 8) + ((int) this.b * 255);
        this.argb = ColorHelper.fromFloats(this.a, this.r, this.g, this.b);
    }

    public static RenderColor fromHex(int hex) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, 255);
    }

    public static RenderColor fromArgb(int hex) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (hex >> 24) & 0xFF);
    }

    public static RenderColor fromHex(int hex, float alpha) {
        return new RenderColor((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (int) (255 * alpha));
    }

    public static RenderColor fromColor(io.wispforest.owo.ui.core.Color color) {
        return RenderColor.fromHex(color.rgb(), color.alpha());
    }

    public static RenderColor fromFloat(float r, float g, float b, float a) {
        return new RenderColor(r, g, b, a);
    }
}