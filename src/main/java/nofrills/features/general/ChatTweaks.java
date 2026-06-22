package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.util.Mth;
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
        ChatComponent chatHud = mc.gui.getChat();
        float mouseX = (float) mc.mouseHandler.getScaledXPos(mc.getWindow());
        float mouseY = (float) mc.mouseHandler.getScaledYPos(mc.getWindow());
        int chatBottom = Mth.floor((mc.getWindow().getGuiScaledHeight() - 40));
        double chatScale = mc.options.chatScale().get();
        int entryHeight = (int) (9.0 * (mc.options.chatLineSpacing().get() + 1.0));
        int chatHeight = ChatComponent.getHeight(mc.options.chatHeightFocused().get());
        int visibleEnd = Math.min(chatHud.trimmedMessages.size(), chatHud.chatScrollbarPos + chatHeight / entryHeight);
        List<GuiMessage.Line> visible = chatHud.trimmedMessages.subList(chatHud.chatScrollbarPos, visibleEnd);
        for (int index = 0; index < visible.size(); index++) {
            int entryBottom = (int) (chatBottom - index * (entryHeight * chatScale));
            int entryTop = (int) (entryBottom - (entryHeight * chatScale));
            if (ActiveTextCollector.isPointInRectangle(mouseX, mouseY, 0, entryTop, ChatComponent.getWidth(mc.options.chatWidth().get()), entryBottom)) {
                if (singleLine) {
                    return Utils.toPlain(visible.get(index).content());
                }
                return Utils.toPlain(getFullMessage(visible, index).stream().map(GuiMessage.Line::content).toList());
            }
        }
        return "";
    }

    public static List<GuiMessage.Line> getFullMessage(List<GuiMessage.Line> visible, int index) {
        List<GuiMessage.Line> lines = new ArrayList<>();
        for (int i = index + 1; i < visible.size(); i++) {
            GuiMessage.Line line = visible.get(i);
            if (line.endOfEntry()) break;
            lines.addFirst(line);
        }
        for (int i = index; i >= 0; i--) {
            GuiMessage.Line line = visible.get(i);
            lines.add(line);
            if (line.endOfEntry()) break;
        }
        return lines;
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.screen instanceof ChatScreen && (copyKey.isKey(event.key) || copyLineKey.isKey(event.key))) {
            if (event.action == GLFW.GLFW_PRESS) {
                String message = getHoveredMsg(copyLineKey.isKey(event.key));
                if (message.isEmpty()) return;
                mc.keyboardHandler.setClipboard(trimOnCopy.value() ? message.trim() : message);
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
