package nofrills.commands;

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
import nofrills.features.PearlRefill;
import nofrills.hud.HudEditorScreen;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static nofrills.Main.mc;
import static nofrills.misc.SkyblockData.instances;

public class NoFrills {
    private static final LiteralArgumentBuilder<FabricClientCommandSource> queueCommandBuilder = literal("queue").executes(context -> SINGLE_SUCCESS);

    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.", literal("settings").executes(context -> {
                Utils.setScreen(Config.getConfigScreen(null));
                return SINGLE_SUCCESS;
            })),
            new ModCommand("party", "Allows you to manage the player whitelist and blacklist for the Party Commands feature.", literal("party").executes(context -> {
                Utils.info("§7List of options: whitelist, blacklist");
                return SINGLE_SUCCESS;
            }).then(literal("whitelist").executes(context -> {
                Utils.info("§7List of options: add, remove, get, clear");
                return SINGLE_SUCCESS;
            }).then(literal("add").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to add.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
                if (Config.partyWhitelist.contains(name)) {
                    Utils.info("§7" + name + " is already in the party whitelist.");
                } else if (Config.partyBlacklist.contains(name)) {
                    Utils.info("§7" + name + " is already in the party blacklist.");
                } else {
                    Utils.info("§aSuccessfully added \"" + name + "\" to the party commands whitelist.");
                    Config.partyWhitelist.add(name);
                    Config.configHandler.save();
                }
                return SINGLE_SUCCESS;
            }))).then(literal("remove").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to remove.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
                if (!Config.partyWhitelist.contains(name)) {
                    Utils.info("§7" + name + " is not in the party whitelist.");
                } else if (Config.partyBlacklist.contains(name)) {
                    Utils.info("§7" + name + " is in the party blacklist, not whitelist.");
                } else {
                    Utils.info("§aSuccessfully removed " + name + " from the party commands whitelist.");
                    Config.partyWhitelist.remove(name);
                    Config.configHandler.save();
                }
                return SINGLE_SUCCESS;
            }))).then(literal("get").executes(context -> {
                if (Config.partyWhitelist.isEmpty()) {
                    Utils.info("§7Your party whitelist is currently empty.");
                } else {
                    StringBuilder players = new StringBuilder();
                    for (String player : Config.partyWhitelist) {
                        players.append(player).append(" ");
                    }
                    Utils.info("§aList of all whitelisted players:\n\n§7" + players.toString().trim().replaceAll(" ", ", ") + "\n");
                }
                return SINGLE_SUCCESS;
            })).then(literal("clear").executes(context -> {
                if (Config.partyWhitelist.isEmpty()) {
                    Utils.info("§7Your party whitelist is currently empty.");
                } else {
                    Utils.info("§aSuccessfully cleared the party commands whitelist.");
                    Config.partyWhitelist.clear();
                    Config.configHandler.save();
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
                if (Config.partyBlacklist.contains(name)) {
                    Utils.info("§7" + name + " is already in the party blacklist.");
                } else if (Config.partyWhitelist.contains(name)) {
                    Utils.info("§7" + name + " is already in the party whitelist.");
                } else {
                    Utils.info("§aSuccessfully added " + name + " to the party commands blacklist.");
                    Config.partyBlacklist.add(name);
                    Config.configHandler.save();
                }
                return SINGLE_SUCCESS;
            }))).then(literal("remove").executes(context -> {
                Utils.info("§7You must provide the name of the player that you want to remove.");
                return SINGLE_SUCCESS;
            }).then(argument("playerName", StringArgumentType.string()).executes(context -> {
                String name = StringArgumentType.getString(context, "playerName").toLowerCase();
                if (!Config.partyBlacklist.contains(name)) {
                    Utils.info("§7" + name + " is not in the party blacklist.");
                } else if (Config.partyWhitelist.contains(name)) {
                    Utils.info("§7" + name + " is in the party whitelist, not blacklist.");
                } else {
                    Utils.info("§aSuccessfully removed " + name + " from the party commands blacklist.");
                    Config.partyBlacklist.remove(name);
                    Config.configHandler.save();
                }
                return SINGLE_SUCCESS;
            }))).then(literal("get").executes(context -> {
                if (Config.partyBlacklist.isEmpty()) {
                    Utils.info("§7Your party blacklist is currently empty.");
                } else {
                    StringBuilder players = new StringBuilder();
                    for (String player : Config.partyBlacklist) {
                        players.append(player).append(" ");
                    }
                    Utils.info("§aList of all blacklisted players:\n\n" + players.toString().trim().replaceAll(" ", ", ") + "\n");
                }
                return SINGLE_SUCCESS;
            })).then(literal("clear").executes(context -> {
                if (Config.partyBlacklist.isEmpty()) {
                    Utils.info("§7Your party blacklist is currently empty.");
                } else {
                    Utils.info("§aSuccessfully cleared the party commands blacklist.");
                    Config.partyBlacklist.clear();
                    Config.configHandler.save();
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
                for (Entity ent : mc.world.getEntities()) {
                    if (ent instanceof LivingEntity livingEntity) {
                        ItemStack helmet = livingEntity.getEquippedStack(EquipmentSlot.HEAD);
                        GameProfile textures = Utils.getTextures(helmet);
                        if (textures != null && helmet.getItem() instanceof PlayerHeadItem) {
                            Utils.infoFormat("entity name: {}\nhelmet name: {}\ntexture url: {}", ent.getName().getString(), helmet.getName().getString(), Utils.getTextureUrl(textures));
                        }
                    }
                }
                return SINGLE_SUCCESS;
            }))),
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
            Utils.setScreen(Config.getConfigScreen(null));
            return SINGLE_SUCCESS;
        });
        LiteralArgumentBuilder<FabricClientCommandSource> commandShort = literal("nf").executes(context -> {
            Utils.setScreen(Config.getConfigScreen(null));
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

