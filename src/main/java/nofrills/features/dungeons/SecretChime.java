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
    public static final SettingDouble itemsVolume = new SettingDouble(2.0, "itemsVolume", instance);
    public static final SettingDouble itemsPitch = new SettingDouble(2.0, "itemsPitch", instance);
    public static final SettingBool chestToggle = new SettingBool(false, "chestToggle", instance);
    public static final SettingString chestSound = new SettingString("minecraft:entity.blaze.hurt", "chestSound", instance);
    public static final SettingDouble chestVolume = new SettingDouble(2.0, "chestVolume", instance);
    public static final SettingDouble chestPitch = new SettingDouble(2.0, "chestPitch", instance);
    public static final SettingBool essenceToggle = new SettingBool(false, "essenceToggle", instance);
    public static final SettingString essenceSound = new SettingString("minecraft:entity.blaze.hurt", "essenceSound", instance);
    public static final SettingDouble essenceVolume = new SettingDouble(2.0, "essenceVolume", instance);
    public static final SettingDouble essencePitch = new SettingDouble(2.0, "essencePitch", instance);
    public static final SettingBool batToggle = new SettingBool(false, "batToggle", instance);
    public static final SettingString batSound = new SettingString("minecraft:entity.blaze.hurt", "batSound", instance);
    public static final SettingDouble batVolume = new SettingDouble(2.0, "batVolume", instance);
    public static final SettingDouble batPitch = new SettingDouble(2.0, "batPitch", instance);
    public static final SettingBool leverToggle = new SettingBool(false, "leverToggle", instance);
    public static final SettingString leverSound = new SettingString("minecraft:entity.blaze.hurt", "leverSound", instance);
    public static final SettingDouble leverVolume = new SettingDouble(2.0, "leverVolume", instance);
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
    private static final int[] essenceUUID = new int[]{-520885975, -2036449846, -1794878266, 1726902051};
    private static int lastItemId = -1;
    private static boolean clickedThisTick = false;

    private static void playSound(SettingString sound, SettingDouble volume, SettingDouble pitch) {
        Utils.playSound(SoundEvent.of(Identifier.of(sound.value())), SoundCategory.MASTER, volume.valueFloat(), pitch.valueFloat());
    }

    private static boolean isSecretItem(int id, boolean distCheck) {
        return mc.world != null && mc.player != null && id != lastItemId && mc.world.getEntityById(id) instanceof ItemEntity item
                && secretItems.contains(Utils.getMarketId(item.getStack())) && (!distCheck || mc.player.getEyePos().distanceTo(item.getPos()) <= 10.0);
    }

    private static boolean isSecretBat(int id) {
        return mc.world != null && mc.player != null && mc.world.getEntityById(id) instanceof BatEntity bat
                && batCache.has(bat) && mc.player.getEyePos().distanceTo(bat.getPos()) <= 10.0;
    }

    private static boolean isEssence(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof PlayerSkullBlock && mc.world != null) {
            BlockEntity skull = mc.world.getBlockEntity(pos);
            if (skull != null) {
                NbtCompound nbt = new BlockDataObject(skull, pos).getNbt();
                Optional<NbtCompound> profile = nbt.getCompound("profile");
                if (profile.isPresent()) {
                    Optional<int[]> id = profile.get().getIntArray("id");
                    return id.isPresent() && Arrays.equals(id.get(), essenceUUID); // match the uuid, profile data is corrupted at the time of adding
                }
            }
        }
        return false;
    }

    private static void playItemChime(int id) {
        playSound(itemsSound, itemsVolume, itemsPitch);
        lastItemId = id;
    }

    private static void playBatChime() {
        playSound(batSound, batVolume, batPitch);
        batCache.removeDead();
    }

    @EventHandler
    private static void onPacket(ReceivePacketEvent event) {
        if (instance.isActive() && Utils.isInDungeons()) {
            if (event.packet instanceof EntitiesDestroyS2CPacket packet) {
                for (int id : packet.getEntityIds()) {
                    if (isSecretItem(id, true) && itemsToggle.value()) {
                        playItemChime(id);
                        break;
                    }
                    if (isSecretBat(id) && batToggle.value()) {
                        playBatChime();
                        break;
                    }
                }
            }
            if (event.packet instanceof ItemPickupAnimationS2CPacket packet) {
                if (isSecretItem(packet.getEntityId(), false) && itemsToggle.value()) {
                    playItemChime(packet.getEntityId());
                }
            }
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && Utils.isInDungeons() && !clickedThisTick && mc.world != null) {
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
        if (instance.isActive() && Utils.isInDungeons() && event.entity instanceof BatEntity bat && batToggle.value()) {
            if (SecretBatHighlight.isSecretBat(bat)) {
                batCache.add(bat);
            }
            if (!bat.isAlive() && isSecretBat(bat.getId())) {
                playBatChime();
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
        lastItemId = -1;
        batCache.clear();
    }
}
