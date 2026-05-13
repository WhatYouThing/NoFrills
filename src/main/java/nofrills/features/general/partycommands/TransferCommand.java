package nofrills.features.general.partycommands;

import nofrills.misc.Utils;

import java.util.Optional;

public class TransferCommand extends Command {

    public TransferCommand() {
        super(PartyCommands.transfer, "pt", "ptme");
    }

    @Override
    public Optional<String> getTarget(String msg) {
        if (msg.startsWith("ptme")) {
            return Optional.empty();
        }
        return super.getTarget(msg);
    }

    @Override
    public void onAutomatic(String author, String msg) {
        Utils.sendMessage("/party transfer " + this.getTarget(msg).orElse(author));
    }

    @Override
    public void onManual(String author, String msg) {
        String target = this.getTarget(msg).orElse(author);
        Utils.infoButton("§aClick here to promote " + target + " as leader.", "/party transfer " + target);
    }
}