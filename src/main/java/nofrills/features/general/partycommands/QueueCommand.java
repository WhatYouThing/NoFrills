package nofrills.features.general.partycommands;

import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.Optional;

public class QueueCommand extends Command {

    public QueueCommand() {
        super(PartyCommands.queue, SkyblockData.instances.stream().map(type -> type.name).toList());
    }

    public Optional<SkyblockData.InstanceType> getType(String msg) {
        for (SkyblockData.InstanceType type : SkyblockData.instances) {
            if (msg.startsWith(type.name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    @Override
    public void onAutomatic(String author, String msg) {
        this.getType(msg).ifPresent(type -> Utils.sendMessage("/joininstance " + type.type));
    }

    @Override
    public void onManual(String author, String msg) {
        this.getType(msg).ifPresent(type -> Utils.infoButton(
                "§aClick here to queue for " + Utils.uppercaseFirst(Utils.toLower(type.type), true) + ".",
                "/joininstance " + type.type)
        );
    }
}