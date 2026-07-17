package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.RenderStyle;

@EventListener
public class PlatformHighlight {
    public static final Feature instance = new Feature("platformHighlight");

    public static final SettingBool healerOnly = new SettingBool(true, "healerOnly", instance);
    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN), "outlineColor", instance);
    public static final SettingColor fillColor = new SettingColor(RenderColor.fromFormat(ChatFormatting.GREEN).withAlpha(0.5f), "fillColor", instance);

    private static final AABB box = AABB.encapsulatingFullBlocks(new BlockPos(55, 63, 115), new BlockPos(53, 63, 113));

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isInBossRoom("7")) {
            if (healerOnly.value() && !DungeonUtil.isClass("Healer")) {
                return;
            }
            event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
        }
    }
}
