package nofrills.features.misc;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.ServerJoinEvent;
import nofrills.misc.Utils;

import static nofrills.Main.eventBus;

public class UpdateChecker {
    public static final Feature instance = new Feature("updateChecker");

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        if (instance.isActive()) {
            Utils.checkUpdate(false);
            eventBus.unsubscribe(UpdateChecker.class);
        }
    }
}
