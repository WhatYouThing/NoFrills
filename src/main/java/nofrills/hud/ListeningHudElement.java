package nofrills.hud;

import nofrills.events.*;

public interface ListeningHudElement {

    default void onClientTick() {
    }

    default void onServerTick() {
    }

    default void onServerJoin() {
    }

    default void onReceivePacket(ReceivePacketEvent event) {
    }

    default void onEntityNamed(EntityNamedEvent event) {
    }

    default void onChatMessage(ChatMsgEvent event) {
    }

    default void onBlockUpdate(BlockUpdateEvent event) {
    }

    default void onSlotUpdate(SlotUpdateEvent event) {
    }

    default void onInventoryUpdate(InventoryUpdateEvent event) {
    }
}
