package nofrills.hud.clickgui.components;

import io.wispforest.owo.ui.core.Sizing;

import java.util.function.Function;

public class ValidatorFlatTextbox extends FlatTextbox {
    private Function<ValidatorFlatTextbox, Boolean> validator;
    private boolean valid;

    public ValidatorFlatTextbox(Sizing horizontalSizing) {
        super(horizontalSizing);
        this.setupValidationListener();
    }

    private void setupValidationListener() {
        this.onChanged().subscribe(newValue -> {
            this.validate();
        });
    }

    public void setValidator(Function<ValidatorFlatTextbox, Boolean> validator) {
        this.validator = validator;
        if (this.validator != null) {
            this.validate();
        }
    }

    private void validate() {
        if (this.validator != null) {
            this.valid = this.validator.apply(this);
        } else {
            this.valid = false;
        }
    }

    public boolean isValid() {
        return this.valid;
    }
}
