package nofrills.features.general;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.ChatFormatting;
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
        int chatBottom = Mth.floor((mc.getWindow().getGuiScaledHeight() - 40) / mc.options.chatScale().get());
        int messageHeight = 9;
        double chatLineSpacing = mc.options.chatLineSpacing().get();
        int entryHeight = (int) (messageHeight * (chatLineSpacing + 1.0));
        List<GuiMessage.Line> visibleMessages = chatHud.trimmedMessages.subList(chatHud.chatScrollbarPos, chatHud.trimmedMessages.size());
        int i = -1;
        for (int index = 0; index < visibleMessages.size(); index++) {
            int entryBottom = chatBottom - index * entryHeight;
            int entryTop = entryBottom - entryHeight;
            if (ActiveTextCollector.isPointInRectangle(mouseX, mouseY, 0, entryTop, mc.options.chatWidth().get().floatValue() * 320.0f, entryBottom)) {
                i = index;
                break;
            }
        }
        if (i >= 0) {
            StringBuilder builder = new StringBuilder();
            List<GuiMessage.Line> lines = new ArrayList<>();
            if (singleLine) {
                lines.addFirst(visibleMessages.get(i));
            } else {
                for (int index = i + 1; index < visibleMessages.size(); index++) {
                    GuiMessage.Line line = visibleMessages.get(index);
                    if (line.endOfEntry()) break;
                    lines.addFirst(line);
                }
                for (int index = i; index >= 0; index--) {
                    GuiMessage.Line line = visibleMessages.get(index);
                    lines.add(line);
                    if (line.endOfEntry()) break;
                }
            }
            for (GuiMessage.Line line : lines) {
                line.content().accept((index, style, codePoint) -> {
                    builder.appendCodePoint(codePoint);
                    return true;
                });
            }
            return ChatFormatting.stripFormatting(builder.toString());
        }
        return "";
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
