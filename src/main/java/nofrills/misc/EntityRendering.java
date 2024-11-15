package nofrills.misc;

public interface EntityRendering {
    void nofrills_mod$setGlowingColored(boolean glowing, int color);

    boolean nofrills_mod$getGlowing();

    void nofrills_mod$setRenderBoxOutline(boolean render, float red, float green, float blue, float alpha);

    boolean nofrills_mod$getRenderingOutline();

    float[] nofrills_mod$getRenderingOutlineColors();

    void nofrills_mod$setRenderBoxFilled(boolean render, float red, float green, float blue, float alpha);

    boolean nofrills_mod$getRenderingFilled();

    float[] nofrills_mod$getRenderingFilledColors();
}