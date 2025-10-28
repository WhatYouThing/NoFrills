package nofrills.features.dungeons;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.PlayerSkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.BlockDataObject;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingString;
import nofrills.events.*;
import nofrills.misc.EntityCache;
import nofrills.misc.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static nofrills.Main.mc;

public class SecretChime {
    public static final Feature instance = new Feature("secretChime");

    public static final SettingBool itemsToggle = new SettingBool(false, "itemsToggle", instance);
    public static final SettingString itemsSound = new SettingString("minecraft:entity.blaze.hurt", "itemsSound", instance);
    public static final SettingDouble itemsVolume = new SettingDouble(1.0, "itemsVolume", instance);
    public static final SettingDouble itemsPitch = new SettingDouble(2.0, "itemsPitch", instance);
    public static final SettingBool chestToggle = new SettingBool(false, "chestToggle", instance);
    public static final SettingString chestSound = new SettingString("minecraft:entity.blaze.hurt", "chestSound", instance);
    public static final SettingDouble chestVolume = new SettingDouble(1.0, "chestVolume", instance);
    public static final SettingDouble chestPitch = new SettingDouble(2.0, "chestPitch", instance);
    public static final SettingBool essenceToggle = new SettingBool(false, "essenceToggle", instance);
    public static final SettingString essenceSound = new SettingString("minecraft:entity.blaze.hurt", "essenceSound", instance);
    public static final SettingDouble essenceVolume = new SettingDouble(1.0, "essenceVolume", instance);
    public static final SettingDouble essencePitch = new SettingDouble(2.0, "essencePitch", instance);
    public static final SettingBool batToggle = new SettingBool(false, "batToggle", instance);
    public static final SettingString batSound = new SettingString("minecraft:entity.blaze.hurt", "batSound", instance);
    public static final SettingDouble batVolume = new SettingDouble(1.0, "batVolume", instance);
    public static final SettingDouble batPitch = new SettingDouble(2.0, "batPitch", instance);
    public static final SettingBool leverToggle = new SettingBool(false, "leverToggle", instance);
    public static final SettingString leverSound = new SettingString("minecraft:entity.blaze.hurt", "leverSound", instance);
    public static final SettingDouble leverVolume = new SettingDouble(1.0, "leverVolume", instance);
    public static final SettingDouble leverPitch = new SettingDouble(2.0, "leverPitch", instance);

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
    private static final EntityCache batCache = new EntityCache();
    private static int lastSecretId = -1;
    private static boolean clickedThisTick = false;

    private static void playSound(SettingString sound, SettingDouble volume, SettingDouble pitch) {
        Utils.playSound(SoundEvent.of(Identifier.of(sound.value())), SoundCategory.MASTER, volume.valueFloat(), pitch.valueFloat());
    }

    private static boolean isSecretItem(int id) {
        if (mc.world != null && mc.player != null && id != lastSecretId && mc.world.getEntityById(id) instanceof ItemEntity item) {
            return secretItems.contains(Utils.getMarketId(item.getStack())) && mc.player.getPos().distanceTo(item.getPos()) <= 8.0;
        }
        return false;
    }

    private static boolean isEssence(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof PlayerSkullBlock) {
            BlockEntity skull = mc.world.getBlockEntity(pos);
            if (skull != null) {
                NbtCompound nbt = new BlockDataObject(skull, pos).getNbt();
                Optional<NbtCompound> profile = nbt.getCompound("profile");
                if (profile.isPresent()) {
                    Optional<int[]> id = profile.get().getIntArray("id"); // match the uuid, profile data is corrupted at the time of adding
                    return id.isPresent() && Arrays.equals(id.get(), new int[]{-520885975, -2036449846, -1794878266, 1726902051});
                }
            }
        }
        return false;
    }

    private static boolean isSecretBat(int id) {
        if (mc.world != null && mc.player != null && mc.world.getEntityById(id) instanceof BatEntity bat) {
            return batCache.has(bat) && bat.distanceTo(mc.player) <= 16.0;
        }
        return false;
    }

    private static void playItemChime(int id) {
        playSound(itemsSound, itemsVolume, itemsPitch);
        lastSecretId = id;
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (event.packet instanceof EntitiesDestroyS2CPacket packet) {
                for (int id : packet.getEntityIds()) {
                    if (isSecretItem(id) && itemsToggle.value()) {
                        playItemChime(id);
                        break;
                    }
                    if (isSecretBat(id) && batToggle.value()) {
                        playSound(batSound, batVolume, batPitch);
                        batCache.removeDead();
                        break;
                    }
                }
            }
            if (event.packet instanceof ItemPickupAnimationS2CPacket packet) {
                if (isSecretItem(packet.getEntityId()) && itemsToggle.value()) {
                    playItemChime(packet.getEntityId());
                }
            }
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !clickedThisTick) {
            BlockPos pos = event.blockHitResult.getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            clickedThisTick = true;
            if (state.getBlock() instanceof ChestBlock && chestToggle.value()) {
                playSound(chestSound, chestVolume, chestPitch);
            }
            if (state.getBlock() instanceof LeverBlock && leverToggle.value()) {
                playSound(leverSound, leverVolume, leverPitch);
            }
            if (isEssence(state, pos) && essenceToggle.value()) {
                playSound(essenceSound, essenceVolume, essencePitch);
            }
        }
    }

    @EventHandler
    private static void onUpdated(EntityUpdatedEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && SecretBatHighlight.isSecretBat(event.entity) && batToggle.value()) {
            batCache.add(event.entity);
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (clickedThisTick) {
            clickedThisTick = false;
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        clickedThisTick = false;
        lastSecretId = -1;
        batCache.clear();
    }
}
