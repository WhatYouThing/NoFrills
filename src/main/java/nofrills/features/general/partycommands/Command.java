package nofrills.features.general.partycommands;

import com.google.common.collect.Sets;
import nofrills.config.SettingBool;
import nofrills.config.SettingEnum;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Command {
    public final SettingEnum<PartyCommands.Behavior> behavior;
    public final SettingBool toggle;
    public final HashSet<String> names;

    public Command(SettingEnum<PartyCommands.Behavior> behavior, List<String> names) {
        this.behavior = behavior;
        this.toggle = null;
        this.names = Sets.newHashSet(names);
    }

    public Command(SettingEnum<PartyCommands.Behavior> behavior, String... names) {
        this(behavior, Arrays.asList(names));
    }

    public Command(SettingBool toggle, String... names) {
        this.behavior = null;
        this.toggle = toggle;
        this.names = Sets.newHashSet(names);
    }

    public boolean isActive() {
        return this.toggle != null ? this.toggle.value() : !this.behavior.value().equals(PartyCommands.Behavior.Disabled);
    }

    public Optional<String> getTarget(String msg) {
        String[] parts = msg.split(" ");
        if (parts.length > 1) {
            return Optional.of(parts[1]);
        }
        return Optional.empty();
    }

    public boolean process(String author, String content, boolean whitelisted) {
        if (this.behavior == null) {
            this.onAutomatic(author, content);
            return true;
        }
        PartyCommands.Behavior value = this.behavior.value();
        if (whitelisted || value.equals(PartyCommands.Behavior.Automatic)) {
            this.onAutomatic(author, content);
            return true;
        }
        if (value.equals(PartyCommands.Behavior.Manual)) {
            this.onManual(author, content);
            return true;
        }
        return false;
    }

    public void onAutomatic(String author, String msg) {
    }

    public void onManual(String author, String msg) {
    }
}