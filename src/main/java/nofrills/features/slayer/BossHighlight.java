package nofrills.features.slayer;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final EntityCache blazeCache = new EntityCache();
    private static final ConcurrentHashMap<Integer, String> attunementMap = new ConcurrentHashMap<>();

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SlayerUtil.isFightingBoss(SlayerUtil.blaze) && SlayerUtil.isTimer(event.namePlain)) {
            String attunement = event.namePlain.contains(" ") ? event.namePlain.substring(0, event.namePlain.indexOf(" ")) : "";
            List<Entity> other = Utils.getOtherEntities(event.entity, 1.0, 3.0, 1.0, SlayerUtil.blaze.predicate);
            Entity owner = Utils.findNametagOwner(event.entity, other);
            if (owner != null) {
                attunementMap.put(owner.getId(), attunement);
                blazeCache.add(owner);
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && SlayerUtil.bossAlive) {
            if (SlayerUtil.isFightingBoss(SlayerUtil.blaze)) {
                for (Entity ent : blazeCache.get()) {
                    if (!ent.isAlive()) return;
                    Box box = Utils.getLerpedBox(ent, event.tickCounter.getTickProgress(true));
                    String attunement = attunementMap.getOrDefault(ent.getId(), "");
                    RenderStyle style = highlightStyle.value();
                    switch (attunement) {
                        case "ASHEN" -> event.drawStyled(box, style, false, ashenOutline.value(), ashenFill.value());
                        case "SPIRIT" -> event.drawStyled(box, style, false, spiritOutline.value(), spiritFill.value());
                        case "AURIC" -> event.drawStyled(box, style, false, auricOutline.value(), auricFill.value());
                        case "CRYSTAL" ->
                                event.drawStyled(box, style, false, crystalOutline.value(), crystalFill.value());
                    }
                }
            } else {
                Entity boss = SlayerUtil.getBossEntity();
                if (boss == null || !boss.isAlive()) return;
                Box box = Utils.getLerpedBox(boss, event.tickCounter.getTickProgress(true));
                event.drawStyled(box, highlightStyle.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && !SlayerUtil.bossAlive && !attunementMap.isEmpty()) {
            attunementMap.clear();
        }
    }
}
