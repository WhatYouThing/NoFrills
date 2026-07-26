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
    private final ItemStack iconStack;
    private double buttonScaleX;
    private double buttonScaleY;

    public InventoryButtonWidget(double x, double y, double scaleX, double scaleY, ItemStack stack, String command, String tooltip, JsonObject buttonObject) {
        super((int) x, (int) y, (int) (20 * scaleX), (int) (20 * scaleY), inventoryButtonSprites, btn -> {
            Utils.sendMessage(command);
            btn.setFocused(false);
        });
        this.setTooltip(Tooltip.create(Component.literal(tooltip)));
        this.buttonObject = buttonObject;
        this.buttonScaleX = scaleX;
        this.buttonScaleY = scaleY;
        this.iconStack = stack;
    }

    public static InventoryButtonWidget of(JsonObject buttonObject) {
        String model = buttonObject.get("model").getAsString();
        String textures = buttonObject.get("textures").getAsString();
        String command = buttonObject.get("command").getAsString();
        String tooltip = buttonObject.get("tooltip").getAsString();
        double posX = buttonObject.get("x").getAsDouble() * mc.getWindow().getGuiScaledWidth();
        double posY = buttonObject.get("y").getAsDouble() * mc.getWindow().getGuiScaledHeight();
        double scaleX = buttonObject.has("scaleX") ? buttonObject.get("scaleX").getAsDouble() : 1.0;
        double scaleY = buttonObject.has("scaleY") ? buttonObject.get("scaleY").getAsDouble() : 1.0;
        ItemStack stack = BuiltInRegistries.ITEM.getValue(Identifier.parse(model)).getDefaultInstance().copy();
        if (stack.is(Items.PLAYER_HEAD) && !textures.isEmpty()) {
            Multimap<String, Property> properties = ImmutableMultimap.of("textures", new Property("textures", textures));
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "", new PropertyMap(properties));
            ResolvableProfile profile = ResolvableProfile.createResolved(gameProfile);
            stack.set(DataComponents.PROFILE, profile);
        }
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, buttonObject.get("glint").getAsBoolean());
        return new InventoryButtonWidget(posX, posY, scaleX, scaleY, stack, command, tooltip, buttonObject);
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        graphics.pose().pushMatrix();
        graphics.pose().translate((float) (this.getX() + 2 * this.buttonScaleX), (float) (this.getY() + 2 * this.buttonScaleY));
        graphics.pose().scale((float) this.buttonScaleX, (float) this.buttonScaleY);
        graphics.fakeItem(this.iconStack, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    protected boolean isValidClickButton(final MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == 1 || super.isValidClickButton(buttonInfo);
    }

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (!event.buttonInfo().hasControlDown() && !event.buttonInfo().hasAltDown()) {
            if (event.buttonInfo().button() == 1) {
                mc.setScreen(InventoryButtonSettings.of(this.buttonObject));
            } else {
                super.onClick(event, doubleClick);
            }
        }
    }

    @Override
    protected void onDrag(final @NonNull MouseButtonEvent event, final double dx, final double dy) {
        if (event.buttonInfo().button() == 0) {
            double windowX = mc.getWindow().getGuiScaledWidth();
            double windowY = mc.getWindow().getGuiScaledHeight();
            double buttonSizeX = 20 * this.buttonScaleX;
            double buttonSizeY = 20 * this.buttonScaleY;
            if (event.buttonInfo().hasControlDown()) {
                double newX = Math.clamp(event.x() - buttonSizeX * 0.5, 0, windowX - buttonSizeX);
                double newY = Math.clamp(event.y() - buttonSizeY * 0.5, 0, windowY - buttonSizeY);
                this.setX((int) newX);
                this.setY((int) newY);
                this.buttonObject.addProperty("x", newX / windowX);
                this.buttonObject.addProperty("y", newY / windowY);
            } else if (event.buttonInfo().hasAltDown()) {
                buttonSizeX = Math.clamp(event.x() - this.getX(), 0.25 * 20, windowX - buttonSizeX);
                buttonSizeY = Math.clamp(event.y() - this.getY(), 0.25 * 20, windowY - buttonSizeY);
                if (this.buttonObject.get("keepSquare").getAsBoolean()) {
                    double finalSize = Math.min(buttonSizeX, buttonSizeY);
                    this.setSize((int) finalSize, (int) finalSize);
                    this.buttonScaleX = finalSize / 20.;
                    this.buttonScaleY = finalSize / 20.;
                } else {
                    this.setSize((int) buttonSizeX, (int) buttonSizeY);
                    this.buttonScaleX = buttonSizeX / 20.;
                    this.buttonScaleY = buttonSizeY / 20.;
                }
                this.buttonObject.addProperty("scaleX", this.buttonScaleX);
                this.buttonObject.addProperty("scaleY", this.buttonScaleY);
            }
        }
    }
}
