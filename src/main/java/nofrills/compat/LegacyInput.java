package nofrills.compat;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLMouse;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.HashMap;

/** Keeps the historical integer config format while using real SDL input at runtime. */
public final class LegacyInput {
    private LegacyInput() {}
    public static final int KEY_1 = 49;
    public static final int KEY_9 = 57;
    public static final int KEY_ESCAPE = 256;
    public static final int KEY_LEFT = 263;
    public static final int KEY_PAGE_DOWN = 267;
    public static final int KEY_PAGE_UP = 266;
    public static final int KEY_RIGHT = 262;
    public static final int KEY_SPACE = 32;
    public static final int KEY_UNKNOWN = -1;
    public static final int MOD_ALT = 4;
    public static final int MOD_CONTROL = 2;
    public static final int MOD_SHIFT = 1;
    public static final int MOUSE_BUTTON_1 = 0;
    public static final int MOUSE_BUTTON_2 = 1;
    public static final int MOUSE_BUTTON_3 = 2;
    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_MIDDLE = 2;
    public static final int MOUSE_BUTTON_RIGHT = 1;
    public static final int PRESS = 1;
    public static final int RELEASE = 0;
    public static final int REPEAT = 2;
    private static final int EXTENDED_KEY_BASE = 10000;
    private static final Map<Integer, Integer> KEYBOARD = Map.ofEntries(
        Map.entry(-1, 0),
        Map.entry(48, 39),
        Map.entry(49, 30),
        Map.entry(50, 31),
        Map.entry(51, 32),
        Map.entry(52, 33),
        Map.entry(53, 34),
        Map.entry(54, 35),
        Map.entry(55, 36),
        Map.entry(56, 37),
        Map.entry(57, 38),
        Map.entry(65, 4),
        Map.entry(66, 5),
        Map.entry(67, 6),
        Map.entry(68, 7),
        Map.entry(69, 8),
        Map.entry(70, 9),
        Map.entry(71, 10),
        Map.entry(72, 11),
        Map.entry(73, 12),
        Map.entry(74, 13),
        Map.entry(75, 14),
        Map.entry(76, 15),
        Map.entry(77, 16),
        Map.entry(78, 17),
        Map.entry(79, 18),
        Map.entry(80, 19),
        Map.entry(81, 20),
        Map.entry(82, 21),
        Map.entry(83, 22),
        Map.entry(84, 23),
        Map.entry(85, 24),
        Map.entry(86, 25),
        Map.entry(87, 26),
        Map.entry(88, 27),
        Map.entry(89, 28),
        Map.entry(90, 29),
        Map.entry(290, 58),
        Map.entry(291, 59),
        Map.entry(292, 60),
        Map.entry(293, 61),
        Map.entry(294, 62),
        Map.entry(295, 63),
        Map.entry(296, 64),
        Map.entry(297, 65),
        Map.entry(298, 66),
        Map.entry(299, 67),
        Map.entry(300, 68),
        Map.entry(301, 69),
        Map.entry(302, 104),
        Map.entry(303, 105),
        Map.entry(304, 106),
        Map.entry(305, 107),
        Map.entry(306, 108),
        Map.entry(307, 109),
        Map.entry(308, 110),
        Map.entry(309, 111),
        Map.entry(310, 112),
        Map.entry(311, 113),
        Map.entry(312, 114),
        Map.entry(313, 115),
        Map.entry(282, 83),
        Map.entry(320, 98),
        Map.entry(321, 89),
        Map.entry(322, 90),
        Map.entry(323, 91),
        Map.entry(324, 92),
        Map.entry(325, 93),
        Map.entry(326, 94),
        Map.entry(327, 95),
        Map.entry(328, 96),
        Map.entry(329, 97),
        Map.entry(334, 87),
        // GLFW keypad decimal is the physical keypad period; SDL KP_DECIMAL is a distinct key.
        Map.entry(330, org.lwjgl.sdl.SDLScancode.SDL_SCANCODE_KP_PERIOD),
        Map.entry(335, 88),
        Map.entry(336, 103),
        Map.entry(332, 85),
        Map.entry(331, 84),
        Map.entry(333, 86),
        Map.entry(264, 81),
        Map.entry(263, 80),
        Map.entry(262, 79),
        Map.entry(265, 82),
        Map.entry(39, 52),
        Map.entry(92, 49),
        Map.entry(44, 54),
        Map.entry(61, 46),
        Map.entry(96, 53),
        Map.entry(91, 47),
        Map.entry(45, 45),
        Map.entry(46, 55),
        Map.entry(93, 48),
        Map.entry(59, 51),
        Map.entry(47, 56),
        Map.entry(32, 44),
        Map.entry(258, 43),
        Map.entry(342, 226),
        Map.entry(341, 224),
        Map.entry(340, 225),
        Map.entry(343, 227),
        Map.entry(346, 230),
        Map.entry(345, 228),
        Map.entry(344, 229),
        Map.entry(347, 231),
        Map.entry(257, 40),
        Map.entry(256, 41),
        Map.entry(259, 42),
        Map.entry(261, 76),
        Map.entry(269, 77),
        Map.entry(268, 74),
        Map.entry(260, 73),
        Map.entry(267, 78),
        Map.entry(266, 75),
        Map.entry(280, 57),
        Map.entry(284, 72),
        Map.entry(281, 71),
        Map.entry(348, 118),
        Map.entry(283, 70),
        Map.entry(161, 100),
        Map.entry(162, 50)
    );
    private static final Map<Integer, Integer> REVERSE_KEYBOARD = new HashMap<>();
    static { KEYBOARD.forEach((legacy, sdl) -> REVERSE_KEYBOARD.put(sdl, legacy)); }

