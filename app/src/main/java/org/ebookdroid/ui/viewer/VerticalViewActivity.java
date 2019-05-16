package org.ebookdroid.ui.viewer;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.Intents;
import net.autogroup.android.utils.Keyboards;
import net.autogroup.android.utils.LOG;
import net.autogroup.drive.GFile;
import net.autogroup.model.AppBook;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.ADS;
import net.autogroup.pdf.info.Android6;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.PasswordDialog;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.pdf.info.view.BrightnessHelper;
import net.autogroup.pdf.info.widget.RecentUpates;
import net.autogroup.pdf.info.wrapper.DocumentController;
import net.autogroup.pdf.search.view.CloseAppDialog;
import net.autogroup.sys.TempHolder;
import net.autogroup.tts.TTSNotification;
import net.autogroup.ui2.FileMetaCore;
import net.autogroup.ui2.MainTabs2;
import net.autogroup.ui2.MyContextWrapper;

import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.ui.viewer.viewers.PdfSurfaceView;
import org.emdev.ui.AbstractActionActivity;

public class VerticalViewActivity extends AbstractActionActivity<VerticalViewActivity, ViewerActivityController> {
    public static final DisplayMetrics DM = new DisplayMetrics();

    IView view;

    private FrameLayout frameLayout;

    /**
     * Instantiates a new base viewer activity.
     */
    public VerticalViewActivity() {
        super();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        LOG.d("VerticalViewActivity", "onNewIntent");
        if (TTSNotification.ACTION_TTS.equals(intent.getAction())) {
            return;
        }
        if (!intent.filterEquals(getIntent())) {
            finish();
            startActivity(intent);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @see org.emdev.ui.AbstractActionActivity#createController()
     */
    @Override
    protected ViewerActivityController createController() {
        return new ViewerActivityController(this);
    }

    private Handler handler;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        intetrstialTimeoutSec = ADS.FULL_SCREEN_TIMEOUT_SEC;
        DocumentController.doRotation(this);


        FileMetaCore.checkOrCreateMetaInfo(this);

        if (getIntent().getData() != null) {
            String path = getIntent().getData().getPath();
            final AppBook bs = SettingsManager.getBookSettings(path);
            // AppState.get().setNextScreen(bs.isNextScreen);
            if (bs != null) {
                // AppState.get().l = bs.l;
                AppState.get().autoScrollSpeed = bs.s;
                final boolean isTextFomat = ExtUtils.isTextFomat(bs.path);
                AppTemp.get().isCut = isTextFomat ? false : bs.sp;
                AppTemp.get().isCrop = bs.cp;
                AppTemp.get().isDouble = false;
                AppTemp.get().isDoubleCoverAlone = false;
                AppTemp.get().isLocked = bs.getLock(isTextFomat);
                TempHolder.get().pageDelta = bs.d;
                if (AppState.get().isCropPDF && !isTextFomat) {
                    AppTemp.get().isCrop = true;
                }
            }
            BookCSS.get().detectLang(path);
        }

        getController().beforeCreate(this);

        BrightnessHelper.applyBrigtness(this);

        if (AppState.get().isDayNotInvert) {
            setTheme(R.style.StyledIndicatorsWhite);
        } else {
            setTheme(R.style.StyledIndicatorsBlack);
        }
        super.onCreate(savedInstanceState);

        //FirebaseAnalytics.getInstance(this);

        if (PasswordDialog.isNeedPasswordDialog(this)) {
            return;
        }
        setContentView(R.layout.activity_vertical_view);

        if (!Android6.canWrite(this)) {
            Android6.checkPermissions(this, true);
            return;
        }


        getController().createWrapper(this);
        frameLayout = (FrameLayout) findViewById(R.id.documentView);

        view = new PdfSurfaceView(getController());

        frameLayout.addView(view.getView());

        getController().afterCreate(this);

        // ADS.activate(this, adView);

        handler = new Handler();

        getController().onBookLoaded(new Runnable() {

            @Override
            public void run() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        isInitPosistion = Dips.screenHeight() > Dips.screenWidth();
                        isInitOrientation = AppState.get().orientation;
                    }
                }, 1000);

            }
        });

    }

    @Override
    protected void attachBaseContext(Context context) {
        AppProfile.init(context);
        super.attachBaseContext(MyContextWrapper.wrap(context));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Android6.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentController.doRotation(this);

        if (AppState.get().isFullScreen) {
            Keyboards.hideNavigation(this);
        }
        getController().onResume();
        if (handler != null) {
            handler.removeCallbacks(closeRunnable);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int page = Math.round(getController().getDocumentModel().getPageCount() * Intents.getFloatAndClear(data,DocumentController.EXTRA_PERCENT));
            getController().getDocumentController().goToPage(page);
        }
    }

    boolean needToRestore = false;

    @Override
    protected void onPause() {
        super.onPause();
        LOG.d("onPause", this.getClass());
        getController().onPause();
        needToRestore = AppState.get().isAutoScroll;
        AppState.get().isAutoScroll = false;
        AppProfile.save(this);
        TempHolder.isSeaching = false;
        TempHolder.isActiveSpeedRead.set(false);

        if (handler != null) {
            handler.postDelayed(closeRunnable, AppState.APP_CLOSE_AUTOMATIC);
        }
        GFile.runSyncService(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Analytics.onStart(this);
        if (needToRestore) {
            AppState.get().isAutoScroll = true;
            getController().getListener().onAutoScroll();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Analytics.onStop(this);
        RecentUpates.updateAll(this);
    }

    Runnable closeRunnable = new Runnable() {

        @Override
        public void run() {
            LOG.d("Close App");
            getController().closeActivityFinal(null);
            MainTabs2.closeApp(VerticalViewActivity.this);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    Dialog rotatoinDialog;
    Boolean isInitPosistion;
    int isInitOrientation;

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        TempHolder.isActiveSpeedRead.set(false);
        if (isInitPosistion == null) {
            return;
        }

        final boolean currentPosistion = Dips.screenHeight() > Dips.screenWidth();

        if (ExtUtils.isTextFomat(getIntent()) && isInitOrientation == AppState.get().orientation) {

            if (rotatoinDialog != null) {
                try {
                    rotatoinDialog.dismiss();
                } catch (Exception e) {
                    LOG.e(e);
                }
            }

            if (isInitPosistion != currentPosistion) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setCancelable(false);
                dialog.setMessage(R.string.apply_a_new_screen_orientation_);
                dialog.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doConifChange();
                        isInitPosistion = currentPosistion;
                    }
                });
                rotatoinDialog = dialog.show();
                rotatoinDialog.getWindow().setLayout((int) (Dips.screenMinWH() * 0.8f), LayoutParams.WRAP_CONTENT);

            }
        } else {
            doConifChange();
        }

        isInitOrientation = AppState.get().orientation;
    }

    public void doConifChange() {
        try {
            if (!getController().getDocumentController().isInitialized()) {
                LOG.d("Skip onConfigurationChanged");
                return;
            }
        } catch (Exception e) {
            LOG.e(e);
            return;
        }

        AppProfile.save(this);

        if (ExtUtils.isTextFomat(getIntent())) {

            //float value = getController().getDocumentModel().getPercentRead();
            //Intents.putFloat(getIntent(),DocumentController.EXTRA_PERCENT, value);

            //LOG.d("READ PERCEnt", value);

            getController().closeActivityFinal(new Runnable() {

                @Override
                public void run() {
                    startActivity(getIntent());
                }
            });

        } else {
            getController().onConfigChanged();
            activateAds();
        }
    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getController().afterPostCreate();
    }

    @Override
    public boolean onGenericMotionEvent(final MotionEvent event) {
        if (Integer.parseInt(Build.VERSION.SDK) >= 12) {
            return GenericMotionEvent12.onGenericMotionEvent(event, this);
        }
        return false;
    }

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
        // Toast.makeText(this, "onKeyLongPress", Toast.LENGTH_SHORT).show();
        if (CloseAppDialog.checkLongPress(this, event)) {
            CloseAppDialog.showOnLongClickDialog(getController().getActivity(), null, getController().getListener());
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        // Toast.makeText(this, "onBackPressed", Toast.LENGTH_SHORT).show();

        if (isInterstialShown()) {
            getController().closeActivityFinal(null);
            return;
        }
        if (getController().getWrapperControlls().checkBack(new KeyEvent(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BACK))) {
            return;
        }

        if (AppState.get().isShowLongBackDialog) {
            CloseAppDialog.showOnLongClickDialog(getController().getActivity(), null, getController().getListener());
        } else {
            // showInterstial();
            getController().getListener().onCloseActivityAdnShowInterstial();
        }

    }

    private volatile boolean isMyKey = false;

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        LOG.d("onKeyUp");
        if (isMyKey) {
            return true;
        }

        if (getController().getWrapperControlls().dispatchKeyEventUp(event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        LOG.d("onKeyDown");
        isMyKey = false;
        int repeatCount = event.getRepeatCount();
        if (repeatCount >= 1 && repeatCount < DocumentController.REPEAT_SKIP_AMOUNT) {
            isMyKey = true;
            return true;
        }


        if (getController().getWrapperControlls().dispatchKeyEventDown(event)) {
            isMyKey = true;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFinishActivity() {
        getController().closeActivityFinal(null);

    }

}
