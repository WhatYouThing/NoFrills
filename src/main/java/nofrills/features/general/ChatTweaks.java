package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Formatting;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.config.SettingKeybind;
import nofrills.events.InputEvent;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class ChatTweaks {
    public static final Feature instance = new Feature("chatTweaks");

    public static final SettingKeybind copyKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "copyKey", instance);
    public static final SettingBool trimOnCopy = new SettingBool(false, "trimOnCopy", instance);
    public static final SettingBool msgOnCopy = new SettingBool(false, "msgOnCopy", instance);
    public static final SettingInt copyMsgLength = new SettingInt(50, "copyMsgLength", instance);
    public static final SettingBool keepHistory = new SettingBool(false, "keepHistory", instance);
    public static final SettingBool extraLines = new SettingBool(false, "extraLines", instance);
    public static final SettingInt lines = new SettingInt(1000, "lines", instance);

    private static String getHoveredMsg() {
        ChatHud chatHud = mc.inGameHud.getChatHud();
        double x = chatHud.toChatLineX(mc.mouse.getScaledX(mc.getWindow()));
        double y = chatHud.toChatLineY(mc.mouse.getScaledY(mc.getWindow()));
        int i = chatHud.getMessageLineIndex(x, y);
        if (i >= 0 && i < chatHud.visibleMessages.size()) {
            StringBuilder builder = new StringBuilder();
            List<ChatHudLine.Visible> lines = new ArrayList<>();
            for (int j = i + 1; j < chatHud.visibleMessages.size(); j++) {
                ChatHudLine.Visible line = chatHud.visibleMessages.get(j);
                if (line.endOfEntry()) break;
                lines.addFirst(line);
            }
            for (int j = i; j >= 0; j--) {
                ChatHudLine.Visible line = chatHud.visibleMessages.get(j);
                lines.add(line);
                if (line.endOfEntry()) break;
            }
            for (ChatHudLine.Visible line : lines) {
                line.content().accept((index, style, codePoint) -> {
                    builder.appendCodePoint(codePoint);
                    return true;
                });
            }
            return Formatting.strip(builder.toString());
        }
        return "";
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen instanceof ChatScreen && copyKey.bound() && copyKey.key() == event.key) {
            if (event.action == GLFW.GLFW_PRESS) {
                String message = getHoveredMsg();
                if (message.isEmpty()) return;
                mc.keyboard.setClipboard(trimOnCopy.value() ? message.trim() : message);
                if (msgOnCopy.value()) {
                    int length = copyMsgLength.value();
                    if (length == 0) {
                        Utils.info("§aMessage copied to clipboard.");
                    } else {
                        Utils.infoFormat("§aMessage copied to clipboard: \"{}\".",
                                message.length() > length ? message.substring(0, length) + "..." : message
                        );
                    }
                }
            }
            event.cancel();
        }
    }
}
