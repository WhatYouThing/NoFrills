package nofrills.hud.clickgui.settings;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import nofrills.hud.InnerSettingsScreen;
import nofrills.hud.clickgui.SettingsScreen;
import nofrills.hud.clickgui.components.FlatAutofillTextbox;
import nofrills.hud.clickgui.components.FlatTextbox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class TextEntryList extends InnerSettings {

    public TextEntryList(String name, Supplier<List<String>> currentValue, String tooltip, Consumer<List<String>> updateCallback, @Nullable Function<String, List<String>> suggestionProvider) {
        AtomicReference<Supplier<List<FlowLayout>>> supplier = new AtomicReference<>();
        supplier.set(() -> {
            List<FlowLayout> list = new ArrayList<>();
            list.add(new BigButton("Add New Entry", _ -> {
                List<String> newValue = new ArrayList<>(currentValue.get());
                newValue.add("");
                updateCallback.accept(newValue);
                InnerSettingsScreen.rebuild(supplier.get().get());
            }));
            List<String> value = currentValue.get();
            for (AtomicInteger i = new AtomicInteger(0); i.get() < value.size(); i.incrementAndGet()) {
                int index = i.get();
                FlowLayout layout = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
                layout.padding(Insets.of(5)).horizontalAlignment(HorizontalAlignment.LEFT);
                FlatTextbox text = suggestionProvider != null
                        ? new FlatAutofillTextbox(Sizing.fixed(240), suggestionProvider)
                        : new FlatTextbox(Sizing.fixed(240));
                text.onChanged().subscribe(t -> {
                    List<String> newValue = new ArrayList<>(currentValue.get());
                    newValue.set(index, t);
                    updateCallback.accept(newValue);
                });
                text.text(value.get(index));
                ButtonComponent button = UIComponents.button(Component.literal("Delete").withColor(0xffffff), _ -> {
                    List<String> newValue = new ArrayList<>(currentValue.get());
                    newValue.remove(index);
                    updateCallback.accept(newValue);
                    InnerSettingsScreen.rebuild(supplier.get().get());
                });
                button.renderer(SettingsScreen.buttonRendererWhite).positioning(Positioning.relative(100, 0));
                layout.child(text);
                layout.child(button);
                list.add(layout);
            }
            return list;
        });
        super(name, supplier.get(), "Edit List", tooltip);
    }

    public TextEntryList(String name, Supplier<List<String>> currentValue, String tooltip, Consumer<List<String>> updateCallback) {
        this(name, currentValue, tooltip, updateCallback, null);
    }
}
