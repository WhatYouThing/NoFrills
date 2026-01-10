package nofrills.mixin;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.SendPacketEvent;
import nofrills.events.ServerTickEvent;
import nofrills.misc.SkyblockData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static nofrills.Main.eventBus;

@Mixin(targets = "net.minecraft.network.PacketApplyBatcher$Entry")
final class PacketApplyBatcherEntryMixin<T extends PacketListener> {
    @Shadow
    @NotNull
    private Packet<T> packet;

    private PacketApplyBatcherEntryMixin() {
        super();

        throw new UnsupportedOperationException("mixin class");
    }

    @Inject(method = "apply", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/Packet;apply(Lnet/minecraft/network/listener/PacketListener;)V"), cancellable = true)
    private final void nofrills$beforeMainThreadPacket(@NotNull final CallbackInfo ci) {
        final var packet = this.packet;

        if (packet instanceof CommonPingS2CPacket) {
            eventBus.post(new ServerTickEvent());
        } else if (packet instanceof PlayerListS2CPacket listPacket) {
            SkyblockData.updateTabList(listPacket, listPacket.getEntries());
        }

        if (eventBus.post(new ReceivePacketEvent(packet)).isCancelled()) {
            ci.cancel();
        }
    }
}
