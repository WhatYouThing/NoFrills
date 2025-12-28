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
        Utils.showTitle(Utils.toUpper(message), "", 0, 50, 10);
        Utils.infoFormat("§eCrate Priority: {}", message);
    }

    private static boolean isSpot(String message, KuudraUtil.PickupSpot spot) {
        return message.replaceAll(" ", "").contains(Utils.toLower(spot.name).replaceAll(" ", ""));
    }

    @EventHandler
    private static void onPartyMsg(PartyChatMsgEvent event) {
        if (instance.isActive() && Utils.isInKuudra() && KuudraUtil.getCurrentPhase().equals(KuudraUtil.Phase.Collect)) {
            KuudraUtil.PickupSpot preSpot = KuudraUtil.getPreSpot();
            String msg = Utils.toLower(event.message);
            if (!msg.startsWith("no ") || preSpot == null) return;
            if (isSpot(msg, Shop)) {
                if (preSpot.equals(Triangle)) announce("Grab X Cannon");
                if (preSpot.equals(X)) announce("Grab X Cannon");
                if (preSpot.equals(Equals)) announce("Grab Square");
                if (preSpot.equals(Slash)) announce("Grab Square");
            }
            if (isSpot(msg, Triangle)) {
                if (preSpot.equals(Triangle)) announce("Pull Square, Grab Shop");
                if (preSpot.equals(X)) announce("Grab X Cannon");
                if (preSpot.equals(Equals)) announce("Grab X Cannon");
                if (preSpot.equals(Slash)) announce("Grab Square");
            }
            if (isSpot(msg, Equals)) {
                if (preSpot.equals(Triangle)) announce("Grab X Cannon");
                if (preSpot.equals(X)) announce("Grab X Cannon");
                if (preSpot.equals(Equals)) announce("Pull Square, Grab Shop");
                if (preSpot.equals(Slash)) announce("Grab Square");
            }
            if (isSpot(msg, Slash)) {
                if (preSpot.equals(Triangle)) announce("Grab Square");
                if (preSpot.equals(X)) announce("Grab X Cannon");
                if (preSpot.equals(Equals)) announce("Grab X Cannon");
                if (preSpot.equals(Slash)) announce("Pull Square, Grab Shop");
            }
            if (isSpot(msg, Square)) {
                if (preSpot.equals(Triangle)) announce("Grab Shop");
                if (preSpot.equals(X)) announce("Grab X Cannon");
                if (preSpot.equals(Equals)) announce("Grab Shop");
                if (preSpot.equals(Slash)) announce("Grab X Cannon");
            }
            if (isSpot(msg, XCannon)) {
                if (preSpot.equals(Triangle)) announce("Grab Shop");
                if (preSpot.equals(X)) announce("Grab Square");
                if (preSpot.equals(Equals)) announce("Grab Shop");
                if (preSpot.equals(Slash)) announce("Grab Square");
            }
            if (isSpot(msg, X)) {
                if (preSpot.equals(Triangle)) announce("Grab X Cannon");
                if (preSpot.equals(X)) announce("Pull Square, Grab Shop");
                if (preSpot.equals(Equals)) announce("Grab X Cannon");
                if (preSpot.equals(Slash)) announce("Grab Square");
            }
        }
    }
}
