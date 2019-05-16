package net.autogroup.ui2;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.autogroup.pdf.info.MyADSProvider;
import net.autogroup.tts.TTSEngine;
import net.autogroup.tts.TTSNotification;

import fi.iki.elonen.SampleServer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public abstract class AdsFragmentActivity extends FragmentActivity {

    private final MyADSProvider myAds = new MyADSProvider();

    public abstract void onFinishActivity();

    protected int intetrstialTimeoutSec = 0;

    SampleServer sampleServer;

    Runnable onFinish = new Runnable() {

        @Override
        public void run() {
            onFinishActivity();
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        myAds.intetrstialTimeout = intetrstialTimeoutSec;
        myAds.createHandler();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        activateAds();

        // try {
        // sampleServer = new SampleServer(this);
        // } catch (IOException e) {
        // LOG.e(e);
        // }
    }

    public void activateAds() {
        myAds.activate(this, onFinish);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myAds.resume();
        // sampleServer.run();
    }

    @Override
    protected void onPause() {
        myAds.pause();
        super.onPause();
        // sampleServer.stop();
    }

    public void adsPause() {
        myAds.pause();
    }

    @Override
    protected void onDestroy() {
        myAds.destroy();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // myAds.activate(this, onFinish);
    }

    public void showInterstial() {
        TTSNotification.hideNotification();
        TTSEngine.get().shutdown();
        adsPause();
        if (myAds.showInterstial()) {
            // ok
        } else {
            onFinish.run();
        }

    }

    public boolean isInterstialShown() {
        return false;
    }


}
