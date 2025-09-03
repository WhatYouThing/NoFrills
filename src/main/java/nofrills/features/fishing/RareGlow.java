package nofrills.features.fishing;

import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.GiantEntity;
import nofrills.config.Feature;
import nofrills.config.SettingColor;
import nofrills.events.EntityNamedEvent;
import nofrills.events.EntityUpdatedEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.SeaCreatureData;
import nofrills.misc.Utils;

public class RareGlow {
    public static final Feature instance = new Feature("rareGlow");

    public static final SettingColor color = new SettingColor(new RenderColor(255, 170, 0, 255), "color", instance.key());

    private static boolean isValidMob(Entity ent) {
        return !Rendering.Entities.isDrawingGlow(ent) && Utils.isMob(ent) && !(ent instanceof GiantEntity);
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && event.entity.age <= 20 && !Utils.isInDungeons() && event.entity instanceof ArmorStandEntity armorStand) {
            GameProfile textures = Utils.getTextures(armorStand.getEquippedStack(EquipmentSlot.HEAD));
            if (textures != null) {
                for (SeaCreatureData.SeaCreature creature : SeaCreatureData.list) {
                    for (String texture : creature.textures) {
                        if (Utils.isTextureEqual(textures, texture)) {
                            Rendering.Entities.drawGlow(event.entity, true, color.value());
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && !Utils.isInDungeons() && SeaCreatureData.isSeaCreature(event.namePlain)) {
            for (SeaCreatureData.SeaCreature creature : SeaCreatureData.list) {
                if (creature.rare && creature.glow && event.entity.age <= 20 && event.namePlain.contains(creature.name)) {
                    Entity owner = Utils.findNametagOwner(event.entity, Utils.getOtherEntities(event.entity, 0.5, 2, 0.5, RareGlow::isValidMob));
                    if (owner != null) {
                        Rendering.Entities.drawGlow(owner, true, color.value());
                        if (owner.hasVehicle()) {
                            Rendering.Entities.drawGlow(owner.getVehicle(), true, color.value());
                        } else if (owner.hasPassengers()) {
                            Rendering.Entities.drawGlow(owner.getFirstPassenger(), true, color.value());
                        }
                    }
                }
            }
        }
    }
}
