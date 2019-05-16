package org.ebookdroid;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;

import com.artifex.mupdf.fitz.StructuredText;
import net.autogroup.android.utils.Apps;
import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.LOG;
import net.autogroup.ext.CacheZipUtils;
import net.autogroup.model.AppProfile;
import net.autogroup.pdf.info.ADS;
import net.autogroup.pdf.info.AppsConfig;
import net.autogroup.pdf.info.BuildConfig;
import net.autogroup.pdf.info.Clouds;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.tts.TTSNotification;
import net.autogroup.ui2.AppDB;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;

import org.ebookdroid.common.bitmaps.BitmapManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class EBookiApp extends MultiDexApplication {

    static {
        System.loadLibrary("mypdf");
        System.loadLibrary("mobi");
        System.loadLibrary("antiword");
    }

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();


        MobileAds.initialize(this, Apps.getMetaData(this, "com.google.android.gms.ads.APPLICATION_ID"));

        context = getApplicationContext();

        LOG.isEnable = BuildConfig.LOG;

        AppProfile.init(this);

        if (AppsConfig.MUPDF_VERSION == AppsConfig.MUPDF_1_12) {
            int initNative = StructuredText.initNative();
            LOG.d("initNative", initNative);
        }

        TTSNotification.initChannels(this);
        Dips.init(this);
        AppDB.get().open(this, AppProfile.getCurrent(this));

        CacheZipUtils.init(this);
        ExtUtils.init(this);
        IMG.init(this);

        Clouds.get().init(this);

        LOG.d("Build", "Build.MANUFACTURER", Build.MANUFACTURER);
        LOG.d("Build", "Build.PRODUCT", Build.PRODUCT);
        LOG.d("Build", "Build.DEVICE", Build.DEVICE);
        LOG.d("Build", "Build.BRAND", Build.BRAND);
        LOG.d("Build", "Build.MODEL", Build.MODEL);

        LOG.d("Build", "Build.screenWidth", Dips.screenWidthDP(), Dips.screenWidth());

        LOG.d("Build.Context", "Context.getFilesDir()", getFilesDir());
        LOG.d("Build.Context", "Context.getCacheDir()", getCacheDir());
        LOG.d("Build.Context", "Context.getExternalCacheDir", getExternalCacheDir());
        LOG.d("Build.Context", "Context.getExternalFilesDir(null)", getExternalFilesDir(null));
        LOG.d("Build.Context", "Environment.getExternalStorageDirectory()", Environment.getExternalStorageDirectory());
        LOG.d("Build.Height", Dips.screenHeight());

        try {
            if (LOG.isEnable) {
                String myID = ADS.getByTestID(this);
                ADS.adRequest = new AdRequest.Builder()//
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)//
                        .addTestDevice(myID)//
                        .build();//
            }
        } catch (Exception e) {
            LOG.e(e);
        }


        if (BuildConfig.IS_BETA && BuildConfig.IS_BETA_SEND_REPORTS) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, final Throwable e) {
                    LOG.e(e);
                    e.printStackTrace();
                    try {

                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        String log = errors.toString();
                        log = log + "/n";
                        log = log + Build.MANUFACTURER + "/n";
                        log = log + Build.PRODUCT + "/n";
                        log = log + Build.DEVICE + "/n";
                        log = log + Build.BRAND + "/n";
                        log = log + Build.BRAND + "/n";
                        log = log + Build.MODEL + "/n";
                        log = log + Build.VERSION.SDK_INT + "/n";
                        Apps.onCrashEmail(context, log,  context.getString(R.string.application_error_please_send_this_report_by_emial));

                        System.exit(1);

                    } catch (Exception e1) {
                        LOG.e(e1);
                    }
                }
            });
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        LOG.d("AppState save onLowMemory");
        IMG.clearMemoryCache();
        BitmapManager.clear("on Low Memory: ");
        TintUtil.clean();
    }

}
