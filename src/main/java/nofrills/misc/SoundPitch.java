package nofrills.misc;

public record SoundPitch(int multiplier) {
    public static final float RESOLUTION = 1.0f / 63; // common pitch resolution for hypixel sounds


    public static SoundPitch from(float v) {
        return new SoundPitch(Math.round(v / RESOLUTION));
    }

    public float value() {
        return multiplier * RESOLUTION;
    }
}
