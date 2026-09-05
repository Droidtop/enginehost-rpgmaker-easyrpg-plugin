/*
 * This file is part of the enginehost integration for EasyRPG Player.
 * EasyRPG Player is GPL-3.0-or-later; see the repository COPYING file.
 */
package org.easyrpg.player;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import org.easyrpg.player.player.EasyRpgPlayerActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Converts the enginehost contract to EasyRPG Player's existing CLI seam. */
public final class EngineHostRunActivity extends EasyRpgPlayerActivity {
    private boolean playerCreated;

    @Override protected void onCreate(Bundle state) {
        String context = getIntent().getStringExtra("dev.enginehost.runtime.ENGINE_CONTEXT");
        if (!"2000".equals(context) && !"2003".equals(context)) {
            fail("Unsupported RPG Maker engineContext: " + context);
            return;
        }

        File game;
        try {
            String path = getIntent().getStringExtra("dev.enginehost.runtime.PATH");
            if (path == null || !(game = new File(path).getCanonicalFile()).isDirectory()) {
                fail("enginehost did not provide a valid game folder");
                return;
            }
        } catch (IOException error) {
            fail("Unable to resolve the RPG Maker game folder");
            return;
        }

        JSONObject options;
        try {
            String raw = getIntent().getStringExtra("dev.enginehost.runtime.OPTIONS");
            options = raw == null || raw.isBlank() ? new JSONObject() : new JSONObject(raw);
        } catch (JSONException error) {
            fail("EasyRPG options must be a JSON object");
            return;
        }

        File privateRoot = new File(getFilesDir(), "enginehost");
        File configPath = new File(privateRoot, "config");
        File logFile = new File(privateRoot, "easyrpg-player.log");
        if (!configPath.mkdirs() && !configPath.isDirectory()) {
            fail("Unable to create EasyRPG configuration storage");
            return;
        }

        String savePath = getIntent().getStringExtra("dev.enginehost.runtime.SAVE_PATH");
        if (savePath == null || savePath.isBlank()) {
            // Compatibility with early host builds that had not added the
            // dedicated save extra yet. Current Enginehost always supplies it.
            savePath = options.optString("savePath", game.getPath());
        }
        ArrayList<String> args = new ArrayList<>();
        args.add("--project-path");
        args.add(game.getPath());
        args.add("--save-path");
        args.add(savePath);
        args.add("--config-path");
        args.add(configPath.getPath());
        args.add("--log-file");
        args.add(logFile.getPath());

        addValueOption(args, options, "encoding", "--encoding");
        addValueOption(args, options, "soundfont", "--soundfont");
        addValueOption(args, options, "fontPath", "--font-path");
        addValueOption(args, options, "language", "--language");
        addValueOption(args, options, "engine", "--engine");
        addValueOption(args, options, "scalingMode", "--scaling");
        addValueOption(args, options, "gameResolution", "--game-resolution");
        addValueOption(args, options, "font1", "--font1");
        addValueOption(args, options, "font2", "--font2");
        addNumberOption(args, options, "font1Size", "--font1-size");
        addNumberOption(args, options, "font2Size", "--font2-size");
        addNumberOption(args, options, "fpsLimit", "--fps-limit");
        addNumberOption(args, options, "musicVolume", "--music-volume");
        addNumberOption(args, options, "soundVolume", "--sound-volume");
        if (options.has("fullscreen")) {
            args.add(options.optBoolean("fullscreen", false) ? "--fullscreen" : "--window");
        }
        addSwitchOption(args, options, "vsync", "--vsync", "--no-vsync");
        if (options.optBoolean("testPlay", false)) args.add("--test-play");
        if (options.optBoolean("hideTitle", false)) args.add("--hide-title");

        // RTP search paths are one option, not several: EasyRPG splits the
        // single --rtp-path value on ':' or ';' itself
        // (src/filefinder_rtp.cpp:129-150), so a list is joined back into the
        // form the engine already understands.
        String rtpPaths = joinPaths(options.opt("rtpPaths"));
        if (!rtpPaths.isBlank()) {
            args.add("--rtp-path");
            args.add(rtpPaths);
        }

        addPatchOptions(args, options);

        getIntent().putExtra(EasyRpgPlayerActivity.TAG_SAVE_PATH, savePath);
        getIntent().putExtra(EasyRpgPlayerActivity.TAG_LOG_FILE, logFile.getPath());
        getIntent().putExtra(EasyRpgPlayerActivity.TAG_COMMAND_LINE, args.toArray(new String[0]));
        getIntent().putExtra(EasyRpgPlayerActivity.TAG_STANDALONE, true);
        super.onCreate(state);
        playerCreated = true;
    }

