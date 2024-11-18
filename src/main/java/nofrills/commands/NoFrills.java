package nofrills.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import nofrills.config.Config;
import nofrills.misc.Utils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class NoFrills {
    public static final ModCommand[] commands = {
            new ModCommand("settings", "Opens the settings GUI.", literal("settings").executes(context -> {
                Config.openConfigScreen();
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
                    Utils.info("§7\"" + name + "\" is already in the party whitelist.");
                } else if (Config.partyBlacklist.contains(name)) {
                    Utils.info("§7\"" + name + "\" is already in the party blacklist.");
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
                    Utils.info("§7\"" + name + "\" is not in the party whitelist.");
                } else if (Config.partyBlacklist.contains(name)) {
                    Utils.info("§7\"" + name + "\" is in the party blacklist, not whitelist.");
                } else {
                    Utils.info("§aSuccessfully removed \"" + name + "\" from the party commands whitelist.");
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
                    Utils.info("§7\"" + name + "\" is already in the party blacklist.");
                } else if (Config.partyWhitelist.contains(name)) {
                    Utils.info("§7\"" + name + "\" is already in the party whitelist.");
                } else {
                    Utils.info("§aSuccessfully added \"" + name + "\" to the party commands blacklist.");
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
                    Utils.info("§7\"" + name + "\" is not in the party blacklist.");
                } else if (Config.partyWhitelist.contains(name)) {
                    Utils.info("§7\"" + name + "\" is in the party whitelist, not blacklist.");
                } else {
                    Utils.info("§aSuccessfully removed \"" + name + "\" from the party commands blacklist.");
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
                Utils.sendCoords("x: {x}, y: {y}, z: {z}");
                return SINGLE_SUCCESS;
            }).then(literal("patcher").executes(context -> {
                Utils.sendCoords("x: {x}, y: {y}, z: {z}");
                return SINGLE_SUCCESS;
            })).then(literal("simple").executes(context -> {
                Utils.sendCoords("{x} {y} {z}");
                return SINGLE_SUCCESS;
            }))),
            new ModCommand("queue", "Command that lets you queue for any Dungeon floor/Kuudra tier.", literal("queue").executes(context -> SINGLE_SUCCESS)
                    .then(literal("f0").executes(context -> {
                        joinInstance("CATACOMBS_ENTRANCE");
                        return SINGLE_SUCCESS;
                    })).then(literal("f1").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_ONE");
                        return SINGLE_SUCCESS;
                    })).then(literal("f2").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_TWO");
                        return SINGLE_SUCCESS;
                    })).then(literal("f3").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_THREE");
                        return SINGLE_SUCCESS;
                    })).then(literal("f4").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_FOUR ");
                        return SINGLE_SUCCESS;
                    })).then(literal("f5").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_FIVE");
                        return SINGLE_SUCCESS;
                    })).then(literal("f6").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_SIX");
                        return SINGLE_SUCCESS;
                    })).then(literal("f7").executes(context -> {
                        joinInstance("CATACOMBS_FLOOR_SEVEN");
                        return SINGLE_SUCCESS;
                    })).then(literal("m1").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_ONE");
                        return SINGLE_SUCCESS;
                    })).then(literal("m2").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_TWO");
                        return SINGLE_SUCCESS;
                    })).then(literal("m3").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_THREE");
                        return SINGLE_SUCCESS;
                    })).then(literal("m4").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_FOUR");
                        return SINGLE_SUCCESS;
                    })).then(literal("m5").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_FIVE");
                        return SINGLE_SUCCESS;
                    })).then(literal("m6").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_SIX");
                        return SINGLE_SUCCESS;
                    })).then(literal("m7").executes(context -> {
                        joinInstance("MASTER_CATACOMBS_FLOOR_SEVEN");
                        return SINGLE_SUCCESS;
                    })).then(literal("k1").executes(context -> {
                        joinInstance("KUUDRA_NORMAL");
                        return SINGLE_SUCCESS;
                    })).then(literal("k2").executes(context -> {
                        joinInstance("KUUDRA_HOT");
                        return SINGLE_SUCCESS;
                    })).then(literal("k3").executes(context -> {
                        joinInstance("KUUDRA_BURNING");
                        return SINGLE_SUCCESS;
                    })).then(literal("k4").executes(context -> {
                        joinInstance("KUUDRA_FIERY");
                        return SINGLE_SUCCESS;
                    })).then(literal("k5").executes(context -> {
                        joinInstance("KUUDRA_INFERNAL");
                        return SINGLE_SUCCESS;
                    }))),
    };

    private static void joinInstance(String id) {
        Utils.sendMessage("/joininstance " + id);
    }

    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> helpArg = literal("help").executes(context -> {
            Utils.info("§7Printing command list...");
            for (ModCommand command : commands) {
                Utils.info("§l" + command.command + "§r§7: " + command.description);
            }
            return SINGLE_SUCCESS;
        });
        LiteralArgumentBuilder<FabricClientCommandSource> commandMain = literal("nofrills").executes(context -> {
            Config.openConfigScreen();
            return SINGLE_SUCCESS;
        });
        LiteralArgumentBuilder<FabricClientCommandSource> commandShort = literal("nf").executes(context -> {
            Config.openConfigScreen();
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

