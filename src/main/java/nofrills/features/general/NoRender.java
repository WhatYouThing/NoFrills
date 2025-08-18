package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.EntityNamedEvent;
import nofrills.events.SpawnParticleEvent;
import nofrills.misc.Utils;

import java.util.List;
import java.util.regex.Pattern;

public class NoRender {
    public static final Feature instance = new Feature("noRender");

    public static final SettingBool explosions = new SettingBool(false, "explosions", instance.key());
    public static final SettingBool emptyTooltips = new SettingBool(false, "emptyTooltips", instance.key());
    public static final SettingBool fireOverlay = new SettingBool(false, "fireOverlay", instance.key());
    public static final SettingBool breakParticles = new SettingBool(false, "breakParticles", instance.key());
    public static final SettingBool bossBar = new SettingBool(false, "bossBar", instance.key());
    public static final SettingBool effectDisplay = new SettingBool(false, "effectDisplay", instance.key());
    public static final SettingBool deadEntities = new SettingBool(false, "deadEntities", instance.key());
    public static final SettingBool lightning = new SettingBool(false, "lightning", instance.key());
    public static final SettingBool fallingBlocks = new SettingBool(false, "fallingBlocks", instance.key());
    public static final SettingBool mageBeam = new SettingBool(false, "mageBeam", instance.key());
    public static final SettingBool treeBits = new SettingBool(false, "treeBits", instance.key());
    public static final SettingBool nametagInvisibility = new SettingBool(false, "nametagInvisibility", instance.key());

    private static final List<Pattern> deadPatterns = List.of(
            Pattern.compile(".* 0" + Utils.Symbols.heart),
            Pattern.compile(".* 0/.*" + Utils.Symbols.heart),
            Pattern.compile(".* 0/.*" + Utils.Symbols.heart + " " + Utils.Symbols.vampLow)
    );

    private static final List<Block> treeBlocks = List.of(
            Blocks.MANGROVE_WOOD,
            Blocks.MANGROVE_LEAVES,
            Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.AZALEA_LEAVES
    );

    private static final List<ParticleType<?>> explosionParticles = List.of(
            ParticleTypes.EXPLOSION,
            ParticleTypes.EXPLOSION_EMITTER,
            ParticleTypes.GUST,
            ParticleTypes.GUST_EMITTER_LARGE
    );

    public static boolean isTreeBlock(Entity entity) {
        if (entity instanceof DisplayEntity.BlockDisplayEntity blockDisplay) {
            Block block = blockDisplay.getBlockState().getBlock();
            for (Block blacklisted : treeBlocks) {
                if (block.equals(blacklisted)) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (instance.isActive() && deadEntities.value() && event.entity instanceof ArmorStandEntity) {
            for (Pattern pattern : deadPatterns) {
                if (pattern.matcher(event.namePlain).matches()) {
                    event.entity.setCustomNameVisible(false);
                    break;
                }
            }
        }
    }

    @EventHandler
    public static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive()) {
            if (explosions.value()) {
                for (ParticleType<?> type : explosionParticles) {
                    if (event.type.equals(type)) {
                        event.cancel();
                        return;
                    }
                }
            }
            if (mageBeam.value() && Utils.isInDungeons() && event.type.equals(ParticleTypes.FIREWORK)) {
                event.cancel();
            }
        }
    }
}
