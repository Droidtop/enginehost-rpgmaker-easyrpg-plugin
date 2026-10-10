/*
 * This file is part of the Enginehost integration for EasyRPG Player.
 * EasyRPG Player is GPL-3.0-or-later; see the repository COPYING file.
 */
package dev.enginehost.plugin.easyrpg;

import dev.enginehost.api.EnginePluginSession;
import dev.enginehost.libretro.LibretroPlugin;
import java.util.ArrayList;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * RPG Maker 2000/2003 games through EasyRPG Player's own libretro core, in
 * Enginehost's sandbox (LibretroPlugin, plugin-native/libretro/). The Player
 * gets the command line it always had, built from the game's options exactly
 * as the earlier Activity built it, through the core's
 * easyrpg_enginehost_arguments variable (src/platform/libretro/ui.cpp).
 *
 * <p>The game folder is the content; saves stay beside the game, where RPG
 * Maker 2000/2003 and the Player keep them (SaveNN.lsd), unless the savePath
 * option says otherwise; the bundle declares writesGameFolder for that.
 */
public final class EasyRpgPlugin extends LibretroPlugin {
    @Override protected boolean supports(String engineContext) {
        return "2000".equals(engineContext) || "2003".equals(engineContext);
    }

    @Override protected String contentPath(EnginePluginSession session) {
        return session.gamePath();
    }

    @Override protected Map<String, String> options(EnginePluginSession session) {
        JSONObject options;
        try {
            String raw = session.optionsJson();
            options = raw == null || raw.isBlank() ? new JSONObject() : new JSONObject(raw);
        } catch (JSONException error) {
            options = new JSONObject();
        }
        ArrayList<String> args = new ArrayList<>();
        args.add("--save-path");
        args.add(options.optString("savePath", session.gamePath()));
        addValue(args, options, "encoding", "--encoding");
        addValue(args, options, "soundfont", "--soundfont");
        addValue(args, options, "fontPath", "--font-path");
        addValue(args, options, "language", "--language");
        addValue(args, options, "engine", "--engine");
        addValue(args, options, "gameResolution", "--game-resolution");
        addValue(args, options, "font1", "--font1");
        addValue(args, options, "font2", "--font2");
        addNumber(args, options, "font1Size", "--font1-size");
        addNumber(args, options, "font2Size", "--font2-size");
        addNumber(args, options, "fpsLimit", "--fps-limit");
        addNumber(args, options, "musicVolume", "--music-volume");
        addNumber(args, options, "soundVolume", "--sound-volume");
        if (options.optBoolean("testPlay", false)) args.add("--test-play");
        if (options.optBoolean("hideTitle", false)) args.add("--hide-title");
        // One option, which EasyRPG splits on ':' itself (src/filefinder_rtp.cpp).
        String rtpPaths = joinPaths(options.opt("rtpPaths"));
        if (!rtpPaths.isBlank()) {
            args.add("--rtp-path");
            args.add(rtpPaths);
        }
        addPatches(args, options);
        return Map.of("easyrpg_enginehost_arguments", String.join("\n", args));
    }

    /**
     * The host's EasyRPG actions onto the RetroPad buttons the core's
     * default mapping reads (src/platform/libretro/input_buttons.cpp).
     */
    @Override protected int joypadButton(String action) {
        switch (action) {
            case "easyrpg_up": return JOYPAD_UP;
            case "easyrpg_down": return JOYPAD_DOWN;
            case "easyrpg_left": return JOYPAD_LEFT;
            case "easyrpg_right": return JOYPAD_RIGHT;
            case "easyrpg_decision": return JOYPAD_A;
            case "easyrpg_cancel": return JOYPAD_B;
            case "easyrpg_shift": return JOYPAD_Y;
            case "easyrpg_fast_forward_a": case "easyrpg_fast_forward_b": return JOYPAD_R2;
            case "easyrpg_settings_menu": return JOYPAD_START;
            case "easyrpg_reset": return JOYPAD_SELECT;
            case "easyrpg_debug_abort_event": return JOYPAD_L;
            case "easyrpg_debug_save": return JOYPAD_R;
            case "easyrpg_n0": return JOYPAD_L3;
            case "easyrpg_n5": return JOYPAD_R3;
            default: return super.joypadButton(action);
        }
    }

    private static void addPatches(ArrayList<String> args, JSONObject options) {
        if (options.optBoolean("noPatch", false)) {
            args.add("--no-patch");
            return;
        }
        addSwitch(args, options, "patchEasyrpg", "--patch-easyrpg", "--no-patch-easyrpg");
        addSwitch(args, options, "patchDynrpg", "--patch-dynrpg", "--no-patch-dynrpg");
        addSwitch(args, options, "patchManiac", "--patch-maniac", "--no-patch-maniac");
        addSwitch(args, options, "patchCommonThis", "--patch-common-this", "--no-patch-common-this");
        addSwitch(args, options, "patchPicUnlock", "--patch-pic-unlock", "--no-patch-pic-unlock");
        addSwitch(args, options, "patchKeyPatch", "--patch-key-patch", "--no-patch-key-patch");
        addSwitch(args, options, "patchRpg2k3Cmds", "--patch-rpg2k3-cmds", "--no-patch-rpg2k3-cmds");
        addSwitchValue(args, options, "patchAntilagSwitch", "--patch-antilag-switch", "--no-patch-antilag-switch");
        addSwitchValue(args, options, "patchDirectMenu", "--patch-direct-menu", "--no-patch-direct-menu");
    }

    private static void addSwitch(ArrayList<String> args, JSONObject options, String key, String on, String off) {
        if (options.has(key)) args.add(options.optBoolean(key, false) ? on : off);
    }

    private static void addSwitchValue(ArrayList<String> args, JSONObject options, String key, String on, String off) {
        if (!options.has(key)) return;
        int value = options.optInt(key, 0);
        if (value <= 0) {
            args.add(off);
            return;
        }
        args.add(on);
        args.add(Integer.toString(value));
    }

    private static void addNumber(ArrayList<String> args, JSONObject options, String key, String flag) {
        if (!options.has(key)) return;
        args.add(flag);
        args.add(Long.toString(options.optLong(key, 0L)));
    }

    private static void addValue(ArrayList<String> args, JSONObject options, String key, String flag) {
        String value = options.optString(key, "");
        if (!value.isBlank()) {
            args.add(flag);
            args.add(value);
        }
    }

    private static String joinPaths(Object value) {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            StringBuilder joined = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                String entry = array.optString(i, "");
                if (entry.isBlank()) continue;
                if (joined.length() > 0) joined.append(':');
                joined.append(entry);
            }
            return joined.toString();
        }
        return value == null ? "" : value.toString();
    }
}
