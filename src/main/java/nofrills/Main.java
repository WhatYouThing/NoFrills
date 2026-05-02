package nofrills;

import com.mojang.brigadier.CommandDispatcher;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import nofrills.commands.NoFrillsCommand;
import nofrills.commands.YeetCommand;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.OverlayMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.features.dungeons.*;
import nofrills.features.farming.*;
import nofrills.features.fishing.*;
import nofrills.features.general.*;
import nofrills.features.hunting.*;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.misc.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.*;
import nofrills.features.tweaks.DoubleUseFix;
import nofrills.features.tweaks.NoCursorReset;
import nofrills.features.tweaks.NoGhostPlace;
import nofrills.hud.HudManager;
import nofrills.hud.clickgui.ClickGui;
import nofrills.hud.elements.DungeonMap;
import nofrills.misc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class Main implements ModInitializer {
    public static final String MOD_ID = "nofrills";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final IEventBus eventBus = new EventBus();

    public static MinecraftClient mc;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        CommandShortcuts.init(dispatcher); // register shortcuts first so that the user cant softlock the mod by overriding the main commands
        YeetCommand.init(dispatcher);
        NoFrillsCommand.init(dispatcher);
    }

    public static void injectRenderDoc() {
        String path = System.getProperty("nofrills.renderdoc.library_path");
        if (path != null) {
            try {
                System.load(path);
                LOGGER.info("Loaded RenderDoc lib: {}", path);
            } catch (Exception exception) {
                LOGGER.error("Failed to load RenderDoc lib.", exception);
            }
        }
    }

    @Override
    public void onInitialize() {
        long start = Util.getMeasuringTimeMs();

        mc = MinecraftClient.getInstance();

        injectRenderDoc();

        Config.load();

        ConfigScreenProviders.register(MOD_ID, screen -> new ClickGui());

        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DungeonMap.mapTexture = new NativeImageBackedTexture("NoFrills Dungeon Map", 128, 128, true);
            client.getTextureManager().registerTexture(Identifier.of("nofrills", "dungeon_map_texture"), DungeonMap.mapTexture);
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            String msg = Utils.toPlain(message);

            if (overlay) {
                return !eventBus.post(new OverlayMsgEvent(message, msg)).isCancelled();
            }

            boolean cancelled = eventBus.post(new ChatMsgEvent(message, msg)).isCancelled();
            if (msg.startsWith("Party > ") && msg.contains(": ")) {
                int nameStart = msg.contains("]") & msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                cancelled = eventBus.post(new PartyChatMsgEvent(content, author)).isCancelled() || cancelled;
            }
            return !cancelled;
        });

        eventBus.registerLambdaFactory(MOD_ID, (lookupInMethod, glass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, glass, MethodHandles.lookup()));

        eventBus.subscribe(SkyblockData.class);
        eventBus.subscribe(SlotOptions.class);
        eventBus.subscribe(EntityCache.class);
        eventBus.subscribe(NoFrillsAPI.class);
        eventBus.subscribe(KuudraUtil.class);
        eventBus.subscribe(SlayerUtil.class);
        eventBus.subscribe(DungeonUtil.class);
        eventBus.subscribe(HudManager.class);
        eventBus.subscribe(ItemProtection.class);
        eventBus.subscribe(SpookyChests.class);
        eventBus.subscribe(ExperimentSolver.class);
        eventBus.subscribe(BeaconTuningSolver.class);
        eventBus.subscribe(CalendarDate.class);
        eventBus.subscribe(ChaliceHighlight.class);
        eventBus.subscribe(NoAttunementSpam.class);
        eventBus.subscribe(MuteVampire.class);
        eventBus.subscribe(KillTimer.class);
        eventBus.subscribe(PillarAlert.class);
        eventBus.subscribe(BossHighlight.class);
        eventBus.subscribe(ScathaMining.class);
        eventBus.subscribe(GhostVision.class);
        eventBus.subscribe(CorpseHighlight.class);
        eventBus.subscribe(AbilityAlert.class);
        eventBus.subscribe(PreMessage.class);
        eventBus.subscribe(BuildPileHighlight.class);
        eventBus.subscribe(SupplyHighlight.class);
        eventBus.subscribe(DropOffHighlight.class);
        eventBus.subscribe(KuudraHitbox.class);
        eventBus.subscribe(FreshTimer.class);
        eventBus.subscribe(DrainMessage.class);
        eventBus.subscribe(RecipeLookup.class);
        eventBus.subscribe(LassoAlert.class);
        eventBus.subscribe(InvisibugHighlight.class);
        eventBus.subscribe(FusionKeybinds.class);
        eventBus.subscribe(CinderbatHighlight.class);
        eventBus.subscribe(WardrobeKeybinds.class);
        eventBus.subscribe(UpdateChecker.class);
        eventBus.subscribe(SlotBinding.class);
        eventBus.subscribe(PriceTooltips.class);
        eventBus.subscribe(PartyFinder.class);
        eventBus.subscribe(PartyCommands.class);
        eventBus.subscribe(NoRender.class);
        eventBus.subscribe(EtherwarpOverlay.class);
        eventBus.subscribe(ChatWaypoints.class);
        eventBus.subscribe(AutoSprint.class);
        eventBus.subscribe(RareHighlight.class);
        eventBus.subscribe(RareAnnounce.class);
        eventBus.subscribe(MuteDrake.class);
        eventBus.subscribe(CapTracker.class);
        eventBus.subscribe(SpaceFarmer.class);
        eventBus.subscribe(GlowingMushroom.class);
        eventBus.subscribe(WitherDragons.class);
        eventBus.subscribe(TerracottaTimer.class);
        eventBus.subscribe(TerminalSolvers.class);
        eventBus.subscribe(LeapOverlay.class);
        eventBus.subscribe(StarredMobHighlight.class);
        eventBus.subscribe(MinibossHighlight.class);
        eventBus.subscribe(KeyHighlight.class);
        eventBus.subscribe(SpiritBowHighlight.class);
        eventBus.subscribe(DeviceSolvers.class);
        eventBus.subscribe(Fullbright.class);
        eventBus.subscribe(CommandKeybinds.class);
        eventBus.subscribe(EndNodeHighlight.class);
        eventBus.subscribe(HotbarSwap.class);
        eventBus.subscribe(TempleSkip.class);
        eventBus.subscribe(SecretBatHighlight.class);
        eventBus.subscribe(AutoRequeue.class);
        eventBus.subscribe(ShopCleaner.class);
        eventBus.subscribe(KuudraChestValue.class);
        eventBus.subscribe(DoubleUseFix.class);
        eventBus.subscribe(ShardTracker.class);
        eventBus.subscribe(HuntaxeLock.class);
        eventBus.subscribe(PlotBorders.class);
        eventBus.subscribe(LividSolver.class);
        eventBus.subscribe(MimicMessage.class);
        eventBus.subscribe(PrinceMessage.class);
        eventBus.subscribe(EggHitsDisplay.class);
        eventBus.subscribe(BeaconTracer.class);
        eventBus.subscribe(CocoonAlert.class);
        eventBus.subscribe(GuiKeybinds.class);
        eventBus.subscribe(SecretChime.class);
        eventBus.subscribe(MelodyMessage.class);
        eventBus.subscribe(QuickClose.class);
        eventBus.subscribe(DungeonChestValue.class);
        eventBus.subscribe(NoCursorReset.class);
        eventBus.subscribe(RelicHighlight.class);
        eventBus.subscribe(GemstoneDesyncFix.class);
        eventBus.subscribe(DianaSolver.class);
        eventBus.subscribe(HoppitySolver.class);
        eventBus.subscribe(RadarSolver.class);
        eventBus.subscribe(VacuumSolver.class);
        eventBus.subscribe(ChatRules.class);
        eventBus.subscribe(BreakResetFix.class);
        eventBus.subscribe(ChatTweaks.class);
        eventBus.subscribe(SpawnAlert.class);
        eventBus.subscribe(CratePriority.class);
        eventBus.subscribe(ClassNametags.class);
        eventBus.subscribe(InfoTooltips.class);
        eventBus.subscribe(AnvilHelper.class);
        eventBus.subscribe(ShaftAnnounce.class);
        eventBus.subscribe(AutoTip.class);
        eventBus.subscribe(ScoreCalculator.class);
        eventBus.subscribe(SkillTracker.class);
        eventBus.subscribe(WateringHelper.class);
        eventBus.subscribe(PlatformHighlight.class);
        eventBus.subscribe(NoDamageSplash.class);
        eventBus.subscribe(CroesusSolver.class);
        eventBus.subscribe(EquipmentHighlight.class);
        eventBus.subscribe(CommissionHighlight.class);
        eventBus.subscribe(BlockList.class);
        eventBus.subscribe(NoGhostPlace.class);
        eventBus.subscribe(DebugStuff.class);
        eventBus.subscribe(PhantomleafSolver.class);
        eventBus.subscribe(StreamerMode.class);

        LOGGER.info("It's time to get real, NoFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}