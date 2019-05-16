package net.autogroup.pdf.info;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import net.autogroup.android.utils.LOG;
import net.autogroup.model.AppProfile;

import net.autogroup.pdf.info.R;

import net.autogroup.ui2.AppDB;

public class Android6 {

    public static final int MY_PERMISSIONS_REQUEST_WES = 1;

    public static boolean canWrite(Context c) {
        if(Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.checkSelfPermission(c, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void checkPermissions(final Activity a, boolean checkWhatIsNew) {

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(a, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setCancelable(false);
                builder.setMessage(R.string.you_need_grant_permission);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            a.finish();
                            final Intent i = new Intent();
                            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + a.getPackageName()));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            a.startActivity(i);
                        } catch (Exception e) {
                            LOG.e(e);
                        }

                    }
                });
                builder.show();

            } else {
                ActivityCompat.requestPermissions(a, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, MY_PERMISSIONS_REQUEST_WES);
            }
        } else {
            if(checkWhatIsNew) {
                AndroidWhatsNew.checkWhatsNew(a);
            }
            if(!AppDB.get().isOpen){
                AppDB.get().open(a, AppProfile.getCurrent(a));
            }
            FontExtractor.extractFonts(a);
        }
    }

    public static void onRequestPermissionsResult(Activity a, int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
        case MY_PERMISSIONS_REQUEST_WES: {
            // If request is cancelled, the result arrays are empty.
            AppProfile.save(a);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT <= 22) {// kill to restart 22 ????fa
                    android.os.Process.killProcess(android.os.Process.myPid());
                    return;
                }
                a.finish();
                a.getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                a.startActivity(a.getIntent());
            } else {
                a.finish();
                a.startActivity(a.getIntent());
            }
            return;
        }
        }
    }

}
