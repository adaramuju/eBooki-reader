package net.autogroup.ui2.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.buzzingandroid.ui.HSVColorPickerDialog;
import com.buzzingandroid.ui.HSVColorPickerDialog.OnColorSelectedListener;
import net.autogroup.android.utils.Apps;
import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.IntegerResponse;
import net.autogroup.android.utils.Keyboards;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.ResultResponse2;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.drive.GFile;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.AndroidWhatsNew;
import net.autogroup.pdf.info.AppsConfig;
import net.autogroup.pdf.info.BookmarksData;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.PasswordDialog;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.pdf.info.Urls;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.pdf.info.view.AlertDialogs;
import net.autogroup.pdf.info.view.BrightnessHelper;
import net.autogroup.pdf.info.view.CustomSeek;
import net.autogroup.pdf.info.view.Dialogs;
import net.autogroup.pdf.info.view.KeyCodeDialog;
import net.autogroup.pdf.info.view.MultyDocSearchDialog;
import net.autogroup.pdf.info.view.MyPopupMenu;
import net.autogroup.pdf.info.widget.ChooserDialogFragment;
import net.autogroup.pdf.info.widget.ColorsDialog;
import net.autogroup.pdf.info.widget.ColorsDialog.ColorsDialogResult;
import net.autogroup.pdf.info.widget.DialogTranslateFromTo;
import net.autogroup.pdf.info.widget.PrefDialogs;
import net.autogroup.pdf.info.widget.RecentUpates;
import net.autogroup.pdf.info.widget.ShareDialog;
import net.autogroup.pdf.info.wrapper.DocumentController;
import net.autogroup.pdf.info.wrapper.PasswordState;
import net.autogroup.pdf.info.wrapper.UITab;
import net.autogroup.pdf.search.activity.msg.GDriveSycnEvent;
import net.autogroup.pdf.search.activity.msg.MessageSync;
import net.autogroup.sys.TempHolder;
import net.autogroup.ui2.AppDB;
import net.autogroup.ui2.BooksService;
import net.autogroup.ui2.MainTabs2;
import net.autogroup.ui2.MyContextWrapper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PrefFragment2 extends UIFragment {
    public static final Pair<Integer, Integer> PAIR = new Pair<Integer, Integer>(R.string.preferences, R.drawable.glyphicons_281_settings);

    private static final String WWW_SITE = "http://librera.mobi";
    private static final String WWW_BETA_SITE = "http://beta.librera.mobi";
    private static final String WWW_WIKI_SITE = "http://wiki.librera.mobi";
    private TextView curBrightness, themeColor, profileLetter;
    private CheckBox isRememberDictionary;

    @Override
    public Pair<Integer, Integer> getNameAndIconRes() {
        return PAIR;
    }

    @Override
    public boolean isBackPressed() {
        return false;
    }

    @Override
    public void notifyFragment() {
    }

    @Override
    public void resetFragment() {
    }


    @Override
    public void onTintChanged() {
        TintUtil.setStatusBarColor(getActivity(), TintUtil.color);
        TintUtil.setBackgroundFillColor(section1, TintUtil.color);
        TintUtil.setBackgroundFillColor(section2, TintUtil.color);
        TintUtil.setBackgroundFillColor(section3, TintUtil.color);
        TintUtil.setBackgroundFillColor(section4, TintUtil.color);
        TintUtil.setBackgroundFillColor(section5, TintUtil.color);
        TintUtil.setBackgroundFillColor(section6, TintUtil.color);
        TintUtil.setBackgroundFillColor(section7, TintUtil.color);
        TintUtil.setBackgroundFillColor(section8, TintUtil.color);
        TintUtil.setBackgroundFillColor(section9, TintUtil.color);

        if (profileLetter != null) {
            final String p = AppProfile.getCurrent(getActivity());
            profileLetter.setText(TxtUtils.getFirstLetter(p));
            profileLetter.setBackgroundDrawable(AppProfile.getProfileColorDrawable(getActivity(), TintUtil.color));
        }

        if (AppState.get().appTheme == AppState.THEME_INK) {
            TxtUtils.setInkTextView(inflate.getRootView());
        }


    }

    View section1, section2, section3, section4, section5, section6, section7, section8, section9, overlay;


    @Subscribe
    public void updateSyncInfo(GDriveSycnEvent event) {
        String gdriveInfo = GFile.getDisplayInfo(getActivity());

        if (TxtUtils.isEmpty(gdriveInfo)) {
            BookCSS.get().isEnableSync = false;
            syncInfo.setVisibility(View.GONE);
            singIn.setText(R.string.sign_in);
            TxtUtils.underlineTextView(singIn);
            singIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    GFile.init(getActivity());
                    updateSyncInfo(null);
                }
            });
        } else {
            syncInfo.setVisibility(View.VISIBLE);
            syncInfo.setText(gdriveInfo);
            singIn.setText(R.string.sign_out);
            TxtUtils.underlineTextView(singIn);

            singIn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    BookCSS.get().isEnableSync = false;
                    BookCSS.get().syncRootID = "";
                    AppTemp.get().syncTime = 0;
                    GFile.logout(getActivity());
                    updateSyncInfo(null);
                }
            });
        }

        isEnableSync.setChecked(BookCSS.get().isEnableSync);
        onSync(null);


    }

    TextView singIn, syncInfo, syncHeader;
    CheckBox isEnableSync;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSync(MessageSync msg) {
        if (AppTemp.get().syncTime > 0) {

            final Date date = new Date(AppTemp.get().syncTime);
            String format = "";
            if (DateUtils.isToday(AppTemp.get().syncTime)) {
                format = getString(R.string.today) + " " + DateFormat.getTimeInstance().format(date);
            } else {
                format = DateFormat.getDateTimeInstance().format(date);
            }

            String status = AppTemp.get().syncTimeStatus == MessageSync.STATE_SUCCESS ? getString(R.string.success) : getString(R.string.fail);
            if (AppTemp.get().syncTimeStatus == MessageSync.STATE_VISIBLE) {
                status = "...";
            }

            syncHeader.setText(getString(R.string.sync) + " (" + format + " - " + status + ")");
        } else {
            syncHeader.setText(R.string.sync_google_drive);

        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.preferences, container, false);


        singIn = inflate.findViewById(R.id.signIn);
        syncInfo = inflate.findViewById(R.id.syncInfo);
        syncHeader = inflate.findViewById(R.id.syncHeader);
        onSync(null);
        syncHeader.setOnClickListener((in) -> Dialogs.showSyncLOGDialog(getActivity()));


        isEnableSync = inflate.findViewById(R.id.isEnableSync);
        isEnableSync.setChecked(BookCSS.get().isEnableSync);
        isEnableSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            BookCSS.get().isEnableSync = isChecked;
            if (isChecked) {
                if (GoogleSignIn.getLastSignedInAccount(getActivity()) == null) {
                    GFile.init(getActivity());
                } else {
                    GFile.runSyncService(getActivity());
                }
            }
        });

        inflate.findViewById(R.id.isEnableSyncSettings).setOnClickListener(v -> {
            final CheckBox isSyncManualOnly = new CheckBox(getActivity());
            isSyncManualOnly.setText(R.string.manual_sync_only);
            isSyncManualOnly.setChecked(BookCSS.get().isSyncManualOnly);
            isSyncManualOnly.setOnCheckedChangeListener((buttonView, isChecked) -> BookCSS.get().isSyncManualOnly = isChecked);

            final CheckBox isSyncWifiOnly = new CheckBox(getActivity());
            isSyncWifiOnly.setText(R.string.wifi_sync_only);
            isSyncWifiOnly.setChecked(BookCSS.get().isSyncWifiOnly);
            isSyncWifiOnly.setOnCheckedChangeListener((buttonView, isChecked) -> BookCSS.get().isSyncWifiOnly = isChecked);

            final CheckBox isShowSyncWheel = new CheckBox(getActivity());
            isShowSyncWheel.setText(getString(R.string.show_sync_wheel));
            isShowSyncWheel.setChecked(BookCSS.get().isShowSyncWheel);
            isShowSyncWheel.setOnCheckedChangeListener((buttonView, isChecked) -> BookCSS.get().isShowSyncWheel = isChecked);

            AlertDialogs.showViewDialog(getActivity(), null, isSyncManualOnly, isSyncWifiOnly, isShowSyncWheel);
        });
        updateSyncInfo(null);


        section8 = inflate.findViewById(R.id.section8);
        section9 = inflate.findViewById(R.id.section9);


        // tabs position
        final DragLinearLayout dragLinearLayout = (DragLinearLayout) inflate.findViewById(R.id.dragLinearLayout);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(Dips.dpToPx(2), Dips.dpToPx(2), Dips.dpToPx(2), Dips.dpToPx(2));

        final Handler handler = new Handler();
        final Runnable ask2 = new Runnable() {

            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }

                AlertDialogs.showDialog(getActivity(), getActivity().getString(R.string.you_neet_to_apply_the_new_settings), getString(R.string.ok), new Runnable() {
                    @Override
                    public void run() {
                        inflate.findViewById(R.id.tabsApply).performClick();
                    }
                }, null);
            }
        };

        final int timeout = 1500;
        final CheckBox isshowPrefAsMenu = (CheckBox) inflate.findViewById(R.id.isshowPrefAsMenu);

        final Runnable dragLinear = new Runnable() {

            @Override
            public void run() {
                dragLinearLayout.removeAllViews();
                for (UITab tab : UITab.getOrdered(AppState.get().tabsOrder7)) {
                    if (Apps.getVersionName(getActivity()).endsWith("-fdroid") && tab == UITab.CloudsFragment) {
                        continue;
                    }

                    View library = LayoutInflater.from(getActivity()).inflate(R.layout.item_tab_line, null, false);
                    if (AppState.get().appTheme == AppState.THEME_DARK_OLED || AppState.get().appTheme == AppState.THEME_DARK) {
                        library.setBackgroundColor(Color.BLACK);
                    }

                    ((TextView) library.findViewById(R.id.text1)).setText(tab.getName());
                    ((CheckBox) library.findViewById(R.id.isVisible)).setChecked(tab.isVisible());
                    ((CheckBox) library.findViewById(R.id.isVisible)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            //Log.d("MUNYUK", "1");
                            //handler.removeCallbacks(ask2);
                            //handler.postDelayed(ask2, timeout);

                            if(tab == UITab.PrefFragment){
                                isshowPrefAsMenu.setChecked(!isChecked);
                            }
                        }
                    });
                    ((ImageView) library.findViewById(R.id.image1)).setImageResource(tab.getIcon());
                    TintUtil.setTintImageWithAlpha(((ImageView) library.findViewById(R.id.image1)), TintUtil.COLOR_TINT_GRAY);
                    library.setTag(tab.getIndex());
                    dragLinearLayout.addView(library, layoutParams);
                }

                for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
                    View child = dragLinearLayout.getChildAt(i);
                    View handle = child.findViewById(R.id.imageDrag);
                    dragLinearLayout.setViewDraggable(child, handle);
                }
            }
        };
        dragLinear.run();
        TxtUtils.underlineTextView(inflate.findViewById(R.id.tabsApply)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.removeCallbacks(ask2);
                AppState.get().tabsOrder7 = "";
                for (int i = 0; i < dragLinearLayout.getChildCount(); i++) {
                    View child = dragLinearLayout.getChildAt(i);
                    boolean isVisible = ((CheckBox) child.findViewById(R.id.isVisible)).isChecked();
                    AppState.get().tabsOrder7 += child.getTag() + "#" + (isVisible ? "1" : "0") + ",";
                }
                AppState.get().tabsOrder7 = TxtUtils.replaceLast(AppState.get().tabsOrder7, ",", "");
                LOG.d("tabsApply", AppState.get().tabsOrder7);
                AppProfile.save(getActivity());
                onTheme();
            }
        });

        /*
        isshowPrefAsMenu.setChecked(AppState.get().tabsOrder7.contains(UITab.PrefFragment.index + "#0"));
        isshowPrefAsMenu.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("MUNYUK", "3");
                handler.removeCallbacksAndMessages(ask2);
                handler.postDelayed(ask2, timeout);
                if (isChecked) {
                    AppState.get().tabsOrder7 = AppState.get().tabsOrder7.replace(UITab.PrefFragment.index + "#1", UITab.PrefFragment.index + "#0");
                } else {
                    AppState.get().tabsOrder7 = AppState.get().tabsOrder7.replace(UITab.PrefFragment.index + "#0", UITab.PrefFragment.index + "#1");
                }
                dragLinear.run();
            }
        });
        */

        TxtUtils.underlineTextView(inflate.findViewById(R.id.tabsDefaul)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialogs.showOkDialog(getActivity(), getActivity().getString(R.string.restore_defaults_full), new Runnable() {

                    @Override
                    public void run() {
                        AppState.get().tabsOrder7 = AppState.DEFAULTS_TABS_ORDER;
                        isshowPrefAsMenu.setChecked(false);
                        AppProfile.save(getActivity());
                        onTheme();
                    }
                });

            }
        });

        // tabs position

        section1 = inflate.findViewById(R.id.section1);
        section2 = inflate.findViewById(R.id.section2);
        section3 = inflate.findViewById(R.id.section3);
        section4 = inflate.findViewById(R.id.section4);
        section5 = inflate.findViewById(R.id.section5);
        section6 = inflate.findViewById(R.id.section6);
        section7 = inflate.findViewById(R.id.section7);


        onTintChanged();

        final int max = Dips.pxToDp(Dips.screenMinWH() / 2) - 2 * 4;

        final CustomSeek coverSmallSize = (CustomSeek) inflate.findViewById(R.id.coverSmallSize);
        coverSmallSize.init(40, max, AppState.get().coverSmallSize);

        coverSmallSize.setOnSeekChanged(new IntegerResponse() {

            @Override
            public boolean onResultRecive(int result) {
                TempHolder.listHash++;
                AppState.get().coverSmallSize = result;
                return false;
            }
        });

        final CustomSeek coverBigSize = (CustomSeek) inflate.findViewById(R.id.coverBigSize);
        coverBigSize.init(40, Math.max(max, AppState.get().coverBigSize), AppState.get().coverBigSize);
        coverBigSize.setOnSeekChanged(new IntegerResponse() {

            @Override
            public boolean onResultRecive(int result) {
                TempHolder.listHash++;
                AppState.get().coverBigSize = result;
                return false;
            }
        });

        final TextView columsCount = (TextView) inflate.findViewById(R.id.columsCount);
        columsCount.setText("" + Dips.screenWidthDP() / AppState.get().coverBigSize);
        TxtUtils.underlineTextView(columsCount);
        columsCount.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getContext(), columsCount);
                for (int i = 1; i <= 8; i++) {
                    final int k = i;
                    p.getMenu().add("" + k).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int result = Dips.screenWidthDP() / k - 8;

                            TempHolder.listHash++;
                            AppState.get().coverBigSize = result;

                            columsCount.setText("" + k);
                            TxtUtils.underlineTextView(columsCount);

                            coverBigSize.init(40, Math.max(max, AppState.get().coverBigSize), AppState.get().coverBigSize);
                            return false;
                        }
                    });
                }

                p.show();
            }
        });
        final TextView columsDefaul = (TextView) inflate.findViewById(R.id.columsDefaul);
        TxtUtils.underlineTextView(columsDefaul);
        columsDefaul.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (getActivity() == null) {
                    return;
                }

                AlertDialogs.showOkDialog(getActivity(), getActivity().getString(R.string.restore_defaults_full), new Runnable() {

                    @Override
                    public void run() {
                        IMG.clearDiscCache();
                        IMG.clearMemoryCache();
                        AppState.get().coverBigSize = (int) (((Dips.screenWidthDP() / (Dips.screenWidthDP() / 120)) - 8) * (Dips.isXLargeScreen() ? 1.5f : 1));
                        AppState.get().coverSmallSize = 80;
                        TempHolder.listHash++;

                        columsCount.setText("" + Dips.screenWidthDP() / AppState.get().coverBigSize);
                        TxtUtils.underlineTextView(columsCount);

                        coverSmallSize.init(40, max, AppState.get().coverSmallSize);
                        coverBigSize.init(40, Math.max(max, AppState.get().coverBigSize), AppState.get().coverBigSize);
                    }
                });

            }
        });

        final ScrollView scrollView = (ScrollView) inflate.findViewById(R.id.scroll);
        scrollView.setVerticalScrollBarEnabled(false);

        if (AppState.get().appTheme == AppState.THEME_DARK_OLED) {
            scrollView.setBackgroundColor(Color.BLACK);
        }

        ((TextView) inflate.findViewById(R.id.section6)).setText(String.format("%s: %s", getString(R.string.product), Apps.getApplicationName(getActivity())));
        // ((TextView) findViewById(R.id.appName)).setText(AppsConfig.APP_NAME);

        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            final String version = packageInfo.versionName;
            // ((TextView) inflate.findViewById(R.id.pVersion)).setText(String.format("%s:
            // %s (%s)", getString(R.string.version), version, AppsConfig.MUPDF_VERSION));
            ((TextView) inflate.findViewById(R.id.pVersion)).setText(String.format("%s: %s", getString(R.string.version), version));
        } catch (final NameNotFoundException e) {
        }

        TextView onCloseApp = (TextView) inflate.findViewById(R.id.onCloseApp);
        TxtUtils.underlineTextView(onCloseApp);
        onCloseApp.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainTabs2) getActivity()).showInterstial();
            }
        });

        final TextView onFullScreen = (TextView) inflate.findViewById(R.id.fullscreen);
        onFullScreen.setText(AppState.get().isFullScreenMain ? R.string.yes : R.string.no);
        TxtUtils.underlineTextView(onFullScreen);

        onFullScreen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                MyPopupMenu popup = new MyPopupMenu(getActivity(), v);
                popup.getMenu().add(R.string.yes).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().isFullScreenMain = true;

                        onFullScreen.setText(R.string.yes);
                        TxtUtils.underlineTextView(onFullScreen);
                        DocumentController.chooseFullScreen(getActivity(), AppState.get().isFullScreenMain);
                        return false;
                    }
                });

                popup.getMenu().add(R.string.no).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().isFullScreenMain = false;

                        onFullScreen.setText(R.string.no);
                        TxtUtils.underlineTextView(onFullScreen);
                        DocumentController.chooseFullScreen(getActivity(), AppState.get().isFullScreenMain);
                        return false;
                    }
                });

                popup.show();

            }
        });

        final TextView tapPositionTop = (TextView) inflate.findViewById(R.id.tapPositionTop);
        tapPositionTop.setText(AppState.get().tapPositionTop ? R.string.top : R.string.bottom);
        TxtUtils.underlineTextView(tapPositionTop);

        tapPositionTop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                MyPopupMenu popup = new MyPopupMenu(getActivity(), v);
                popup.getMenu().add(R.string.top).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().tapPositionTop = true;
                        onTheme();
                        return false;
                    }
                });

                popup.getMenu().add(R.string.bottom).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().tapPositionTop = false;
                        onTheme();
                        return false;
                    }
                });

                popup.show();

            }
        });

        screenOrientation = (TextView) inflate.findViewById(R.id.screenOrientation);
        screenOrientation.setText(DocumentController.getRotationText());
        TxtUtils.underlineTextView(screenOrientation);

        screenOrientation.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(v.getContext(), v);
                for (int i = 0; i < DocumentController.orientationIds.size(); i++) {
                    final int j = i;
                    final int name = DocumentController.orientationTexts.get(i);
                    menu.getMenu().add(name).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AppState.get().orientation = DocumentController.orientationIds.get(j);
                            screenOrientation.setText(DocumentController.orientationTexts.get(j));
                            TxtUtils.underlineTextView(screenOrientation);
                            DocumentController.doRotation(getActivity());
                            return false;
                        }
                    });
                }
                menu.show();
            }
        });

        // inflate.findViewById(R.id.onHelpTranslate).setOnClickListener(new
        // OnClickListener() {
        //
        // @Override
        // public void onClick(final View v) {
        // Urls.open(getActivity(),
        // "https://www.dropbox.com/sh/8el7kon2sbx46w8/xm3qoHYT7n");
        // }
        // });

        inflate.findViewById(R.id.onKeyCode).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                new KeyCodeDialog(getActivity(), onCloseDialog);
            }
        });

        themeColor = (TextView) inflate.findViewById(R.id.themeColor);
        themeColor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {

                PopupMenu p = new PopupMenu(getContext(), themeColor);
                p.getMenu().add(R.string.light).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().appTheme = AppState.THEME_LIGHT;

                        AppState.get().contrastImage = 0;
                        AppState.get().brigtnessImage = 0;
                        AppState.get().bolderTextOnImage = false;
                        AppState.get().isEnableBC = false;

                        AppProfile.save(getActivity());

                        IMG.clearDiscCache();
                        IMG.clearMemoryCache();
                        onTheme();

                        return false;
                    }
                });
                p.getMenu().add(R.string.black).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().appTheme = AppState.THEME_DARK;

                        AppState.get().contrastImage = 0;
                        AppState.get().brigtnessImage = 0;
                        AppState.get().bolderTextOnImage = false;
                        AppState.get().isEnableBC = false;

                        AppProfile.save(getActivity());

                        IMG.clearDiscCache();
                        IMG.clearMemoryCache();

                        onTheme();
                        return false;
                    }
                });
                p.getMenu().add(R.string.dark_oled).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AppState.get().appTheme = AppState.THEME_DARK_OLED;

                        AppState.get().contrastImage = 0;
                        AppState.get().brigtnessImage = 0;
                        AppState.get().bolderTextOnImage = false;
                        AppState.get().isEnableBC = false;

                        AppProfile.save(getActivity());

                        IMG.clearDiscCache();
                        IMG.clearMemoryCache();

                        onTheme();
                        return false;
                    }
                });
                p.getMenu().add("Ink").setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        IMG.clearDiscCache();
                        IMG.clearMemoryCache();

                        onEink();
                        return false;
                    }
                });
                p.show();
            }
        });

        final TextView hypenLang = (TextView) inflate.findViewById(R.id.appLang);
        hypenLang.setText(DialogTranslateFromTo.getLanuageByCode(AppState.get().appLang));
        TxtUtils.underlineTextView(hypenLang);

        hypenLang.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

                final List<String> codes = Arrays.asList(//
                        "en", "ar", "de", "es", "fa", "fi", "fr", "he", //
                        "hi", "hu", "id", "it", "ja", "ko", "la", "lt", //
                        "nl", "no", "pl", "pt", "ro", "ru", "sk", "sv", //
                        "sw", "th", "tr", "uk", "vi", DialogTranslateFromTo.CHINESE_SIMPLE, DialogTranslateFromTo.CHINESE_TRADITIOANAL);
                List<String> langs = new ArrayList<String>();
                for (String code : codes) {
                    langs.add(DialogTranslateFromTo.getLanuageByCode(code) + ":" + code);
                }
                Collections.sort(langs);

                popupMenu.getMenu().add(R.string.system_language).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        TxtUtils.underlineTextView(hypenLang);
                        AppState.get().appLang = AppState.MY_SYSTEM_LANG;
                        TempHolder.get().forseAppLang = true;
                        MyContextWrapper.wrap(getContext());
                        AppProfile.save(getActivity());
                        onTheme();
                        return false;
                    }
                });

                for (int i = 0; i < langs.size(); i++) {
                    String all[] = langs.get(i).split(":");
                    final String name = all[0];
                    final String code = all[1];
                    popupMenu.getMenu().add(name).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AppState.get().appLang = code;
                            TxtUtils.underlineTextView(hypenLang);
                            AppProfile.save(getActivity());
                            onTheme();
                            return false;
                        }
                    });
                }
                popupMenu.show();

            }
        });

        final TextView appFontScale = (TextView) inflate.findViewById(R.id.appFontScale);
        appFontScale.setText(getFontName(BookCSS.get().appFontScale));
        TxtUtils.underlineTextView(appFontScale);
        appFontScale.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                for (float i = 0.7f; i < 2.1f; i += 0.1) {
                    final float number = i;
                    popupMenu.getMenu().add(getFontName(number)).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            BookCSS.get().appFontScale = number;
                            AppProfile.save(getActivity());
                            onTheme();
                            return false;
                        }
                    });
                }
                popupMenu.show();
            }
        });

        final TextView onMail = (TextView) inflate.findViewById(R.id.onMailSupport);
        onMail.setText(TxtUtils.underline(getString(R.string.my_email)));

        onMail.setOnClickListener(new OnClickListener() {
            @Override

            public void onClick(final View v) {
                onEmail();
            }
        });

        rememberMode = (CheckBox) inflate.findViewById(R.id.isRememberMode);
        rememberMode.setChecked(AppState.get().isRememberMode);
        rememberMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isRememberMode = isChecked;
            }
        });

        selectedOpenMode = (TextView) inflate.findViewById(R.id.selectedOpenMode);
        selectedOpenMode.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT <= 10) {
                    Toast.makeText(selectedOpenMode.getContext(), R.string.this_function_will_works_in_modern_android, Toast.LENGTH_SHORT).show();
                    return;
                }
                final PopupMenu popupMenu = new PopupMenu(selectedOpenMode.getContext(), selectedOpenMode);

                final MenuItem advanced_mode = popupMenu.getMenu().add(AppState.get().nameVerticalMode);
                advanced_mode.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppTemp.get().readingMode = AppState.READING_MODE_SCROLL;
                        checkOpenWithSpinner();
                        return false;
                    }
                });

                final MenuItem easy_mode = popupMenu.getMenu().add(AppState.get().nameHorizontalMode);
                easy_mode.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppTemp.get().readingMode = AppState.READING_MODE_BOOK;
                        checkOpenWithSpinner();
                        return false;
                    }
                });
                final MenuItem music_mode = popupMenu.getMenu().add(AppState.get().nameMusicianMode);
                music_mode.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppTemp.get().readingMode = AppState.READING_MODE_MUSICIAN;
                        checkOpenWithSpinner();
                        return false;
                    }
                });
                final MenuItem tags = popupMenu.getMenu().add(getString(R.string.tag_manager));
                tags.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppTemp.get().readingMode = AppState.READING_MODE_TAG_MANAGER;
                        checkOpenWithSpinner();
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

        inflate.findViewById(R.id.moreModeSettings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_reading_modes, null, false);
                builder.setView(view);

                EditText prefScrollMode = view.findViewById(R.id.prefScrollMode);
                EditText prefBookMode = view.findViewById(R.id.prefBookMode);
                EditText prefMusicianMode = view.findViewById(R.id.prefMusicianMode);

                prefScrollMode.setText(AppState.get().prefScrollMode);
                prefBookMode.setText(AppState.get().prefBookMode);
                prefMusicianMode.setText(AppState.get().prefMusicianMode);

                CheckBox isPrefFormatMode = view.findViewById(R.id.isPrefFormatMode);
                isPrefFormatMode.setChecked(AppState.get().isPrefFormatMode);

                view.findViewById(R.id.prefRestore).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialogs.showDialog(getActivity(), getActivity().getString(R.string.restore_defaults_full), getString(R.string.ok), new Runnable() {

                            @Override
                            public void run() {
                                AppState.get().isPrefFormatMode = false;
                                AppState.get().prefScrollMode = AppState.PREF_SCROLL_MODE;
                                AppState.get().prefBookMode = AppState.PREF_BOOK_MODE;
                                AppState.get().prefMusicianMode = AppState.PREF_MUSIC_MODE;

                                isPrefFormatMode.setChecked(AppState.get().isPrefFormatMode);
                                prefScrollMode.setText(AppState.get().prefScrollMode);
                                prefBookMode.setText(AppState.get().prefBookMode);
                                prefMusicianMode.setText(AppState.get().prefMusicianMode);
                            }
                        }, null);


                    }
                });

                builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {
                        Keyboards.close(prefScrollMode);
                        AppState.get().isPrefFormatMode = isPrefFormatMode.isChecked();
                        AppState.get().prefScrollMode = prefScrollMode.getText().toString();
                        AppState.get().prefBookMode = prefBookMode.getText().toString();
                        AppState.get().prefMusicianMode = prefMusicianMode.getText().toString();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int id) {

                    }
                });
                builder.show();
            }
        });

        checkOpenWithSpinner();

        final CheckBox isCropBookCovers = (CheckBox) inflate.findViewById(R.id.isCropBookCovers);
        isCropBookCovers.setOnCheckedChangeListener(null);
        isCropBookCovers.setChecked(AppState.get().isCropBookCovers);
        isCropBookCovers.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isCropBookCovers = isChecked;
                TempHolder.listHash++;

            }
        });

        final CheckBox isBookCoverEffect = (CheckBox) inflate.findViewById(R.id.isBookCoverEffect);
        isBookCoverEffect.setOnCheckedChangeListener(null);
        isBookCoverEffect.setChecked(AppState.get().isBookCoverEffect);
        isBookCoverEffect.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isBookCoverEffect = isChecked;
                ImageLoader.getInstance().clearMemoryCache();
                ImageLoader.getInstance().clearDiskCache();
                TempHolder.listHash++;
                if (isChecked) {
                    isCropBookCovers.setEnabled(false);
                    isCropBookCovers.setChecked(true);
                } else {
                    isCropBookCovers.setEnabled(true);
                }
            }
        });

        final CheckBox isBorderAndShadow = (CheckBox) inflate.findViewById(R.id.isBorderAndShadow);
        isBorderAndShadow.setOnCheckedChangeListener(null);
        isBorderAndShadow.setChecked(AppState.get().isBorderAndShadow);
        isBorderAndShadow.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isBorderAndShadow = isChecked;
                TempHolder.listHash++;

            }
        });

        final CheckBox isShowImages = (CheckBox) inflate.findViewById(R.id.isShowImages);
        isShowImages.setOnCheckedChangeListener(null);
        isShowImages.setChecked(AppState.get().isShowImages);
        isShowImages.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isShowImages = isChecked;
                TempHolder.listHash++;
                isCropBookCovers.setEnabled(AppState.get().isShowImages);
                isBookCoverEffect.setEnabled(AppState.get().isShowImages);
                isBorderAndShadow.setEnabled(AppState.get().isShowImages);

            }
        });
        isCropBookCovers.setEnabled(AppState.get().isShowImages);
        isBookCoverEffect.setEnabled(AppState.get().isShowImages);
        isBorderAndShadow.setEnabled(AppState.get().isShowImages);

        CheckBox isLoopAutoplay = (CheckBox) inflate.findViewById(R.id.isLoopAutoplay);
        isLoopAutoplay.setChecked(AppState.get().isLoopAutoplay);
        isLoopAutoplay.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isLoopAutoplay = isChecked;
            }
        });

        CheckBox isOpenLastBook = (CheckBox) inflate.findViewById(R.id.isOpenLastBook);
        isOpenLastBook.setChecked(AppState.get().isOpenLastBook);
        isOpenLastBook.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isOpenLastBook = isChecked;
            }
        });

        CheckBox isShowCloseAppDialog = (CheckBox) inflate.findViewById(R.id.isShowCloseAppDialog);
        isShowCloseAppDialog.setChecked(AppState.get().isShowCloseAppDialog);
        isShowCloseAppDialog.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isShowCloseAppDialog = isChecked;
            }
        });

        final Runnable ask = new Runnable() {

            @Override
            public void run() {
                if (getActivity() == null) {
                    return;
                }

                AlertDialogs.showDialog(getActivity(), getActivity().getString(R.string.you_need_to_update_the_library), getString(R.string.ok), new Runnable() {

                    @Override
                    public void run() {
                        onScan();
                    }
                }, null);
            }
        };

        inflate.findViewById(R.id.moreLybraryettings).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final CheckBox check = new CheckBox(v.getContext());
                check.setText(R.string.displaying_the_author_and_title_of_the_pdf_book_from_the_meta_tags);

                final AlertDialog d = AlertDialogs.showViewDialog(getActivity(), check);

                check.setChecked(AppState.get().isAuthorTitleFromMetaPDF);

                check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        AppState.get().isAuthorTitleFromMetaPDF = isChecked;
                        handler.removeCallbacksAndMessages(null);
                        handler.postDelayed(ask, timeout);
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                d.dismiss();
                            }
                        }, timeout);
                    }
                });

            }
        });

        final CheckBox isFirstSurname = (CheckBox) inflate.findViewById(R.id.isFirstSurname);
        isFirstSurname.setChecked(AppState.get().isFirstSurname);
        isFirstSurname.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isFirstSurname = isChecked;
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ////
        ((CheckBox) inflate.findViewById(R.id.supportPDF)).setChecked(AppState.get().supportPDF);
        ((CheckBox) inflate.findViewById(R.id.supportPDF)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportPDF = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportXPS)).setChecked(AppState.get().supportXPS);
        ((CheckBox) inflate.findViewById(R.id.supportXPS)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportXPS = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportDJVU)).setChecked(AppState.get().supportDJVU);
        ((CheckBox) inflate.findViewById(R.id.supportDJVU)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportDJVU = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });
        ((CheckBox) inflate.findViewById(R.id.supportEPUB)).setChecked(AppState.get().supportEPUB);
        ((CheckBox) inflate.findViewById(R.id.supportEPUB)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportEPUB = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });
        ((CheckBox) inflate.findViewById(R.id.supportFB2)).setChecked(AppState.get().supportFB2);
        ((CheckBox) inflate.findViewById(R.id.supportFB2)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportFB2 = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportTXT)).setChecked(AppState.get().supportTXT);
        ((CheckBox) inflate.findViewById(R.id.supportTXT)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportTXT = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportMOBI)).setChecked(AppState.get().supportMOBI);
        ((CheckBox) inflate.findViewById(R.id.supportMOBI)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportMOBI = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportRTF)).setChecked(AppState.get().supportRTF);
        ((CheckBox) inflate.findViewById(R.id.supportRTF)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportRTF = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportDOCX)).setChecked(AppState.get().supportDOCX);
        ((CheckBox) inflate.findViewById(R.id.supportDOCX)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportDOCX = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });


        ((CheckBox) inflate.findViewById(R.id.supportODT)).setChecked(AppState.get().supportODT);
        ((CheckBox) inflate.findViewById(R.id.supportDOCX)).setText(AppsConfig.isDOCXSupported ? "DOC/DOCX" : "DOC");
        ((CheckBox) inflate.findViewById(R.id.supportODT)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportODT = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        ((CheckBox) inflate.findViewById(R.id.supportCBZ)).setChecked(AppState.get().supportCBZ);
        ((CheckBox) inflate.findViewById(R.id.supportCBZ)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportCBZ = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        CheckBox supportZIP = (CheckBox) inflate.findViewById(R.id.supportZIP);
        supportZIP.setChecked(AppState.get().supportZIP);
        supportZIP.setText(getString(R.string.archives) + " (ZIP/RAR/...)");
        supportZIP.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportZIP = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        CheckBox supportOther = (CheckBox) inflate.findViewById(R.id.supportOther);
        supportOther.setChecked(AppState.get().supportOther);
        supportOther.setText(getString(R.string.other) + " (CHM/...)");
        supportOther.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().supportOther = isChecked;
                ExtUtils.updateSearchExts();
                handler.removeCallbacks(ask);
                handler.postDelayed(ask, timeout);
            }
        });

        CheckBox isDisplayAllFilesInFolder = (CheckBox) inflate.findViewById(R.id.isDisplayAllFilesInFolder);
        isDisplayAllFilesInFolder.setChecked(AppState.get().isDisplayAllFilesInFolder);
        isDisplayAllFilesInFolder.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isDisplayAllFilesInFolder = isChecked;
                TempHolder.listHash++;
            }
        });
        // app password
        final CheckBox isAppPassword = (CheckBox) inflate.findViewById(R.id.isAppPassword);
        isAppPassword.setChecked(PasswordState.get().isAppPassword);
        isAppPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                PasswordState.get().isAppPassword = isChecked;
                if (isChecked && TxtUtils.isEmpty(PasswordState.get().appPassword)) {
                    PasswordDialog.showDialog(getActivity(), true, new Runnable() {

                        @Override
                        public void run() {
                            if (TxtUtils.isEmpty(PasswordState.get().appPassword)) {
                                isAppPassword.setChecked(false);
                            }
                        }
                    });
                }
            }
        });

        TxtUtils.underlineTextView(inflate.findViewById(R.id.appPassword)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PasswordDialog.showDialog(getActivity(), true, new Runnable() {

                    @Override
                    public void run() {
                        if (TxtUtils.isEmpty(PasswordState.get().appPassword)) {
                            isAppPassword.setChecked(false);
                        }
                    }
                });
            }
        });

        // What is new
        CheckBox showWhatIsNew = (CheckBox) inflate.findViewById(R.id.isShowWhatIsNewDialog);
        showWhatIsNew.setChecked(AppState.get().isShowWhatIsNewDialog);
        showWhatIsNew.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isShowWhatIsNewDialog = isChecked;
            }
        });

        final TextView whatIsNew = (TextView) inflate.findViewById(R.id.whatIsNew);
        whatIsNew.setText(getActivity().getString(R.string.what_is_new_in) + " " + Apps.getApplicationName(getActivity()) + " " + Apps.getVersionName(getActivity()));
        TxtUtils.underlineTextView(whatIsNew);
        whatIsNew.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AndroidWhatsNew.show2(getActivity());

            }
        });

        ///

        // BrightnessHelper.controlsWrapper(inflate, getActivity());

        nextKeys = (TextView) inflate.findViewById(R.id.textNextKeys);
        prevKeys = (TextView) inflate.findViewById(R.id.textPrevKeys);

        ch = (CheckBox) inflate.findViewById(R.id.onReverse);
        ch.setOnCheckedChangeListener(null);
        ch.setChecked(AppState.get().isReverseKeys);
        ch.setOnCheckedChangeListener(reverseListener);

        inflate.findViewById(R.id.onColorChoser).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
            }
        });
        initKeys();

        searchPaths = (TextView) inflate.findViewById(R.id.searchPaths);
        searchPaths.setText(TxtUtils.underline(BookCSS.get().searchPaths.replace(",", "<br>")));
        searchPaths.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onFolderConfigDialog();
            }
        });

        TextView addFolder = (TextView) inflate.findViewById(R.id.onConfigPath);
        TxtUtils.underlineTextView(addFolder);
        addFolder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                onFolderConfigDialog();
            }
        });

        TxtUtils.underlineTextView(inflate.findViewById(R.id.importButton)).setOnClickListener(v -> PrefDialogs.importDialog(getActivity()));

        TxtUtils.underlineTextView(inflate.findViewById(R.id.exportButton)).setOnClickListener(v -> PrefDialogs.exportDialog(getActivity()));

        final CheckBox isAutomaticExport = (CheckBox) inflate.findViewById(R.id.isAutomaticExport);
        isAutomaticExport.setChecked(AppState.get().isAutomaticExport);
        isAutomaticExport.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppState.get().isAutomaticExport = isChecked;
            }
        });
        // folders

        final TextView fontFolder = (TextView) inflate.findViewById(R.id.fontFolder);
        TxtUtils.underline(fontFolder, TxtUtils.lastTwoPath(BookCSS.get().fontFolder));
        fontFolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooserDialogFragment.chooseFolder(getActivity(), BookCSS.get().fontFolder).setOnSelectListener(new ResultResponse2<String, Dialog>() {
                    @Override
                    public boolean onResultRecive(String nPath, Dialog dialog) {
                        BookCSS.get().fontFolder = nPath;
                        TxtUtils.underline(fontFolder, TxtUtils.lastTwoPath(BookCSS.get().fontFolder));
                        dialog.dismiss();
                        return false;
                    }
                });
            }
        });

        final TextView downloadFolder = (TextView) inflate.findViewById(R.id.downloadFolder);
        TxtUtils.underline(downloadFolder, TxtUtils.lastTwoPath(BookCSS.get().downlodsPath));
        downloadFolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooserDialogFragment.chooseFolder(getActivity(), BookCSS.get().downlodsPath).setOnSelectListener(new ResultResponse2<String, Dialog>() {
                    @Override
                    public boolean onResultRecive(String nPath, Dialog dialog) {
                        BookCSS.get().downlodsPath = nPath;
                        TxtUtils.underline(downloadFolder, TxtUtils.lastTwoPath(BookCSS.get().downlodsPath));
                        dialog.dismiss();
                        return false;
                    }
                });
            }
        });

        final TextView syncPath = (TextView) inflate.findViewById(R.id.syncPath);
        TxtUtils.underline(syncPath, TxtUtils.lastTwoPath(BookCSS.get().syncDropboxPath));
        syncPath.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooserDialogFragment.chooseFolder(getActivity(), BookCSS.get().syncDropboxPath).setOnSelectListener(new ResultResponse2<String, Dialog>() {
                    @Override
                    public boolean onResultRecive(String nPath, Dialog dialog) {
                        BookCSS.get().syncDropboxPath = nPath;
                        TxtUtils.underline(downloadFolder, TxtUtils.lastTwoPath(BookCSS.get().syncDropboxPath));
                        dialog.dismiss();
                        return false;
                    }
                });
            }
        });

        final TextView ttsFolder = (TextView) inflate.findViewById(R.id.ttsFolder);
        TxtUtils.underline(ttsFolder, TxtUtils.lastTwoPath(BookCSS.get().ttsSpeakPath));
        ttsFolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooserDialogFragment.chooseFolder(getActivity(), BookCSS.get().ttsSpeakPath).setOnSelectListener(new ResultResponse2<String, Dialog>() {
                    @Override
                    public boolean onResultRecive(String nPath, Dialog dialog) {
                        BookCSS.get().ttsSpeakPath = nPath;
                        TxtUtils.underline(ttsFolder, TxtUtils.lastTwoPath(BookCSS.get().ttsSpeakPath));
                        dialog.dismiss();
                        return false;
                    }
                });
            }
        });

        final TextView backupPath = (TextView) inflate.findViewById(R.id.backupFolder);
        TxtUtils.underline(backupPath, TxtUtils.lastTwoPath(BookCSS.get().backupPath));
        backupPath.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooserDialogFragment.chooseFolder(getActivity(), BookCSS.get().backupPath).setOnSelectListener(new ResultResponse2<String, Dialog>() {
                    @Override
                    public boolean onResultRecive(String nPath, Dialog dialog) {
                        BookCSS.get().backupPath = nPath;
                        TxtUtils.underline(backupPath, TxtUtils.lastTwoPath(BookCSS.get().backupPath));
                        dialog.dismiss();
                        return false;
                    }
                });
            }
        });

        // Widget Configuration

        final TextView widgetLayout = (TextView) inflate.findViewById(R.id.widgetLayout);
        widgetLayout.setText(AppState.get().widgetType == AppState.WIDGET_LIST ? R.string.list : R.string.grid);
        TxtUtils.underlineTextView(widgetLayout);

        widgetLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

                final MenuItem recent = popupMenu.getMenu().add(R.string.list);
                recent.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppState.get().widgetType = AppState.WIDGET_LIST;
                        widgetLayout.setText(R.string.list);
                        TxtUtils.underlineTextView(widgetLayout);
                        RecentUpates.updateAll(getActivity());
                        return false;
                    }
                });

                final MenuItem starred = popupMenu.getMenu().add(R.string.grid);
                starred.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppState.get().widgetType = AppState.WIDGET_GRID;
                        widgetLayout.setText(R.string.grid);
                        TxtUtils.underlineTextView(widgetLayout);
                        RecentUpates.updateAll(getActivity());
                        return false;
                    }
                });

                popupMenu.show();

            }

        });

        final TextView widgetForRecent = (TextView) inflate.findViewById(R.id.widgetForRecent);
        widgetForRecent.setText(AppState.get().isStarsInWidget ? R.string.starred : R.string.recent);
        TxtUtils.underlineTextView(widgetForRecent);

        widgetForRecent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(widgetForRecent.getContext(), widgetForRecent);

                final MenuItem recent = popupMenu.getMenu().add(R.string.recent);
                recent.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppState.get().isStarsInWidget = false;
                        widgetForRecent.setText(AppState.get().isStarsInWidget ? R.string.starred : R.string.recent);
                        TxtUtils.underlineTextView(widgetForRecent);

                        RecentUpates.updateAll(getActivity());
                        return false;
                    }
                });

                final MenuItem starred = popupMenu.getMenu().add(R.string.starred);
                starred.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        AppState.get().isStarsInWidget = true;
                        widgetForRecent.setText(AppState.get().isStarsInWidget ? R.string.starred : R.string.recent);
                        TxtUtils.underlineTextView(widgetForRecent);

                        RecentUpates.updateAll(getActivity());
                        return false;
                    }
                });

                popupMenu.show();

            }

        });

        final TextView widgetItemsCount = (TextView) inflate.findViewById(R.id.widgetItemsCount);
        widgetItemsCount.setText("" + AppState.get().widgetItemsCount);
        TxtUtils.underlineTextView(widgetItemsCount);
        widgetItemsCount.setOnClickListener(new OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getContext(), columsCount);
                for (int i = 1; i <= 50; i++) {
                    final int k = i;
                    p.getMenu().add("" + k).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AppState.get().widgetItemsCount = k;
                            widgetItemsCount.setText("" + k);
                            TxtUtils.underlineTextView(widgetItemsCount);
                            return false;
                        }
                    });
                }

                p.show();
            }
        });

        // dictionary
        isRememberDictionary = (CheckBox) inflate.findViewById(R.id.isRememberDictionary);
        isRememberDictionary.setChecked(AppState.get().isRememberDictionary);
        isRememberDictionary.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                AppState.get().isRememberDictionary = isChecked;
            }
        });

        selectedDictionaly = (TextView) inflate.findViewById(R.id.selectedDictionaly);
        selectedDictionaly.setText(DialogTranslateFromTo.getSelectedDictionaryUnderline());
        selectedDictionaly.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogTranslateFromTo.show(getActivity(), false, new Runnable() {

                    @Override
                    public void run() {
                        selectedDictionaly.setText(DialogTranslateFromTo.getSelectedDictionaryUnderline());
                    }
                }, false);
            }
        });

        textDayColor = (TextView) inflate.findViewById(R.id.onDayColor);
        textDayColor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new ColorsDialog(getActivity(), true, AppState.get().colorDayText, AppState.get().colorDayBg, false, true, new ColorsDialogResult() {

                    @Override
                    public void onChooseColor(int colorText, int colorBg) {
                        textDayColor.setTextColor(colorText);
                        textDayColor.setBackgroundColor(colorBg);

                        AppState.get().colorDayText = colorText;
                        AppState.get().colorDayBg = colorBg;

                        ImageLoader.getInstance().clearDiskCache();
                        ImageLoader.getInstance().clearMemoryCache();
                    }
                });
            }
        });

        textNigthColor = (TextView) inflate.findViewById(R.id.onNigthColor);
        textNigthColor.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                new ColorsDialog(getActivity(), false, AppState.get().colorNigthText, AppState.get().colorNigthBg, false, true, new ColorsDialogResult() {

                    @Override
                    public void onChooseColor(int colorText, int colorBg) {
                        textNigthColor.setTextColor(colorText);
                        textNigthColor.setBackgroundColor(colorBg);

                        AppState.get().colorNigthText = colorText;
                        AppState.get().colorNigthBg = colorBg;

                    }
                });
            }
        });

        TextView onDefalt = TxtUtils.underlineTextView(inflate.findViewById(R.id.onDefaultColor));
        onDefalt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AppState.get().colorDayText = AppState.COLOR_BLACK;
                AppState.get().colorDayBg = AppState.COLOR_WHITE;

                textDayColor.setTextColor(AppState.COLOR_BLACK);
                textDayColor.setBackgroundColor(AppState.COLOR_WHITE);

                AppState.get().colorNigthText = AppState.COLOR_WHITE;
                AppState.get().colorNigthBg = AppState.COLOR_BLACK;

                textNigthColor.setTextColor(AppState.COLOR_WHITE);
                textNigthColor.setBackgroundColor(AppState.COLOR_BLACK);
            }
        });

        LinearLayout colorsLine = (LinearLayout) inflate.findViewById(R.id.colorsLine);
        colorsLine.removeAllViews();

        for (String color : AppState.STYLE_COLORS) {
            View view = inflater.inflate(R.layout.item_color, (ViewGroup) inflate, false);
            view.setBackgroundColor(Color.TRANSPARENT);
            final int intColor = Color.parseColor(color);
            final View img = view.findViewById(R.id.itColor);
            img.setBackgroundColor(intColor);

            colorsLine.addView(view, new LayoutParams(Dips.dpToPx(30), Dips.dpToPx(30)));

            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    TintUtil.color = intColor;
                    AppState.get().tintColor = intColor;
                    TempHolder.listHash++;

                    onTintChanged();
                    sendNotifyTintChanged();

                    AppProfile.save(getActivity());

                }
            });
        }

        View view = inflater.inflate(R.layout.item_color, (ViewGroup) inflate, false);
        view.setBackgroundColor(Color.TRANSPARENT);
        final ImageView img = (ImageView) view.findViewById(R.id.itColor);
        img.setColorFilter(getResources().getColor(R.color.tint_gray));
        img.setImageResource(R.drawable.glyphicons_433_plus);
        img.setBackgroundColor(AppState.get().userColor);
        colorsLine.addView(view, new LayoutParams(Dips.dpToPx(30), Dips.dpToPx(30)));

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new HSVColorPickerDialog(getContext(), AppState.get().userColor, new OnColorSelectedListener() {

                    @Override
                    public void colorSelected(Integer color) {
                        AppState.get().userColor = color;
                        AppState.get().tintColor = color;
                        TintUtil.color = color;
                        img.setBackgroundColor(color);

                        onTintChanged();
                        sendNotifyTintChanged();

                        AppProfile.save(getActivity());

                        TempHolder.listHash++;

                    }
                }).show();

            }
        });

        underline(inflate.findViewById(R.id.linksColor)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                closeLeftMenu();
                Dialogs.showLinksColorDialog(getActivity(), new Runnable() {

                    @Override
                    public void run() {
                        TempHolder.listHash++;
                        onTintChanged();
                        sendNotifyTintChanged();
                        ((MainTabs2) getActivity()).updateCurrentFragment();

                        TxtUtils.updateAllLinks(inflate.getRootView());

                    }
                });
            }
        });

        underline(inflate.findViewById(R.id.onContrast)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Dialogs.showContrastDialogByUrl(getActivity(), new Runnable() {

                    @Override
                    public void run() {
                        ImageLoader.getInstance().clearDiskCache();
                        ImageLoader.getInstance().clearMemoryCache();
                        TempHolder.listHash++;
                        notifyFragment();

                    }
                });
            }
        });

        underline(inflate.findViewById(R.id.onRateIt)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.rateIT(getActivity());
            }
        });
        underline(inflate.findViewById(R.id.openWeb)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.open(getActivity(), WWW_SITE);
            }
        });
        underline(inflate.findViewById(R.id.openBeta)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.open(getActivity(), WWW_BETA_SITE);
            }
        });
        underline(inflate.findViewById(R.id.openWiki)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.open(getActivity(), WWW_WIKI_SITE);
            }
        });

        underline(inflate.findViewById(R.id.onTelegram)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.open(getActivity(), "https://t.me/LibreraReader");
            }
        });

        TextView proText = (TextView) inflate.findViewById(R.id.downloadPRO);
        TxtUtils.underlineTextView(proText);
        ((View) proText.getParent()).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                Urls.openPdfPro(getActivity());
            }
        });

        if (AppsConfig.checkIsProInstalled(getActivity())) {
            ((View) proText.getParent()).setVisibility(View.GONE);
        }

        inflate.findViewById(R.id.cleanRecent).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setMessage(getString(R.string.clear_all_recent) + "?");
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //BookmarksData.get().cleanRecent();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                builder.show();
            }
        });

        inflate.findViewById(R.id.cleanBookmarks).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setMessage(getString(R.string.clear_all_bookmars) + "?");
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BookmarksData.get().cleanBookmarks();

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                builder.show();

            }
        });

        // licences link
        underline(inflate.findViewById(R.id.libraryLicenses)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.licenses_for_libraries);

                WebView wv = new WebView(getActivity());
                wv.loadUrl("file:///android_asset/licenses.html");
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog create = alert.create();
                create.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Keyboards.hideNavigation(getActivity());
                    }
                });
                create.show();
            }
        });

        TxtUtils.underlineTextView(inflate.findViewById(R.id.docSearch)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MultyDocSearchDialog.show(getActivity());
            }
        });
        // convert
        final TextView docConverter = (TextView) inflate.findViewById(R.id.docConverter);
        TxtUtils.underlineTextView(docConverter);
        docConverter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu p = new PopupMenu(getContext(), v);
                for (final String id : AppState.CONVERTERS.keySet()) {
                    p.getMenu().add("" + getActivity().getString(R.string.convert_to) + " " + id).setOnMenuItemClickListener(new OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ShareDialog.showsItemsDialog(getActivity(), getActivity().getString(R.string.convert_to) + " " + id, AppState.CONVERTERS.get(id));

                            return false;
                        }
                    });

                }
                p.show();
            }
        });

        overlay = getActivity().findViewById(R.id.overlay);


        TextView onProfile = inflate.findViewById(R.id.onProfile);

        profileLetter = inflate.findViewById(R.id.profileLetter);


        final String p = AppProfile.getCurrent(getActivity());

        profileLetter.setText(TxtUtils.getFirstLetter(p));
        profileLetter.setBackgroundDrawable(AppProfile.getProfileColorDrawable(getActivity(), p));

        onProfile.setText(p);
        TxtUtils.underlineTextView(onProfile);
        onProfile.setOnClickListener(v -> {
            MyPopupMenu popup = new MyPopupMenu(getActivity(), v);

            List<String> all = AppProfile.getAllProfiles();
            for (String profile : all) {

                popup.getMenu().setDrawable(TxtUtils.getFirstLetter(profile), AppProfile.getProfileColorDrawable(getActivity(), profile)).add(profile).setOnMenuItemClickListener(menu -> {
                    {
                        if (!profile.equals(AppProfile.getCurrent(getActivity()))) {

                            AlertDialogs.showOkDialog(getActivity(), getActivity().getString(R.string.do_you_want_to_switch_profile_), new Runnable() {

                                @Override
                                public void run() {
                                    AppProfile.saveCurrent(getActivity(), profile);
                                    AppDB.get().open(getActivity(), AppProfile.getCurrent(getActivity()));
                                    RecentUpates.updateAll(getActivity());
                                    onTheme();
                                }
                            });
                        }

                        return false;
                    }
                });
            }
            popup.show();

        });
        profileLetter.setOnClickListener(v -> onProfile.performClick());

        inflate.findViewById(R.id.onProfileEdit).setOnClickListener(v -> {
                    AppProfile.showDialog(getActivity(), profile -> {
                        if (!profile.equals(AppProfile.getCurrent(getActivity()))) {
                            AlertDialogs.showOkDialog(getActivity(), getActivity().getString(R.string.do_you_want_to_switch_profile_), new Runnable() {

                                @Override
                                public void run() {
                                    AppProfile.saveCurrent(getActivity(), profile);
                                    AppDB.get().open(getActivity(), AppProfile.getCurrent(getActivity()));
                                    onTheme();
                                }
                            });
                        }
                        return false;
                    });
                }
        );


        return inflate;

    }

    private void onEink() {
        AppState.get().appTheme = AppState.THEME_INK;
        AppState.get().blueLightAlpha = 0;
        AppState.get().tintColor = Color.BLACK;
        TintUtil.color = Color.BLACK;

        onTintChanged();
        sendNotifyTintChanged();

        AppProfile.save(getActivity());

        getActivity().finish();
        MainTabs2.startActivity(getActivity(), TempHolder.get().currentTab);

    }

    OnCheckedChangeListener reverseListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            AppState.get().isReverseKeys = isChecked;
            initKeys();
            saveChanges();
            LOG.d("Save Changes", 3);
        }
    };

    public View underline(View text) {
        CharSequence myText = ((TextView) text).getText();
        ((TextView) text).setText(Html.fromHtml("<u>" + myText + "</u>"));
        return text;
    }

    private void checkOpenWithSpinner() {
        String modId = AppState.get().nameVerticalMode;
        if (AppTemp.get().readingMode == AppState.READING_MODE_MUSICIAN) {
            modId = AppState.get().nameMusicianMode;
        } else if (AppTemp.get().readingMode == AppState.READING_MODE_BOOK) {
            modId = AppState.get().nameHorizontalMode;
        } else if (AppTemp.get().readingMode == AppState.READING_MODE_SCROLL) {
            modId = AppState.get().nameVerticalMode;
        } else if (AppTemp.get().readingMode == AppState.READING_MODE_TAG_MANAGER) {
            modId = getString(R.string.tag_manager);
        }

        selectedOpenMode.setText(TxtUtils.underline(modId));
    }

    public void onFolderConfigDialog() {
        BookCSS.get().searchPaths = BookCSS.get().searchPaths.replace("//", "/");
        PrefDialogs.chooseFolderDialog(getActivity(), new Runnable() {

            @Override
            public void run() {
                BookCSS.get().searchPaths = BookCSS.get().searchPaths.replace("//", "/");
                searchPaths.setText(TxtUtils.underline(BookCSS.get().searchPaths.replace(",", "<br>")));
                saveChanges();
                LOG.d("Save Changes", 2);
            }
        }, new Runnable() {

            @Override
            public void run() {
                onScan();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        BrightnessHelper.updateOverlay(overlay);
        BrightnessHelper.showBlueLigthDialogAndBrightness(getActivity(), inflate, new Runnable() {

            @Override
            public void run() {
                BrightnessHelper.updateOverlay(overlay);
            }
        });

        rotationText();

        ch.setOnCheckedChangeListener(null);
        ch.setChecked(AppState.get().isReverseKeys);
        ch.setOnCheckedChangeListener(reverseListener);

        rememberMode.setChecked(AppState.get().isRememberMode);
        checkOpenWithSpinner();

        textNigthColor.setTextColor(AppState.get().colorNigthText);
        textNigthColor.setBackgroundColor(AppState.get().colorNigthBg);

        textDayColor.setTextColor(AppState.get().colorDayText);
        textDayColor.setBackgroundColor(AppState.get().colorDayBg);

        isRememberDictionary.setChecked(AppState.get().isRememberDictionary);
        selectedDictionaly.setText(DialogTranslateFromTo.getSelectedDictionaryUnderline());

    }

    public void onColorChoose() {

    }

    public void initKeys() {
        nextKeys.setText(String.format("%s: %s", getActivity().getString(R.string.next_keys), AppState.keyToString(AppState.get().nextKeys)));
        prevKeys.setText(String.format("%s: %s", getActivity().getString(R.string.prev_keys), AppState.keyToString(AppState.get().prevKeys)));
    }

    Runnable onCloseDialog = new Runnable() {

        @Override
        public void run() {
            initKeys();
        }
    };

    private TextView nextKeys;
    private TextView prevKeys;
    private SeekBar bar;
    private CheckBox autoSettings;
    private TextView searchPaths;
    private CheckBox ch;
    private CheckBox rememberMode;
    private TextView selectedOpenMode;

    private TextView textNigthColor;
    private TextView textDayColor;
    private TextView selectedDictionaly;

    private TextView screenOrientation;

    private View inflate;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onEmail() {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String string = getResources().getString(R.string.my_email).replace("<u>", "").replace("</u>", "");
        final String aEmailList[] = {string};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Apps.getApplicationName(getContext()) + " " + Apps.getVersionName(getContext()));
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hi Support, ");

        try {
            startActivity(Intent.createChooser(emailIntent, getActivity().getString(R.string.send_mail)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), R.string.there_are_no_email_applications_installed_, Toast.LENGTH_SHORT).show();
        }
    }

    public String getFullDeviceInfo() {
        return "(" + Build.BRAND + ", " + Build.MODEL + ", " + android.os.Build.VERSION.RELEASE + ", " + Dips.screenWidthDP() + "dp" + ")";
    }

    public void onTheme() {
        AppProfile.clear();
        getActivity().finish();
        MainTabs2.startActivity(getActivity(), TempHolder.get().currentTab);
    }

    public void onScan() {
        if (getActivity() == null) {
            return;
        }
        AppProfile.save(getActivity());
        closeLeftMenu();

        getActivity().startService(new Intent(getActivity(), BooksService.class).setAction(BooksService.ACTION_SEARCH_ALL));

        Intent intent = new Intent(INTENT_TINT_CHANGE)//
                .putExtra(MainTabs2.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.SearchFragment));//

        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void closeLeftMenu() {
        try {
            final DrawerLayout drawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawer(Gravity.START, !Dips.isEInk(getActivity()));
            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    public void rotationText() {
        screenOrientation.setText(DocumentController.getRotationText());
        TxtUtils.underlineTextView(screenOrientation);
        DocumentController.doRotation(getActivity());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rotationText();

        if (AppState.get().appTheme == AppState.THEME_INK) {
            themeColor.setText(TxtUtils.underline("Ink"));
        } else if (AppState.get().appTheme == AppState.THEME_LIGHT) {
            themeColor.setText(TxtUtils.underline(getString(R.string.light)));
        } else if (AppState.get().appTheme == AppState.THEME_DARK) {
            themeColor.setText(TxtUtils.underline(getString(R.string.black)));
        } else if (AppState.get().appTheme == AppState.THEME_DARK_OLED) {
            themeColor.setText(TxtUtils.underline(getString(R.string.dark_oled)));
        } else {
            themeColor.setText("unknown");

        }
    }

    private void saveChanges() {
        if (getActivity() != null) {
            AppProfile.save(getActivity());
        }
    }

    public String getFontName(float number) {
        String prefix = getActivity().getString(R.string.normal);
        float f1 = (number - 1f) * 10;
        float f2 = (1f - number) * 10 + 0.01f;
        if (number < 1) {
            prefix = getActivity().getString(R.string.small) + " (-" + (int) f2 + ")";
        } else if (number > 1) {
            prefix = getActivity().getString(R.string.large) + " (+" + (int) f1 + ")";
        }
        return prefix;
    }

}