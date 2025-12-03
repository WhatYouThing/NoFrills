package nofrills.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Config;
import nofrills.features.general.PartyCommands;
import nofrills.features.general.PearlRefill;
import nofrills.features.hunting.ShardTracker;
import nofrills.hud.HudEditorScreen;
import nofrills.hud.clickgui.ClickGui;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nofrills.Main.LOGGER;
import static nofrills.Main.mc;
import static nofrills.misc.SkyblockData.instances;

public class NoFrillsCommand {
    private static final LiteralArgumentBuilder<FabricClientCommandSource> queueCommandBuilder = literal("queue").executes(context -> SINGLE_SUCCESS);

    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.", literal("settings").executes(context -> {
                Utils.setScreen(new ClickGui());
                return SINGLE_SUCCESS;
            }).then(literal("load").executes(context -> {
                Config.load();
                Utils.info("§aLoaded latest settings from the configuration file.");
                return SINGLE_SUCCESS;
            })).then(literal("save").executes(context -> {
                Config.save();
                Utils.info("§aSaved your current settings to the configuration file.");
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("partyCommands", "Allows you to manage the player whitelist and blacklist for the Party Commands feature.", literal("partyCommands").executes(context -> {
                Utils.info("§f§lWhitelist and blacklist§7: You can manage either list with the §f\"/nf partyCommands whitelist/blacklist\" §7command. Whitelisted players always have their commands processed automatically (unless disabled), and blacklisted players always have their commands rejected.\n\n§f§lHow each command mode works§7:\n- Automatic: Automatically process the command.\n- Manual: Adds a button in chat which must be clicked to process the command.\n- Ignore: Rejects the command from any non-whitelisted players.\n- Disabled: The command is fully disabled.");
                return SINGLE_SUCCESS;
            }).then(literal("whitelist").executes(context -> {
                Utils.info("§7List of options: add, remove, get, clear");
                return SINGLE_SUCCESS;
            }).then(literal("add").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to add.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = Utils.toLower(StringArgumentType.getString(context, "playerName"));
                if (PartyCommands.isOnList(name, "whitelist")) {
                    Utils.infoFormat("§7{} is already in the party whitelist.", name);
                } else if (PartyCommands.isOnList(name, "blacklist")) {
                    Utils.infoFormat("§7{} is already in the party blacklist.", name);
                } else {
                    PartyCommands.addToList(name, "whitelist");
                    Utils.infoFormat("§aAdded {} to the party commands whitelist.", name);
                }
                return SINGLE_SUCCESS;
            }))).then(literal("remove").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to remove.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = Utils.toLower(StringArgumentType.getString(context, "playerName"));
                if (!PartyCommands.isOnList(name, "whitelist")) {
                    Utils.infoFormat("§7{} is not in the party whitelist.", name);
                } else if (PartyCommands.isOnList(name, "blacklist")) {
                    Utils.infoFormat("§7{} is already in the party blacklist.", name);
                } else {
                    PartyCommands.removeFromList(name, "whitelist");
                    Utils.infoFormat("§aRemoved {} to the party commands whitelist.", name);
                }
                return SINGLE_SUCCESS;
            }))).then(literal("get").executes(context -> {
                if (PartyCommands.isListEmpty("whitelist")) {
                    Utils.info("§7Your party whitelist is currently empty.");
                } else {
                    StringBuilder players = new StringBuilder();
                    for (JsonElement player : PartyCommands.lists.value().get("whitelist").getAsJsonArray()) {
                        players.append(player.getAsString()).append(" ");
                    }
                    Utils.infoFormat("§aList of all whitelisted players: §7{}", players.toString().trim().replaceAll(" ", ", "));
                }
                return SINGLE_SUCCESS;
            })).then(literal("clear").executes(context -> {
                if (PartyCommands.isListEmpty("whitelist")) {
                    Utils.info("§7Your party whitelist is currently empty.");
                } else {
                    PartyCommands.lists.value().add("whitelist", new JsonArray());
                    Utils.info("§aSuccessfully cleared the party commands whitelist.");
                }
                return SINGLE_SUCCESS;
            }))).then(literal("blacklist").executes(context -> {
                Utils.info("§7List of options: add, remove, get, clear");
                return SINGLE_SUCCESS;
            }).then(literal("add").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to add.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = Utils.toLower(StringArgumentType.getString(context, "playerName"));
                if (PartyCommands.isOnList(name, "blacklist")) {
                    Utils.infoFormat("§7{} is already in the party blacklist.", name);
                } else if (PartyCommands.isOnList(name, "whitelist")) {
                    Utils.infoFormat("§7{} is already in the party whitelist.", name);
                } else {
                    PartyCommands.addToList(name, "blacklist");
                    Utils.infoFormat("§aSuccessfully added {} to the party commands blacklist.", name);
                }
                return SINGLE_SUCCESS;
            }))).then(literal("remove").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to remove.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = Utils.toLower(StringArgumentType.getString(context, "playerName"));
                if (!PartyCommands.isOnList(name, "blacklist")) {
                    Utils.infoFormat("§7{} is not in the party blacklist.", name);
                } else if (PartyCommands.isOnList(name, "whitelist")) {
                    Utils.infoFormat("§7{} is in the party whitelist, not blacklist.", name);
                } else {
                    PartyCommands.removeFromList(name, "blacklist");
                    Utils.infoFormat("§aSuccessfully removed {} from the party commands blacklist.", name);
                }
                return SINGLE_SUCCESS;
            }))).then(literal("get").executes(context -> {
                if (PartyCommands.isListEmpty("blacklist")) {
                    Utils.info("§7Your party blacklist is currently empty.");
                } else {
                    StringBuilder players = new StringBuilder();
                    for (JsonElement player : PartyCommands.lists.value().get("blacklist").getAsJsonArray()) {
                        players.append(player.getAsString()).append(" ");
                    }
                    Utils.infoFormat("§aList of all blacklisted players: §7{}", players.toString().trim().replaceAll(" ", ", "));
                }
                return SINGLE_SUCCESS;
            })).then(literal("clear").executes(context -> {
                if (PartyCommands.isListEmpty("blacklist")) {
                    Utils.info("§7Your party blacklist is currently empty.");
                } else {
                    PartyCommands.lists.value().add("blacklist", new JsonArray());
                    Utils.info("§aSuccessfully cleared the party commands blacklist.");
                }
                return SINGLE_SUCCESS;
            })))),
            new ModCommand("checkUpdate", "Manually checks if a new release version of the mod is available.", literal("checkUpdate").executes(context -> {
                Utils.info("§7Checking for updates...");
                Utils.checkUpdate(true);
                return SINGLE_SUCCESS;
            })),
            new ModCommand("sendCoords", "Easily send your coordinates in the chat, with the option to choose the format. Uses Patcher format by default.", literal("sendCoords").executes(context -> {
                Utils.sendMessage(Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
                return SINGLE_SUCCESS;
            }).then(literal("patcher").executes(context -> {
                Utils.sendMessage(Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
                return SINGLE_SUCCESS;
            })).then(literal("simple").executes(context -> {
                Utils.sendMessage(Utils.getCoordsFormatted("{} {} {}"));
                return SINGLE_SUCCESS;
            })).then(literal("location").executes(context -> {
                Utils.sendMessage(Utils.format("{} [ {} ]", Utils.getCoordsFormatted("x: {}, y: {}, z: {}"), SkyblockData.getLocation()));
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("copyCoords", "Alternative to the sendCoords command, which copies your coordinates to your clipboard instead of sending them in the chat.", literal("copyCoords").executes(context -> {
                mc.keyboard.setClipboard(Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
                return SINGLE_SUCCESS;
            }).then(literal("patcher").executes(context -> {
                mc.keyboard.setClipboard(Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
                return SINGLE_SUCCESS;
            })).then(literal("simple").executes(context -> {
                mc.keyboard.setClipboard(Utils.getCoordsFormatted("{} {} {}"));
                return SINGLE_SUCCESS;
            })).then(literal("location").executes(context -> {
                mc.keyboard.setClipboard(Utils.format("{} [ {} ]", Utils.getCoordsFormatted("x: {}, y: {}, z: {}"), SkyblockData.getLocation()));
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("queue", "Command that lets you queue for any Dungeon floor/Kuudra tier.", queueCommandBuilder),
            new ModCommand("getPearls", "Refills your Ender Pearls (up to 16) directly from your sacks.", literal("getPearls").executes(context -> {
                PearlRefill.getPearls();
                return SINGLE_SUCCESS;
            })),
            new ModCommand("ping", "Checks your current ping.", literal("ping").executes(context -> {
                Utils.info("§7Pinging...");
                SkyblockData.showPing();
                return SINGLE_SUCCESS;
            })),
            new ModCommand("hudEditor", "Opens the NoFrills hud editor.", literal("hudEditor").executes(context -> {
                Utils.setScreen(new HudEditorScreen());
                return SINGLE_SUCCESS;
            })),
            new ModCommand("debug", "Random commands for logging, debugging, or testing.", literal("debug").executes(context -> {
                return SINGLE_SUCCESS;
            }).then(literal("dumpHeadTextures").executes(context -> {
                List<EquipmentSlot> searchedSlots = List.of(
                        EquipmentSlot.HEAD,
                        EquipmentSlot.MAINHAND,
                        EquipmentSlot.OFFHAND
                );
                for (Entity ent : Utils.getEntities()) {
                    if (ent instanceof LivingEntity living) {
                        for (EquipmentSlot slot : searchedSlots) {
                            ItemStack stack = living.getEquippedStack(slot);
                            GameProfile textures = Utils.getTextures(stack);
                            if (textures != null && stack.getItem() instanceof PlayerHeadItem) {
                                Vec3d pos = living.getEntityPos();
                                LOGGER.info(Utils.format("\n\tURL - {}\n\tSlot - {}\n\tEntity Name - {}\n\tHead Name - {}\n\tPosition - {} {} {}",
                                        Utils.getTextureUrl(textures),
                                        Utils.toUpper(slot.name()),
                                        living.getName().getString(),
                                        stack.getName().getString(),
                                        pos.getX(),
                                        pos.getY(),
                                        pos.getZ()
                                ));
                            }
                        }
                    }
                }
                Utils.info("Dumped head texture URL's to latest.log.");
                return SINGLE_SUCCESS;
            })).then(literal("dumpPlayerTextures").executes(context -> {
                MinecraftSessionService service = mc.getApiServices().sessionService();
                for (Entity ent : Utils.getEntities()) {
                    if (ent instanceof PlayerEntity player) {
                        if (player.getGameProfile() != null) {
                            MinecraftProfileTextures textures = service.getTextures(player.getGameProfile());
                            Vec3d pos = player.getEntityPos();
                            if (textures.skin() == null) {
                                continue;
                            }
                            LOGGER.info(Utils.format("\n\tURL - {}\n\tEntity Name - {}\n\tPosition - {} {} {}",
                                    textures.skin().getUrl(),
                                    player.getName().getString(),
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                            ));
                        }
                    }
                }
                Utils.info("Dumped player texture URL's to latest.log.");
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("shardTracker", "Commands for managing the Shard Tracker feature.", literal("shardTracker").executes(context -> {
                Utils.info("§f§lImporting shards§7: Click \"Copy Tree\" on the SkyShards calculator, choose the NoFrills format in the pop-up, and click the Import Shard List button on the Shard Tracker settings screen.\n\n§f§lTracking shards§7: Make sure to enable the Shard Tracker feature, and the Shard Tracker element in the NoFrills HUD editor. When enabled, the feature will track the obtained quantity of each shard that you are tracking.");
                return SINGLE_SUCCESS;
            }).then(literal("import").executes(context -> {
                ShardTracker.importTreeData();
                ShardTracker.refreshDisplay();
                return SINGLE_SUCCESS;
            })).then(literal("clear").executes(context -> {
                ShardTracker.data.value().add("shards", new JsonArray());
                ShardTracker.refreshDisplay();
                Utils.info("§aTracked shard list cleared successfully.");
                return SINGLE_SUCCESS;
            })).then(literal("settings").executes(context -> {
                Utils.setScreen(ShardTracker.buildSettings());
                return SINGLE_SUCCESS;
            })))
    };

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> helpArg = literal("help").executes(context -> {
            Utils.info("§7Printing command list...");
            for (ModCommand command : commands) {
                Utils.info("§l" + command.command + "§r§7: " + command.description);
            }
            return SINGLE_SUCCESS;
        });
        for (SkyblockData.InstanceType instanceType : instances) {
            queueCommandBuilder.then(literal(instanceType.name).executes(context -> {
                Utils.sendMessage("/joininstance " + instanceType.type);
                return SINGLE_SUCCESS;
            }));
        }
        LiteralArgumentBuilder<FabricClientCommandSource> commandMain = literal("nofrills").executes(context -> {
            Utils.setScreen(new ClickGui());
            return SINGLE_SUCCESS;
        });
        LiteralArgumentBuilder<FabricClientCommandSource> commandShort = literal("nf").executes(context -> {
            Utils.setScreen(new ClickGui());
            return SINGLE_SUCCESS;
        });
        commandMain.then(helpArg);
        commandShort.then(helpArg);
        for (ModCommand command : commands) {
            commandMain.then(command.builder);
            commandShort.then(command.builder);
        }
        dispatcher.register(commandMain);
        dispatcher.register(commandShort);
    }

    public static class ModCommand {
        public String command;
        public String description;
        public LiteralArgumentBuilder<FabricClientCommandSource> builder;

        public ModCommand(String command, String description, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
            this.command = command;
            this.description = description;
            this.builder = builder;
        }
    }
}
