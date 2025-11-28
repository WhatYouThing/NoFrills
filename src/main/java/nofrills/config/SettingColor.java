package nofrills.config;

import nofrills.misc.RenderColor;

public class SettingColor extends SettingGeneric {
    private RenderColor color = RenderColor.white;

    public SettingColor(RenderColor defaultValue, String key, String parentKey) {
        super(defaultValue, key, parentKey);
    }

    public SettingColor(RenderColor defaultValue, String key, Feature instance) {
        this(defaultValue, key, instance.key());
    }

    public RenderColor value() {
        int current = this.get().getAsInt();
        if (this.color.argb != current) {
            this.color = RenderColor.fromArgb(current);
        }
        return this.color;
    }

    public RenderColor valueWithAlpha(float alpha) {
        return RenderColor.fromHex(this.value().hex, alpha);
    }
}