package nofrills.config;

import com.google.gson.JsonObject;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import nofrills.config.category.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final ConfigClassHandler<Config> configHandler = ConfigClassHandler.createBuilder(Config.class)
            .serializer(cfg -> GsonConfigSerializerBuilder.create(cfg).setPath(FabricLoader.getInstance().getConfigDir().resolve("NoFrills/Config.json")).setJson5(false).build()).build();
    private static final DecimalFormat floatSliderFormat = new DecimalFormat("0.00");

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
    public static boolean middleClickOverride = false;
    @SerialEntry
    public static boolean slotBinding = false;
    @SerialEntry
    public static JsonObject slotBindData = new JsonObject();
    @SerialEntry
    public static boolean hideDeadMobs = false;
    @SerialEntry
    public static boolean noExplosions = false;
    @SerialEntry
    public static boolean noFireOverlay = false;
    @SerialEntry
    public static boolean noBreakParticles = false;
    @SerialEntry
    public static boolean noBossBar = false;
    @SerialEntry
    public static boolean noLoadingScreen = false;
    @SerialEntry
    public static boolean overlayEtherwarp = false;
    @SerialEntry
    public static boolean partyFinderOptions = false;
    @SerialEntry
    public static boolean partyWaypoints = false;
    @SerialEntry
    public static Color partyWaypointColor = new Color(85, 85, 255, 170);
    @SerialEntry
    public static int partyWaypointTime = 60;
    @SerialEntry
    public static boolean chatWaypoints = false;
    @SerialEntry
    public static Color chatWaypointColor = new Color(85, 255, 255, 170);
    @SerialEntry
    public static int chatWaypointTime = 30;
    @SerialEntry
    public static String partyPrefixes = "! ?";
    @SerialEntry
    public static List<String> partyWhitelist = new ArrayList<>();
    @SerialEntry
    public static List<String> partyBlacklist = new ArrayList<>();
    @SerialEntry
    public static partyBehaviorList partyCmdWarp = partyBehaviorList.Disabled;
    @SerialEntry
    public static partyBehaviorList partyCmdTransfer = partyBehaviorList.Disabled;
    @SerialEntry
    public static partyBehaviorList partyCmdAllInvite = partyBehaviorList.Disabled;
    @SerialEntry
    public static partyBehaviorList partyCmdDowntime = partyBehaviorList.Disabled;
    @SerialEntry
    public static partyBehaviorList partyCmdQueue = partyBehaviorList.Disabled;
    @SerialEntry
    public static partyBehaviorList partyCmdCoords = partyBehaviorList.Disabled;
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
    @SerialEntry
    public static fixModes armorStandFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes abilityPlaceFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes efficiencyFix = fixModes.Disabled;
    @SerialEntry
    public static fixModes clearCursorStack = fixModes.Disabled;

    // Events

    @SerialEntry
    public static boolean spookyChestAlert = false;
    @SerialEntry
    public static boolean spookyChestHighlight = false;
    @SerialEntry
    public static Color spookyChestHighlightColor = new Color(255, 170, 0, 170);
    @SerialEntry
    public static boolean calendarDate = false;

    // Solvers

    @SerialEntry
    public static boolean solveChronomatron = false;
    @SerialEntry
    public static boolean solveUltrasequencer = false;
    @SerialEntry
    public static boolean solveSuperpairs = false;

    // HUD

    @SerialEntry
    public static boolean bobberEnabled = false;
    @SerialEntry
    public static boolean bobberLeftHand = false;
    @SerialEntry
    public static double bobberPosX = 0.01;
    @SerialEntry
    public static double bobberPosY = 0.1;
    @SerialEntry
    public static boolean seaCreaturesEnabled = false;
    @SerialEntry
    public static boolean seaCreaturesLeftHand = false;
    @SerialEntry
    public static double seaCreaturesPosX = 0.01;
    @SerialEntry
    public static double seaCreaturesPosY = 0.13;
    @SerialEntry
    public static boolean tpsEnabled = false;
    @SerialEntry
    public static boolean tpsLeftHand = false;
    @SerialEntry
    public static double tpsPosX = 0.01;
    @SerialEntry
    public static double tpsPosY = 0.04;
    @SerialEntry
    public static boolean lagMeterEnabled = false;
    @SerialEntry
    public static int lagMeterMinTime = 500;
    @SerialEntry
    public static boolean lagMeterLeftHand = false;
    @SerialEntry
    public static double lagMeterPosX = 0.01;
    @SerialEntry
    public static double lagMeterPosY = 0.19;
    @SerialEntry
    public static boolean powerEnabled = false;
    @SerialEntry
    public static boolean powerDungeonsOnly = false;
    @SerialEntry
    public static boolean powerLeftHand = false;
    @SerialEntry
    public static double powerPosX = 0.01;
    @SerialEntry
    public static double powerPosY = 0.16;
    @SerialEntry
    public static boolean dayEnabled = false;
    @SerialEntry
    public static boolean dayLeftHand = false;
    @SerialEntry
    public static double dayPosX = 0.01;
    @SerialEntry
    public static double dayPosY = 0.07;
    @SerialEntry
    public static boolean pingEnabled = false;
    @SerialEntry
    public static boolean pingLeftHand = false;
    @SerialEntry
    public static double pingPosX = 0.01;
    @SerialEntry
    public static double pingPosY = 0.01;

    // Fishing

    @SerialEntry
    public static boolean capEnabled = false;
    @SerialEntry
    public static int capTarget = 50;
    @SerialEntry
    public static int capDelay = 30;
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
    public static boolean fastTerminals = false;
    @SerialEntry
    public static boolean solveDevices = false;
    @SerialEntry
    public static boolean melodyAnnounce = false;
    @SerialEntry
    public static String melodyMessage = "/pc Melody";
    @SerialEntry
    public static boolean dragAlert = false;
    @SerialEntry
    public static float dragSkip = 22.0f;
    @SerialEntry
    public static float dragSkipEasy = 19.0f;
    @SerialEntry
    public static boolean dragHealth = false;
    @SerialEntry
    public static boolean dragTimer = false;
    @SerialEntry
    public static boolean dragBoxes = false;
    @SerialEntry
    public static boolean dragGlow = false;
    @SerialEntry
    public static boolean dragStack = false;
    @SerialEntry
    public static boolean dragStackAdvanced = false;
    @SerialEntry
    public static boolean wishReminder = false;
    @SerialEntry
    public static boolean leapOverlay = false;
    @SerialEntry
    public static boolean leapOverlayMsg = false;
    @SerialEntry
    public static Color leapColorHealer = new Color(0xecb50c);
    @SerialEntry
    public static Color leapColorMage = new Color(0x1793c4);
    @SerialEntry
    public static Color leapColorBers = new Color(0xe7413c);
    @SerialEntry
    public static Color leapColorArch = new Color(0x4a14b7);
    @SerialEntry
    public static Color leapColorTank = new Color(0x768f46);
    @SerialEntry
    public static boolean gyroTimer = false;
    @SerialEntry
    public static boolean campReminder = false;
    @SerialEntry
    public static boolean ragAxeReminder = false;
    @SerialEntry
    public static boolean hideMageBeam = false;

    // Kuudra

    @SerialEntry
    public static boolean kuudraHitbox = false;
    @SerialEntry
    public static Color kuudraColor = new Color(255, 255, 0, 255);
    @SerialEntry
    public static boolean kuudraHealth = false;
    @SerialEntry
    public static boolean kuudraDPS = false;
    @SerialEntry
    public static boolean kuudraMissing = false;
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
    @SerialEntry
    public static boolean kuudraStunWaypoint = false;
    @SerialEntry
    public static Color kuudraStunColor = new Color(0, 255, 255, 170);
    @SerialEntry
    public static boolean kuudraSupplyHighlight = false;
    @SerialEntry
    public static Color kuudraSupplyColor = new Color(0, 255, 255, 127);
    @SerialEntry
    public static boolean kuudraDropHighlight = false;
    @SerialEntry
    public static Color kuudraDropColor = new Color(255, 255, 0, 127);
    @SerialEntry
    public static boolean kuudraBuildHighlight = false;
    @SerialEntry
    public static Color kuudraBuildColor = new Color(0, 255, 0, 127);

    // Mining

    @SerialEntry
    public static boolean miningCorpseGlow = false;
    @SerialEntry
    public static boolean ghostVision = false;
    @SerialEntry
    public static boolean betterSkymall = false;
    @SerialEntry
    public static String skymallWhitelist = "titanium, goblins";
    @SerialEntry
    public static boolean safePickobulus = false;
    @SerialEntry
    public static boolean wormCooldown = false;
    @SerialEntry
    public static boolean wormAlert = false;

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

    public static BooleanControllerBuilder booleanController(Option<Boolean> option) {
        return BooleanControllerBuilder.create(option).formatValue(value -> value ? Text.of("Enabled") : Text.of("Disabled")).coloured(true);
    }

    public static IntegerSliderControllerBuilder intSliderController(Option<Integer> option, int min, int max, int step) {
        return IntegerSliderControllerBuilder.create(option).range(min, max).step(step);
    }

    public static FloatSliderControllerBuilder floatSliderController(Option<Float> option, float min, float max, float step) {
        return FloatSliderControllerBuilder.create(option).range(min, max).step(step).formatValue(value -> Text.of(floatSliderFormat.format(value)));
    }

    public static DoubleSliderControllerBuilder doubleSliderController(Option<Double> option, double min, double max, double step) {
        return DoubleSliderControllerBuilder.create(option).range(min, max).step(step).formatValue(value -> Text.of(floatSliderFormat.format(value)));
    }

    public static Screen getConfigScreen(Screen parent) {
        return YetAnotherConfigLib.create(configHandler, (defaults, config, builder) -> builder
                .title(Text.of("NoFrills Config"))
                .category(General.create(defaults, config))
                .category(Fixes.create(defaults, config))
                .category(Events.create(defaults, config))
                .category(Solvers.create(defaults, config))
                .category(Hud.create(defaults, config))
                .category(Fishing.create(defaults, config))
                .category(Dungeons.create(defaults, config))
                .category(Kuudra.create(defaults, config))
                .category(Slayers.create(defaults, config))
                .category(Mining.create(defaults, config))
                .category(Farming.create(defaults, config))
        ).generateScreen(parent);
    }

    public enum partyBehaviorList {
        Automatic,
        Manual,
        Ignore,
        Disabled
    }

    public enum fixModes {
        Disabled,
        SkyblockOnly,
        Enabled
    }
}
