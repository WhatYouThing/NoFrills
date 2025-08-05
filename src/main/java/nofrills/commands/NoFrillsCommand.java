package nofrills.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PlayerHeadItem;
import nofrills.config.Config;
import nofrills.features.AttributeDebug;
import nofrills.features.general.PartyCommands;
import nofrills.features.keybinds.PearlRefill;
import nofrills.hud.HudEditorScreen;
import nofrills.hud.clickgui.ClickGui;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
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
                Utils.info("§aLoaded the settings file.");
                return SINGLE_SUCCESS;
            })).then(literal("save").executes(context -> {
                Config.save();
                Utils.info("§aSaved the settings file.");
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
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
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
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
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
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
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
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
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
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                Utils.sendMessage(coords);
                return SINGLE_SUCCESS;
            }).then(literal("patcher").executes(context -> {
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                Utils.sendMessage(coords);
                return SINGLE_SUCCESS;
            })).then(literal("simple").executes(context -> {
                String coords = Utils.getCoordsFormatted("{x} {y} {z}");
                Utils.sendMessage(coords);
                return SINGLE_SUCCESS;
            })).then(literal("location").executes(context -> {
                String location = SkyblockData.getLocation();
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                if (!location.isEmpty()) {
                    coords += " [ " + location + " ]";
                }
                Utils.sendMessage(coords);
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("copyCoords", "Alternative to the sendCoords command, which copies your coordinates to your clipboard instead of sending them in the chat.", literal("copyCoords").executes(context -> {
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                mc.keyboard.setClipboard(coords);
                return SINGLE_SUCCESS;
            }).then(literal("patcher").executes(context -> {
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                mc.keyboard.setClipboard(coords);
                return SINGLE_SUCCESS;
            })).then(literal("simple").executes(context -> {
                String coords = Utils.getCoordsFormatted("{x} {y} {z}");
                mc.keyboard.setClipboard(coords);
                return SINGLE_SUCCESS;
            })).then(literal("location").executes(context -> {
                String location = SkyblockData.getLocation();
                String coords = Utils.getCoordsFormatted("x: {x}, y: {y}, z: {z}");
                if (!location.isEmpty()) {
                    coords += " [ " + location + " ]";
                }
                mc.keyboard.setClipboard(coords);
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
                for (Entity ent : Utils.getEntities()) {
                    if (ent instanceof LivingEntity livingEntity) {
                        ItemStack helmet = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
                        GameProfile textures = Utils.getTextures(helmet);
                        if (textures != null && helmet.getItem() instanceof PlayerHeadItem) {
                            Utils.infoFormat("entity name: {}\nhelmet name: {}\ntexture url: {}", ent.getName().getString(), helmet.getName().getString(), Utils.getTextureUrl(textures));
                        }
                    }
                }
                return SINGLE_SUCCESS;
            })).then(literal("toggleFusionDebug").executes(context -> {
                AttributeDebug.isEnabled = !AttributeDebug.isEnabled;
                if (AttributeDebug.isEnabled) {
                    Utils.info("Attribute Fusion debug enabled.");
                } else {
                    AttributeDebug.saveData();
                    Utils.info("Attribute Fusion debug disabled, saved information to \".minecraft/config/NoFrills/fusion_data.json\".");
                }
                return SINGLE_SUCCESS;
            })).then(literal("saveShardTextures").executes(context -> {
                AttributeDebug.saveTextures = !AttributeDebug.saveTextures;
                if (AttributeDebug.saveTextures) {
                    Utils.info("Started saving shard texture URL's in the Bazaar.");
                } else {
                    mc.keyboard.setClipboard(AttributeDebug.textures.toString());
                    Utils.info("Stopped saving shard texture URL's, copied JSON data to clipboard.");
                }
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

