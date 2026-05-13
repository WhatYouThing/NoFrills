package nofrills.features.general.partycommands;

import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

public class DowntimeCommand extends Command {

    public DowntimeCommand() {
        super(PartyCommands.downtime, "dt");
    }

    @Override
    public void onAutomatic(String author, String msg) {
        if (SkyblockData.isInInstance() && !SkyblockData.isInstanceOver()) {
            Utils.info("§aScheduled downtime reminder.");
            PartyCommands.setDowntimeNeeded();
        }
    }
}
