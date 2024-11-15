package nofrills.events;

public class WorldTickEvent {
    private static final WorldTickEvent INSTANCE = new WorldTickEvent();

    public static WorldTickEvent get() {
        return INSTANCE;
    }
}
