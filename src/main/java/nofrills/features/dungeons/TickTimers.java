package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ServerJoinEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

public class TickTimers {
    public static final Feature instance = new Feature("tickTimers");

    public static final SettingBool storm = new SettingBool(false, "storm", instance);
    public static final SettingBool terminalStart = new SettingBool(false, "terminalStart", instance);
    public static final SettingBool goldor = new SettingBool(false, "goldor", instance);

    public static final List<Timer> timerList = new ArrayList<>();

    public static List<Timer> getTimerList() {
        return new ArrayList<>(timerList);
    }

    public static void addTimer(Timer newTimer) {
        timerList.add(newTimer);
    }

    public static void clearTimer(TimerType type) {
        timerList.removeIf(timer -> timer.type.equals(type));
    }

    public static String getText() {
        StringBuilder text = new StringBuilder("§bTick Timers");
        for (Timer timer : getTimerList()) {
            if (timer.currentTicks == 0) continue;
            String duration = Utils.formatDecimal(timer.currentTicks / 20.0);
            String label = switch (timer.type) {
                case Storm -> "§3Pad";
                case TerminalStart -> "§eTerminals";
                case Goldor -> "§6Death Tick";
            };
            text.append(Utils.format("\n{}: §f{}s", label, duration));
        }
        return text.toString();
    }

    @EventHandler
    private static void onMessage(ChatMsgEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7")) {
            switch (event.messagePlain) {
                case "[BOSS] Storm: Pathetic Maxor, just like expected." -> {
                    if (storm.value()) {
                        addTimer(new Timer(20, true, TimerType.Storm));
                    }
                }
                case "[BOSS] Storm: I should have known that I stood no chance." -> {
                    clearTimer(TimerType.Storm);
                    if (terminalStart.value()) {
                        addTimer(new Timer(101, false, TimerType.TerminalStart));
                    }
                }
                case "[BOSS] Goldor: Who dares trespass into my domain?" -> {
                    if (goldor.value()) {
                        addTimer(new Timer(60, true, TimerType.Goldor));
                    }
                }
                case "The Core entrance is opening!" -> clearTimer(TimerType.Goldor);
                default -> {
                }
            }
        }
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isOnDungeonFloor("7")) {
            for (Timer timer : getTimerList()) {
                timer.tick();
                if (timer.currentTicks == 0 && !timer.repeating) {
                    timerList.remove(timer);
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        timerList.clear();
    }

    public enum TimerType {
        Storm,
        TerminalStart,
        Goldor,
    }

    public static class Timer {
        public int ticks;
        public int currentTicks;
        public boolean repeating;
        public TimerType type;

        public Timer(int ticks, boolean repeating, TimerType type) {
            this.ticks = ticks;
            this.currentTicks = ticks;
            this.repeating = repeating;
            this.type = type;
        }

        public void tick() {
            if (this.currentTicks > 0) {
                this.currentTicks--;
            }
            if (this.currentTicks == 0 && this.repeating) {
                this.currentTicks = this.ticks;
            }
        }
    }
}
