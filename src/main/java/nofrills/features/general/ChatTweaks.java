package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
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
    public static final SettingKeybind copyLineKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "copyLineKey", instance);
    public static final SettingBool trimOnCopy = new SettingBool(false, "trimOnCopy", instance);
    public static final SettingBool msgOnCopy = new SettingBool(false, "msgOnCopy", instance);
    public static final SettingInt copyMsgLength = new SettingInt(50, "copyMsgLength", instance);
    public static final SettingBool keepHistory = new SettingBool(false, "keepHistory", instance);
    public static final SettingBool extraLines = new SettingBool(false, "extraLines", instance);
    public static final SettingInt lines = new SettingInt(1000, "lines", instance);

    private static double[] getChatLinePos() {
        ChatHud chatHud = mc.inGameHud.getChatHud();
        double x = mc.mouse.getScaledX(mc.getWindow());
        double y = mc.mouse.getScaledY(mc.getWindow());
        double d = mc.getWindow().getScaledHeight() - y - 40.0;
        return new double[]{x / chatHud.getChatScale() - 4.0, d / (chatHud.getChatScale() * chatHud.getLineHeight())};
    }

    // imported function from previous version of the game
    private static int getChatLineIndex(double x, double y) {
        ChatHud chatHud = mc.inGameHud.getChatHud();
        if (chatHud.isChatFocused() && !chatHud.isChatHidden()) {
            if (!(x < -4.0) && !(x > MathHelper.floor(chatHud.getWidth() / chatHud.getChatScale()))) {
                int i = Math.min(chatHud.getVisibleLineCount(), chatHud.visibleMessages.size());
                if (y >= 0.0 && y < i) {
                    int j = MathHelper.floor(y + chatHud.scrolledLines);
                    if (j >= 0 && j < chatHud.visibleMessages.size()) {
                        return j;
                    }
                }
            }
        }
        return -1;
    }

    private static String getHoveredMsg(boolean singleLine) {
        ChatHud chatHud = mc.inGameHud.getChatHud();
        double[] pos = getChatLinePos();
        int i = getChatLineIndex(pos[0], pos[1]);
        if (i >= 0 && i < chatHud.visibleMessages.size()) {
            StringBuilder builder = new StringBuilder();
            List<ChatHudLine.Visible> lines = new ArrayList<>();
            if (singleLine) {
                lines.addFirst(chatHud.visibleMessages.get(i));
            } else {
                for (int index = i + 1; index < chatHud.visibleMessages.size(); index++) {
                    ChatHudLine.Visible line = chatHud.visibleMessages.get(index);
                    if (line.endOfEntry()) break;
                    lines.addFirst(line);
                }
                for (int index = i; index >= 0; index--) {
                    ChatHudLine.Visible line = chatHud.visibleMessages.get(index);
                    lines.add(line);
                    if (line.endOfEntry()) break;
                }
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
        if (instance.isActive() && mc.currentScreen instanceof ChatScreen && (copyKey.isKey(event.key) || copyLineKey.isKey(event.key))) {
            if (event.action == GLFW.GLFW_PRESS) {
                String message = getHoveredMsg(copyLineKey.isKey(event.key));
                if (message.isEmpty()) return;
                mc.keyboard.setClipboard(trimOnCopy.value() ? message.trim() : message);
                if (msgOnCopy.value()) {
                    String type = copyLineKey.isKey(event.key) ? "Line" : "Message";
                    int length = copyMsgLength.value();
                    if (length == 0) {
                        Utils.infoFormat("§a{} copied to clipboard.", type);
                    } else {
                        Utils.infoFormat("§a{} copied to clipboard: \"§7{}§a\".",
                                type,
                                message.length() > length ? message.substring(0, length) + "..." : message
                        );
                    }
                }
            }
            event.cancel();
        }
    }
}
