package nofrills.config;

import com.google.gson.JsonObject;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.ui.core.Color;

import java.util.ArrayList;
import java.util.List;

@Config(name = "NoFrillsConfig", wrapperName = "NoFrillsConfigWrapper")
public class NoFrillsConfig {

    // General

    public boolean updateChecker = false;
    public boolean autoSprint = false;
    public boolean autoSprintWater = false;
    public boolean noSelfieCam = false;
    public boolean terrorFix = false;
    public boolean wardrobeHotkeys = false;
    public boolean wardrobeHotkeysSound = false;
    public boolean fetchPricing = false;
    public boolean pricingAuction = false;
    public boolean pricingBazaar = false;
    public boolean pricingAttribute = false;
    public boolean pricingNPC = false;
    public boolean pricingMote = false;
    public int pricingMoteStacks = 0;
    public boolean hotbarSwap = false;
    public int hotbarSwapOverride = 8;
    public boolean ignoreBackground = false;
    public boolean middleClickOverride = false;
    public boolean slotBinding = false;
    public boolean slotBindingLines = false;
    public boolean slotBindingBorders = false;
    public Color slotBindingColor = new Color(0.0f, 1.0f, 1.0f, 1.0f);
    public JsonObject slotBindData = new JsonObject();
    public boolean hideDeadMobs = false;
    public boolean keepNametags = false;
    public boolean noExplosions = false;
    public boolean noFireOverlay = false;
    public boolean noBreakParticles = false;
    public boolean noBossBar = false;
    public boolean noLoadingScreen = false;
    public boolean overlayEtherwarp = false;
    public boolean partyFinderOptions = false;
    public boolean partyWaypoints = false;
    public Color partyWaypointColor = new Color(0.34f, 0.34f, 1.0f, 0.67f);
    public int partyWaypointTime = 60;
    public boolean chatWaypoints = false;
    public Color chatWaypointColor = new Color(0.34f, 1.0f, 1.0f, 0.67f);
    public int chatWaypointTime = 30;
    public String partyPrefixes = "! ?";
    public List<String> partyWhitelist = new ArrayList<>();
    public List<String> partyBlacklist = new ArrayList<>();
    public partyBehavior partyCmdWarp = partyBehavior.Disabled;
    public partyBehavior partyCmdTransfer = partyBehavior.Disabled;
    public partyBehavior partyCmdAllInvite = partyBehavior.Disabled;
    public partyBehavior partyCmdDowntime = partyBehavior.Disabled;
    public partyBehavior partyCmdQueue = partyBehavior.Disabled;
    public partyBehavior partyCmdCoords = partyBehavior.Disabled;
    public boolean noHaste = false;
    public boolean noEquipAnim = false;
    public int viewmodelSpeed = 0;
    public boolean viewmodelEnable = false;
    public float viewmodelOffsetX = 0.0f;
    public float viewmodelOffsetY = 0.0f;
    public float viewmodelOffsetZ = 0.0f;
    public float viewmodelScaleX = 1.0f;
    public float viewmodelScaleY = 1.0f;
    public float viewmodelScaleZ = 1.0f;
    public float viewmodelRotX = 0.0f;
    public float viewmodelRotY = 0.0f;
    public float viewmodelRotZ = 0.0f;

    // Fixes

    public fixMode stonkFix = fixMode.Disabled;
    public fixMode oldSneak = fixMode.Disabled;
    public fixMode antiSwim = fixMode.Disabled;
    public fixMode noPearlCooldown = fixMode.Disabled;
    public fixMode snowFix = fixMode.Disabled;
    public fixMode noDropSwing = fixMode.Disabled;
    public fixMode itemCountFix = fixMode.Disabled;
    public fixMode ridingCamFix = fixMode.Disabled;
    public fixMode middleClickFix = fixMode.Disabled;
    public fixMode armorStandFix = fixMode.Disabled;
    public fixMode efficiencyFix = fixMode.Disabled;
    public fixMode clearCursorStack = fixMode.Disabled;

    // Events

    public boolean spookyChestAlert = false;
    public boolean spookyChestHighlight = false;
    public Color spookyChestHighlightColor = new Color(1.0f, 0.67f, 0.0f, 0.67f);
    public boolean calendarDate = false;

    // Solvers

    public boolean solveChronomatron = false;
    public boolean solveUltrasequencer = false;
    public boolean solveSuperpairs = false;

    // HUD

    public boolean bobberEnabled = false;
    public boolean bobberLeftHand = false;
    public double bobberPosX = 0.01;
    public double bobberPosY = 0.1;
    public boolean seaCreaturesEnabled = false;
    public boolean seaCreaturesLeftHand = false;
    public double seaCreaturesPosX = 0.01;
    public double seaCreaturesPosY = 0.13;
    public boolean tpsEnabled = false;
    public boolean tpsLeftHand = false;
    public double tpsPosX = 0.01;
    public double tpsPosY = 0.04;
    public boolean lagMeterEnabled = false;
    public int lagMeterMinTime = 500;
    public boolean lagMeterLeftHand = false;
    public double lagMeterPosX = 0.01;
    public double lagMeterPosY = 0.19;
    public boolean powerEnabled = false;
    public boolean powerDungeonsOnly = false;
    public boolean powerLeftHand = false;
    public double powerPosX = 0.01;
    public double powerPosY = 0.16;
    public boolean dayEnabled = false;
    public boolean dayLeftHand = false;
    public double dayPosX = 0.01;
    public double dayPosY = 0.07;
    public boolean pingEnabled = false;
    public boolean pingLeftHand = false;
    public double pingPosX = 0.01;
    public double pingPosY = 0.01;

