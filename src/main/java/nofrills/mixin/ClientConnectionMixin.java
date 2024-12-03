package nofrills.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import nofrills.events.PlaySoundEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.SendPacketEvent;
import nofrills.misc.Utils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (eventBus.post(new ReceivePacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
        if (packet instanceof PlaySoundS2CPacket soundPacket) {
            if (eventBus.post(new PlaySoundEvent(soundPacket)).isCancelled() && !ci.isCancelled()) {
                ci.cancel();
            }
        }
        if (packet instanceof PlayerListS2CPacket listPacket) {
            for (PlayerListS2CPacket.Entry entry : listPacket.getEntries()) {
                Text name = entry.displayName();
                if (name != null) {
                    String nameClean = Formatting.strip(name.getString().trim());
                    if (!nameClean.isEmpty()) {
                        if (nameClean.startsWith("Area: ")) {
                            Utils.skyblockData.currentArea = nameClean.replace("Area:", "").trim();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (eventBus.post(new SendPacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}