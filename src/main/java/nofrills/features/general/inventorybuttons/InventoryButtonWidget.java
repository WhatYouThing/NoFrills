package nofrills.features.general.inventorybuttons;

import com.google.gson.JsonObject;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.jspecify.annotations.NonNull;

import static nofrills.Main.mc;

public final class InventoryButtonWidget extends ImageButton {

    private static final WidgetSprites inventoryButtonSprites = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/button"),
            Identifier.withDefaultNamespace("widget/button_highlighted")
    );
    final JsonObject buttonObject;
    final ItemStack iconStack;
    final InventoryButtonStyle buttonStyle;
    final RenderColor buttonColorBackground;
    final RenderColor buttonColorBorder;
    final RenderColor buttonColorBorderHover;
    boolean unlockPosition = false;
    double buttonScaleX;
    double buttonScaleY;

    public InventoryButtonWidget(double x, double y, double scaleX, double scaleY, ItemStack stack, String command, String tooltip, JsonObject buttonObject) {
        super((int) x, (int) y, (int) (20 * scaleX), (int) (20 * scaleY), inventoryButtonSprites, btn -> {
            Utils.sendMessage(command);
            btn.setFocused(false);
        });
        this.setTooltip(Tooltip.create(Component.literal(tooltip)));
        this.buttonObject = buttonObject;
        this.iconStack = stack;
        this.buttonStyle = Utils.toEnumConstant(buttonObject.get("style").getAsString(), InventoryButtonStyle.values(), InventoryButtonStyle.Vanilla);
        this.buttonColorBackground = RenderColor.fromArgb(buttonObject.get("colorBackground").getAsInt());
        this.buttonColorBorder = RenderColor.fromArgb(buttonObject.get("colorBorder").getAsInt());
        this.buttonColorBorderHover = RenderColor.fromArgb(buttonObject.get("colorBorderHover").getAsInt());
        this.buttonScaleX = scaleX;
        this.buttonScaleY = scaleY;
    }

    public static InventoryButtonWidget of(JsonObject buttonObject, AbstractContainerScreen<?> container) {
        String model = buttonObject.get("model").getAsString();
        String itemId = buttonObject.get("itemId").getAsString();
        String customModel = buttonObject.get("customModel").getAsString();
        String textures = buttonObject.get("textures").getAsString();
        String command = buttonObject.get("command").getAsString();
        String tooltip = buttonObject.get("tooltip").getAsString();
        double posX = buttonObject.get("x").getAsDouble() * mc.getWindow().getGuiScaledWidth();
        double posY = buttonObject.get("y").getAsDouble() * mc.getWindow().getGuiScaledHeight();
        double scaleX = buttonObject.get("scaleX").getAsDouble();
        double scaleY = buttonObject.get("scaleY").getAsDouble();
        ItemStack stack = BuiltInRegistries.ITEM.getValue(Identifier.tryParse(model)).getDefaultInstance().copy();
        if (stack.is(Items.PLAYER_HEAD) && !textures.isEmpty()) {
            stack.set(DataComponents.PROFILE, Utils.toResolvableProfile(textures));
        }
        if (!itemId.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.put("id", StringTag.valueOf(itemId));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if (!customModel.isEmpty()) {
            stack.set(DataComponents.ITEM_MODEL, Identifier.tryParse(customModel));
        }
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, buttonObject.get("glint").getAsBoolean());
        double finalY = buttonObject.get("snapPosition").getAsBoolean() && intersectsGui(posX, posY, scaleX, scaleY, container)
                ? Utils.getClosest(posY, Math.floor(container.topPos - 20 * scaleX) - 1, container.topPos + container.imageHeight)
                : posY;
        return new InventoryButtonWidget(posX, finalY, scaleX, scaleY, stack, command, tooltip, buttonObject);
    }

    private static boolean intersectsGui(double posX, double posY, double scaleX, double scaleY, AbstractContainerScreen<?> container) {
        PositionedRectangle buttonRect = PositionedRectangle.of((int) posX, (int) posY, (int) (scaleX * 20), (int) (scaleY * 20));
        PositionedRectangle screenRect = PositionedRectangle.of(container.leftPos, container.topPos, container.imageWidth, container.imageHeight);
        return buttonRect.intersects(screenRect);
    }

    @Override
    public void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.buttonStyle.equals(InventoryButtonStyle.Vanilla)) {
            super.extractContents(graphics, mouseX, mouseY, a);
        } else if (this.buttonStyle.equals(InventoryButtonStyle.Color)) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), this.buttonColorBackground.getArgb());
            graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(),
                    this.isHovered()
                            ? this.buttonColorBorderHover.getArgb()
                            : this.buttonColorBorder.getArgb()
            );
        }
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
        if (this.unlockPosition) return;
        if (event.buttonInfo().button() == 1) {
            Screen previous = mc.screen;
            mc.setScreen(InventoryButtonSettings.of(this, previous));
        } else {
            super.onClick(event, doubleClick);
        }
    }

    @Override
    protected void onDrag(final @NonNull MouseButtonEvent event, final double dx, final double dy) {
        if (!this.unlockPosition) return;
        if (event.buttonInfo().button() == 0) {
            double windowX = mc.getWindow().getGuiScaledWidth();
            double windowY = mc.getWindow().getGuiScaledHeight();
            double buttonSizeX = 20 * this.buttonScaleX;
            double buttonSizeY = 20 * this.buttonScaleY;
            if (!event.buttonInfo().hasAltDown()) {
                double newX = Math.clamp(event.x() - buttonSizeX * 0.5, 0, windowX - buttonSizeX);
                double newY = Math.clamp(event.y() - buttonSizeY * 0.5, 0, windowY - buttonSizeY);
                if (event.buttonInfo().hasShiftDown()) {
                    int precision = InventoryButtons.gridPrecision.value();
                    newX = Math.min(newX - (newX % precision), newX);
                    newY = Math.min(newY - (newY % precision), newY);
                }
                this.setX((int) newX);
                this.setY((int) newY);
                this.buttonObject.addProperty("x", newX / windowX);
                this.buttonObject.addProperty("y", newY / windowY);
            } else {
                buttonSizeX = Math.clamp(event.x() - this.getX(), 0.25 * 20, windowX - buttonSizeX);
                buttonSizeY = Math.clamp(event.y() - this.getY(), 0.25 * 20, windowY - buttonSizeY);
                if (this.buttonObject.get("uniform").getAsBoolean()) {
                    double finalSize = Math.min(buttonSizeX, buttonSizeY);
                    this.setSize((int) finalSize, (int) finalSize);
                    this.buttonScaleX = finalSize / 20.0;
                    this.buttonScaleY = finalSize / 20.0;
                } else {
                    this.setSize((int) buttonSizeX, (int) buttonSizeY);
                    this.buttonScaleX = buttonSizeX / 20.0;
                    this.buttonScaleY = buttonSizeY / 20.0;
                }
                this.buttonObject.addProperty("scaleX", this.buttonScaleX);
                this.buttonObject.addProperty("scaleY", this.buttonScaleY);
            }
        }
    }
}
