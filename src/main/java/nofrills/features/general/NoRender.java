package nofrills.features.general;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;
import nofrills.events.*;
import nofrills.misc.Utils;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@EventListener
public class NoRender {
    public static final Feature instance = new Feature("noRender");

    public static final SettingBool explosions = new SettingBool(false, "explosions", instance.key());
    public static final SettingBool emptyTooltips = new SettingBool(false, "emptyTooltips", instance.key());
    public static final SettingBool fireOverlay = new SettingBool(false, "fireOverlay", instance.key());
    public static final SettingBool breakParticles = new SettingBool(false, "breakParticles", instance.key());
    public static final SettingBool bossBar = new SettingBool(false, "bossBar", instance.key());
    public static final SettingBool armorBar = new SettingBool(false, "armorBar", instance.key());
    public static final SettingBool foodBar = new SettingBool(false, "foodBar", instance.key());
    public static final SettingBool fog = new SettingBool(false, "fog", instance.key());
    public static final SettingBool effectDisplay = new SettingBool(false, "effectDisplay", instance.key());
    public static final SettingBool recipeBook = new SettingBool(false, "recipeBook", instance.key());
    public static final SettingBool selectedItemName = new SettingBool(false, "selectedItemName", instance.key());
    public static final SettingBool deadEntities = new SettingBool(false, "deadEntities", instance.key());
    public static final SettingBool deadPoof = new SettingBool(false, "deadPoof", instance.key());
    public static final SettingBool lightning = new SettingBool(false, "lightning", instance.key());
    public static final SettingBool fallingBlocks = new SettingBool(false, "fallingBlocks", instance.key());
    public static final SettingBool entityFire = new SettingBool(false, "entityFire", instance.key());
    public static final SettingBool mageBeam = new SettingBool(false, "mageBeam", instance.key());
    public static final SettingBool iceSpray = new SettingBool(false, "iceSpray", instance.key());
    public static final SettingBool powderCoating = new SettingBool(false, "powderCoating", instance.key());
    public static final SettingBool soulweaverSkulls = new SettingBool(false, "soulweaverSkulls", instance.key());
    public static final SettingBool guidedSheep = new SettingBool(false, "guidedSheep", instance.key());
    public static final SettingBool bonePlating = new SettingBool(false, "bonePlating", instance.key());
    public static final SettingBool healerFairy = new SettingBool(false, "healerFairy", instance.key());
    public static final SettingBool treeBits = new SettingBool(false, "treeBits", instance.key());
    public static final SettingBool nausea = new SettingBool(false, "nausea", instance.key());
    public static final SettingEnum<VignetteMode> vignette = new SettingEnum<>(VignetteMode.None, VignetteMode.class, "vignetteMode", instance.key());
    public static final SettingBool expOrbs = new SettingBool(false, "expOrbs", instance.key());
    public static final SettingBool stuckArrows = new SettingBool(false, "stuckArrows", instance);

    private static final List<Pattern> deadPatterns = List.of(
            Pattern.compile(".* 0" + Utils.Symbols.heart),
            Pattern.compile(".* 0/.*" + Utils.Symbols.heart)
    );
    private static final HashSet<ParticleType<?>> explosionParticles = Sets.newHashSet(
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            ParticleTypes.GUST,
            ParticleTypes.GUST_EMITTER_LARGE
    );
    private static final EntityPredicates entityPredicates = new EntityPredicates();
    private static boolean inDungeons = false;

    public static FogData getFogAsEmpty(FogData data) {
        data.renderDistanceStart = Float.MAX_VALUE;
        data.renderDistanceEnd = Float.MAX_VALUE;
        data.environmentalStart = Float.MAX_VALUE;
        data.environmentalEnd = Float.MAX_VALUE;
        return data;
    }

    public static boolean shouldHideTooltip(Slot slot, String title) {
        if (title.startsWith("Ultrasequencer (")) {
            return false;
        }
        return instance.isActive() && emptyTooltips.value() && slot != null && slot.getItem().getHoverName().getString().trim().isEmpty();
    }

