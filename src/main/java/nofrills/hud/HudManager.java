package nofrills.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import nofrills.events.HudRenderEvent;
import nofrills.hud.elements.*;
import nofrills.misc.RenderColor;

import java.util.List;

import static nofrills.Main.mc;

public class HudManager {
    private static final RenderColor defaultColor = RenderColor.fromHex(0xffffff);

    public static FishingBobber bobberElement = new FishingBobber(Text.of("§cBobber: §7Inactive"), defaultColor);
    public static SeaCreatures seaCreaturesElement = new SeaCreatures(Text.of("§3Sea Creatures: §70"), defaultColor);
    public static TPS tpsElement = new TPS(Text.of("§bTPS: §f20.00"), defaultColor);
    public static LagMeter lagMeterElement = new LagMeter(Text.of("§cLast server tick was 0.00s ago"), defaultColor);
    public static Power powerElement = new Power(Text.of("§bPower: §f0"), defaultColor);
    public static Day dayElement = new Day(Text.of("§bDay: §f0"), defaultColor);
    public static Ping pingElement = new Ping(Text.of("§bPing: §f0§7ms"), defaultColor);

    public static List<HudElement> elements = List.of(
            bobberElement,
            seaCreaturesElement,
            tpsElement,
            lagMeterElement,
            powerElement,
            dayElement,
            pingElement
    );

    public static boolean isEditingHud() {
        return mc.currentScreen instanceof HudEditorScreen;
    }

    @EventHandler
    private static void onRenderHud(HudRenderEvent event) {
        if (!isEditingHud()) {
            for (HudElement element : elements) {
                element.render(event.context, 0, 0, event.tickCounter.getTickDelta(true));
            }
        }
    }
}
