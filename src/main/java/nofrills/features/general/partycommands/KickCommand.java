package nofrills.features.general.partycommands;

import nofrills.misc.Utils;

public class KickCommand extends Command {

    public KickCommand() {
        super(PartyCommands.kick, "kick", "k");
    }

    @Override
    public void onAutomatic(String author, String msg) {
        this.getTarget(msg).ifPresent(target -> Utils.sendMessage(Utils.format("/party kick {}", target)));
    }

    @Override
    public void onManual(String author, String msg) {
        this.getTarget(msg).ifPresent(target -> Utils.infoButton(
                "§aClick here to kick " + target + ".",
                Utils.format("/party kick {}", target))
        );
    }
}