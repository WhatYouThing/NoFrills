package nofrills.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.SendPacketEvent;
import nofrills.events.TabListUpdateEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.eventBus;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void onPacketReceive(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (eventBus.post(new ReceivePacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
        if (packet instanceof PlayerListS2CPacket listPacket) {
            List<PlayerListS2CPacket.Entry> entries = new ArrayList<>(listPacket.getEntries());
            entries.removeIf(entry -> entry.displayName() == null);
            eventBus.post(new TabListUpdateEvent(listPacket, entries));
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void onPacketSend(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (eventBus.post(new SendPacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}