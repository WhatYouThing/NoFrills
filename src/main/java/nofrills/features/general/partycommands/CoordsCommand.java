package nofrills.features.general.partycommands;

import nofrills.misc.Utils;

public class CoordsCommand extends Command {

    public CoordsCommand() {
        super(PartyCommands.coords, "coords");
    }

    @Override
    public void onAutomatic(String author, String msg) {
        Utils.sendMessage("/pc " + Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
    }

    @Override
    public void onManual(String author, String msg) {
        Utils.infoButton("§aClick here to send your coordinates.", "/pc " + Utils.getCoordsFormatted("x: {}, y: {}, z: {}"));
    }
}