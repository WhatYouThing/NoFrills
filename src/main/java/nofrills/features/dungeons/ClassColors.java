package nofrills.features.dungeons;

import net.minecraft.ChatFormatting;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.Optional;

public class ClassColors {
    public static final Feature instance = new Feature("classColors");

    public static final RenderColor healDefault = RenderColor.fromFormat(ChatFormatting.GOLD);
    public static final RenderColor mageDefault = RenderColor.fromHex(0x1793c4);
    public static final RenderColor bersDefault = RenderColor.fromHex(0xe7413c);
    public static final RenderColor archDefault = RenderColor.fromHex(0x4a14b7);
    public static final RenderColor tankDefault = RenderColor.fromFormat(ChatFormatting.DARK_GREEN);

    public static final SettingColor heal = new SettingColor(healDefault, "healer", instance);
    public static final SettingColor mage = new SettingColor(mageDefault, "mage", instance);
    public static final SettingColor bers = new SettingColor(bersDefault, "bers", instance);
    public static final SettingColor arch = new SettingColor(archDefault, "arch", instance);
    public static final SettingColor tank = new SettingColor(tankDefault, "tank", instance);

    public static RenderColor getHealer() {
        return instance.isActive() ? heal.value() : healDefault;
    }

    public static RenderColor getMage() {
        return instance.isActive() ? mage.value() : mageDefault;
    }

    public static RenderColor getBerserk() {
        return instance.isActive() ? bers.value() : bersDefault;
    }

    public static RenderColor getArcher() {
        return instance.isActive() ? arch.value() : archDefault;
    }

    public static RenderColor getTank() {
        return instance.isActive() ? tank.value() : tankDefault;
    }

    public static Optional<RenderColor> getForClass(String className) {
        return switch (Utils.toLower(className)) {
            case "healer" -> Optional.of(getHealer());
            case "mage" -> Optional.of(getMage());
            case "berserk" -> Optional.of(getBerserk());
            case "archer" -> Optional.of(getArcher());
            case "tank" -> Optional.of(getTank());
            default -> Optional.empty();
        };
    }
}
