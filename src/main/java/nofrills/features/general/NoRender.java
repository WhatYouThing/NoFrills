package nofrills.features.general;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
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
import nofrills.events.EntityNamedEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NoRender {
    public static final Feature instance = new Feature("noRender");

    public static final SettingBool explosions = new SettingBool(false, "explosions", instance.key());
    public static final SettingBool emptyTooltips = new SettingBool(false, "emptyTooltips", instance.key());
    public static final SettingBool fireOverlay = new SettingBool(false, "fireOverlay", instance.key());
    public static final SettingBool breakParticles = new SettingBool(false, "breakParticles", instance.key());
    public static final SettingBool bossBar = new SettingBool(false, "bossBar", instance.key());
    public static final SettingBool fog = new SettingBool(false, "fog", instance.key());
    public static final SettingBool effectDisplay = new SettingBool(false, "effectDisplay", instance.key());
    public static final SettingBool deadEntities = new SettingBool(false, "deadEntities", instance.key());
    public static final SettingBool deadPoof = new SettingBool(false, "deadPoof", instance.key());
    public static final SettingBool lightning = new SettingBool(false, "lightning", instance.key());
    public static final SettingBool fallingBlocks = new SettingBool(false, "fallingBlocks", instance.key());
    public static final SettingBool entityFire = new SettingBool(false, "entityFire", instance.key());
    public static final SettingBool mageBeam = new SettingBool(false, "mageBeam", instance.key());
    public static final SettingBool iceSpray = new SettingBool(false, "iceSpray", instance.key());
    public static final SettingBool soulweaverSkulls = new SettingBool(false, "soulweaverSkulls", instance.key());
    public static final SettingBool guidedSheep = new SettingBool(false, "guidedSheep", instance.key());
    public static final SettingBool bonePlating = new SettingBool(false, "bonePlating", instance.key());
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

    private static List<Predicate<Entity>> initEntityPredicates() {
        String skullTexture = "2f24ed6875304fa4a1f0c785b2cb6a6a72563e9f3e24ea55e18178452119aa66";
        HashSet<Block> treeBlocks = Sets.newHashSet(
                Blocks.MANGROVE_WOOD,
                Blocks.MANGROVE_LEAVES,
                Blocks.STRIPPED_SPRUCE_WOOD,
                Blocks.AZALEA_LEAVES
        );
        return List.of(
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
                    if (soulweaverSkulls.value() && entity instanceof ArmorStand stand) {
                        ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
                        if (!helmet.isEmpty() && helmet.getItem().equals(Items.PLAYER_HEAD)) {
                            GameProfile profile = Utils.getTextures(helmet);
                            return Utils.isTextureEqual(profile, skullTexture) && Utils.isInDungeons();
                        }
                    }
                    return false;
                })
        );
    }

    private static boolean isPoofParticle(ClientboundLevelParticlesPacket packet) {
        if (packet.getCount() == 1 && packet.getMaxSpeed() == 0.0f) {
            for (float offset : List.of(packet.getXDist(), packet.getYDist(), packet.getZDist())) {
                if (offset >= 0.1f || offset <= -0.1f || offset == 0.0f) {
                    return false;
                }
            }
            return true;
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
            if (deadPoof.value() && event.type.equals(ParticleTypes.POOF) && isPoofParticle(event.packet)) {
                event.cancel();
            }
            if (explosions.value() && explosionParticles.contains(event.type)) {
                event.cancel();
            }
            if (mageBeam.value() && event.type.equals(ParticleTypes.FIREWORK) && Utils.isInDungeons()) {
                event.cancel();
            }
            if (iceSpray.value() && event.matchParameters(ParticleTypes.POOF, 3, 0.0, 0.0, 0.0, 0.0)) {
                event.cancel();
            }
        }
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
            String skullTexture = "2f24ed6875304fa4a1f0c785b2cb6a6a72563e9f3e24ea55e18178452119aa66";
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
                        if (soulweaverSkulls.value() && entity instanceof ArmorStand stand) {
                            ItemStack helmet = Utils.getEntityArmor(stand).getFirst();
                            if (!helmet.isEmpty() && helmet.getItem().equals(Items.PLAYER_HEAD)) {
                                GameProfile profile = Utils.getTextures(helmet);
                                return Utils.isTextureEqual(profile, skullTexture) && Utils.isInDungeons();
                            }
                        }
                        return false;
                    }),
                    (entity -> guidedSheep.value() && entity instanceof Sheep sheep && sheep.getHealth() == 8.0f && Utils.isInDungeons()),
                    (entity -> {
                        if (bonePlating.value() && entity instanceof ItemEntity item) {
                            ItemStack stack = item.getItem();
                            return stack.getItem().equals(Items.BONE_MEAL) && stack.getHoverName().getString().equals("Bone Meal") && Utils.isInDungeons();
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
