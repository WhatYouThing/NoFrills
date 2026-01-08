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
        Utils.infoFormat("§eCrate Priority: {}", message);
    }

    @EventHandler
    private static void onPartyMsg(PartyChatMsgEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect)) {
            KuudraUtil.PickupSpot spot = KuudraUtil.getPreSpot();
            String msg = Utils.toLower(event.message);
            if (!msg.startsWith("no ") || spot == null) return;
            if (Shop.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab X Cannon");
                if (spot.equals(X)) announce("Grab X Cannon");
                if (spot.equals(Equals)) announce("Grab Square");
                if (spot.equals(Slash)) announce("Grab Square");
            }
            if (Triangle.matches(msg)) {
                if (spot.equals(Triangle)) announce("Pull Square, Grab Shop");
                if (spot.equals(X)) announce("Grab X Cannon");
                if (spot.equals(Equals)) announce("Grab X Cannon");
                if (spot.equals(Slash)) announce("Grab Square");
            }
            if (Equals.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab X Cannon");
                if (spot.equals(X)) announce("Grab X Cannon");
                if (spot.equals(Equals)) announce("Pull Square, Grab Shop");
                if (spot.equals(Slash)) announce("Grab Square");
            }
            if (Slash.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab Square");
                if (spot.equals(X)) announce("Grab X Cannon");
                if (spot.equals(Equals)) announce("Grab X Cannon");
                if (spot.equals(Slash)) announce("Pull Square, Grab Shop");
            }
            if (Square.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab Shop");
                if (spot.equals(X)) announce("Grab X Cannon");
                if (spot.equals(Equals)) announce("Grab Shop");
                if (spot.equals(Slash)) announce("Grab X Cannon");
            }
            if (XCannon.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab Shop");
                if (spot.equals(X)) announce("Grab Square");
                if (spot.equals(Equals)) announce("Grab Shop");
                if (spot.equals(Slash)) announce("Grab Square");
            }
            if (X.matches(msg)) {
                if (spot.equals(Triangle)) announce("Grab X Cannon");
                if (spot.equals(X)) announce("Pull Square, Grab Shop");
                if (spot.equals(Equals)) announce("Grab X Cannon");
                if (spot.equals(Slash)) announce("Grab Square");
            }
        }
    }
}
