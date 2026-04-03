package nofrills.features.fishing;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.config.SettingEnum;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.*;

public class RareHighlight {
    public static final Feature instance = new Feature("rareGlow");

    public static final SettingEnum<RenderStyle> style = new SettingEnum<>(RenderStyle.Outline, RenderStyle.class, "style", instance);
    public static final SettingColor outlineColor = new SettingColor(new RenderColor(255, 170, 0, 255), "color", instance);
    public static final SettingColor fillColor = new SettingColor(new RenderColor(255, 170, 0, 127), "fillColor", instance);

    private static final EntityCache cache = new EntityCache();

    private static boolean isMob(Entity entity) {
        return Utils.isMob(entity) && !cache.has(entity);
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity instanceof ArmorStandEntity stand && !Utils.isInDungeons()) {
            ItemStack helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
            if (helmet.isEmpty()) return;
            GameProfile textures = Utils.getTextures(helmet);
            if (textures != null) {
                for (SeaCreatureData.SeaCreature creature : SeaCreatureData.list) {
                    for (String texture : creature.textures) {
                        if (Utils.isTextureEqual(textures, texture)) {
                            cache.add(stand);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && SeaCreatureData.isSeaCreature(event.namePlain) && !Utils.isInDungeons()) {
            for (SeaCreatureData.SeaCreature creature : SeaCreatureData.list) {
                if (creature.rare && creature.glow && event.namePlain.contains(creature.name)) {
                    Entity owner = Utils.findNametagOwner(event.entity, Utils.getOtherEntities(event.entity, 0.5, 2, 0.5, RareHighlight::isMob));
                    if (owner != null) {
                        cache.add(owner);
                        if (owner.hasVehicle()) {
                            cache.add(owner.getVehicle());
                        } else if (owner.hasPassengers()) {
                            cache.add(owner.getFirstPassenger());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && !cache.empty()) {
            float delta = event.tickCounter.getTickProgress(true);
            for (Entity ent : cache.get()) {
                if (!ent.isAlive()) continue;
                Box box = ent instanceof ArmorStandEntity
                        ? Box.of(ent.getLerpedPos(delta).add(0.0, ent.getStandingEyeHeight(), 0.0), 1.0, 1.0, 1.0)
                        : Utils.getLerpedBox(ent, delta);
                event.drawStyled(box, style.value(), false, outlineColor.value(), fillColor.value());
            }
        }
    }
}
