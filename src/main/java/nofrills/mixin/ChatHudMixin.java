package nofrills.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
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
    private void onMessage(Text ignored, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Text> message) {
        if (indicator != Utils.noFrillsIndicator) {
            String msg = Formatting.strip(message.get().getString());
            ChatMsgEvent event = eventBus.post(new ChatMsgEvent(message.get(), msg, signatureData, indicator));
            if (event.isReplaced()) {
                message.set(event.getMessage());
            }
            if (event.isCancelled()) {
                ci.cancel();
            }
            if (partyMessagePattern.matcher(msg).matches()) {
                int nameStart = msg.contains("]") & msg.indexOf("]") < msg.indexOf(":") ? msg.indexOf("]") : msg.indexOf(">");
                String[] clean = msg.replace(msg.substring(0, nameStart + 1), "").split(":", 2);
                String author = clean[0].trim(), content = clean[1].trim();
                boolean self = author.equalsIgnoreCase(mc.getSession().getUsername());
                if (eventBus.post(new PartyChatMsgEvent(content, author, self)).isCancelled() && !ci.isCancelled()) {
                    ci.cancel();
                }
            }
        }
    }
}
