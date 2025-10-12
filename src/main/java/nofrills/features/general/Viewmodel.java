package nofrills.features.general;

import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingDouble;
import nofrills.config.SettingInt;

public class Viewmodel {
    public static final Feature instance = new Feature("viewmodel");

    public static final SettingBool noHaste = new SettingBool(false, "noHaste", instance.key());
    public static final SettingBool noEquip = new SettingBool(false, "noEquipAnim", instance.key());
    public static final SettingBool keepEmptyHand = new SettingBool(false, "keepEmptyHand", instance.key());
    public static final SettingBool noBowSwing = new SettingBool(false, "noBowSwing", instance.key());
    public static final SettingInt speed = new SettingInt(0, "speed", instance.key());
    public static final SettingDouble offsetX = new SettingDouble(0.0, "offsetX", instance.key());
    public static final SettingDouble offsetY = new SettingDouble(0.0, "offsetY", instance.key());
    public static final SettingDouble offsetZ = new SettingDouble(0.0, "offsetZ", instance.key());
    public static final SettingDouble scaleX = new SettingDouble(1.0, "scaleX", instance.key());
    public static final SettingDouble scaleY = new SettingDouble(1.0, "scaleY", instance.key());
    public static final SettingDouble scaleZ = new SettingDouble(1.0, "scaleZ", instance.key());
    public static final SettingDouble rotX = new SettingDouble(0.0, "rotX", instance.key());
    public static final SettingDouble rotY = new SettingDouble(0.0, "rotY", instance.key());
    public static final SettingDouble rotZ = new SettingDouble(0.0, "rotZ", instance.key());
    public static final SettingDouble swingX = new SettingDouble(1.0, "swingX", instance.key());
    public static final SettingDouble swingY = new SettingDouble(1.0, "swingY", instance.key());
    public static final SettingDouble swingZ = new SettingDouble(1.0, "swingZ", instance.key());
}
