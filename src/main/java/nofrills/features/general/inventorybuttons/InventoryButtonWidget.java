package nofrills.features.general.inventorybuttons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import nofrills.misc.Utils;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

import static nofrills.Main.mc;

public final class InventoryButtonWidget extends ImageButton {

    private static final WidgetSprites inventoryButtonSprites = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    );
    private final JsonObject buttonObject;
    private final double buttonScale;
    private final ItemStack iconStack;

    public InventoryButtonWidget(double x, double y, double scale, ItemStack stack, String command, String tooltip, JsonObject buttonObject) {
        super((int) x, (int) y, (int) (20 * scale), (int) (20 * scale), inventoryButtonSprites, btn -> {
            if (!buttonObject.get("dragAndDrop").getAsBoolean()) {
                Utils.sendMessage(command);
            }
            btn.setFocused(false);
        });
        this.setTooltip(Tooltip.create(Component.literal(tooltip)));
        this.buttonObject = buttonObject;
        this.buttonScale = scale;
        this.iconStack = stack;
    }

    public static InventoryButtonWidget of(JsonObject buttonObject) {
        String model = buttonObject.get("model").getAsString();
        String textures = buttonObject.get("textures").getAsString();
        String command = buttonObject.get("command").getAsString();
        String tooltip = buttonObject.get("tooltip").getAsString();
        double posX = buttonObject.get("x").getAsDouble() * mc.getWindow().getGuiScaledWidth();
        double posY = buttonObject.get("y").getAsDouble() * mc.getWindow().getGuiScaledHeight();
        double scale = buttonObject.get("scale").getAsDouble();
        ItemStack stack = BuiltInRegistries.ITEM.getValue(Identifier.parse(model)).getDefaultInstance().copy();
        if (stack.is(Items.PLAYER_HEAD) && !textures.isEmpty()) {
            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", textures));
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "", new PropertyMap(properties));
            ResolvableProfile profile = ResolvableProfile.createResolved(gameProfile);
            stack.set(DataComponents.PROFILE, profile);
        }
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, buttonObject.get("glint").getAsBoolean());
        return new InventoryButtonWidget(posX, posY, scale, stack, command, tooltip, buttonObject);
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) (this.getX() + 2 * this.buttonScale), (float) (this.getY() + 2 * this.buttonScale));
        graphics.pose().scale((float) this.buttonScale, (float) this.buttonScale);
        graphics.fakeItem(this.iconStack, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    protected boolean isValidClickButton(final MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 1 || super.isValidClickButton(buttonInfo);
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (event.buttonInfo().button() == 1) {
            mc.setScreen(InventoryButtonSettings.of(this.buttonObject));
        } else {
            super.onClick(event, doubleClick);
        }
    }

    @Override
    protected void onDrag(final @NonNull MouseButtonEvent event, final double dx, final double dy) {
        if (this.buttonObject.get("dragAndDrop").getAsBoolean()) {
            double windowX = mc.getWindow().getGuiScaledWidth();
            double windowY = mc.getWindow().getGuiScaledHeight();
            double buttonSize = 20 * this.buttonScale;
            double newX = Math.clamp(event.x() - buttonSize * 0.5, 0, windowX - buttonSize);
            double newY = Math.clamp(event.y() - buttonSize * 0.5, 0, windowY - buttonSize);
            this.setX((int) newX);
            this.setY((int) newY);
            this.buttonObject.addProperty("x", newX / windowX);
            this.buttonObject.addProperty("y", newY / windowY);
        }
    }
}
