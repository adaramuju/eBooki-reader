package net.autogroup.tts;

import android.app.Activity;
import android.os.Bundle;

import net.autogroup.model.AppProfile;

public class TTSActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppProfile.init(this);
        TTSService.playLastBook();
        finish();

    }

}
