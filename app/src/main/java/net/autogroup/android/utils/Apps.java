package net.autogroup.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings.Secure;
import android.widget.Toast;

import net.autogroup.pdf.info.R;

public class Apps {

    public static int ANDROID_VERSION = Build.VERSION.SDK_INT;

    public static String getAndoroidID(Context c) {
        return Secure.getString(c.getContentResolver(), Secure.ANDROID_ID);
    }

    public static boolean isPackageInstalled(String packageName, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            LOG.e(e);
            return false;
        }
    }

    public static Drawable getApplicationImage(Context context) {
        return context.getPackageManager().getApplicationIcon(context.getApplicationInfo());
    }

    public static String getApplicationName(Context context) {
        try {
            return (String) context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
        } catch (Exception e) {
            LOG.e(e);
        }
        return "";
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            LOG.e(e);
        }
        return "";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (Exception e) {
            LOG.e(e);
        }
        return -1;
    }

    public static int getTargetSdkVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.applicationInfo.targetSdkVersion;
        } catch (Exception e) {
            LOG.e(e);
        }
        return -1;
    }

    public static String getPackageName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.packageName;
        } catch (Exception e) {
            LOG.e(e);
        }
        return "";
    }

    public static void showDesctop(Context c) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(startMain);
    }

    public static void onCrashEmail(Context c, String msg, String title) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String string = c.getResources().getString(R.string.my_email).replace("<u>", "").replace("</u>", "");
        final String aEmailList[] = {string};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getApplicationName(c) + " " + Apps.getVersionName(c) + " Crash report");
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);

        try {
            c.startActivity(Intent.createChooser(emailIntent, title));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(c, R.string.there_are_no_email_applications_installed_, Toast.LENGTH_SHORT).show();
        }
    }


    public static String getMetaData(Context context, String name) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString(name);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LOG.e(e);
        }
        new RuntimeException("can't find meta-data:" + name);
        return null;
    }

    public static boolean isWifiEnabled(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }
        return false;
    }


}
