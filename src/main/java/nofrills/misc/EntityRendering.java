package nofrills.misc;

public interface EntityRendering {
    void nofrills_mod$setGlowingColored(boolean glowing, RenderColor color);

    boolean nofrills_mod$getGlowing();

    void nofrills_mod$setRenderBoxOutline(boolean render, RenderColor color);

    boolean nofrills_mod$getRenderingOutline();

    RenderColor nofrills_mod$getOutlineColors();

    void nofrills_mod$setRenderBoxFilled(boolean render, RenderColor color);

    boolean nofrills_mod$getRenderingFilled();

    RenderColor nofrills_mod$getFilledColors();
}