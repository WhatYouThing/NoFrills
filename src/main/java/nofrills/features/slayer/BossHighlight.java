package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.*;

import java.util.List;
import java.util.Map;

@EventListener
public class BossHighlight {
    public static final Feature instance = new Feature("bossHighlight");

    public static final SettingColor fillColor = new SettingColor(RenderColor.fromArgb(0x5500ffff), "fillColor", instance.key());
    public static final SettingColor outlineColor = new SettingColor(RenderColor.fromArgb(0xff00ffff), "outlineColor", instance.key());
    public static final SettingEnum<RenderStyle> highlightStyle = new SettingEnum<>(RenderStyle.Both, RenderStyle.class, "highlightStyle", instance.key());
    public static final SettingColor ashenFill = new SettingColor(RenderColor.fromArgb(0x55000000), "ashenFill", instance.key());
    public static final SettingColor ashenOutline = new SettingColor(RenderColor.fromArgb(0xff000000), "ashenOutline", instance.key());
    public static final SettingColor spiritFill = new SettingColor(RenderColor.fromArgb(0x55ffffff), "spiritFill", instance.key());
    public static final SettingColor spiritOutline = new SettingColor(RenderColor.fromArgb(0xffffffff), "spiritOutline", instance.key());
    public static final SettingColor auricFill = new SettingColor(RenderColor.fromArgb(0x55ffff00), "auricFill", instance.key());
    public static final SettingColor auricOutline = new SettingColor(RenderColor.fromArgb(0xffffff00), "auricOutline", instance.key());
    public static final SettingColor crystalFill = new SettingColor(RenderColor.fromArgb(0x5500ffff), "crystalFill", instance.key());
    public static final SettingColor crystalOutline = new SettingColor(RenderColor.fromArgb(0xff00ffff), "crystalOutline", instance.key());

    private static final MappedEntityCache<String> blazeCache = new MappedEntityCache<>();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.BLAZE) && SlayerUtil.isTimer(event.namePlain)) {
            String attunement = event.namePlain.contains(" ") ? event.namePlain.substring(0, event.namePlain.indexOf(" ")) : "";
            List<Entity> other = Utils.getOtherEntities(event.entity, 1.0, 3.0, 1.0, SlayerUtil.BLAZE.predicate);
            Entity owner = Utils.getNameTagOwner(event.entity, other);
            if (owner != null) {
                blazeCache.add(owner, attunement);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.bossAlive) {
            if (SlayerUtil.isFightingBoss(SlayerUtil.BLAZE)) {
                for (Map.Entry<Entity, String> entry : blazeCache.get()) {
                    Entity ent = entry.getKey();
                    if (!ent.isAlive()) return;
                    AABB box = Utils.getLerpedBox(ent, event.delta());
                    switch (entry.getValue()) {
                        case "ASHEN" ->
                                event.drawStyled(box, highlightStyle.value(), false, ashenOutline.value(), ashenFill.value());
                        case "SPIRIT" ->
                                event.drawStyled(box, highlightStyle.value(), false, spiritOutline.value(), spiritFill.value());
                        case "AURIC" ->
                                event.drawStyled(box, highlightStyle.value(), false, auricOutline.value(), auricFill.value());
                        case "CRYSTAL" ->
                                event.drawStyled(box, highlightStyle.value(), false, crystalOutline.value(), crystalFill.value());
                    }
                }
            } else {
                Entity boss = SlayerUtil.getBossEntity();
                if (boss == null || !boss.isAlive()) return;
                event.drawStyled(Utils.getLerpedBox(boss, event.delta()), highlightStyle.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
