package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SlayerUtil;
import nofrills.misc.Utils;

import java.util.List;

public class BossHighlight {
    public static final Feature instance = new Feature("bossHighlight");

    public static final SettingColor fillColor = new SettingColor(RenderColor.fromArgb(0x5500ffff), "fillColor", instance.key());
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "outlineColor", instance.key());
    public static final SettingEnum<style> highlightStyle = new SettingEnum<>(style.Both, style.class, "highlightStyle", instance.key());
    public static final SettingColor ashenFill = new SettingColor(RenderColor.fromArgb(0x55000000), "ashenFill", instance.key());
    public static final SettingColor ashenOutline = new SettingColor(RenderColor.fromArgb(0xff000000), "ashenOutline", instance.key());
    public static final SettingColor spiritFill = new SettingColor(RenderColor.fromArgb(0x55ffffff), "spiritFill", instance.key());
    public static final SettingColor spiritOutline = new SettingColor(RenderColor.fromArgb(0xffffffff), "spiritOutline", instance.key());
    public static final SettingColor auricFill = new SettingColor(RenderColor.fromArgb(0x55ffff00), "auricFill", instance.key());
    public static final SettingColor auricOutline = new SettingColor(RenderColor.fromArgb(0xffffff00), "auricOutline", instance.key());
    public static final SettingColor crystalFill = new SettingColor(RenderColor.fromArgb(0x5500ffff), "crystalFill", instance.key());
    public static final SettingColor crystalOutline = new SettingColor(RenderColor.fromArgb(0xff00ffff), "crystalOutline", instance.key());

    private static void highlightBlaze(Entity ent, String name) {
        String attunement = name.contains(" ") ? name.substring(0, name.indexOf(" ")) : "";
        List<Entity> other = Utils.getOtherEntities(ent, 1, 3, 1, SlayerUtil.currentBoss.predicate);
        Entity owner = Utils.findNametagOwner(ent, other);
        if (owner != null) {
            RenderColor fill = getBlazeFillColor(attunement);
            RenderColor outline = getBlazeOutlineColor(attunement);
            if (!highlightStyle.value().equals(style.Outline)) {
                Rendering.Entities.drawFilled(owner, fill != null, fill);
            }
            if (!highlightStyle.value().equals(style.Filled)) {
                Rendering.Entities.drawOutline(owner, outline != null, outline);
            }
        }
    }

    private static RenderColor getBlazeFillColor(String attunement) {
        return switch (attunement) {
            case "IMMUNE" -> null;
            case "ASHEN" -> ashenFill.value();
            case "SPIRIT" -> spiritFill.value();
            case "AURIC" -> auricFill.value();
            case "CRYSTAL" -> crystalFill.value();
            default -> fillColor.value();
        };
    }

    private static RenderColor getBlazeOutlineColor(String attunement) {
        return switch (attunement) {
            case "IMMUNE" -> null;
            case "ASHEN" -> ashenOutline.value();
            case "SPIRIT" -> spiritOutline.value();
            case "AURIC" -> auricOutline.value();
            case "CRYSTAL" -> crystalOutline.value();
            default -> outlineColor.value();
        };
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.currentBoss != null) {
            if (SlayerUtil.currentBoss.equals(SlayerUtil.blaze)) {
                if (SlayerUtil.isTimer(event.namePlain)) {
                    highlightBlaze(event.entity, event.namePlain);
                }
            } else if (SlayerUtil.isSpawner(event.namePlain)) {
                List<Entity> other = Utils.getOtherEntities(event.entity, 1, 3, 1, SlayerUtil.currentBoss.predicate);
                Entity owner = Utils.findNametagOwner(event.entity, other);
                if (owner != null) {
                    if (!highlightStyle.value().equals(style.Outline)) {
                        Rendering.Entities.drawFilled(owner, true, fillColor.value());
                    }
                    if (!highlightStyle.value().equals(style.Filled)) {
                        Rendering.Entities.drawOutline(owner, true, outlineColor.value());
                    }
                }
            }
        }
    }

    public enum style {
        Outline,
        Filled,
        Both
    }
}
