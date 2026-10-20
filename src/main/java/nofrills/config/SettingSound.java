package nofrills.config;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public record SettingSound(SettingString sound, SettingDouble volume, SettingDouble pitch) {

    public SettingSound(String defaultSound, double defaultVolume, double defaultPitch, String key, Feature instance) {
        this(
                new SettingString(defaultSound, key + "Identifier", instance),
                new SettingDouble(defaultVolume, key + "Volume", instance),
                new SettingDouble(defaultPitch, key + "Pitch", instance)
        );
    }

    public SettingSound(SoundEvent defaultSound, double defaultVolume, double defaultPitch, String key, Feature instance) {
        this(defaultSound.location().toString(), defaultVolume, defaultPitch, key, instance);
    }

    public SettingSound(Holder.Reference<SoundEvent> defaultSound, double defaultVolume, double defaultPitch, String key, Feature instance) {
        this(defaultSound.value(), defaultVolume, defaultPitch, key, instance);
    }
}
