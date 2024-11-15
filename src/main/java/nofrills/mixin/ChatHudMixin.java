package nofrills.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.events.ChatMsgEvent;
import nofrills.events.PartyChatMsgEvent;
import nofrills.misc.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;
import static nofrills.Main.mc;
import static nofrills.misc.Utils.partyMessagePattern;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (indicator != Utils.noFrillsIndicator) {
            String msg = Formatting.strip(message.getString());
            if (eventBus.post(new ChatMsgEvent(message, msg, signatureData, indicator)).isCancelled()) {
                ci.cancel();
            }
            if (partyMessagePattern.matcher(message.getString()).matches()) {
                int nameStart = msg.contains("]") & msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                boolean self = author.equalsIgnoreCase(mc.getSession().getUsername());
                PartyChatMsgEvent event = eventBus.post(new PartyChatMsgEvent(content, author, self));
                if (event.isCancelled() && !ci.isCancelled()) {
                    ci.cancel();
                }
            }
        }
    }
}
