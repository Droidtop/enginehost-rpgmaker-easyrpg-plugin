/*
 * This file is part of the enginehost integration for EasyRPG Player.
 * EasyRPG Player is GPL-3.0-or-later; see the repository COPYING file.
 */
package org.easyrpg.player;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import org.easyrpg.player.player.EasyRpgPlayerActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Converts the enginehost contract to EasyRPG Player's existing CLI seam. */
public final class EngineHostRunActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);

        String context = getIntent().getStringExtra("engineContext");
        if (!"2000".equals(context) && !"2003".equals(context)) {
            fail("Unsupported RPG Maker engineContext: " + context);
            return;
        }

        File game;
        try {
            String path = getIntent().getStringExtra("path");
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
            String raw = getIntent().getStringExtra("options");
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

        String savePath = options.optString("savePath", game.getPath());
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
        addValueOption(args, options, "rtpPath", "--rtp-path");
        addValueOption(args, options, "soundfont", "--soundfont");
        addValueOption(args, options, "fontPath", "--font-path");
        if (options.optBoolean("testPlay", false)) args.add("--test-play");
        if (options.optBoolean("hideTitle", false)) args.add("--hide-title");

        Intent player = new Intent(this, EasyRpgPlayerActivity.class);
        player.putExtra(EasyRpgPlayerActivity.TAG_SAVE_PATH, savePath);
        player.putExtra(EasyRpgPlayerActivity.TAG_LOG_FILE, logFile.getPath());
        player.putExtra(EasyRpgPlayerActivity.TAG_COMMAND_LINE, args.toArray(new String[0]));
        player.putExtra(EasyRpgPlayerActivity.TAG_STANDALONE, true);
        startActivity(player);
        finish();
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
