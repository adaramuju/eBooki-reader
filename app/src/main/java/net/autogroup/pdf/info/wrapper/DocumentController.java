package net.autogroup.pdf.info.wrapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.Keyboards;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.MyMath;
import net.autogroup.android.utils.ResultResponse;
import net.autogroup.android.utils.Safe;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.android.utils.Vibro;
import net.autogroup.dao2.FileMeta;
import net.autogroup.model.AppBook;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.MyADSProvider;
import net.autogroup.pdf.info.OutlineHelper;
import net.autogroup.pdf.info.PageUrl;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.model.AnnotationType;
import net.autogroup.pdf.info.model.OutlineLinkWrapper;
import net.autogroup.pdf.info.view.AlertDialogs;
import net.autogroup.sys.ImageExtractor;
import net.autogroup.sys.TempHolder;
import net.autogroup.tts.TTSEngine;
import net.autogroup.ui2.AppDB;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.ebookdroid.common.settings.SettingsManager;
import org.ebookdroid.common.settings.books.SharedBooks;
import org.ebookdroid.core.codec.Annotation;
import org.ebookdroid.core.codec.PageLink;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressLint("NewApi")
public abstract class DocumentController {

    public static final String EXTRA_PASSWORD = "password";
    public static final String EXTRA_PERCENT = "p";
    public static final String EXTRA_PLAYLIST = "playlist";

    public static final int REPEAT_SKIP_AMOUNT = 15;

    public final static List<Integer> orientationIds = Arrays.asList(//
            ActivityInfo.SCREEN_ORIENTATION_SENSOR, //
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, //
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, //
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, //
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT//
    );

    public final static List<Integer> orientationTexts = Arrays.asList(//
            R.string.automatic, //
            R.string.landscape, //
            R.string.portrait, //
            R.string.landscape_180, //
            R.string.portrait_180////
    );

    public static int getRotationText() {
        return orientationTexts.get(orientationIds.indexOf(AppState.get().orientation));
    }

    protected final Activity activity;
    private DocumentWrapperUI ui;

    public Handler handler = new Handler(Looper.getMainLooper());
    public Handler handler2 = new Handler(Looper.getMainLooper());

    public long readTimeStart;

    public DocumentController(final Activity activity) {
        this.activity = activity;
        readTimeStart = System.currentTimeMillis();
    }


    private File currentBook;
    private String title;

    public boolean isPasswordProtected() {
        try {
            return TxtUtils.isNotEmpty(activity.getIntent().getStringExtra(EXTRA_PASSWORD));
        } catch (Exception e) {
            return false;
        }
    }

    Runnable timer = new Runnable() {

        @Override
        public void run() {
            try {
                timerTask.run();
                handler.postDelayed(timer, timeout);
                LOG.d("Timer-Task Run");
            } catch (Exception e) {
                LOG.e(e);
            }
        }
    };

    private int timeout;
    private Runnable timerTask;

    public synchronized void runTimer(int timeout, Runnable run) {
        this.timeout = timeout;
        this.timerTask = run;
        stopTimer();
        if (handler != null) {
            handler.post(timer);
        }
    }

    public void stopTimer() {
        if (handler != null) {
            handler.removeCallbacks(timer);
        }
    }

    private final LinkedList<Integer> linkHistory = new LinkedList<Integer>();

    public abstract void onGoToPage(int page);

    public abstract void onSrollLeft();

    public abstract void onSrollRight();

    public abstract void onScrollUp();

    public abstract void onScrollDown();

    public abstract void onNextPage(boolean animate);

    public abstract void onPrevPage(boolean animate);

    public abstract void onNextScreen(boolean animate);

    public abstract void onPrevScreen(boolean animate);

    public abstract void onZoomInc();

    public abstract void onZoomDec();

    public abstract void onZoomInOut(int x, int y);

    public abstract void onCloseActivityAdnShowInterstial();

    public abstract void onCloseActivityFinal(Runnable run);


    public abstract void onNightMode();

    public abstract void onCrop();

    public abstract void onFullScreen();

    public abstract int getCurentPage();

    public abstract int getCurentPageFirst1();

    public abstract int getPageCount();

