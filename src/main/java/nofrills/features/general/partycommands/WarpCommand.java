package nofrills.features.general.partycommands;

import nofrills.misc.Utils;

public class WarpCommand extends Command {

    public WarpCommand() {
        super(PartyCommands.warp, "warp", "w");
    }

    @Override
    public void onAutomatic(String author, String msg) {
        Utils.sendMessage("/party warp");
    }

    @Override
    public void onManual(String author, String msg) {
        Utils.infoButton("§aClick here to warp your party.", "/party warp");
    }
}