package nofrills.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import meteordevelopment.orbit.EventHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import nofrills.config.category.*;
import nofrills.events.WorldTickEvent;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class Config {
    public static final ConfigClassHandler<Config> configHandler = ConfigClassHandler.createBuilder(Config.class)
            .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg).setPath(FabricLoader.getInstance().getConfigDir().resolve("NoFrills/Config.json")).setJson5(false).build()).build();
    private static final DecimalFormat floatSliderFormat = new DecimalFormat("0.##");

    // General

    @SerialEntry
    public static boolean updateChecker = false;
    @SerialEntry
    public static boolean autoSprint = false;
    @SerialEntry
    public static boolean noSelfieCam = false;
    @SerialEntry
    public static boolean terrorFix = false;
    @SerialEntry
    public static boolean wardrobeHotkeys = false;
    @SerialEntry
    public static boolean wardrobeHotkeysSound = false;
    @SerialEntry
    public static boolean priceTooltips = false;
    @SerialEntry
    public static boolean hotbarSwap = false;
    @SerialEntry
    public static int hotbarSwapOverride = 8;
    @SerialEntry
    public static boolean ignoreBackground = false;
    @SerialEntry
    public static boolean hideDeadMobs = false;
    @SerialEntry
    public static boolean keepChunks = false;
    @SerialEntry
    public static boolean noExplosions = false;
    @SerialEntry
    public static boolean noFireOverlay = false;
    @SerialEntry
    public static boolean noBreakParticles = false;
    @SerialEntry
    public static boolean overlayEtherwarp = false;
    @SerialEntry
    public static boolean partyFinderOptions = false;
    @SerialEntry
    public static String partyPrefixes = "! ?";
    @SerialEntry
    public static List<String> partyWhitelist = new ArrayList<>();
    @SerialEntry
    public static List<String> partyBlacklist = new ArrayList<>();
    @SerialEntry
    public static partyBehaviorList partyBehavior = partyBehaviorList.Manual;
    @SerialEntry
    public static boolean partyCmdWarp = false;
    @SerialEntry
    public static boolean partyCmdTransfer = false;
    @SerialEntry
    public static boolean partyCmdAllInvite = false;
    @SerialEntry
    public static boolean partyCmdDowntime = false;
    @SerialEntry
    public static boolean partyCmdQueue = false;
    @SerialEntry
    public static boolean noHaste = false;
    @SerialEntry
    public static int viewmodelSpeed = 0;
    @SerialEntry
    public static boolean viewmodelEnable = false;
    @SerialEntry
    public static float viewmodelOffsetX = 0.0f;
    @SerialEntry
    public static float viewmodelOffsetY = 0.0f;
    @SerialEntry
    public static float viewmodelOffsetZ = 0.0f;
    @SerialEntry
    public static float viewmodelScaleX = 1.0f;
    @SerialEntry
    public static float viewmodelScaleY = 1.0f;
    @SerialEntry
    public static float viewmodelScaleZ = 1.0f;
    @SerialEntry
    public static float viewmodelRotX = 0.0f;
    @SerialEntry
    public static float viewmodelRotY = 0.0f;
    @SerialEntry
    public static float viewmodelRotZ = 0.0f;

    // Fixes

    @SerialEntry
    public static fixModes stonkFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes oldSneak = fixModes.Disabled;
    @SerialEntry
    public static fixModes antiSwim = fixModes.Disabled;
    @SerialEntry
    public static fixModes noPearlCooldown = fixModes.Disabled;
    @SerialEntry
    public static fixModes snowFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes noDropSwing = fixModes.Disabled;
    @SerialEntry
    public static fixModes itemCountFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes ridingCamFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes sneakFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes middleClickFix = fixModes.Disabled;

    // Solvers

    // Fishing

    @SerialEntry
    public static boolean capEnabled = false;
    @SerialEntry
    public static int capTarget = 50;
    @SerialEntry
    public static int capDelay = 30;
    @SerialEntry
    public static boolean capRender = false;
    @SerialEntry
    public static boolean capSendMsg = false;
    @SerialEntry
    public static String capMsg = "/pc SEA CREATURE CAP REACHED!";
    @SerialEntry
    public static boolean capSound = false;
    @SerialEntry
    public static boolean capTitle = false;
    @SerialEntry
    public static boolean rareTitle = false;
    @SerialEntry
    public static boolean rareGlow = false;
    @SerialEntry
    public static boolean rareSendMsg = false;
    @SerialEntry
    public static String rareMsg = "/pc {spawnmsg}";
    @SerialEntry
    public static boolean rareSound = false;
    @SerialEntry
    public static boolean rareReplace = false;
    @SerialEntry
    public static boolean muteDrake = false;

    // Dungeons

    @SerialEntry
    public static String dungeonClass = "Berserk"; // this stores the user's last selected/known Dungeon class, not for use with config screens
    @SerialEntry
    public static boolean starredMobHighlight = false;
    @SerialEntry
    public static Color starredMobColor = new Color(0, 255, 255, 255);
    @SerialEntry
    public static boolean miniHighlight = false;
    @SerialEntry
    public static Color miniColor = new Color(255, 255, 0, 255);
    @SerialEntry
    public static boolean keyHighlight = false;
    @SerialEntry
    public static Color keyColor = new Color(0, 255, 0, 128);
    @SerialEntry
    public static boolean spiritHighlight = false;
    @SerialEntry
    public static Color spiritColor = new Color(175, 0, 255, 170);
    @SerialEntry
    public static boolean solveTerminals = false;
    @SerialEntry
    public static boolean solveDevices = false;
    @SerialEntry
    public static boolean melodyAnnounce = false;
    @SerialEntry
    public static String melodyMessage = "/pc Melody";
    @SerialEntry
    public static boolean wishReminder = false;

    // Kuudra

    @SerialEntry
    public static boolean kuudraHitbox = false;
    @SerialEntry
    public static boolean kuudraHealth = false;
    @SerialEntry
    public static boolean kuudraDPS = false;
    @SerialEntry
    public static boolean kuudraMissing = false;
    @SerialEntry
    public static boolean kuudraPileFix = false;
    @SerialEntry
    public static boolean kuudraFresh = false;
    @SerialEntry
    public static String kuudraFreshMsg = "/pc EAT FRESH!";
    @SerialEntry
    public static boolean kuudraFreshTimer = false;
    @SerialEntry
    public static boolean kuudraDrain = false;
    @SerialEntry
    public static String kuudraDrainMsg = "/pc Used {mana} Mana on {players} players!";

    // Mining

    @SerialEntry
    public static boolean miningCorpseGlow = false;
    @SerialEntry
    public static boolean ghostVision = false;
    @SerialEntry
    public static boolean betterSkymall = false;
    @SerialEntry
    public static String skymallWhitelist = "titanium, goblins";

    // Farming

    @SerialEntry
    public static boolean spaceFarmer = false;
    @SerialEntry
    public static boolean lockView = false;
    @SerialEntry
    public static boolean shroomHighlight = false;

    // Slayers

    @SerialEntry
    public static boolean slayerHitboxes = false;
    @SerialEntry
    public static boolean slayerKillTime = false;
    @SerialEntry
    public static boolean blazeNoSpam = false;
    @SerialEntry
    public static boolean blazePillarWarn = false;
    @SerialEntry
    public static boolean vampIce = false;
    @SerialEntry
    public static boolean vampSteak = false;
    @SerialEntry
    public static boolean vampSteakHighlight = false;
    @SerialEntry
    public static boolean vampChalice = false;
    @SerialEntry
    public static Color vampChaliceColor = new Color(175, 0, 255, 170);
    @SerialEntry
    public static boolean vampManiaSilence = false;
    @SerialEntry
    public static boolean vampManiaReplace = false;
    @SerialEntry
    public static boolean vampSpringSilence = false;
    @SerialEntry
    public static boolean vampSpringReplace = false;
    @SerialEntry
    public static boolean emanHitDisplay = false;

    // end of setting values

    public static Screen configScreen = null;

    public static BooleanControllerBuilder booleanController(Option<Boolean> option) {
        return BooleanControllerBuilder.create(option).formatValue(value -> value ? Text.of("Enabled") : Text.of("Disabled")).coloured(true);
    }

    public static IntegerSliderControllerBuilder intSliderController(Option<Integer> option, int min, int max, int step) {
        return IntegerSliderControllerBuilder.create(option).range(min, max).step(step);
    }

    public static FloatSliderControllerBuilder floatSliderController(Option<Float> option, float min, float max, float step) {
        return FloatSliderControllerBuilder.create(option).range(min, max).step(step).formatValue(value -> Text.of(floatSliderFormat.format(value)));
    }

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(configHandler, (defaults, config, builder) -> builder
                .title(Text.of("NoFrills Config"))
                .category(General.create(defaults, config))
                .category(Fixes.create(defaults, config))
                .category(Fishing.create(defaults, config))
                .category(Dungeons.create(defaults, config))
                .category(Kuudra.create(defaults, config))
                .category(Slayers.create(defaults, config))
                .category(Mining.create(defaults, config))
                .category(Farming.create(defaults, config))
        ).generateScreen(parent);
    }

    public static void openConfigScreen() {
        configScreen = getConfigScreen(null);
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (configScreen != null) {
            mc.setScreen(configScreen);
            configScreen = null;
        }
        // we have to use this voodoo because running setScreen directly from a command does absolutely nothing.
    }

    public enum partyBehaviorList {
        Automatic,
        Manual,
        Ignore
    }

    public enum fixModes {
        Disabled,
        SkyblockOnly,
        Enabled
    }
}
