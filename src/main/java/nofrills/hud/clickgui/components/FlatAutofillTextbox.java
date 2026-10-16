package nofrills.hud.clickgui.components;

import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.KeyEvent;

import java.util.List;
import java.util.function.Function;

public class FlatAutofillTextbox extends FlatTextbox {
    private final Function<String, List<String>> suggestionProvider;
    private List<String> currentSuggestion = List.of();
    private int currentIndex = 0;

    public FlatAutofillTextbox(Sizing horizontalSizing, Function<String, List<String>> suggestionProvider) {
        super(horizontalSizing);
        this.suggestionProvider = suggestionProvider;
        this.onChanged().subscribe(_ -> this.updateSuggestion());
    }

    @Override
    public boolean keyPressed(final KeyEvent event) {
        if (event.isUp() || event.isDown()) {
            this.currentIndex += event.isUp() ? -1 : 1;
            this.updateSuggestion();
            return true;
        }
        if (event.key() == InputConstants.KEY_TAB) {
            this.setValue(this.currentSuggestion.get(this.currentIndex));
            this.setSuggestion("");
            return true;
        }
        return super.keyPressed(event);
    }

    public void updateSuggestion() {
        this.currentSuggestion = !this.getValue().isBlank() ? this.suggestionProvider.apply(this.getValue()) : List.of();
        if (this.currentSuggestion.isEmpty()) {
            this.currentIndex = 0;
            this.setSuggestion("");
        } else {
            if (this.currentIndex < 0) {
                this.currentIndex = this.currentSuggestion.size() - 1;
            } else if (this.currentIndex > this.currentSuggestion.size() - 1) {
                this.currentIndex = 0;
            }
            this.setSuggestion(this.currentSuggestion.get(this.currentIndex).substring(this.getValue().length()));
        }
    }
}