    public abstract void onScrollY(int value);

    public abstract void onScrollYPercent(float value);

    public abstract void onAutoScroll();

    public abstract void clearSelectedText();

    public abstract void saveChanges(List<PointF> points, int color);

    public abstract void deleteAnnotation(long pageHander, int page, int index);

    public abstract void underlineText(int color, float width, AnnotationType type);

    public abstract void getOutline(ResultResponse<List<OutlineLinkWrapper>> outline, boolean forse);

    public abstract String getFootNote(String text);

    public abstract List<String> getMediaAttachments();

    public abstract PageUrl getPageUrl(int page);

    public abstract void saveAnnotationsToFile();

    public abstract int getBookWidth();

    public abstract int getBookHeight();

    public void saveSettings() {

    }

    public float getPercentage() {
        return MyMath.percent(getCurentPageFirst1(), getPageCount());
    }


    public MyADSProvider getAdsProvider() {
        return null;
    }

    public void updateRendering() {

    }

    public void goToPageByTTS() {
        try {
            if (TTSEngine.get().isPlaying()) {

                AppBook bs = SettingsManager.getBookSettings(getCurrentBook().getPath());

                if (getCurrentBook().getPath().equals(AppTemp.get().lastBookPath)) {
                    onGoToPage(bs.getCurrentPage(getPageCount()).viewIndex + 1);
                    LOG.d("goToPageByTTS", AppTemp.get().lastBookPage + 1);
                }
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public abstract void cleanImageMatrix();

    public void checkReadingTimer() {
        long timeout = System.currentTimeMillis() - readTimeStart;
        if (AppState.get().remindRestTime != -1 && timeout >= TimeUnit.MINUTES.toMillis(AppState.get().remindRestTime)) {
            AlertDialogs.showOkDialog(activity, getString(R.string.remind_msg), new Runnable() {

                @Override
                public void run() {
                    readTimeStart = System.currentTimeMillis();
                }
            }, new Runnable() {
                @Override
                public void run() {
                    readTimeStart = System.currentTimeMillis();
                }
            });
        }

    }

    public void closeActivity() {
        handler2.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
    }

    public void saveCurrentPage() {
        AppBook bs = SettingsManager.getBookSettings();
        if (bs != null) {
            bs.updateFromAppState();
            bs.currentPageChanged(getCurentPageFirst1(), getPageCount());
            handler2.removeCallbacks(saveCurrentPageRunnable);
            handler2.postDelayed(saveCurrentPageRunnable, 1000);
        }
    }


    Runnable saveCurrentPageRunnable = new Runnable() {
        @Override
        public void run() {
            saveCurrentPageAsync();
        }
    };

    public void saveCurrentPageAsync() {
        if (TempHolder.get().loadingCancelled) {
            LOG.d("Loading cancelled");
            return;
        }
        // int page = PageUrl.fakeToReal(currentPage);
        LOG.d("_PAGE", "saveCurrentPage", getCurentPageFirst1(), getPageCount());
        try {
            if (getPageCount() <= 0) {
                LOG.d("_PAGE", "saveCurrentPage skip");
                return;
            }
            AppBook bs = SettingsManager.getBookSettings();
            bs.updateFromAppState();
            SharedBooks.save(bs);

            //AppBook bs = SettingsManager.getBookSettings(getCurrentBook().getPath());
            //bs.updateFromAppState();
            //bs.currentPageChanged(getCurentPageFirst1(), getPageCount());
            //SharedBooks.save(bs);
        } catch (Exception e) {
            LOG.e(e);
        }

    }

    public void onChangeTextSelection() {
        Vibro.vibrate();
        AppState.get().isAllowTextSelection = !AppState.get().isAllowTextSelection;
        String txt = AppState.get().isAllowTextSelection ? getString(R.string.text_highlight_mode_is_enable) : getString(R.string.text_highlight_mode_is_disable);
        Toast.makeText(getActivity(), txt, Toast.LENGTH_LONG).show();
        if (AppState.get().isAllowTextSelection) {
            TempHolder.get().isAllowTextSelectionFirstTime = true;
        }
    }

    public boolean isBookMode() {
        return AppTemp.get().readingMode == AppState.READING_MODE_BOOK;
    }

    public boolean isScrollMode() {
        return AppTemp.get().readingMode == AppState.READING_MODE_SCROLL;
    }

    public boolean isMusicianMode() {
        return AppTemp.get().readingMode == AppState.READING_MODE_MUSICIAN;
    }

    public void onResume() {
        readTimeStart = System.currentTimeMillis();
        try {
            if (getPageCount() != 0) {
                AppBook bs = SettingsManager.getBookSettings(getCurrentBook().getPath());
                if (getCurentPage() != bs.getCurrentPage(getPageCount()).viewIndex + 1) {
                    onGoToPage(bs.getCurrentPage(getPageCount()).viewIndex + 1);
                }
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public Bitmap getBookImage() {
        String url = IMG.toUrl(getCurrentBook().getPath(), ImageExtractor.COVER_PAGE_WITH_EFFECT, IMG.getImageSize());
        return ImageLoader.getInstance().loadImageSync(url, IMG.displayCacheMemoryDisc);
    }

    public FileMeta getBookFileMeta() {
        return AppDB.get().getOrCreate(getCurrentBook().getPath());
    }

    public String getBookFileMetaName() {
        return TxtUtils.getFileMetaBookName(getBookFileMeta());
    }

    public void loadOutline(final ResultResponse<List<OutlineLinkWrapper>> resultTop) {
        getOutline(new ResultResponse<List<OutlineLinkWrapper>>() {

            @Override
            public boolean onResultRecive(List<OutlineLinkWrapper> result) {
                outline = result;
                if (resultTop != null) {
                    resultTop.onResultRecive(result);
                }
                return false;
            }
        }, false);
    }

    protected volatile List<OutlineLinkWrapper> outline;
    private FrameLayout anchor;

    public void initAnchor(FrameLayout anchor) {
        this.anchor = anchor;
    }

    public List<OutlineLinkWrapper> getCurrentOutline() {
        return outline;
    }

    public String getCurrentChapter() {
        return OutlineHelper.getCurrentChapterAsString(this);
    }

    public static boolean isEinkOrMode(Context c) {
        return Dips.isEInk(c) || AppState.get().appTheme == AppState.THEME_INK;

    }

    public boolean isTextFormat() {
        try {
            boolean textFomat = ExtUtils.isTextFomat(getCurrentBook().getPath());
            LOG.d("isTextFormat", getCurrentBook().getPath(), textFomat);
            return textFomat;
        } catch (Exception e) {
            LOG.e(e);
            return false;
        }
    }

    public boolean closeDialogs() {
        if (anchor == null) {
            return false;
        }
        boolean isVisible = anchor.getVisibility() == View.VISIBLE;
        if (isVisible) {
            try {
                activity.findViewById(R.id.closePopup).performClick();
            } catch (Exception e) {
                LOG.e(e);
            }
            clearSelectedText();
        }
        return isVisible;
    }

    public abstract boolean isCropCurrentBook();

    public float getOffsetX() {
        return -1;
    }

    public float getOffsetY() {
        return -1;
    }

    public void onGoToPage(final int page, final float offsetX, final float offsetY) {

    }

    public void addRecent(final Uri uri) {
        AppDB.get().addRecent(uri.getPath());
        // BookmarksData.get().addRecent(uri);
    }

    public void onClickTop() {

    }

    public String getString(int resId) {
        return activity.getString(resId);
    }

    public void onLinkHistory() {
        if (!getLinkHistory().isEmpty()) {
            final int last = getLinkHistory().removeLast();
            onScrollY(last);
            LOG.d("onLinkHistory", last);

        }
    }

    public static void turnOffButtons(final Activity a) {
        a.getWindow().getAttributes().buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
    }

    public static void turnOnButtons(final Activity a) {
        a.getWindow().getAttributes().buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
    }

    public static void runFullScreen(final Activity a) {
        try {
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            Keyboards.hideNavigation(a);
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public static void runNormalScreen(final Activity a) {
        try {
            a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            a.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            final View decorView = a.getWindow().getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public static void chooseFullScreen(final Activity a, final boolean isFullscren) {
        if (isFullscren) {
            runFullScreen(a);
        } else {
            runNormalScreen(a);
        }
    }

    private static void applyTheme(final Activity a) {
        if (AppState.get().appTheme == AppState.THEME_LIGHT) {
            a.setTheme(R.style.StyledIndicatorsWhite);
        } else {
            a.setTheme(R.style.StyledIndicatorsBlack);
        }
    }

    public void restartActivity() {
        IMG.clearMemoryCache();
        saveAppState();
        TTSEngine.get().stop();

        Safe.run(new Runnable() {

            @Override
            public void run() {
                ImageExtractor.clearCodeDocument();
                activity.finish();
                activity.startActivity(activity.getIntent());
            }
        });
    }

    public void saveAppState() {
        AppProfile.save(activity);
    }

    public static void doRotation(final Activity a) {
        try {
            // LOG.d("isSystemAutoRotation isSystemAutoRotation",
            // Dips.isSystemAutoRotation(a));
            // LOG.d("isSystemAutoRotation geUserRotation", Dips.geUserRotation(a));
            a.setRequestedOrientation(AppState.get().orientation);
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public void onLeftPress() {
        if (AppState.get().tapZoneLeft == AppState.TAP_DO_NOTHING) {
            return;
        }
        if (AppState.get().tapZoneLeft == AppState.TAP_PREV_PAGE) {
            ui.prevChose(false);
        } else {
            ui.nextChose(false);
        }
    }

    public void onTopPress() {
        if (AppState.get().tapZoneTop == AppState.TAP_DO_NOTHING) {
            return;
        }
        if (AppState.get().tapZoneTop == AppState.TAP_PREV_PAGE) {
            ui.prevChose(false);
        } else {
            ui.nextChose(false);
        }
    }

    public void onBottomPress() {
        if (AppState.get().tapZoneBottom == AppState.TAP_DO_NOTHING) {
            return;
        }
        if (AppState.get().tapZoneBottom == AppState.TAP_NEXT_PAGE) {
            ui.nextChose(false);
        } else {
            ui.prevChose(false);
        }
    }

    public void onRightPress() {
        if (AppState.get().tapZoneRight == AppState.TAP_DO_NOTHING) {
            return;
        }
        if (AppState.get().tapZoneRight == AppState.TAP_NEXT_PAGE) {
            ui.nextChose(false);
        } else {
            ui.prevChose(false);
        }
    }

    public void onLeftPressAnimate() {
        ui.prevChose(true);
    }

    public void onRightPressAnimate() {
        ui.nextChose(true);
    }

    public void showAnnotation(Annotation annotation) {
        Toast.makeText(activity, "" + annotation.text, Toast.LENGTH_SHORT).show();
    }

    public void onSingleTap() {
        ui.onSingleTap();
    }

    public void onDoubleTap(int x, int y) {
        ui.doDoubleTap(x, y);
    }

    public abstract String getTextForPage(int page);

    public abstract List<PageLink> getLinksForPage(int page);

    public void onAnnotationTap(long pageHander, int page, int index) {
        deleteAnnotation(pageHander, page, index);
        showEditDialogIfNeed();
        saveAnnotationsToFile();
    }

    public void showEditDialogIfNeed() {
        ui.showEditDialogIfNeed();
    }

    public void onLongPress(MotionEvent ev) {
        ui.onLongPress(ev);
    }

    public abstract void doSearch(String text, ResultResponse<Integer> result);

    public Activity getActivity() {
        return activity;
    }

    public void toast(final String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

    public DocumentWrapperUI getUi() {
        return ui;
    }

    public void setUi(final DocumentWrapperUI ui) {
        this.ui = ui;
    }

    public boolean showContent(final ListView contentList) {
        return false;
    }

    public File getCurrentBook() {
        return currentBook;
    }

    public void setCurrentBook(final File currentBook) {
        this.currentBook = currentBook;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public LinkedList<Integer> getLinkHistory() {
        return linkHistory;
    }

    public void toPageDialog() {

    }

    public abstract void alignDocument();

    public abstract void centerHorizontal();

    public void recyclePage(int page) {

    }

}
