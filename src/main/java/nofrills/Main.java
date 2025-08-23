package nofrills;

import com.mojang.brigadier.CommandDispatcher;
import io.wispforest.owo.config.ui.ConfigScreenProviders;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Util;
import nofrills.commands.NoFrillsCommand;
import nofrills.commands.YeetCommand;
import nofrills.config.Config;
import nofrills.features.dungeons.*;
import nofrills.features.farming.GlowingMushroom;
import nofrills.features.farming.SpaceFarmer;
import nofrills.features.fishing.CapTracker;
import nofrills.features.fishing.MuteDrake;
import nofrills.features.fishing.RareAnnounce;
import nofrills.features.fishing.RareGlow;
import nofrills.features.general.*;
import nofrills.features.hunting.*;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.misc.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.CalendarDate;
import nofrills.features.solvers.ExperimentSolver;
import nofrills.features.solvers.SpookyChests;
import nofrills.features.tweaks.DoubleUseFix;
import nofrills.features.tweaks.MiddleClickOverride;
import nofrills.hud.HudManager;
import nofrills.hud.clickgui.ClickGui;
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
        YeetCommand.init(dispatcher);
        NoFrillsCommand.init(dispatcher);
    }

    @Override
    public void onInitialize() {
        long start = Util.getMeasuringTimeMs();

        mc = MinecraftClient.getInstance();

        Config.load();

        ConfigScreenProviders.register(MOD_ID, screen -> new ClickGui());

        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        eventBus.registerLambdaFactory(MOD_ID, (lookupInMethod, glass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, glass, MethodHandles.lookup()));

        eventBus.subscribe(SkyblockData.class);
        eventBus.subscribe(SlotOptions.class);
        eventBus.subscribe(Utils.class);
        eventBus.subscribe(NoFrillsAPI.class);
        eventBus.subscribe(KuudraUtil.class);
        eventBus.subscribe(HudManager.class);
        eventBus.subscribe(SpookyChests.class);
        eventBus.subscribe(ExperimentSolver.class);
        eventBus.subscribe(CalendarDate.class);
        eventBus.subscribe(VoidgloomSeraph.class);
        eventBus.subscribe(RiftstalkerBloodfiend.class);
        eventBus.subscribe(KillTimer.class);
        eventBus.subscribe(InfernoDemonlord.class);
        eventBus.subscribe(BossHighlight.class);
        eventBus.subscribe(ScathaMining.class);
        eventBus.subscribe(SafePickobulus.class);
        eventBus.subscribe(GhostVision.class);
        eventBus.subscribe(CorpseHighlight.class);
        eventBus.subscribe(BetterSkyMall.class);
        eventBus.subscribe(AbilityAlert.class);
        eventBus.subscribe(PreMessage.class);
        eventBus.subscribe(KuudraWaypoints.class);
        eventBus.subscribe(KuudraHitbox.class);
        eventBus.subscribe(KuudraHealth.class);
        eventBus.subscribe(FreshTimer.class);
        eventBus.subscribe(DrainMessage.class);
        eventBus.subscribe(RecipeLookup.class);
        eventBus.subscribe(PearlRefill.class);
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
        eventBus.subscribe(MiddleClickOverride.class);
        eventBus.subscribe(EtherwarpOverlay.class);
        eventBus.subscribe(ChatWaypoints.class);
        eventBus.subscribe(AutoSprint.class);
        eventBus.subscribe(RareGlow.class);
        eventBus.subscribe(RareAnnounce.class);
        eventBus.subscribe(MuteDrake.class);
        eventBus.subscribe(CapTracker.class);
        eventBus.subscribe(SpaceFarmer.class);
        eventBus.subscribe(GlowingMushroom.class);
        eventBus.subscribe(WitherDragons.class);
        eventBus.subscribe(TerracottaTimer.class);
        eventBus.subscribe(TerminalSolvers.class);
        eventBus.subscribe(LeapOverlay.class);
        eventBus.subscribe(DungeonReminders.class);
        eventBus.subscribe(StarredMobHighlight.class);
        eventBus.subscribe(MinibossHighlight.class);
        eventBus.subscribe(KeyHighlight.class);
        eventBus.subscribe(SpiritBowHighlight.class);
        eventBus.subscribe(DeviceSolvers.class);
        eventBus.subscribe(Fullbright.class);
        eventBus.subscribe(CustomKeybinds.class);
        eventBus.subscribe(EndNodeHighlight.class);
        eventBus.subscribe(HotbarSwap.class);
        eventBus.subscribe(TempleSkip.class);
        eventBus.subscribe(SecretBatHighlight.class);
        eventBus.subscribe(AutoRequeue.class);
        eventBus.subscribe(ShopCleaner.class);
        eventBus.subscribe(KuudraChestValue.class);
        eventBus.subscribe(DoubleUseFix.class);
        eventBus.subscribe(ShardTracker.class);

        LOGGER.info("It's time to get real, NoFrills mod initialized in {}ms.", Util.getMeasuringTimeMs() - start);
    }
}