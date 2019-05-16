/**
 *
 */
package net.autogroup.pdf.info.wrapper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import net.autogroup.android.utils.Apps;
import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.IntegerResponse;
import net.autogroup.android.utils.Keyboards;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.android.utils.Vibro;
import net.autogroup.android.utils.Views;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.DictsHelper;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.OutlineHelper;
import net.autogroup.pdf.info.OutlineHelper.Info;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.pdf.info.UiSystemUtils;
import net.autogroup.pdf.info.model.OutlineLinkWrapper;
import net.autogroup.pdf.info.view.AnchorHelper;
import net.autogroup.pdf.info.view.BookmarkPanel;
import net.autogroup.pdf.info.view.BrightnessHelper;
import net.autogroup.pdf.info.view.CustomSeek;
import net.autogroup.pdf.info.view.Dialogs;
import net.autogroup.pdf.info.view.DialogsPlaylist;
import net.autogroup.pdf.info.view.DragingDialogs;
import net.autogroup.pdf.info.view.DrawView;
import net.autogroup.pdf.info.view.HorizontallSeekTouchEventListener;
import net.autogroup.pdf.info.view.HypenPanelHelper;
import net.autogroup.pdf.info.view.MyPopupMenu;
import net.autogroup.pdf.info.view.ProgressDraw;
import net.autogroup.pdf.info.view.UnderlineImageView;
import net.autogroup.pdf.info.widget.DraggbleTouchListener;
import net.autogroup.pdf.info.widget.ShareDialog;
import net.autogroup.pdf.search.activity.msg.MessagePageXY;
import net.autogroup.pdf.search.activity.msg.MessegeBrightness;
import net.autogroup.pdf.search.view.CloseAppDialog;
import net.autogroup.sys.TempHolder;
import net.autogroup.tts.MessagePageNumber;
import net.autogroup.tts.TTSControlsView;
import net.autogroup.tts.TTSEngine;
import net.autogroup.tts.TTSService;
import net.autogroup.tts.TtsStatus;
import net.autogroup.ui2.AppDB;
import net.autogroup.ui2.MainTabs2;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.ebookdroid.BookType;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author iivanenko
 */
public class DocumentWrapperUI {

    final DocumentController dc;
    Activity a;
    String bookTitle;

    TextView toastBrightnessText, pagesCountIndicator, currentSeek, maxSeek, currentTime, bookName, nextTypeBootom, batteryLevel, lirbiLogo, reverseKeysIndicator;
    ImageView onDocDontext, toolBarButton, linkHistory, lockUnlock, lockUnlockTop, textToSpeachTop, clockIcon, batteryIcon;
    ImageView showSearch, nextScreenType, moveCenter, autoScroll, textToSpeach, onModeChange, imageMenuArrow, editTop2, goToPage1, goToPage1Top;
    View adFrame, titleBar, overlay, menuLayout, moveLeft, moveRight, bottomBar, onCloseBook, seekSpeedLayot, zoomPlus, zoomMinus;
    View line1, line2, lineFirst, lineClose, closeTop, pagesBookmark, musicButtonPanel, parentParent;
    TTSControlsView ttsActive;
    SeekBar seekBar, speedSeekBar;
    FrameLayout anchor;
    ImageView anchorX, anchorY;
    DrawView drawView;
    ProgressDraw progressDraw;
    UnderlineImageView crop, cut, onBC;
    LinearLayout pageshelper;

    final Handler handler = new Handler();
    final Handler handlerTimer = new Handler();
    String quickBookmark;

    public DocumentWrapperUI(final DocumentController controller) {
        AppState.get().annotationDrawColor = "";
        AppState.get().editWith = AppState.EDIT_NONE;

        this.dc = controller;
        controller.setUi(this);

        EventBus.getDefault().register(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPageNumber(MessagePageNumber event) {
        try {
            if (dc != null) {
                dc.onGoToPage(event.getPage() + 1);
                ttsActive.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTTSStatus(TtsStatus status) {
        try {
            ttsActive.setVisibility(TxtUtils.visibleIf(!TTSEngine.get().isShutdown()));
        } catch (Exception e) {
            LOG.e(e);
        }

    }

    public void onSingleTap() {
        if (dc.isMusicianMode()) {
            onAutoScrollClick();
        } else {
            doShowHideWrapperControlls();
        }
    }

    public static boolean isCJK(int ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block) || Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block) || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)) {
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onLongPress(MotionEvent ev) {
        if (dc.isTextFormat() && TxtUtils.isFooterNote(AppState.get().selectedText)) {
            DragingDialogs.showFootNotes(anchor, dc, new Runnable() {

                @Override
                public void run() {
                    showHideHistory();
                }
            });
        } else {
            if (AppState.get().isRememberDictionary) {
                DictsHelper.runIntent(dc.getActivity(), AppState.get().selectedText);
                dc.clearSelectedText();
            } else {
                DragingDialogs.selectTextMenu(anchor, dc, true, updateUIRunnable);
            }
        }
    }

    Runnable updateUIRunnable = new Runnable() {

        @Override
        public void run() {
            updateUI();
        }
    };

    public void showSelectTextMenu() {
        DragingDialogs.selectTextMenu(anchor, dc, true, updateUIRunnable);

    }

    public boolean checkBack(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 0) {
            keyCode = event.getScanCode();
        }

        if (anchor == null) {
            closeAndRunList();
            return true;
        }
        if (AppState.get().isAutoScroll) {
            AppState.get().isAutoScroll = false;
            updateUI();
            return true;
        }

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (closeDialogs()) {
                return true;
            } else if (!dc.getLinkHistory().isEmpty()) {
                dc.onLinkHistory();
                return true;
            }
        }
        return false;
    }

    public boolean dispatchKeyEventUp(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 0) {
            keyCode = event.getScanCode();
        }

        if (KeyEvent.KEYCODE_MENU == keyCode || KeyEvent.KEYCODE_M == keyCode) {
            doShowHideWrapperControlls();
            return true;
        }

        return false;

    }

    public boolean dispatchKeyEventDown(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == 0) {
            keyCode = event.getScanCode();
        }

