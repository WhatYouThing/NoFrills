package nofrills;

import com.mojang.brigadier.CommandDispatcher;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import nofrills.commands.NoFrills;
import nofrills.commands.Yeet;
import nofrills.config.Config;
import nofrills.features.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static nofrills.misc.Utils.Keybinds;

public class Main implements ModInitializer {
    public static final String MOD_ID = "nofrills";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final IEventBus eventBus = new EventBus();
    public static MinecraftClient mc;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess access) {
        Yeet.init(dispatcher);
        NoFrills.init(dispatcher);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("It's time to get real, NoFrills mod initialized.");

        mc = MinecraftClient.getInstance();

        Config.configHandler.load();

        eventBus.registerLambdaFactory("nofrills", (lookupInMethod, glass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, glass, MethodHandles.lookup()));

        eventBus.subscribe(StarredMobHighlight.class);
        eventBus.subscribe(RareSeaCreatures.class);
        eventBus.subscribe(EtherwarpOverlay.class);
        eventBus.subscribe(CorpseHighlight.class);
        eventBus.subscribe(WardrobeHotkeys.class);
        eventBus.subscribe(SeaCreatureCap.class);
        eventBus.subscribe(UpdateChecker.class);
        eventBus.subscribe(SpaceFarmer.class);
        eventBus.subscribe(AutoSprint.class);
        eventBus.subscribe(HotbarSwap.class);
        eventBus.subscribe(Config.class);
        eventBus.subscribe(KuudraFeatures.class);
        eventBus.subscribe(PartyFeatures.class);
        eventBus.subscribe(SlayerFeatures.class);
        eventBus.subscribe(HideDeadMobs.class);
        eventBus.subscribe(PearlRefill.class);

        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        KeyBindingHelper.registerKeyBinding(Keybinds.getPearls);
    }
}