    public static int toKeyboard(int legacy) {
        if (legacy >= EXTENDED_KEY_BASE) return legacy - EXTENDED_KEY_BASE;
        return KEYBOARD.getOrDefault(legacy, InputConstants.UNKNOWN.getValue());
    }

    public static int fromKeyboard(int sdl) {
        return REVERSE_KEYBOARD.getOrDefault(sdl, EXTENDED_KEY_BASE + sdl);
    }

    public static int toMouseButton(int legacy) {
        return switch (legacy) { case 0 -> 1; case 1 -> 3; case 2 -> 2; default -> legacy + 1; };
    }

    public static int fromMouseButton(int sdl) {
        return switch (sdl) { case 1 -> 0; case 3 -> 1; case 2 -> 2; default -> sdl - 1; };
    }

    public static int fromModifiers(int sdl) {
        int legacy = 0;
        if ((sdl & InputConstants.MOD_SHIFT) != 0) legacy |= 1;
        if ((sdl & InputConstants.MOD_CONTROL) != 0) legacy |= 2;
        if ((sdl & InputConstants.MOD_ALT) != 0) legacy |= 4;
        if ((sdl & InputConstants.MOD_SUPER) != 0) legacy |= 8;
        if ((sdl & InputConstants.MOD_NUM_LOCK) != 0) legacy |= 16;
        if ((sdl & InputConstants.MOD_CAPS_LOCK) != 0) legacy |= 32;
        return legacy;
    }

    public static int fromAction(int action) {
        return action == InputConstants.REPEAT ? 2 : action;
    }

    public static boolean isMouse(int legacy) {
        return legacy >= 0 && legacy < 32 && !KEYBOARD.containsKey(legacy);
    }

    public static InputConstants.Key asInputConstant(int legacy) {
        if (legacy == -1) return InputConstants.UNKNOWN;
        return isMouse(legacy) ? InputConstants.Type.MOUSE.getOrCreate(toMouseButton(legacy))
                : InputConstants.Type.KEYBOARD.getOrCreate(toKeyboard(legacy));
    }

    public static boolean isDown(int legacy) {
        if (legacy == -1) return false;
        if (isMouse(legacy)) {
            int buttons = SDLMouse.SDL_GetMouseState((FloatBuffer) null, (FloatBuffer) null);
            return (buttons & (1 << (toMouseButton(legacy) - 1))) != 0;
        }
        return InputConstants.isKeyDown(toKeyboard(legacy));
    }
}
