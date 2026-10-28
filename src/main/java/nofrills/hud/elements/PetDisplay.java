package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.events.ChatMsgEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.hud.ListeningHudElement;
import nofrills.hud.SimpleTextElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public final class PetDisplay extends SimpleTextElement implements ListeningHudElement {
    public final SettingBool compact = new SettingBool(true, "compact", instance);

    private final Pattern autoPetPattern = Pattern.compile("Autopet equipped your \\[Lvl [0-9]*] (?<pet>.*)! VIEW RULE");
    private final Pattern petMenuPattern = Pattern.compile("(?:.* |)\\[Lvl [0-9]*] (?<pet>.*)");
    private final Pattern tabListPattern = Pattern.compile(" (?:No pet selected|\\[Lvl [0-9]*] (?<pet>.*))");
    private final Pattern loadoutsPattern = Pattern.compile("Pet: (?:None|\\[Lvl [0-9]*] (?<pet>.*))");

    public PetDisplay() {
        super(Component.literal("§cNo Pet"), new Feature("petDisplayElement"), "Pet Display");
        this.options = this.getBaseSettings(List.of(
                new Settings.Toggle("Compact Mode", this.compact, "Makes the element more compact by removing the prefix.")
        ));
        this.setDesc("Displays the name of your active pet.");
        this.setCategory(Category.Info);
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.shouldRender()) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
        }
    }

    private void apply(String pet, String plainText, Component text) {
        int petStart = plainText.indexOf(pet);
        int petEnd = petStart + pet.length();
        MutableComponent result = this.compact.value() ? Component.empty() : Component.literal("Pet: ");
        Utils.getStyles(text.getVisualOrderText()).stream()
                .filter(p -> {
                    int index = plainText.indexOf(p.getRight());
                    return index >= petStart && index < petEnd;
                })
                .forEach(p -> result.append(Component.literal(p.getRight()).setStyle(p.getLeft())));
        mc.execute(() -> this.setText(result));
    }

    private void clear() {
        mc.execute(() -> this.setText(this.compact.value() ? this.defaultText : Component.literal("Pet: ").append(this.defaultText)));
    }

    // lower level mixin instead of click event for better mod support
    public void handleInput(int slotNum, int buttonNum, ContainerInput containerInput) {
        if (mc.gui.screen() instanceof AbstractContainerScreen<?> container) {
            if (slotNum < 0 || slotNum >= container.getMenu().slots.size()) return;
            Slot slot = container.getMenu().getSlot(slotNum);
            String title = container.getTitle().getString();
            if (containerInput.equals(ContainerInput.PICKUP) && Utils.isPaginatedMenu(title, "Loadouts")) {
                List<Component> lore = Utils.getLoreText(slot.getItem());
                if (lore.stream().anyMatch(c -> Utils.toPlain(c).equals("Left-click to equip!"))) {
                    for (Component text : lore) {
                        String line = Utils.toPlain(text);
                        Matcher matcher = this.loadoutsPattern.matcher(line);
                        if (!matcher.matches()) continue;
                        String pet = matcher.group("pet");
                        if (pet != null) {
                            this.apply(pet, line, text);
                        }
                        return;
                    }
                }
            } else if (Utils.isPaginatedMenu(title, "Pets")) {
                if (containerInput.equals(ContainerInput.QUICK_MOVE)) return;
                ItemStack stack = slot.getItem();
                String name = Utils.toPlain(stack.getHoverName());
                Matcher matcher = this.petMenuPattern.matcher(name);
                if (!matcher.matches()) return;
                List<String> lore = Utils.getLoreLines(stack);
                if (lore.contains("Left-click to summon!")) {
                    if (containerInput.equals(ContainerInput.PICKUP) && buttonNum == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
                        return;
                    this.apply(matcher.group("pet"), name, stack.getHoverName());
                } else if (lore.contains("Click to despawn!")) {
                    this.clear();
                }
            }
        }
    }

    @Override
    public void onChatMessage(ChatMsgEvent event) {
        Matcher matcher = this.autoPetPattern.matcher(event.msg());
        if (!matcher.matches()) return;
        this.apply(matcher.group("pet"), event.messagePlain, event.message);
    }

    @Override
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof ClientboundPlayerInfoUpdatePacket infoPacket) {
            for (ClientboundPlayerInfoUpdatePacket.Entry entry : infoPacket.entries()) {
                if (entry.displayName() == null) continue;
                String name = Utils.toPlain(entry.displayName());
                Matcher matcher = this.tabListPattern.matcher(name);
                if (!matcher.matches()) continue;
                String pet = matcher.group("pet");
                if (pet != null) {
                    this.apply(pet, name, entry.displayName());
                } else {
                    this.clear();
                }
                return;
            }
        }
    }
}
