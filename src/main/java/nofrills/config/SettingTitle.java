package nofrills.config;

public record SettingTitle(SettingString title, SettingInt fadeIn, SettingInt stay, SettingInt fadeOut,
                           SettingBool vanilla) {

    public SettingTitle(String defaultTitle, int defaultFadeInTicks, int defaultStayTicks, int defaultFadeOutTicks, boolean defaultVanilla, String key, Feature instance) {
        this(
                new SettingString(defaultTitle, key + "Text", instance),
                new SettingInt(defaultFadeInTicks, key + "FadeInTicks", instance),
                new SettingInt(defaultStayTicks, key + "StayTicks", instance),
                new SettingInt(defaultFadeOutTicks, key + "FadeOutTicks", instance),
                new SettingBool(defaultVanilla, key + "Vanilla", instance)
        );
    }
}
