package nofrills.features.general.partycommands;

import nofrills.misc.Utils;

public class AllInviteCommand extends Command {

    public AllInviteCommand() {
        super(PartyCommands.allinv, "allinv");
    }

    @Override
    public void onAutomatic(String author, String msg) {
        Utils.sendMessage("/party settings allinvite");
    }

    @Override
    public void onManual(String author, String msg) {
        Utils.infoButton("§aClick here to toggle all invite.", "/party settings allinvite");
    }
}
