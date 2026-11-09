package nofrills.hud.clickgui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import nofrills.hud.clickgui.components.PlainLabel;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import static nofrills.Main.mc;

public final class Category extends FlowLayout {
    public Consumer<Category> onClick = _ -> {
    };
    public int categoryWidth = 0;
    public List<Module> features;
    public PlainLabel label;
    public String title;

    public Category(String title, List<Module> children) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.margins(Insets.of(5, 0, 3, 0));
        this.title = title;
        this.label = new PlainLabel(Component.literal(title));
        this.label.horizontalTextAlignment(HorizontalAlignment.CENTER).verticalTextAlignment(VerticalAlignment.CENTER);
        this.features = new ArrayList<>(children);
        for (Module module : this.features) {
            this.categoryWidth = Math.max(this.categoryWidth, mc.font.width(module.activeText.getString()) + 10);
        }
        this.child(this.label);
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (this.isInBoundingBox(click.x() + this.x(), click.y() + this.y())) {
            this.onClick.accept(this);
            return true;
        }
        return false;
    }

    private boolean matchSearch(String text, String search) {
        return Utils.toLower(text).replace(" ", "").contains(Utils.toLower(search).replace(" ", ""));
    }

    public List<Module> build(String query) {
        return this.features.stream()
                .filter(f -> {
                    if (query.isBlank()) return true;
                    if (matchSearch(f.label.getText(), query) || matchSearch(f.label.getTooltip(), query)) {
                        return true;
                    }
                    if (f.options != null) {
                        for (FlowLayout setting : f.options.settings) {
                            for (UIComponent child : setting.children()) {
                                if (child instanceof PlainLabel plainLabel) {
                                    if (matchSearch(plainLabel.getText(), query) || matchSearch(plainLabel.getTooltip(), query)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparing(m -> m.activeText.getString()))
                .toList();
    }
}