        if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
            dc.onGoToPage(keyCode - KeyEvent.KEYCODE_1 + 1);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_0) {
            dc.toPageDialog();
            return true;
        }

        if (KeyEvent.KEYCODE_F == keyCode) {
            dc.alignDocument();
            return true;
        }

        if (KeyEvent.KEYCODE_S == keyCode || KeyEvent.KEYCODE_SEARCH == keyCode) {
            showSearchDialog();
            return true;
        }

        if (KeyEvent.KEYCODE_A == keyCode || KeyEvent.KEYCODE_SPACE == keyCode) {
            onAutoScrollClick();
            return true;
        }

        if (AppState.get().isUseVolumeKeys && AppState.get().isZoomInOutWithVolueKeys) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                dc.onZoomInc();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                dc.onZoomDec();
                return true;
            }

        }

        if (AppState.get().isScrollSpeedByVolumeKeys && AppState.get().isUseVolumeKeys && AppState.get().isAutoScroll) {
            if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
                if (AppState.get().autoScrollSpeed > 1) {
                    AppState.get().autoScrollSpeed -= 1;
                    dc.onAutoScroll();
                    updateUI();
                }
                return true;
            }
            if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
                if (AppState.get().autoScrollSpeed <= AppState.MAX_SPEED) {
                    AppState.get().autoScrollSpeed += 1;
                }
                dc.onAutoScroll();
                updateUI();
                return true;
            }
        }

        if (!TTSEngine.get().isPlaying()) {
            if (AppState.get().isUseVolumeKeys && AppState.get().getNextKeys().contains(keyCode)) {
                if (closeDialogs()) {
                    return true;
                }
                nextChose(false, event.getRepeatCount());
                return true;
            }

            if (AppState.get().isUseVolumeKeys && AppState.get().getPrevKeys().contains(keyCode)) {
                if (closeDialogs()) {
                    return true;
                }
                prevChose(false, event.getRepeatCount());
                return true;
            }
        }

        if (AppState.get().isUseVolumeKeys && KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
            if (TTSEngine.get().isPlaying()) {
                if (AppState.get().isFastBookmarkByTTS) {
                    TTSEngine.get().fastTTSBookmakr(dc);
                } else {
                    TTSEngine.get().stop();
                }
            } else {
                //TTSEngine.get().playCurrent();
                TTSService.playPause(dc.getActivity(), dc);
                anchor.setTag("");
            }
            //TTSNotification.showLast();
            //DragingDialogs.textToSpeachDialog(anchor, dc);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            dc.onScrollDown();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            dc.onScrollUp();
            return true;
        }

        if (keyCode == 70) {
            dc.onZoomInc();
            return true;
        }

        if (keyCode == 69) {
            dc.onZoomDec();
            return true;
        }

