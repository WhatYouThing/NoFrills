package nofrills.config;

public record SettingTitle(SettingString title, SettingInt fadeIn, SettingInt stay, SettingInt fadeOut,
                           SettingBool vanilla) {

    public SettingTitle(String defaultTitle, String titleKey, int defaultFadeInTicks, String fadeInKey, int defaultStayTicks, String stayKey, int defaultFadeOutTicks, String fadeOutKey, boolean defaultVanilla, String vanillaKey, Feature instance) {
        this(
                new SettingString(defaultTitle, titleKey, instance),
                new SettingInt(defaultFadeInTicks, fadeInKey, instance),
                new SettingInt(defaultStayTicks, stayKey, instance),
                new SettingInt(defaultFadeOutTicks, fadeOutKey, instance),
                new SettingBool(defaultVanilla, vanillaKey, instance)
        );
    }
}
