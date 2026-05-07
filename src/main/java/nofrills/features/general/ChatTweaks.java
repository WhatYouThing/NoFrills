package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.font.DrawnTextConsumer;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
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

    private static String getHoveredMsg(boolean singleLine) {
        ChatHud chatHud = mc.inGameHud.getChatHud();
        float mouseX = (float) mc.mouse.getScaledX(mc.getWindow());
        float mouseY = (float) mc.mouse.getScaledY(mc.getWindow());
        int chatBottom = MathHelper.floor((mc.getWindow().getScaledHeight() - 40));
        double chatScale = mc.options.getChatScale().getValue();
        int entryHeight = (int) (9.0 * (mc.options.getChatLineSpacing().getValue() + 1.0));
        int chatHeight = ChatHud.getHeight(mc.options.getChatHeightFocused().getValue());
        int chatWidth = ChatHud.getWidth(mc.options.getChatWidth().getValue());
        int visibleEnd = Math.min(chatHud.visibleMessages.size(), chatHud.scrolledLines + chatHeight / entryHeight);
        List<ChatHudLine.Visible> visibleMessages = chatHud.visibleMessages.subList(chatHud.scrolledLines, visibleEnd);
        for (int index = 0; index < visibleMessages.size(); index++) {
            int entryBottom = (int) (chatBottom - index * (entryHeight * chatScale));
            int entryTop = (int) (entryBottom - (entryHeight * chatScale));
            if (DrawnTextConsumer.isWithinBounds(mouseX, mouseY, 0, entryTop, chatWidth, entryBottom)) {
                if (singleLine) {
                    return Utils.toPlain(visibleMessages.get(index).content());
                }
                return Utils.toPlain(getFullMessage(visibleMessages, index).stream().map(ChatHudLine.Visible::content).toList());
            }
        }
        return "";
    }

    public static List<ChatHudLine.Visible> getFullMessage(List<ChatHudLine.Visible> visible, int index) {
        List<ChatHudLine.Visible> lines = new ArrayList<>();
        for (int i = index + 1; i < visible.size(); i++) {
            ChatHudLine.Visible line = visible.get(i);
            if (line.endOfEntry()) break;
            lines.addFirst(line);
        }
        for (int i = index; i >= 0; i--) {
            ChatHudLine.Visible line = visible.get(i);
            lines.add(line);
            if (line.endOfEntry()) break;
        }
        return lines;
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
