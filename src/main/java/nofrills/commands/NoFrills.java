package nofrills.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import nofrills.config.Config;
import nofrills.misc.Utils;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class NoFrills {
    public static void init(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("nofrills").then(literal("settings").executes(context -> {
            Config.openConfigScreen();
            return SINGLE_SUCCESS;
        })).then(literal("party").executes(context -> {
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
        })))).then(literal("sendCoords").executes(context -> {
            Utils.sendCoords("x: {x}, y: {y}, z: {z}");
            return SINGLE_SUCCESS;
        }).then(literal("patcher").executes(context -> {
            Utils.sendCoords("x: {x}, y: {y}, z: {z}");
            return SINGLE_SUCCESS;
        })).then(literal("simple").executes(context -> {
            Utils.sendCoords("{x} {y} {z}");
            return SINGLE_SUCCESS;
        }))).then(literal("help").executes(context -> {
            Utils.info("§aList of all command options:\n§f§l- settings\n§7Opens the settings GUI.\n§f§l- party [whitelist/blacklist] [add/remove/get/clear] [<username>]\n§7Manage the player lists for the Party Commands feature.\n§f§l- sendCoords [<patcher/simple>]\n§7Easily send your coordinates in the chat, with the option to choose the format. Uses Patcher format by default.");
            return SINGLE_SUCCESS;
        })).then(literal("checkUpdate").executes(context -> {
            Utils.info("§7Checking for updates...");
            Utils.checkUpdate(true);
            return SINGLE_SUCCESS;
        })).executes(context -> {
            Config.openConfigScreen();
            return SINGLE_SUCCESS;
        }));
    }
}
