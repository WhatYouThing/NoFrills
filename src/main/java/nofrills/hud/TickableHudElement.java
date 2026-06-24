package nofrills.hud;

public interface TickableHudElement {

    default void onClientTick() {
    }

    default void onServerTick() {
    }

    default void onReset() {
    }
}
