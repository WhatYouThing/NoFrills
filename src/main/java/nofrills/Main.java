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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import nofrills.commands.NoFrillsCommand;
import nofrills.commands.YeetCommand;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.OverlayMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.features.dungeons.*;
import nofrills.features.farming.*;
import nofrills.features.fishing.MuteDrake;
import nofrills.features.fishing.RadarSolver;
import nofrills.features.fishing.RareAnnounce;
import nofrills.features.fishing.RareHighlight;
import nofrills.features.general.*;
import nofrills.features.general.partycommands.PartyCommands;
import nofrills.features.hunting.*;
import nofrills.features.kuudra.*;
import nofrills.features.mining.*;
import nofrills.features.misc.*;
import nofrills.features.slayer.*;
import nofrills.features.solvers.*;
import nofrills.features.tweaks.DoubleUseFix;
import nofrills.features.tweaks.NoCursorReset;
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

    public static Minecraft mc;

    public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext access) {
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
        long start = Util.getMillis();

        mc = Minecraft.getInstance();

        injectRenderDoc();

        Config.load();

        ConfigScreenProviders.register(MOD_ID, screen -> new ClickGui());

        ClientCommandRegistrationCallback.EVENT.register(Main::registerCommands);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DungeonMap.mapTexture = new DynamicTexture("NoFrills Dungeon Map", 128, 128, true);
            client.getTextureManager().register(Identifier.fromNamespaceAndPath("nofrills", "dungeon_map_texture"), DungeonMap.mapTexture);
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

        GeneratedAnnotationVoodoo.eventListeners.forEach(eventBus::subscribe);

        LOGGER.info("It's time to get real, NoFrills mod initialized in {}ms.", Util.getMillis() - start);
    }
}