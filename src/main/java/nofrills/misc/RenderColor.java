package nofrills.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;

public class RenderColor {
    public static final RenderColor WHITE = fromFormat(ChatFormatting.WHITE);
    public static final RenderColor GREEN = fromFormat(ChatFormatting.GREEN);
    public static final RenderColor RED = fromFormat(ChatFormatting.RED);
    public static final RenderColor GRAY = fromFormat(ChatFormatting.GRAY);
    public static final RenderColor NF_BLUE = RenderColor.fromArgb(0xff5ca0bf);

    public final float r;
    public final float g;
    public final float b;
    public final float a;
    public final int hex;
    public final int argb;

    public RenderColor(float r, float g, float b, float a) {
        this.r = Math.clamp(r, 0.0f, 1.0f);
        this.g = Math.clamp(g, 0.0f, 1.0f);
        this.b = Math.clamp(b, 0.0f, 1.0f);
        this.a = Math.clamp(a, 0.0f, 1.0f);
        this.hex = ((int) (this.r * 255) << 16) + ((int) (this.g * 255) << 8) + (int) (this.b * 255);
        this.argb = ARGB.colorFromFloat(this.a, this.r, this.g, this.b);
    }

    public RenderColor(int r, int g, int b, int a) {
        this((float) Math.clamp(r, 0, 255) / 255, (float) Math.clamp(g, 0, 255) / 255, (float) Math.clamp(b, 0, 255) / 255, (float) Math.clamp(a, 0, 255) / 255);
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

    public static RenderColor fromFloat(float r, float g, float b, float a) {
        return new RenderColor(r, g, b, a);
    }

    @SuppressWarnings("DataFlowIssue")
    public static RenderColor fromFormat(ChatFormatting formatting) {
        if (formatting.isColor()) {
            return RenderColor.fromHex(formatting.getColor());
        } else {
            throw new IllegalStateException(Utils.format("RenderColor cannot be created from non-color ChatFormatting ({})", formatting));
        }
    }

    public float getRed() {
        return this.r;
    }

    public float getGreen() {
        return this.g;
    }

    public float getBlue() {
        return this.b;
    }

    public float getAlpha() {
        return this.a;
    }

    public int getHex() {
        return this.hex;
    }

    public int getArgb() {
        return this.argb;
    }

    public RenderColor withRed(float red) {
        return new RenderColor(red, this.g, this.b, this.a);
    }

    public RenderColor withGreen(float green) {
        return new RenderColor(this.r, green, this.b, this.a);
    }

    public RenderColor withBlue(float blue) {
        return new RenderColor(this.r, this.g, blue, this.a);
    }

    public RenderColor withAlpha(float alpha) {
        return new RenderColor(this.r, this.g, this.b, alpha);
    }

    public RenderColor scaled(float red, float green, float blue) {
        return new RenderColor(this.r * red, this.g * green, this.b * blue, this.a);
    }

    public RenderColor scaled(float value) {
        return this.scaled(value, value, value);
    }
}