    // Fishing

    public boolean capEnabled = false;
    public int capTarget = 50;
    public int capDelay = 30;
    public boolean capSendMsg = false;
    public String capMsg = "/pc SEA CREATURE CAP REACHED!";
    public boolean capSound = false;
    public boolean capTitle = false;
    public boolean rareTitle = false;
    public boolean rareGlow = false;
    public boolean rareSendMsg = false;
    public String rareMsg = "/pc {spawnmsg}";
    public boolean rareSound = false;
    public boolean rareReplace = false;
    public boolean muteDrake = false;

    // Dungeons

    public String dungeonClass = "Berserk"; // this stores the user's last selected/known Dungeon class, not for use with config screens
    public boolean starredMobHighlight = false;
    public Color starredMobColor = new Color(0.0f, 1.0f, 1.0f, 1.0f);
    public boolean miniHighlight = false;
    public Color miniColor = new Color(1.0f, 1.0f, 0.0f, 1.0f);
    public boolean keyHighlight = false;
    public Color keyColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);
    public boolean spiritHighlight = false;
    public Color spiritColor = new Color(0.67f, 0.0f, 1.0f, 0.67f);
    public boolean solveTerminals = false;
    public boolean fastTerminals = false;
    public boolean solveDevices = false;
    public boolean melodyAnnounce = false;
    public String melodyMessage = "/pc Melody";
    public boolean dragAlert = false;
    public float dragSkip = 22.0f;
    public float dragSkipEasy = 19.0f;
    public boolean dragHealth = false;
    public boolean dragTimer = false;
    public boolean dragBoxes = false;
    public boolean dragGlow = false;
    public boolean dragStack = false;
    public boolean dragStackAdvanced = false;
    public boolean wishReminder = false;
    public boolean leapOverlay = false;
    public boolean leapOverlayMsg = false;
    public Color leapColorHealer = Color.ofRgb(0xecb50c);
    public Color leapColorMage = Color.ofRgb(0x1793c4);
    public Color leapColorBers = Color.ofRgb(0xe7413c);
    public Color leapColorArch = Color.ofRgb(0x4a14b7);
    public Color leapColorTank = Color.ofRgb(0x768f46);
    public boolean gyroTimer = false;
    public boolean campReminder = false;
    public boolean ragAxeReminder = false;
    public boolean hideMageBeam = false;

    // Kuudra

    public boolean kuudraHitbox = false;
    public Color kuudraColor = new Color(1.0f, 1.0f, 0.0f, 1.0f);
    public boolean kuudraHealth = false;
    public boolean kuudraDPS = false;
    public boolean kuudraMissing = false;
    public boolean kuudraFresh = false;
    public String kuudraFreshMsg = "/pc EAT FRESH!";
    public boolean kuudraFreshTimer = false;
    public boolean kuudraDrain = false;
    public String kuudraDrainMsg = "/pc Used {mana} Mana on {players} players!";
    public boolean kuudraStunWaypoint = false;
    public Color kuudraStunColor = new Color(0.0f, 1.0f, 1.0f, 0.67f);
    public boolean kuudraSupplyHighlight = false;
    public Color kuudraSupplyColor = new Color(0.0f, 1.0f, 1.0f, 0.5f);
    public boolean kuudraDropHighlight = false;
    public Color kuudraDropColor = new Color(1.0f, 1.0f, 0.0f, 0.5f);
    public boolean kuudraBuildHighlight = false;
    public Color kuudraBuildColor = new Color(0.0f, 1.0f, 0.0f, 0.5f);

    // Mining

    public boolean miningCorpseGlow = false;
    public boolean ghostVision = false;
    public boolean betterSkymall = false;
    public String skymallWhitelist = "titanium, goblins";
    public boolean safePickobulus = false;
    public boolean wormCooldown = false;
    public boolean wormAlert = false;
    public boolean pickAbilityAlert = false;

    // Farming

    public boolean spaceFarmer = false;
    public boolean lockView = false;
    public boolean shroomHighlight = false;

    // Slayers

    public boolean slayerHitboxes = false;
    public boolean slayerKillTime = false;
    public boolean blazeNoSpam = false;
    public boolean blazePillarWarn = false;
    public boolean blazeDaggerFix = false;
    public boolean vampIce = false;
    public boolean vampSteak = false;
    public boolean vampSteakHighlight = false;
    public boolean vampChalice = false;
    public Color vampChaliceColor = new Color(0.67f, 0.0f, 1.0f, 0.67f);
    public boolean vampManiaSilence = false;
    public boolean vampManiaReplace = false;
    public boolean vampSpringSilence = false;
    public boolean vampSpringReplace = false;
    public boolean emanHitDisplay = false;

    public enum partyBehavior {
        Automatic,
        Manual,
        Ignore,
        Disabled
    }

    public enum fixMode {
        Disabled,
        SkyblockOnly,
        SkyblockLegacyOnly,
        AlwaysOn
    }
}