    public static boolean shouldCancelRender(Entity entity) {
        for (Predicate<Entity> predicate : entityPredicates.get()) {
            if (predicate.test(entity)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPoofParticle(SpawnParticleEvent event) {
        if (event.packet.getCount() == 1 && event.packet.getMaxSpeed() == 0.0f) {
            for (float offset : new float[]{event.packet.getXDist(), event.packet.getYDist(), event.packet.getZDist()}) {
                if (offset >= 0.1f || offset <= -0.1f || offset == 0.0f) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isCoatingParticle(SpawnParticleEvent event) {
        if (event.packet.getParticle() instanceof DustParticleOptions dustParticle) {
            Vector3f color = dustParticle.getColor();
            if ((color.x == 1.0f && color.y == 1.0f && color.z == 1.0f) || (color.x == 1.0f && color.y == 0.6f && color.z == 0.0f)) {
                return event.matchParameters(ParticleTypes.DUST, 0, 1.0, 1.0, 1.0, 1.0)
                        || event.matchParameters(ParticleTypes.DUST, 0, 1.0, 1.0, 0.6, 0.0);
            }
        }
        return false;
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (instance.isActive()) {
            if (deadEntities.value()) {
                String name = event.namePlain.replaceAll(Utils.Symbols.vampLow, "");
                for (Pattern pattern : deadPatterns) {
                    if (pattern.matcher(name).matches()) {
                        event.entity.setCustomNameVisible(false);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive()) {
            if (deadPoof.value() && event.type.equals(ParticleTypes.POOF) && isPoofParticle(event)) {
                event.cancel();
                return;
            }
            if (explosions.value() && explosionParticles.contains(event.type)) {
                event.cancel();
                return;
            }
            if (mageBeam.value() && event.type.equals(ParticleTypes.FIREWORK) && inDungeons) {
                event.cancel();
                return;
            }
            if (iceSpray.value() && event.matchParameters(ParticleTypes.POOF, 3, 0.0, 0.0, 0.0, 0.0)) {
                event.cancel();
                return;
            }
            if (powderCoating.value() && isCoatingParticle(event)) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive()) {
            inDungeons = Utils.isInDungeons();
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        inDungeons = false;
    }

    public enum VignetteMode {
        None,
        Ambient,
        Danger,
        Both
    }

    public static class EntityPredicates {
        private final List<Predicate<Entity>> predicates;

        public EntityPredicates() {
            HashSet<Block> treeBlocks = Sets.newHashSet(
                    Blocks.MANGROVE_WOOD,
                    Blocks.MANGROVE_LEAVES,
                    Blocks.STRIPPED_SPRUCE_WOOD,
                    Blocks.AZALEA_LEAVES
            );
            this.predicates = List.of(
                    (entity -> deadEntities.value() && entity instanceof LivingEntity && !entity.isAlive()),
                    (entity -> fallingBlocks.value() && entity instanceof FallingBlockEntity),
                    (entity -> {
                        if (treeBits.value() && entity instanceof Display.BlockDisplay blockDisplay) {
                            return treeBlocks.contains(blockDisplay.getBlockState().getBlock());
                        }
                        return false;
                    }),
                    (entity -> lightning.value() && entity instanceof LightningBolt),
                    (entity -> expOrbs.value() && entity instanceof ExperienceOrb),
                    (entity -> {
                        if (soulweaverSkulls.value() && entity instanceof ArmorStand stand && inDungeons) {
                            ItemStack helmet = Utils.getEntityHelmet(stand);
                            if (helmet.getItem().equals(Items.PLAYER_HEAD)) {
                                return Utils.hasTexturePayload(helmet, -1020507406);
                            }
                        }
                        return false;
                    }),
                    (entity -> guidedSheep.value() && entity instanceof Sheep sheep && sheep.getHealth() == 8.0f && inDungeons),
                    (entity -> {
                        if (bonePlating.value() && entity instanceof ItemEntity item) {
                            ItemStack stack = item.getItem();
                            return stack.getItem().equals(Items.BONE_MEAL) && stack.getHoverName().getString().equals("Bone Meal") && inDungeons;
                        }
                        return false;
                    }),
                    (entity -> {
                        if (healerFairy.value() && entity instanceof ArmorStand stand && inDungeons && stand.isMarker()) {
                            ItemStack item = stand.getItemBySlot(EquipmentSlot.MAINHAND);
                            if (item.getItem().equals(Items.PLAYER_HEAD)) {
                                return Utils.hasTexturePayload(item, 758129854);
                            }
                        }
                        return false;
                    })
            );
        }

        public List<Predicate<Entity>> get() {
            return this.predicates;
        }
    }
}
