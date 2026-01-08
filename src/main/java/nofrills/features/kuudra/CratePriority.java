package nofrills.features.kuudra;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.events.PartyChatMsgEvent;
import nofrills.misc.KuudraUtil;
import nofrills.misc.Utils;

import static nofrills.misc.KuudraUtil.PickupSpot.*;

public class CratePriority {
    public static final Feature instance = new Feature("cratePriority");

    private static void announce(String message) {
        Utils.showTitle("§e§l" + Utils.toUpper(message), "", 0, 50, 10);
        Utils.infoFormat("§eCrate Priority: {}.", message);
    }

    private static KuudraUtil.SpotType getPreType() {
        KuudraUtil.PickupSpot spot = KuudraUtil.getPreSpot();
        if (spot != null) {
            if (spot.equals(Triangle)) return KuudraUtil.SpotType.Triangle;
            if (spot.equals(X)) return KuudraUtil.SpotType.X;
            if (spot.equals(Equals)) return KuudraUtil.SpotType.Equals;
            if (spot.equals(Slash)) return KuudraUtil.SpotType.Slash;
        }
        return KuudraUtil.SpotType.None;
    }

    @EventHandler
    private static void onPartyMsg(PartyChatMsgEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect)) {
            KuudraUtil.SpotType type = getPreType();
            String msg = Utils.toLower(event.message);
            if (!msg.startsWith("no ") || type.equals(KuudraUtil.SpotType.None)) return;
            if (Shop.matches(msg)) {
                switch (type) {
                    case Triangle, X -> announce("Grab X Cannon");
                    case Equals, Slash -> announce("Grab Square, Place on Shop");
                }
            }
            if (Triangle.matches(msg)) {
                switch (type) {
                    case Triangle -> announce("Pull Square, Grab Shop");
                    case X, Equals -> announce("Grab X Cannon");
                    case Slash -> announce("Grab Square, Place on Triangle");
                }
            }
            if (Equals.matches(msg)) {
                switch (type) {
                    case Triangle, X -> announce("Grab X Cannon");
                    case Equals -> announce("Pull Square, Grab Shop");
                    case Slash -> announce("Grab Square, Place on Equals");
                }
            }
            if (Slash.matches(msg)) {
                switch (type) {
                    case Triangle -> announce("Grab Square, Place on Slash");
                    case X, Equals -> announce("Grab X Cannon");
                    case Slash -> announce("Pull Square, Grab Shop");
                }
            }
            if (Square.matches(msg)) {
                switch (type) {
                    case Triangle, Equals -> announce("Grab Shop");
                    case X, Slash -> announce("Grab X Cannon");
                }
            }
            if (XCannon.matches(msg)) {
                switch (type) {
                    case Triangle, Equals -> announce("Grab Shop");
                    case Slash, X -> announce("Grab Square, Place on X Cannon");
                }
            }
            if (X.matches(msg)) {
                switch (type) {
                    case Triangle, Equals -> announce("Grab X Cannon");
                    case X -> announce("Pull Square, Grab Shop");
                    case Slash -> announce("Grab Square, Place on X");
                }
            }
        }
    }
}