    /** Ignore the orientation callback SDL can emit before its own fields exist. */
    @Override public void onConfigurationChanged(Configuration newConfig) {
        if (playerCreated) super.onConfigurationChanged(newConfig);
    }

    /**
     * Per-game compatibility patches. A Japanese RPG Maker 2000/2003 game
     * built against DynRPG, Maniac Patch or one of the smaller community
     * patches simply does not run correctly without the matching flag, so
     * these are the difference between a playable game and a broken one.
     * Each maps to the engine's own switch pair in src/game_config_game.cpp.
     */
    private static void addPatchOptions(ArrayList<String> args, JSONObject options) {
        // Disables every patch, including the ones EasyRPG would otherwise
        // guess at, so a vanilla game is run exactly as the original engine
        // would (src/game_config_game.cpp:90).
        if (options.optBoolean("noPatch", false)) {
            args.add("--no-patch");
            return;
        }
        addSwitchOption(args, options, "patchEasyrpg", "--patch-easyrpg", "--no-patch-easyrpg");
        addSwitchOption(args, options, "patchDynrpg", "--patch-dynrpg", "--no-patch-dynrpg");
        addSwitchOption(args, options, "patchManiac", "--patch-maniac", "--no-patch-maniac");
        addSwitchOption(args, options, "patchCommonThis", "--patch-common-this", "--no-patch-common-this");
        addSwitchOption(args, options, "patchPicUnlock", "--patch-pic-unlock", "--no-patch-pic-unlock");
        addSwitchOption(args, options, "patchKeyPatch", "--patch-key-patch", "--no-patch-key-patch");
        addSwitchOption(args, options, "patchRpg2k3Cmds", "--patch-rpg2k3-cmds", "--no-patch-rpg2k3-cmds");
        // These two name a switch or variable rather than being on or off:
        // 0 turns the patch off (src/game_config_game.cpp:146, :158).
        addSwitchValueOption(args, options, "patchAntilagSwitch",
            "--patch-antilag-switch", "--no-patch-antilag-switch");
        addSwitchValueOption(args, options, "patchDirectMenu",
            "--patch-direct-menu", "--no-patch-direct-menu");
    }

    private static void addSwitchOption(
            ArrayList<String> args, JSONObject options, String key, String on, String off) {
        if (!options.has(key)) return;
        args.add(options.optBoolean(key, false) ? on : off);
    }

    private static void addSwitchValueOption(
            ArrayList<String> args, JSONObject options, String key, String on, String off) {
        if (!options.has(key)) return;
        int value = options.optInt(key, 0);
        if (value <= 0) {
            args.add(off);
            return;
        }
        args.add(on);
        args.add(Integer.toString(value));
    }

    private static void addNumberOption(
            ArrayList<String> args, JSONObject options, String key, String flag) {
        if (!options.has(key)) return;
        args.add(flag);
        args.add(Long.toString(options.optLong(key, 0L)));
    }

    /** Accepts either a list of folders or a single one already joined. */
    private static String joinPaths(Object value) {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            StringBuilder joined = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                String entry = array.optString(index, "");
                if (entry.isBlank()) continue;
                if (joined.length() > 0) joined.append(':');
                joined.append(entry);
            }
            return joined.toString();
        }
        return value == null ? "" : value.toString();
    }

    private static void addValueOption(
            ArrayList<String> args, JSONObject options, String key, String flag) {
        String value = options.optString(key, "");
        if (!value.isBlank()) {
            args.add(flag);
            args.add(value);
        }
    }

    private void fail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}
