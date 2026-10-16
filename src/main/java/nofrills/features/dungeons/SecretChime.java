package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import nofrills.config.*;
import nofrills.events.*;
import nofrills.misc.DungeonUtil;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static nofrills.Main.mc;

@EventListener
public class SecretChime {
    public static final Feature instance = new Feature("secretChime");

    public static final SettingBool itemsToggle = new SettingBool(false, "itemsToggle", instance);
    public static final SettingSound itemsSound = new SettingSound(
            new SettingString("minecraft:entity.blaze.hurt", "itemsSound", instance),
            new SettingDouble(2.0, "itemsVolume", instance),
            new SettingDouble(2.0, "itemsPitch", instance)
    );
    public static final SettingBool chestToggle = new SettingBool(false, "chestToggle", instance);
    public static final SettingSound chestSound = new SettingSound(
            new SettingString("minecraft:entity.blaze.hurt", "chestSound", instance),
            new SettingDouble(2.0, "chestVolume", instance),
            new SettingDouble(2.0, "chestPitch", instance)
    );
    public static final SettingBool essenceToggle = new SettingBool(false, "essenceToggle", instance);
    public static final SettingSound essenceSound = new SettingSound(
            new SettingString("minecraft:entity.blaze.hurt", "essenceSound", instance),
            new SettingDouble(2.0, "essenceVolume", instance),
            new SettingDouble(2.0, "essencePitch", instance)
    );
    public static final SettingBool batToggle = new SettingBool(false, "batToggle", instance);
    public static final SettingSound batSound = new SettingSound(
            new SettingString("minecraft:entity.blaze.hurt", "batSound", instance),
            new SettingDouble(2.0, "batVolume", instance),
            new SettingDouble(2.0, "batPitch", instance)
    );
    public static final SettingBool leverToggle = new SettingBool(false, "leverToggle", instance);
    public static final SettingSound leverSound = new SettingSound(
            new SettingString("minecraft:entity.blaze.hurt", "leverSound", instance),
            new SettingDouble(2.0, "leverVolume", instance),
            new SettingDouble(2.0, "leverPitch", instance)
    );

    private static final HashSet<String> secretItems = Sets.newHashSet(
            "ARCHITECT_FIRST_DRAFT",
            "DUNGEON_DECOY",
            "DUNGEON_CHEST_KEY",
            "INFLATABLE_JERRY",
            "SPIRIT_LEAP",
            "DUNGEON_TRAP",
            "CANDYCOMB",
            "HEALING_8_POTION",
            "TRAINING_WEIGHTS",
            "DEFUSE_KIT",
            "TREASURE_TALISMAN",
            "REVIVE_STONE"
    );
    private static final EntityCache entityCache = new EntityCache();
    private static final int[] essenceUUID = new int[]{677717835, 815216974, -2125861790, -1670195120};
    private static boolean clickedThisTick = false;

    private static void playSound(SettingSound sound) {
        Utils.playSound(sound.sound().value(), sound.volume().valueFloat(), sound.pitch().valueFloat());
    }

    private static boolean isEssence(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof PlayerHeadBlock && mc.level != null) {
            BlockEntity skull = mc.level.getBlockEntity(pos);
            if (skull != null) {
                CompoundTag nbt = new BlockDataAccessor(skull, pos).getData();
                Optional<CompoundTag> profile = nbt.getCompound("profile");
                if (profile.isPresent()) {
                    Optional<int[]> id = profile.get().getIntArray("id");
                    return id.isPresent() && Arrays.equals(id.get(), essenceUUID);
                }
            }
        }
        return false;
    }

    private static void playItemChime(Entity entity) {
        if (mc.player != null && entity.distanceTo(mc.player) <= 8.0) {
            playSound(itemsSound);
            entityCache.remove(entity);
        }
    }

    private static void playBatChime(Entity entity) {
        if (mc.player != null && entity.distanceTo(mc.player) <= 10.0) {
            playSound(batSound);
            entityCache.remove(entity);
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !clickedThisTick && mc.level != null) {
            BlockPos pos = event.blockHitResult.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            clickedThisTick = true;
            if (state.getBlock() instanceof ChestBlock && chestToggle.value()) {
                playSound(chestSound);
            }
            if (state.getBlock() instanceof LeverBlock && leverToggle.value()) {
                playSound(leverSound);
            }
            if (isEssence(state, pos) && essenceToggle.value()) {
                playSound(essenceSound);
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (itemsToggle.value() && event.entity instanceof ItemEntity item) {
                if (secretItems.contains(Utils.getMarketId(item.getItem()))) {
                    entityCache.add(item);
                }
                if (!item.isAlive() && entityCache.has(item)) {
                    playItemChime(item);
                }
            }
            if (batToggle.value() && event.entity instanceof Bat bat) {
                if (DungeonUtil.isSecretBat(bat)) {
                    entityCache.add(bat);
                }
                if (!bat.isAlive() && entityCache.has(bat)) {
                    playBatChime(bat);
                }
            }
        }
    }

    @EventHandler
    private static void onRemoved(EntityRemovedEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (itemsToggle.value() && event.entity instanceof ItemEntity && entityCache.has(event.entity)) {
                playItemChime(event.entity);
            }
            if (batToggle.value() && event.entity instanceof Bat && entityCache.has(event.entity)) {
                playBatChime(event.entity);
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (clickedThisTick) { // the game likes to interact with chests twice per tick for some reason
            clickedThisTick = false;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        clickedThisTick = false;
    }
}
