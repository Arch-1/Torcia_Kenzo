package com.torcia.it.torcia;

import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class torcia extends TileService {

    private static final String PATH_FLAG = "WhichPath";
    private static final String SERVICE_STATUS_FLAG = "serviceStatus";
    private static final String PREFERENCES_KEY = "com.google.android_quick_settings";
    public final String FIRST_PATH = "~/sys/class/leds/led:torch_0/brightness";
    public final String SECOND_PATH = "~/sys/class/leds/flashlight/brightness";

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        try {
            Runtime.getRuntime().exec(new String[]{"su"});
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Errore durante la richiesta dei permessi root", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        updateTile();
    }

    private void updateTile() {

        Tile tile = this.getQsTile();
        boolean isActive = getTileStatus();
        String path = getPath();
        Icon newIcon;
        int newState = Tile.STATE_ACTIVE;
        if (isActive) {
            try {
                Runtime.getRuntime().exec(new String[]{"su","-c","echo 100 > " + path});
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Errore durante l'esecuzione", Toast.LENGTH_SHORT).show();
            }
            newIcon = Icon.createWithResource(getApplicationContext(), R.drawable.ic_signal_flashlight_disable);
        } else {
            try {
                Runtime.getRuntime().exec(new String[]{"su","-c","echo 0 > " + path});
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Errore durante l'esecuzione", Toast.LENGTH_SHORT).show();
            }
            newIcon = Icon.createWithResource(getApplicationContext(), R.drawable.ic_signal_flashlight_enable);
        }
        tile.setIcon(newIcon);
        tile.setState(newState);
        tile.updateTile();
    }

    private boolean getTileStatus() {

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFERENCES_KEY, MODE_PRIVATE);
        boolean isActive = prefs.getBoolean(SERVICE_STATUS_FLAG, false);
        isActive = !isActive;
        prefs.edit().putBoolean(SERVICE_STATUS_FLAG, isActive).apply();
        return isActive;
    }

    private String getPath() {

        String path = "";
        SharedPreferences Path = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = Path.edit();
        File file = new File(FIRST_PATH);
        if(file.exists()) {
           path = FIRST_PATH;
        } else {
            path = SECOND_PATH;
        }
        editor.putString(PATH_FLAG, path).apply();
        return path;
    }
}