//        if (PageImageState.get().hasSelectedWords()) {
//            dc.clearSelectedText();
//            return true;
//        }

        return false;

    }

    public void closeAndRunList() {
        EventBus.getDefault().unregister(this);

        AppTemp.get().lastClosedActivity = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        if (titleBar != null) {
            titleBar.removeCallbacks(null);
        }
        dc.saveCurrentPageAsync();
        dc.onCloseActivityAdnShowInterstial();
        dc.closeActivity();

    }

    public void updateSpeedLabel() {

        Info info = OutlineHelper.getForamtingInfo(dc, true);

        maxSeek.setText(info.textPage);
        currentSeek.setText(info.textMax);
        pagesCountIndicator.setText(info.chText);
    }

    public void updateUI() {
        final int max = dc.getPageCount();
        final int current = dc.getCurentPage();

        updateSpeedLabel();

        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setMax(max - 1);
        seekBar.setProgress(current - 1);
        seekBar.setOnSeekBarChangeListener(onSeek);

        speedSeekBar.setOnSeekBarChangeListener(null);
        speedSeekBar.setMax(AppState.MAX_SPEED);
        speedSeekBar.setProgress(AppState.get().autoScrollSpeed);
        speedSeekBar.setOnSeekBarChangeListener(onSpeed);

        // time
        currentTime.setText(UiSystemUtils.getSystemTime(a));

        final int myLevel = UiSystemUtils.getPowerLevel(a);
        batteryLevel.setText(myLevel + "%");
        if (myLevel == -1) {
            batteryLevel.setVisibility(View.GONE);
        }

        showChapter();

        hideShow();
        initNextType();
        initToolBarPlusMinus();

        showHideHistory();

        updateLock();

        reverseKeysIndicator.setVisibility(AppState.get().isReverseKeys ? View.VISIBLE : View.GONE);
        if (true || dc.isMusicianMode()) {
            reverseKeysIndicator.setVisibility(View.GONE);
        }

        moveLeft.setVisibility(Dips.isSmallScreen() && Dips.isVertical() ? View.GONE : View.VISIBLE);
        moveRight.setVisibility(Dips.isSmallScreen() && Dips.isVertical() ? View.GONE : View.VISIBLE);
        zoomPlus.setVisibility(Dips.isSmallScreen() && Dips.isVertical() ? View.GONE : View.VISIBLE);
        zoomMinus.setVisibility(Dips.isSmallScreen() && Dips.isVertical() ? View.GONE : View.VISIBLE);

        if (dc.isTextFormat()) {
            moveLeft.setVisibility(View.GONE);
            moveRight.setVisibility(View.GONE);
            zoomPlus.setVisibility(View.GONE);
            zoomMinus.setVisibility(View.GONE);
            crop.setVisibility(View.GONE);
            cut.setVisibility(View.GONE);
            onModeChange.setVisibility(View.GONE);
            if (Dips.isEInk(dc.getActivity()) || AppState.get().appTheme == AppState.THEME_INK || AppState.get().isEnableBC) {
                onBC.setVisibility(View.VISIBLE);
            } else {
                onBC.setVisibility(View.GONE);
            }
            if (AppTemp.get().isCrop) {
                crop.setVisibility(View.VISIBLE);
            }
            if (AppTemp.get().isCut) {
                cut.setVisibility(View.VISIBLE);
            }
        }

        crop.underline(AppTemp.get().isCrop);
        cut.underline(AppTemp.get().isCut);

        progressDraw.updateProgress(current - 1);

        if (AppState.get().inactivityTime > 0) {
            dc.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            LOG.d("FLAG addFlags", "FLAG_KEEP_SCREEN_ON", dc.getActivity().getWindow().getAttributes().flags);
            handler.removeCallbacks(clearFlags);
            handler.postDelayed(clearFlags, TimeUnit.MINUTES.toMillis(AppState.get().inactivityTime));
        }

        if (AppState.get().isAutoScroll) {
            pagesBookmark.setVisibility(View.GONE);
        } else {
            pagesBookmark.setVisibility(View.VISIBLE);
        }


        dc.saveCurrentPage();
        //SharedBooks.save(bs);

    }

    public void hideShowPrevNext() {
        if (dc.isMusicianMode()) {
            if (AppState.get().isShowRectangularTapZones) {
                line1.setVisibility(View.VISIBLE);
                line2.setVisibility(View.VISIBLE);
            } else {
                line1.setVisibility(View.GONE);
                line2.setVisibility(View.GONE);

            }
        }
    }

    Runnable clearFlags = new Runnable() {

        @Override
        public void run() {
            try {
                dc.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                LOG.d("FLAG clearFlags", "FLAG_KEEP_SCREEN_ON", dc.getActivity().getWindow().getAttributes().flags);
            } catch (Exception e) {
                LOG.e(e);
            }
        }
    };

    public void showChapter() {
        if (TxtUtils.isNotEmpty(dc.getCurrentChapter())) {
            bookName.setText(bookTitle + " " + TxtUtils.LONG_DASH1 + " " + dc.getCurrentChapter().trim());
        } else {
            bookName.setText(bookTitle);

        }
    }

    public void updateLock() {
        // int mode = View.VISIBLE;

        if (AppTemp.get().isLocked) {
            lockUnlock.setImageResource(R.drawable.glyphicons_204_lock);
            lockUnlockTop.setImageResource(R.drawable.glyphicons_204_lock);
            // lockUnlock.setColorFilter(a.getResources().getColor(R.color.tint_yellow));
            // lockUnlockTop.setColorFilter(a.getResources().getColor(R.color.tint_yellow));
            // mode = View.VISIBLE;
        } else {
            lockUnlock.setImageResource(R.drawable.glyphicons_205_unlock);
            lockUnlockTop.setImageResource(R.drawable.glyphicons_205_unlock);
            // lockUnlock.setColorFilter(a.getResources().getColor(R.color.tint_white));
            // lockUnlockTop.setColorFilter(a.getResources().getColor(R.color.tint_white));
            // mode = View.GONE;
        }
//        if (AppState.get().l) {
//            TintUtil.setTintImageWithAlpha(moveCenter, Color.LTGRAY);
//        } else {
//            TintUtil.setTintImageWithAlpha(moveCenter, Color.WHITE);
//        }

    }

    public void showHideHistory() {
        linkHistory.setVisibility(dc.getLinkHistory().isEmpty() ? View.GONE : View.VISIBLE);
    }

    Runnable updateTimePower = new Runnable() {

        @Override
        public void run() {
            try {
                if (currentTime != null) {
                    currentTime.setText(UiSystemUtils.getSystemTime(dc.getActivity()));

                    int myLevel = UiSystemUtils.getPowerLevel(dc.getActivity());
                    batteryLevel.setText(myLevel + "%");
                }
            } catch (Exception e) {
                LOG.e(e);
            }
            LOG.d("Update time and power");
            handlerTimer.postDelayed(updateTimePower, AppState.APP_UPDATE_TIME_IN_UI);

        }
    };

    @Subscribe
    public void showHideTextSelectors(MessagePageXY event) {
        if (event.getType() == MessagePageXY.TYPE_HIDE) {
            anchorX.setVisibility(View.GONE);
            anchorY.setVisibility(View.GONE);

        }
        if (event.getType() == MessagePageXY.TYPE_SHOW) {
            anchorX.setVisibility(View.VISIBLE);
            anchorY.setVisibility(View.VISIBLE);

            AnchorHelper.setXY(anchorX, event.getX(), event.getY());
            AnchorHelper.setXY(anchorY, event.getX1(), event.getY1());

        }

    }

    public void initUI(final Activity a) {
        this.a = a;
        quickBookmark = a.getString(R.string.fast_bookmark);

        a.findViewById(R.id.showHypenLangPanel).setVisibility(View.GONE);

        parentParent = a.findViewById(R.id.parentParent);
        linkHistory = (ImageView) a.findViewById(R.id.linkHistory);
        linkHistory.setOnClickListener(onLinkHistory);

        menuLayout = a.findViewById(R.id.menuLayout);

        bottomBar = a.findViewById(R.id.bottomBar);
        imageMenuArrow = (ImageView) a.findViewById(R.id.imageMenuArrow);
        adFrame = a.findViewById(R.id.adFrame);

        seekBar = (SeekBar) a.findViewById(R.id.seekBar);
        speedSeekBar = (SeekBar) a.findViewById(R.id.seekBarSpeed);
        seekSpeedLayot = a.findViewById(R.id.seekSpeedLayot);
        anchor = (FrameLayout) a.findViewById(R.id.anchor);
        dc.initAnchor(anchor);

        anchorX = (ImageView) a.findViewById(R.id.anchorX);
        anchorY = (ImageView) a.findViewById(R.id.anchorY);

        TintUtil.setTintImageWithAlpha(anchorX, AppState.get().isDayNotInvert ? Color.BLUE : Color.YELLOW, 150);
        TintUtil.setTintImageWithAlpha(anchorY, AppState.get().isDayNotInvert ? Color.BLUE : Color.YELLOW, 150);

        anchorX.setVisibility(View.GONE);
        anchorY.setVisibility(View.GONE);

        DraggbleTouchListener touch1 = new DraggbleTouchListener(anchorX, (View) anchorX.getParent());
        DraggbleTouchListener touch2 = new DraggbleTouchListener(anchorY, (View) anchorY.getParent());

        final Runnable onMoveActionOnce = new Runnable() {

            @Override
            public void run() {
                float x = anchorX.getX() + anchorX.getWidth();
                float y = anchorX.getY() + anchorX.getHeight() / 2;

                float x1 = anchorY.getX();
                float y1 = anchorY.getY();
                EventBus.getDefault().post(new MessagePageXY(MessagePageXY.TYPE_SELECT_TEXT, dc.getCurentPage(), x, y, x1, y1));
            }
        };
        final Runnable onMoveAction = new Runnable() {

            @Override
            public void run() {
                handler.removeCallbacks(onMoveActionOnce);
                handler.postDelayed(onMoveActionOnce, 150);

            }
        };

        Runnable onMoveFinish = new Runnable() {

            @Override
            public void run() {
                onMoveAction.run();
                if (AppState.get().isRememberDictionary) {
                    final String text = AppState.get().selectedText;
                    DictsHelper.runIntent(dc.getActivity(), text);
                    dc.clearSelectedText();

                } else {
                    DragingDialogs.selectTextMenu(anchor, dc, true, updateUIRunnable);
                }

            }
        };

        touch1.setOnMoveFinish(onMoveFinish);
        touch2.setOnMoveFinish(onMoveFinish);

        touch1.setOnMove(onMoveAction);
        touch2.setOnMove(onMoveAction);

        titleBar = a.findViewById(R.id.titleBar);
        titleBar.setOnClickListener(onMenu);

        overlay = a.findViewById(R.id.overlay);
        overlay.setVisibility(View.VISIBLE);

        reverseKeysIndicator = (TextView) a.findViewById(R.id.reverseKeysIndicator);
        // reverseKeysIndicator.setOnClickListener(onReverseKeys);

        zoomPlus = a.findViewById(R.id.zoomPlus);
        zoomPlus.setOnClickListener(onPlus);

        zoomMinus = a.findViewById(R.id.zoomMinus);
        zoomMinus.setOnClickListener(onMinus);

        line1 = a.findViewById(R.id.line1);
        line1.setOnClickListener(onPrevPage);

        line2 = a.findViewById(R.id.line2);
        line2.setOnClickListener(onNextPage);

        lineClose = a.findViewById(R.id.lineClose);
        lineClose.setOnClickListener(onClose);

        closeTop = a.findViewById(R.id.closeTop);
        closeTop.setOnClickListener(onClose);
        closeTop.setOnLongClickListener(onCloseLongClick);

        lineFirst = a.findViewById(R.id.lineFirst);
        lineFirst.setOnClickListener(onGoToPAge1);

        lirbiLogo = (TextView) a.findViewById(R.id.lirbiLogo);
        lirbiLogo.setText(AppState.get().musicText);
        lirbiLogo.setOnClickListener(onLirbiLogoClick);

        editTop2 = (ImageView) a.findViewById(R.id.editTop2);
        editTop2.setOnClickListener(onShowHideEditPanel);

        goToPage1 = (ImageView) a.findViewById(R.id.goToPage1);
        goToPage1Top = (ImageView) a.findViewById(R.id.goToPage1Top);
        goToPage1.setOnClickListener(onGoToPAge1);
        goToPage1Top.setOnClickListener(onGoToPAge1);

        toolBarButton = (ImageView) a.findViewById(R.id.imageToolbar);
        toolBarButton.setOnClickListener(onHideShowToolBar);

        // nextPage.setOnClickListener(onNextPage);
        // prevPage.setOnClickListener(onPrevPage);

        moveLeft = a.findViewById(R.id.moveLeft);
        moveLeft.setOnClickListener(onMoveLeft);

        moveCenter = a.findViewById(R.id.moveCenter);
        moveCenter.setOnClickListener(onMoveCenter);

        moveRight = a.findViewById(R.id.moveRight);
        moveRight.setOnClickListener(onMoveRight);

        ImageView brightness = (ImageView) a.findViewById(R.id.brightness);
        brightness.setOnClickListener(onSun);
        brightness.setImageResource(!AppState.get().isDayNotInvert ? R.drawable.glyphicons_232_sun : R.drawable.glyphicons_2_moon);

        // if (Dips.isEInk(dc.getActivity())) {
        // brightness.setVisibility(View.GONE);
        // AppState.get().isDayNotInvert = true;
        // }

        onBC = (UnderlineImageView) a.findViewById(R.id.onBC);
        onBC.setOnClickListener(onBCclick);
        onBC.underline(AppState.get().isEnableBC);

        a.findViewById(R.id.toPage).setOnClickListener(toPage);

        crop = (UnderlineImageView) a.findViewById(R.id.crop);
        crop.setOnClickListener(onCrop);
        crop.setOnLongClickListener(onCropLong);

        if (AppTemp.get().isCut) {
            crop.setVisibility(View.GONE);
        }

        cut = (UnderlineImageView) a.findViewById(R.id.cut);
        cut.setOnClickListener(onCut);
        cut.setVisibility(View.GONE);

        onModeChange = (ImageView) a.findViewById(R.id.onModeChange);
        onModeChange.setOnClickListener(onModeChangeClick);
        onModeChange.setImageResource(AppTemp.get().isCut ? R.drawable.glyphicons_page_split : R.drawable.glyphicons_two_page_one);

        View prefTop = a.findViewById(R.id.prefTop);
        prefTop.setOnClickListener(onPrefTop);

        ImageView fullscreen = (ImageView) a.findViewById(R.id.fullscreen);
        fullscreen.setOnClickListener(onFull);
        fullscreen.setImageResource(AppState.get().isFullScreen ? R.drawable.glyphicons_487_fit_frame_to_image : R.drawable.glyphicons_488_fit_image_to_frame);

        onCloseBook = a.findViewById(R.id.close);
        onCloseBook.setOnClickListener(onClose);
        onCloseBook.setOnLongClickListener(onCloseLongClick);
        onCloseBook.setVisibility(View.INVISIBLE);

        showSearch = (ImageView) a.findViewById(R.id.onShowSearch);
        showSearch.setOnClickListener(onShowSearch);
        autoScroll = ((ImageView) a.findViewById(R.id.autoScroll));
        autoScroll.setOnClickListener(onAutoScroll);

        // ((View)
        // a.findViewById(R.id.onScreenMode)).setOnClickListener(onScreenMode);

        nextTypeBootom = (TextView) a.findViewById(R.id.nextTypeBootom);

        nextTypeBootom.setOnClickListener(onNextType);

        nextScreenType = ((ImageView) a.findViewById(R.id.imageNextScreen));
        nextScreenType.setOnClickListener(onNextType);

        onDocDontext = (ImageView) a.findViewById(R.id.onDocDontext);
        onDocDontext.setOnClickListener(onShowContext);

        lockUnlock = (ImageView) a.findViewById(R.id.lockUnlock);
        lockUnlockTop = (ImageView) a.findViewById(R.id.lockUnlockTop);
        lockUnlock.setOnClickListener(onLockUnlock);
        lockUnlockTop.setOnClickListener(onLockUnlock);

        textToSpeachTop = (ImageView) a.findViewById(R.id.textToSpeachTop);
        textToSpeachTop.setOnClickListener(onTextToSpeach);

        ttsActive = a.findViewById(R.id.ttsActive);
        ttsActive.setDC(dc);
        ttsActive.addOnDialogRunnable(new Runnable() {

            @Override
            public void run() {
                DragingDialogs.textToSpeachDialog(anchor, dc);
            }
        });

        batteryIcon = (ImageView) a.findViewById(R.id.batteryIcon);
        clockIcon = (ImageView) a.findViewById(R.id.clockIcon);

        textToSpeach = (ImageView) a.findViewById(R.id.textToSpeach);
        textToSpeach.setOnClickListener(onTextToSpeach);

        drawView = (DrawView) a.findViewById(R.id.drawView);

        View bookmarks = a.findViewById(R.id.onBookmarks);
        bookmarks.setOnClickListener(onBookmarks);
        bookmarks.setOnLongClickListener(onBookmarksLong);

        toastBrightnessText = (TextView) a.findViewById(R.id.toastBrightnessText);
        toastBrightnessText.setVisibility(View.GONE);
        TintUtil.setDrawableTint(toastBrightnessText.getCompoundDrawables()[0], Color.WHITE);

        TextView modeName = (TextView) a.findViewById(R.id.modeName);
        modeName.setText(AppState.get().nameVerticalMode);

        pagesCountIndicator = (TextView) a.findViewById(R.id.currentPageIndex);
        pagesCountIndicator.setVisibility(View.GONE);

        currentSeek = (TextView) a.findViewById(R.id.currentSeek);
        maxSeek = (TextView) a.findViewById(R.id.maxSeek);
        bookName = (TextView) a.findViewById(R.id.bookName);

        currentTime = (TextView) a.findViewById(R.id.currentTime);
        batteryLevel = (TextView) a.findViewById(R.id.currentBattery);

        currentSeek.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Dialogs.showDeltaPage(anchor, dc, dc.getCurentPageFirst1(), updateUIRunnable);
                return true;
            }
        });
        maxSeek.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Dialogs.showDeltaPage(anchor, dc, dc.getCurentPageFirst1(), updateUIRunnable);
                return true;
            }
        });

        View thumbnail = a.findViewById(R.id.thumbnail);
        thumbnail.setOnClickListener(onThumbnail);

        View bookMenu = a.findViewById(R.id.bookMenu);
        bookMenu.setOnClickListener(onItemMenu);
        modeName.setOnClickListener(onItemMenu);
        modeName.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                dc.onChangeTextSelection();
                AppState.get().isEditMode = false;
                hideShow();
                return true;
            }
        });

        progressDraw = (ProgressDraw) a.findViewById(R.id.progressDraw);

        AppState.get().isAutoScroll = false;

        ImageView recent = (ImageView) a.findViewById(R.id.onRecent);
        recent.setOnClickListener(onRecent);

        anchor.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onGlobalLayout() {
                if (anchor.getVisibility() == View.VISIBLE || dc.isMusicianMode()) {
                    adFrame.setVisibility(View.GONE);
                    adFrame.setClickable(false);
                } else {
                    if (AppState.get().isEditMode && adFrame.getTag() == null) {
                        adFrame.setVisibility(View.VISIBLE);
                        adFrame.setClickable(true);
                    } else {
                        adFrame.setVisibility(View.GONE);
                        adFrame.setClickable(false);
                    }
                }

                if (anchor.getX() < 0) {
                    anchor.setX(0);
                }
                if (anchor.getY() < 0) {
                    anchor.setY(0);
                }
            }

        });
        updateSeekBarColorAndSize();
        BrightnessHelper.updateOverlay(overlay);

        // bottom 1
        TintUtil.setStatusBarColor(a);

        TintUtil.setTintBgSimple(a.findViewById(R.id.menuLayout), AppState.get().transparencyUI);
        TintUtil.setTintBgSimple(a.findViewById(R.id.bottomBar1), AppState.get().transparencyUI);
        TintUtil.setBackgroundFillColorBottomRight(lirbiLogo, ColorUtils.setAlphaComponent(TintUtil.color, AppState.get().transparencyUI));
        tintSpeed();

        pageshelper = (LinearLayout) a.findViewById(R.id.pageshelper);
        musicButtonPanel = a.findViewById(R.id.musicButtonPanel);
        musicButtonPanel.setVisibility(View.GONE);

        pagesBookmark = a.findViewById(R.id.pagesBookmark);
        pagesBookmark.setOnClickListener(onBookmarks);
        pagesBookmark.setOnLongClickListener(onBookmarksLong);

        line1.setVisibility(View.GONE);
        line2.setVisibility(View.GONE);
        lineFirst.setVisibility(View.GONE);
        lineClose.setVisibility(View.GONE);
        goToPage1.setVisibility(View.GONE);
        goToPage1Top.setVisibility(View.GONE);
        closeTop.setVisibility(View.GONE);

        textToSpeachTop.setVisibility(View.GONE);
        lockUnlockTop.setVisibility(View.GONE);
        nextScreenType.setVisibility(View.GONE);
        goToPage1Top.setVisibility(View.GONE);

        if (dc.isMusicianMode()) {
            AppState.get().isEditMode = false;
            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.VISIBLE);
            lineFirst.setVisibility(View.VISIBLE);
            lineClose.setVisibility(View.VISIBLE);

            goToPage1.setVisibility(View.VISIBLE);
            goToPage1Top.setVisibility(View.VISIBLE);
            lockUnlockTop.setVisibility(View.VISIBLE);
            closeTop.setVisibility(View.VISIBLE);

            reverseKeysIndicator.setVisibility(View.GONE);
            textToSpeachTop.setVisibility(View.GONE);
            progressDraw.setVisibility(View.GONE);
            modeName.setText(AppState.get().nameMusicianMode);
        }

        currentSeek.setVisibility(View.GONE);
        maxSeek.setVisibility(View.GONE);
        seekBar.setVisibility(View.INVISIBLE);

        hideShowPrevNext();

    }

    public void updateSeekBarColorAndSize() {
        lirbiLogo.setText(AppState.get().musicText);
        // TintUtil.setBackgroundFillColorBottomRight(ttsActive,
        // ColorUtils.setAlphaComponent(TintUtil.color, 230));

        TintUtil.setTintText(bookName, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha(textToSpeachTop, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha(lockUnlockTop, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha(nextScreenType, TintUtil.getStatusBarColor());
        TintUtil.setTintText(pagesCountIndicator, TintUtil.getStatusBarColor());
        TintUtil.setTintText(currentTime, TintUtil.getStatusBarColor());
        TintUtil.setTintText(batteryLevel, TintUtil.getStatusBarColor());
        TintUtil.setTintText(reverseKeysIndicator, ColorUtils.setAlphaComponent(TintUtil.getStatusBarColor(), 200));

        TintUtil.setTintImageWithAlpha(goToPage1Top, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha((ImageView) closeTop, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha(toolBarButton, TintUtil.getStatusBarColor());
        TintUtil.setTintImageWithAlpha(clockIcon, TintUtil.getStatusBarColor()).setAlpha(200);
        TintUtil.setTintImageWithAlpha(batteryIcon, TintUtil.getStatusBarColor()).setAlpha(200);

        int titleColor = AppState.get().isDayNotInvert ? MagicHelper.otherColor(AppState.get().colorDayBg, -0.05f) : MagicHelper.otherColor(AppState.get().colorNigthBg, 0.05f);
        titleBar.setBackgroundColor(titleColor);

        int progressColor = AppState.get().isDayNotInvert ? AppState.get().statusBarColorDay : MagicHelper.otherColor(AppState.get().statusBarColorNight, +0.2f);
        progressDraw.updateColor(progressColor);
        progressDraw.getLayoutParams().height = Dips.dpToPx(AppState.get().progressLineHeight);
        progressDraw.requestLayout();

        // textSize
        bookName.setTextSize(AppState.get().statusBarTextSizeAdv);
        pagesCountIndicator.setTextSize(AppState.get().statusBarTextSizeAdv);
        currentTime.setTextSize(AppState.get().statusBarTextSizeAdv);
        batteryLevel.setTextSize(AppState.get().statusBarTextSizeAdv);
        reverseKeysIndicator.setTextSize(AppState.get().statusBarTextSizeAdv);
        lirbiLogo.setTextSize(AppState.get().statusBarTextSizeAdv);

        int iconSize = Dips.spToPx(AppState.get().statusBarTextSizeAdv);
        int smallIconSize = iconSize - Dips.dpToPx(5);

        textToSpeachTop.getLayoutParams().height = textToSpeachTop.getLayoutParams().width = iconSize;
        lockUnlockTop.getLayoutParams().height = lockUnlockTop.getLayoutParams().width = iconSize;
        nextScreenType.getLayoutParams().height = nextScreenType.getLayoutParams().width = iconSize;
        goToPage1Top.getLayoutParams().height = goToPage1Top.getLayoutParams().width = iconSize;
        closeTop.getLayoutParams().height = closeTop.getLayoutParams().width = iconSize;
        toolBarButton.getLayoutParams().height = toolBarButton.getLayoutParams().width = iconSize;

        clockIcon.getLayoutParams().height = clockIcon.getLayoutParams().width = smallIconSize;
        batteryIcon.getLayoutParams().height = batteryIcon.getLayoutParams().width = smallIconSize;

        // lirbiLogo.getLayoutParams().height = panelSize;

    }

    @Subscribe
    public void onMessegeBrightness(MessegeBrightness msg) {
        BrightnessHelper.onMessegeBrightness(handler, msg, toastBrightnessText, overlay);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void tintSpeed() {
        if (Build.VERSION.SDK_INT >= 16) {
            // speedSeekBar.getProgressDrawable().getCurrent().setColorFilter(TintUtil.color,
            // PorterDuff.Mode.SRC_IN);
            // speedSeekBar.getThumb().setColorFilter(TintUtil.color,
            // PorterDuff.Mode.SRC_IN);
        }
    }

    public void showEditDialogIfNeed() {
        DragingDialogs.editColorsPanel(anchor, dc, drawView, true);
    }

    View.OnClickListener onRecent = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            DragingDialogs.recentBooks(anchor, dc);
        }
    };

    View.OnClickListener onItemMenu = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            ShareDialog.show(a, dc.getCurrentBook(), new Runnable() {

                @Override
                public void run() {
                    if (dc.getCurrentBook().delete()) {
                        TempHolder.listHash++;
                        AppDB.get().deleteBy(dc.getCurrentBook().getPath());
                        dc.getActivity().finish();
                    }
                }
            }, dc.getCurentPage() - 1, dc, new Runnable() {

                @Override
                public void run() {
                    hideShow();

                }
            });
            Keyboards.hideNavigation(a);
            hideAds();
        }
    };

    View.OnClickListener onLirbiLogoClick = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            doShowHideWrapperControlls();
        }
    };

    View.OnClickListener onTextToSpeach = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            if (AppTemp.get().isCut) {
                onModeChange.setImageResource(R.drawable.glyphicons_two_page_one);
                onCut.onClick(null);
                return;
            }
            DragingDialogs.textToSpeachDialog(anchor, dc);
        }
    };

    View.OnClickListener onThumbnail = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            DragingDialogs.thumbnailDialog(anchor, dc);
        }
    };

    SeekBar.OnSeekBarChangeListener onSeek = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
            dc.onGoToPage(progress + 1);
            updateUI();
        }
    };

    SeekBar.OnSeekBarChangeListener onSpeed = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
            AppState.get().autoScrollSpeed = progress + 1;
            updateSpeedLabel();

            // hideSeekBarInReadMode();
        }
    };

    public void doDoubleTap(int x, int y) {
        if (dc.isMusicianMode()) {
            dc.alignDocument();
        } else {
            if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_ZOOM_IN_OUT) {
                dc.onZoomInOut(x, y);
                AppState.get().isEditMode = false;
                hideShow();
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_ADJUST_PAGE) {
                dc.alignDocument();
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_CENTER_HORIZONTAL) {
                dc.centerHorizontal();
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_AUTOSCROLL) {
                onAutoScrollClick();
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_CLOSE_BOOK) {
                closeAndRunList();
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_CLOSE_HIDE_APP) {
                Apps.showDesctop(a);
            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_START_STOP_TTS) {
                TTSService.playPause(dc.getActivity(), dc);

            } else if (AppState.get().doubleClickAction1 == AppState.DOUBLE_CLICK_CLOSE_BOOK_AND_APP) {
                dc.onCloseActivityFinal(new Runnable() {

                    @Override
                    public void run() {
                        MainTabs2.closeApp(dc.getActivity());
                    }
                });
                dc.closeActivity();
            }
        }
    }

    public void doShowHideWrapperControlls() {
        AppState.get().isEditMode = !AppState.get().isEditMode;
        hideShow();

    }

    public void showHideHavigationBar() {
        if (!AppState.get().isEditMode && AppState.get().isFullScreen) {
            Keyboards.hideNavigation(a);
        }
    }

    public void doChooseNextType(View view) {
        final MyPopupMenu popupMenu = new MyPopupMenu(view.getContext(), view);

        String pages = dc.getString(R.string.by_pages);
        String screen = dc.getString(R.string.of_screen).toLowerCase(Locale.US);
        String screens = dc.getString(R.string.by_screans);
        final List<Integer> values = Arrays.asList(AppState.NEXT_SCREEN_SCROLL_BY_PAGES, 100, 95, 75, 50, 25, 10);

        for (int i = 0; i < values.size(); i++) {
            final int n = i;
            String name = i == AppState.NEXT_SCREEN_SCROLL_BY_PAGES ? pages : values.get(i) + "% " + screen;
            if (values.get(i) == 100) {
                name = screens;
            }

            popupMenu.getMenu().add(name).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    AppState.get().nextScreenScrollBy = values.get(n);
                    initNextType();
                    Keyboards.hideNavigation(dc.getActivity());
                    return false;
                }
            });
        }

        popupMenu.getMenu().add(R.string.custom_value).setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Activity a = dc.getActivity();
                final AlertDialog.Builder builder = new AlertDialog.Builder(a);
                builder.setTitle(R.string.custom_value);

                final CustomSeek myValue = new CustomSeek(a);
                myValue.init(1, 100, AppState.get().nextScreenScrollMyValue);
                myValue.setOnSeekChanged(new IntegerResponse() {

                    @Override
                    public boolean onResultRecive(int result) {
                        AppState.get().nextScreenScrollMyValue = result;
                        myValue.setValueText(AppState.get().nextScreenScrollMyValue + "%");
                        return false;
                    }
                });
                myValue.setValueText(AppState.get().nextScreenScrollMyValue + "%");

                builder.setView(myValue);

                builder.setPositiveButton(R.string.apply, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppState.get().nextScreenScrollBy = AppState.get().nextScreenScrollMyValue;
                        initNextType();
                        Keyboards.hideNavigation(dc.getActivity());

                    }
                });
                builder.setNegativeButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();

                return false;
            }

        });

        popupMenu.show();

    }

    public void doHideShowToolBar() {
        AppState.get().isShowToolBar = !AppState.get().isShowToolBar;
        initToolBarPlusMinus();
    }

    public void initToolBarPlusMinus() {
        if (AppState.get().isShowToolBar) {
            toolBarButton.setImageResource(R.drawable.glyphicons_336_pushpin);
        } else {
            toolBarButton.setImageResource(R.drawable.glyphicons_200_ban);
        }
        if (AppState.get().isEditMode || AppState.get().isShowToolBar) {
            titleBar.setVisibility(View.VISIBLE);
        } else {
            titleBar.setVisibility(View.GONE);
        }

        progressDraw.setVisibility(AppState.get().isShowReadingProgress ? View.VISIBLE : View.GONE);

        toolBarButton.setVisibility(View.VISIBLE);

        batteryLevel.setVisibility(AppState.get().isShowBattery ? View.VISIBLE : View.GONE);
        batteryIcon.setVisibility(AppState.get().isShowBattery ? View.VISIBLE : View.GONE);

        currentTime.setVisibility(AppState.get().isShowTime ? View.VISIBLE : View.GONE);
        clockIcon.setVisibility(AppState.get().isShowTime ? View.VISIBLE : View.GONE);

    }

    public void initNextType() {
        if (AppState.get().nextScreenScrollBy == AppState.NEXT_SCREEN_SCROLL_BY_PAGES) {
            nextTypeBootom.setText(R.string.by_pages);
            nextScreenType.setImageResource(R.drawable.glyphicons_full_page);

        } else {
            if (AppState.get().nextScreenScrollBy == 100) {
                nextTypeBootom.setText(dc.getString(R.string.by_screans));
            } else {
                nextTypeBootom.setText(AppState.get().nextScreenScrollBy + "% " + dc.getString(R.string.of_screen));
            }
            nextScreenType.setImageResource(R.drawable.glyphicons_halp_page);

        }

    }

    public void hideShow() {
        if (AppState.get().isEditMode) {
            DocumentController.turnOnButtons(a);
            show();
        } else {
            DocumentController.turnOffButtons(a);

            hide();
        }
        initToolBarPlusMinus();

        if (AppState.get().isAutoScroll) {
            autoScroll.setImageResource(R.drawable.glyphicons_37_file_pause);
        } else {
            autoScroll.setImageResource(R.drawable.glyphicons_37_file_play);
        }

        if (dc.isMusicianMode()) {
            if (AppState.get().isAutoScroll) {
                seekSpeedLayot.setVisibility(View.VISIBLE);
            } else {
                seekSpeedLayot.setVisibility(View.GONE);
            }
        } else {
            if (AppState.get().isEditMode && AppState.get().isAutoScroll) {
                seekSpeedLayot.setVisibility(View.VISIBLE);
            } else {
                seekSpeedLayot.setVisibility(View.GONE);
            }
        }

        if (dc.isMusicianMode()) {
            lirbiLogo.setVisibility(View.VISIBLE);
        } else {
            lirbiLogo.setVisibility(View.GONE);
        }

        // hideSeekBarInReadMode();
        // showHideHavigationBar();
        DocumentController.chooseFullScreen(dc.getActivity(), AppState.get().isFullScreen);
        showPagesHelper();
    }

    public void hide() {
        menuLayout.setVisibility(View.GONE);
        bottomBar.setVisibility(View.GONE);
        adFrame.setVisibility(View.GONE);
        adFrame.setClickable(false);
        imageMenuArrow.setImageResource(android.R.drawable.arrow_down_float);

        // speedSeekBar.setVisibility(View.GONE);

    }

    public void _hideSeekBarInReadMode() {
        if (!AppState.get().isEditMode) {
            handler.removeCallbacks(hideSeekBar);
            handler.postDelayed(hideSeekBar, 5000);
        }
    }

    Runnable hideSeekBar = new Runnable() {

        @Override
        public void run() {
            if (!dc.isMusicianMode()) {
                seekSpeedLayot.setVisibility(View.GONE);
            }

        }
    };

    public void show() {
        menuLayout.setVisibility(View.VISIBLE);

        titleBar.setVisibility(View.VISIBLE);

        updateLock();

        bottomBar.setVisibility(View.VISIBLE);
        adFrame.setVisibility(View.VISIBLE);
        adFrame.setClickable(true);
        adFrame.setTag(null);

        imageMenuArrow.setImageResource(android.R.drawable.arrow_up_float);

        // if (AppState.get().isAutoScroll &&
        // AppState.get().isEditMode) {
        // seekSpeedLayot.setVisibility(View.VISIBLE);
        // }

    }

    public View.OnClickListener onShowSearch = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            showSearchDialog();
        }

    };

    public void showSearchDialog() {
        if (AppTemp.get().isCut) {
            onModeChange.setImageResource(R.drawable.glyphicons_two_page_one);
            AppTemp.get().isCut = !false;
            onCut.onClick(null);
        }
        if (AppTemp.get().isCrop) {
            onCrop.onClick(null);
        }

        DragingDialogs.searchMenu(anchor, dc, "");
    }

    public View.OnClickListener onAutoScroll = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            onAutoScrollClick();
        }
    };

    public void onAutoScrollClick() {
        AppState.get().isAutoScroll = !AppState.get().isAutoScroll;
        // changeAutoScrollButton();
        dc.onAutoScroll();
        updateUI();
    }

    // public void changeAutoScrollButton() {
    // if (AppState.get().isAutoScroll) {
    // autoScroll.setImageResource(android.R.drawable.ic_media_pause);
    // seekSpeedLayot.setVisibility(View.VISIBLE);
    // } else {
    // autoScroll.setImageResource(android.R.drawable.ic_media_play);
    // seekSpeedLayot.setVisibility(View.GONE);
    // }
    //
    // }

    public View.OnClickListener onLinkHistory = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.onLinkHistory();
            updateUI();
        }
    };

    public View.OnClickListener onShowContext = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            DragingDialogs.showContent(anchor, dc);
        }
    };
    public View.OnClickListener onLockUnlock = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            AppTemp.get().isLocked = !AppTemp.get().isLocked;
            updateLock();
        }
    };

    public View.OnClickListener onShowHideEditPanel = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            if (AppTemp.get().isCrop) {
                onCrop.onClick(null);
            }

            DragingDialogs.editColorsPanel(anchor, dc, drawView, false);
        }
    };

    public View.OnClickListener onBookmarks = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            DragingDialogs.showBookmarksDialog(anchor, dc, new Runnable() {

                @Override
                public void run() {
                    showHideHistory();
                    showPagesHelper();
                }
            });
        }
    };
    public View.OnLongClickListener onBookmarksLong = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(final View arg0) {
            DragingDialogs.addBookmarksLong(anchor, dc);
            showPagesHelper();
            return true;
        }
    };

    public View.OnClickListener onHideShowToolBar = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            LOG.d("DEBUG", "Click");
            doHideShowToolBar();
        }
    };

    View.OnClickListener onGoToPAge1 = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            dc.onScrollY(0);
            updateUI();
        }
    };

    public View.OnClickListener onNormalMode = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            AppTemp.get().readingMode = AppState.READING_MODE_BOOK;
            initUI(a);
            hideShow();
        }
    };

    public View.OnClickListener onNextType = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            LOG.d("DEBUG", "Click");
            doChooseNextType(arg0);
        }
    };

    public View.OnClickListener onMenu = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            LOG.d("DEBUG", "Click");
            doShowHideWrapperControlls();
        }
    };

    public View.OnClickListener onReverseKeys = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            LOG.d("DEBUG", "Click");
            AppState.get().isReverseKeys = !AppState.get().isReverseKeys;
            updateUI();
        }
    };

    public View.OnClickListener onFull = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            AppState.get().isFullScreen = !AppState.get().isFullScreen;
            ((ImageView) v).setImageResource(AppState.get().isFullScreen ? R.drawable.glyphicons_487_fit_frame_to_image : R.drawable.glyphicons_488_fit_image_to_frame);


            if (dc.isTextFormat()) {
                onRefresh.run();
                dc.restartActivity();
            }

            DocumentController.chooseFullScreen(a, AppState.get().isFullScreen);
        }
    };
    public View.OnClickListener onScreenMode = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            a.finish();
            a.startActivity(a.getIntent());
        }
    };

    public View.OnClickListener onBCclick = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            DragingDialogs.contrastAndBrigtness(anchor, dc, new Runnable() {

                @Override
                public void run() {
                    onBC.underline(AppState.get().isEnableBC);
                    dc.updateRendering();
                }
            }, null);
        }
    };

    public View.OnClickListener onSun = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            arg0.setEnabled(false);
            dc.onNightMode();
        }
    };

    public View.OnClickListener toPage = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.toPageDialog();
        }
    };

    public View.OnClickListener onCrop = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            DragingDialogs.customCropDialog(anchor, dc, new Runnable() {

                @Override
                public void run() {
                    dc.onCrop();
                    updateUI();

                    AppState.get().isEditMode = false;
                    hideShow();
                    hideShowEditIcon();
                }
            });
        }
    };
    public View.OnLongClickListener onCropLong = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            AppTemp.get().isCrop = !AppTemp.get().isCrop;

            dc.onCrop();
            updateUI();

            AppState.get().isEditMode = false;
            hideShow();
            hideShowEditIcon();
            return true;
        }
    };

    private boolean closeDialogs() {
        return dc.closeDialogs();
    }

    public View.OnClickListener onModeChangeClick = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            MyPopupMenu p = new MyPopupMenu(v.getContext(), v);

            p.getMenu().add(R.string.one_page).setIcon(R.drawable.glyphicons_two_page_one).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    closeDialogs();
                    onModeChange.setImageResource(R.drawable.glyphicons_two_page_one);
                    AppTemp.get().isCut = !false;
                    onCut.onClick(null);
                    hideShowEditIcon();
                    return false;
                }
            });
            p.getMenu().add(R.string.half_page).setIcon(R.drawable.glyphicons_page_split).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    closeDialogs();
                    onModeChange.setImageResource(R.drawable.glyphicons_page_split);
                    AppTemp.get().isCut = !true;
                    onCut.onClick(null);
                    hideShowEditIcon();
                    return false;
                }
            });
            p.show();
            Keyboards.hideNavigation(dc.getActivity());

        }
    };
    public View.OnClickListener onCut = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            AppTemp.get().isCrop = false; // no crop with cut
            AppState.get().cutP = 50;
            AppTemp.get().isCut = !AppTemp.get().isCut;

            crop.setVisibility(AppTemp.get().isCut ? View.GONE : View.VISIBLE);


            dc.onCrop();// crop false
            dc.updateRendering();
            dc.alignDocument();

            updateUI();

            progressDraw.updatePageCount(dc.getPageCount() - 1);
            titleBar.setOnTouchListener(new HorizontallSeekTouchEventListener(onSeek, dc.getPageCount(), false));
            progressDraw.setOnTouchListener(new HorizontallSeekTouchEventListener(onSeek, dc.getPageCount(), false));

        }
    };

    public View.OnClickListener onPrefTop = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            DragingDialogs.preferences(anchor, dc, onRefresh, new Runnable() {

                @Override
                public void run() {
                    updateUI();

                }
            });
        }
    };

    Runnable onRefresh = new Runnable() {

        @Override
        public void run() {
            dc.saveCurrentPageAsync();
            initToolBarPlusMinus();
            updateSeekBarColorAndSize();
            hideShow();
            updateUI();
            TTSEngine.get().stop();
            BrightnessHelper.updateOverlay(overlay);
            showPagesHelper();
            hideShowPrevNext();
        }
    };

    public View.OnClickListener onClose = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            ImageLoader.getInstance().clearAllTasks();
            closeDialogs();
            closeAndRunList();
        }
    };
    public View.OnLongClickListener onCloseLongClick = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(final View v) {
            Vibro.vibrate();
            CloseAppDialog.showOnLongClickDialog(a, v, getController());
            hideAds();
            return true;
        }

        ;
    };

    public void hideAds() {
        adFrame.setTag("");
        adFrame.setVisibility(View.GONE);
    }

    public View.OnClickListener onMoveLeft = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.onSrollLeft();
        }
    };

    public View.OnClickListener onMoveCenter = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.alignDocument();
        }
    };

    public View.OnClickListener onMoveRight = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.onSrollRight();
        }
    };

    public View.OnClickListener onNextPage = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            nextChose(false);
        }
    };

    public void nextChose(boolean animate) {
        nextChose(animate, 0);
    }

    public void nextChose(boolean animate, int repeatCount) {
        LOG.d("nextChose");
        dc.checkReadingTimer();

        if (AppState.get().nextScreenScrollBy == AppState.NEXT_SCREEN_SCROLL_BY_PAGES) {
            dc.onNextPage(animate);
        } else {
            if (AppState.get().nextScreenScrollBy <= 50 && repeatCount == 0) {
                animate = true;
            }
            dc.onNextScreen(animate);
        }
        if (AppState.get().isEditMode) {
            AppState.get().isEditMode = false;
        }
        updateUI();

    }

    public void prevChose(boolean animate) {
        prevChose(animate, 0);
    }

    public void prevChose(boolean animate, int repeatCount) {
        dc.checkReadingTimer();

        if (AppState.get().nextScreenScrollBy == AppState.NEXT_SCREEN_SCROLL_BY_PAGES) {
            dc.onPrevPage(animate);
        } else {
            if (AppState.get().nextScreenScrollBy <= 50 && repeatCount == 0) {
                animate = true;
            }
            dc.onPrevScreen(animate);
        }
        if (AppState.get().isEditMode) {
            AppState.get().isEditMode = false;
        }
        updateUI();
    }

    public View.OnClickListener onPrevPage = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            prevChose(false);
        }
    };

    public View.OnClickListener onPlus = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.onZoomInc();
        }
    };
    public View.OnClickListener onMinus = new View.OnClickListener() {

        @Override
        public void onClick(final View arg0) {
            dc.onZoomDec();
        }
    };

    public void setTitle(final String title) {
        this.bookTitle = title;
        hideShowEditIcon();

    }

    public void hideShowEditIcon() {
        if (dc != null && !BookType.PDF.is(dc.getCurrentBook().getPath())) {
            editTop2.setVisibility(View.GONE);
        } else if (AppTemp.get().isCrop || AppTemp.get().isCut) {
            editTop2.setVisibility(View.GONE);
        } else {
            boolean passwordProtected = dc.isPasswordProtected();
            LOG.d("passwordProtected", passwordProtected);
            if (dc != null && passwordProtected) {
                editTop2.setVisibility(View.GONE);
            } else {
                editTop2.setVisibility(View.VISIBLE);
            }
        }

    }

    public DocumentController getController() {
        return dc;
    }

    public DrawView getDrawView() {
        return drawView;
    }

    public void showHelp() {
        if (AppTemp.get().isFirstTimeVertical) {
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    AppTemp.get().isFirstTimeVertical = false;
                    AppState.get().isEditMode = true;
                    hideShow();
                    Views.showHelpToast(lockUnlock);

                }
            }, 1000);
        }
    }

    public void showPagesHelper() {
        try {
            BookmarkPanel.showPagesHelper(pageshelper, musicButtonPanel, dc, pagesBookmark, quickBookmark);
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public void showOutline(final List<OutlineLinkWrapper> list, final int count) {
        try {
            dc.activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    progressDraw.updateDivs(list);
                    progressDraw.updatePageCount(dc.getPageCount() - 1);
                    titleBar.setOnTouchListener(new HorizontallSeekTouchEventListener(onSeek, dc.getPageCount(), false));
                    progressDraw.setOnTouchListener(new HorizontallSeekTouchEventListener(onSeek, dc.getPageCount(), false));
                    if (TxtUtils.isListEmpty(list)) {
                        TintUtil.setTintImageWithAlpha(onDocDontext, Color.LTGRAY);
                    }

                    if (ExtUtils.isNoTextLayerForamt(dc.getCurrentBook().getPath())) {
                        TintUtil.setTintImageWithAlpha(textToSpeach, Color.LTGRAY);
                    }
                    if (dc.isTextFormat()) {
                        // TintUtil.setTintImage(lockUnlock, Color.LTGRAY);
                    }

                    currentSeek.setVisibility(View.VISIBLE);
                    maxSeek.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);

                    onCloseBook.setVisibility(View.VISIBLE);
                    pagesCountIndicator.setVisibility(View.VISIBLE);

                    showHelp();

                    hideShowEditIcon();

                    updateSpeedLabel();

                    DialogsPlaylist.dispalyPlaylist(a, dc);
                    HypenPanelHelper.init(parentParent, dc);


                    showPagesHelper();

                }
            });
        } catch (Exception e) {
            LOG.e(e);
        }

    }

    public void onResume() {
        LOG.d("DocumentWrapperUI", "onResume");
        handlerTimer.post(updateTimePower);

        if (dc != null) {
            dc.goToPageByTTS();
        }

        if (ttsActive != null) {
            ttsActive.setVisibility(TxtUtils.visibleIf(TTSEngine.get().isPlaying()));
        }

    }

    public void onPause() {
        LOG.d("DocumentWrapperUI", "onPause");
        handlerTimer.removeCallbacks(updateTimePower);

    }

    public void onDestroy() {
        LOG.d("DocumentWrapperUI", "onDestroy");
        handlerTimer.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);

    }

    public void onConfigChanged() {
        try {
            updateUI();
        } catch (Exception e) {
            LOG.e(e);
        }
    }

}
