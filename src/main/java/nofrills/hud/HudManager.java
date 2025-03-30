package nofrills.hud;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import nofrills.events.HudRenderEvent;
import nofrills.misc.RenderColor;

import java.util.List;

import static nofrills.Main.mc;

public class HudManager {
    public static SimpleTextElement testElement = new SimpleTextElement(0.25, 0.25, Text.of("§9deez nuts"), RenderColor.fromHex(0xffffff));
    public static List<HudElement> elements = List.of(
            testElement
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
