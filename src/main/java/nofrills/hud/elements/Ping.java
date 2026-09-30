package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.util.Util;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingInt;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.SendPacketEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Ping extends SimpleTextElement implements ListeningHudElement {
    public final SettingInt delay = new SettingInt(20, "delay", this.instance);
    public final SettingBool average = new SettingBool(false, "average", this.instance);
    public final SettingInt averageSize = new SettingInt(30, "averageSize", this.instance);
    private final List<Long> pingList = new CopyOnWriteArrayList<>();
    private int pingTicks = this.delay.value();
    private int updateTicks = this.delay.value();
    private long lastPing = 0;

    public Ping() {
        super(Component.literal("Ping: §f0ms"), new Feature("pingElement"), "Ping Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.SliderInt("Delay", 1, 600, 1, this.delay, "The amount of ticks between ping updates."),
                new Settings.Toggle("Average", this.average, "Tracks and adds your average ping to the element."),
                new Settings.SliderInt("Average Limit", 1, 1000, 1, this.averageSize, "The maximum size of the ping history array.\nHigher values produce more accurate results at a higher performance cost.")
        ));
        this.setDesc("Displays your ping.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public void onClientTick() {
        if (this.pingTicks > 0) {
            this.pingTicks -= 1;
            if (this.pingTicks == 0) {
                Utils.sendPingPacket();
            }
        }
        if (this.updateTicks > 0) {
            this.updateTicks -= 1;
            if (this.updateTicks == 0) {
                if (average.value()) {
                    this.setText(Utils.format("Ping: §f{}ms §7{}ms",
                            this.lastPing,
                            this.pingList.stream().mapToLong(l -> l).sum() / Math.max(this.pingList.size(), 1)
                    ));
                } else {
                    this.setText(Utils.format("Ping: §f{}ms", this.lastPing));
                }
                this.updateTicks = this.delay.value();
            }
        }
    }

    @Override
    public void onServerJoin() {
        this.pingTicks = this.delay.value();
        this.lastPing = 0;
        this.pingList.clear();
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event.packet instanceof ServerboundPingRequestPacket) {
            this.pingTicks = this.delay.value();
        }
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof ClientboundPongResponsePacket(long time)) {
            long ping = Util.getMillis() - time;
            while (this.pingList.size() > this.averageSize.value()) {
                this.pingList.removeFirst();
            }
            this.lastPing = ping;
            this.pingList.add(ping);
        }
    }
}
