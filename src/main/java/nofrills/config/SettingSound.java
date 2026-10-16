package nofrills.config;

public record SettingSound(SettingString sound, SettingDouble volume, SettingDouble pitch) {

    public SettingSound(String defaultSound, String soundKey, double defaultVolume, String volumeKey, double defaultPitch, String pitchKey, Feature instance) {
        this(
                new SettingString(defaultSound, soundKey, instance),
                new SettingDouble(defaultVolume, volumeKey, instance),
                new SettingDouble(defaultPitch, pitchKey, instance)
        );
    }
}
