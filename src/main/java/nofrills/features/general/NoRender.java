package nofrills.features.general;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.fog.FogData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.misc.Utils;

import java.util.HashSet;
import java.util.List;
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
    public static final SettingBool mageBeam = new SettingBool(false, "mageBeam", instance.key());
    public static final SettingBool treeBits = new SettingBool(false, "treeBits", instance.key());
    public static final SettingBool nausea = new SettingBool(false, "nausea", instance.key());
    public static final SettingBool vignette = new SettingBool(false, "vignette", instance.key());
    public static final SettingBool expOrbs = new SettingBool(false, "expOrbs", instance.key());
    public static final SettingBool stuckArrows = new SettingBool(false, "stuckArrows", instance.key());

    private static final List<Pattern> deadPatterns = List.of(
            Pattern.compile(".* 0" + Utils.Symbols.heart),
            Pattern.compile(".* 0/.*" + Utils.Symbols.heart)
    );
    private static final HashSet<Block> treeBlocks = Sets.newHashSet(
            Blocks.MANGROVE_WOOD,
            Blocks.MANGROVE_LEAVES,
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.AZALEA_LEAVES
    );
    private static final HashSet<ParticleType<?>> explosionParticles = Sets.newHashSet(
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            ParticleTypes.GUST,
            ParticleTypes.GUST_EMITTER_LARGE
    );

    public static FogData getFogAsEmpty(FogData data) {
        data.renderDistanceStart = Float.MAX_VALUE;
        data.renderDistanceEnd = Float.MAX_VALUE;
        data.environmentalStart = Float.MAX_VALUE;
        data.environmentalEnd = Float.MAX_VALUE;
        return data;
    }

    public static boolean isTreeBlock(Entity entity) {
        if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {
            return treeBlocks.contains(blockDisplay.getBlockState().getBlock());
        }
        return false;
    }

    public static boolean shouldHideTooltip(Slot slot, String title) {
        if (title.startsWith("Ultrasequencer (")) {
            return false;
        }
        return instance.isActive() && emptyTooltips.value() && slot != null && slot.getStack().getName().getString().trim().isEmpty();
    }

    private static boolean isPoofParticle(ParticleS2CPacket packet) {
        if (packet.getCount() == 1 && packet.getSpeed() == 0.0f) {
            for (float offset : List.of(packet.getOffsetX(), packet.getOffsetY(), packet.getOffsetZ())) {
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
        if (instance.isActive() && deadEntities.value()) {
            String name = event.namePlain.replaceAll(Utils.Symbols.vampLow, "");
            for (Pattern pattern : deadPatterns) {
                if (pattern.matcher(name).matches()) {
                    event.entity.setCustomNameVisible(false);
                    break;
                }
            }
        }
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive()) {
            if (event.type.equals(ParticleTypes.POOF) && deadPoof.value() && isPoofParticle(event.packet)) {
                event.cancel();
            }
            if (explosionParticles.contains(event.type) && explosions.value()) {
                event.cancel();
            }
            if (event.type.equals(ParticleTypes.FIREWORK) && mageBeam.value() && Utils.isInDungeons()) {
                event.cancel();
            }
        }
    }
}
