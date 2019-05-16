package net.autogroup.ui2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudrail.si.CloudRail;
import net.autogroup.android.utils.Apps;
import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.Safe;
import net.autogroup.android.utils.StringDB;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.drive.GFile;
import net.autogroup.ext.CacheZipUtils.CacheDir;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.SlidingTabLayout;
import net.autogroup.pdf.info.Android6;
import net.autogroup.pdf.info.AppsConfig;
import net.autogroup.pdf.info.FontExtractor;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.PasswordDialog;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.pdf.info.view.BrightnessHelper;
import net.autogroup.pdf.info.view.Dialogs;
import net.autogroup.pdf.info.widget.PrefDialogs;
import net.autogroup.pdf.info.widget.RecentUpates;
import net.autogroup.pdf.info.wrapper.DocumentController;
import net.autogroup.pdf.info.wrapper.UITab;
import net.autogroup.pdf.search.activity.HorizontalViewActivity;
import net.autogroup.pdf.search.activity.msg.GDriveSycnEvent;
import net.autogroup.pdf.search.activity.msg.MessageSync;
import net.autogroup.pdf.search.activity.msg.MessegeBrightness;
import net.autogroup.pdf.search.activity.msg.MsgCloseMainTabs;
import net.autogroup.pdf.search.view.CloseAppDialog;
import net.autogroup.sys.TempHolder;
import net.autogroup.ui2.adapter.TabsAdapter2;
import net.autogroup.ui2.fragment.BookmarksFragment2;
import net.autogroup.ui2.fragment.BrowseFragment2;
import net.autogroup.ui2.fragment.OpdsFragment2;
import net.autogroup.ui2.fragment.PrefFragment2;
import net.autogroup.ui2.fragment.RecentFragment2;
import net.autogroup.ui2.fragment.SearchFragment2;
import net.autogroup.ui2.fragment.UIFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.ebookdroid.ui.viewer.VerticalViewActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@SuppressLint("NewApi")
public class MainTabs2 extends AdsFragmentActivity {
    public static final int REQUEST_CODE_ADD_RESOURCE = 123;

    private static final String TAG = "MainTabs";
    public static final String EXTRA_EXIT = "EXTRA_EXIT";
    public static final String EXTRA_SHOW_TABS = "EXTRA_SHOW_TABS";
    public static String EXTRA_PAGE_NUMBER = "EXTRA_PAGE_NUMBER";
    public static String EXTRA_SEACH_TEXT = "EXTRA_SEACH_TEXT";
    public static String EXTRA_NOTIFY_REFRESH = "EXTRA_NOTIFY_REFRESH";
    ViewPager pager;
    List<UIFragment> tabFragments;

    TabsAdapter2 adapter;

    ImageView imageMenu;
    View imageMenuParent, overlay;
    TextView toastBrightnessText;

    public boolean isEink = false;

    @Override
    protected void onNewIntent(final Intent intent) {
        AppProfile.init(this);
        LOG.d(TAG, "onNewIntent");
        // testIntentHandler();
        if (intent.getBooleanExtra(EXTRA_EXIT, false)) {
            finish();
            return;
        }
        if (intent.getCategories() != null && intent.getCategories().contains("android.intent.category.BROWSABLE")) {
            CloudRail.setAuthenticationResponse(intent);
            LOG.d("CloudRail response", intent);

            Intent intent1 = new Intent(UIFragment.INTENT_TINT_CHANGE)//
                    .putExtra(MainTabs2.EXTRA_PAGE_NUMBER, UITab.getCurrentTabIndex(UITab.BrowseFragment));//

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);

        }


        checkGoToPage(intent);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LOG.d("REQUEST_CODE_ADD_RESOURCE", requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == REQUEST_CODE_ADD_RESOURCE && resultCode == RESULT_OK) {
            getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri uri = data.getData();

            String pathSAF = uri.toString();

            StringDB.add(BookCSS.get().pathSAF, pathSAF, (db) -> BookCSS.get().pathSAF = db);

            LOG.d("REQUEST_CODE_ADD_RESOURCE", pathSAF, BookCSS.get().pathSAF);

            UIFragment uiFragment = tabFragments.get(pager.getCurrentItem());
            if (uiFragment instanceof BrowseFragment2) {
                BrowseFragment2 fr = (BrowseFragment2) uiFragment;
                fr.displayAnyPath(pathSAF);
            }
        } else if (requestCode == GFile.REQUEST_CODE_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                    .addOnSuccessListener(googleAccount -> {
                        BookCSS.get().isEnableSync = true;
                        Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                        EventBus.getDefault().post(new GDriveSycnEvent());
                        GFile.runSyncService(MainTabs2.this);
                        swipeRefreshLayout.setEnabled(true);

                    })
                    .addOnFailureListener(exception ->
                            {
                                LOG.e(exception);
                                Toast.makeText(this, R.string.fail, Toast.LENGTH_SHORT).show();
                                BookCSS.get().isEnableSync = false;
                                swipeRefreshLayout.setEnabled(false);

                            }
                    );


        }


    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // testIntentHandler();
        GFile.runSyncService(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        AppProfile.init(context);
        if (AppState.MY_SYSTEM_LANG.equals(AppState.get().appLang) && BookCSS.get().appFontScale == 1.0f) {
            LOG.d("attachBaseContext skip");
            super.attachBaseContext(context);
        } else {
            LOG.d("attachBaseContext apply");
            super.attachBaseContext(MyContextWrapper.wrap(context));
        }
    }

    Handler handler;
    ProgressBar fab;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (AppState.get().appTheme == AppState.THEME_LIGHT || AppState.get().appTheme == AppState.THEME_INK) {
            setTheme(R.style.StyledIndicatorsWhite);
        } else {
            setTheme(R.style.StyledIndicatorsBlack);
        }
        super.onCreate(savedInstanceState);
        //FirebaseAnalytics.getInstance(this);

        if (!Android6.canWrite(this)) {
            Android6.checkPermissions(this, true);
            return;
        }

        if (PasswordDialog.isNeedPasswordDialog(this)) {
            return;
        }

        LOG.d(TAG, "onCreate");

        LOG.d("EXTRA_EXIT", EXTRA_EXIT);
        if (getIntent().getBooleanExtra(EXTRA_EXIT, false)) {
            finish();
            return;
        }

        handler = new Handler();
        isEink = Dips.isEInk(this);

        TintUtil.setStatusBarColor(this);
        DocumentController.doRotation(this);

        setContentView(R.layout.main_tabs);

        imageMenu = (ImageView) findViewById(R.id.imageMenu1);
        imageMenuParent = findViewById(R.id.imageParent1);
        imageMenuParent.setBackgroundColor(TintUtil.color);

        fab = findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.showSyncLOGDialog(MainTabs2.this);
            }
        });
        fab.setBackgroundResource(R.drawable.bg_circular);
        TintUtil.setDrawableTint(fab.getBackground().getCurrent(), TintUtil.color);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(TintUtil.color);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                GFile.runSyncService(MainTabs2.this, true);
            }
        });


        overlay = findViewById(R.id.overlay);

        toastBrightnessText = (TextView) findViewById(R.id.toastBrightnessText);
        toastBrightnessText.setVisibility(View.GONE);
        TintUtil.setDrawableTint(toastBrightnessText.getCompoundDrawables()[0], Color.WHITE);

        tabFragments = new ArrayList<UIFragment>();

        try {
            LOG.d("TABBB", "TRY");
            AppState.get().tabsOrder7 = AppState.DEFAULTS_TABS_ORDER;

            for (UITab tab : UITab.getOrdered(AppState.get().tabsOrder7)) {
                if (tab.isVisible()) {
                    tabFragments.add(tab.getClazz().newInstance());
                }
            }

            if (tabFragments.size() == 0) {
                AppState.get().tabsOrder7 = AppState.DEFAULTS_TABS_ORDER;
                for (UITab tab : UITab.getOrdered(AppState.get().tabsOrder7)) {
                    tabFragments.add(tab.getClazz().newInstance());
                }
            }

        } catch (Exception e) {
            LOG.e(e);
            Toast.makeText(MainTabs2.this, R.string.msg_unexpected_error, Toast.LENGTH_LONG).show();
            tabFragments.add(new SearchFragment2());
            tabFragments.add(new BrowseFragment2());
            tabFragments.add(new RecentFragment2());
            tabFragments.add(new BookmarksFragment2());
            tabFragments.add(new OpdsFragment2());
            tabFragments.add(new PrefFragment2());
            //tabFragments.add(new CloudsFragment2());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.left_drawer, new PrefFragment2()).commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        imageMenu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.START))
                    drawerLayout.closeDrawer(Gravity.START, AppState.get().appTheme != AppState.THEME_INK);
                else
                    drawerLayout.openDrawer(Gravity.START, AppState.get().appTheme != AppState.THEME_INK);

            }
        });

        if (UITab.isShowPreferences()) {
            imageMenu.setVisibility(View.GONE);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            imageMenu.setVisibility(View.VISIBLE);
        }

        // ((BrigtnessDraw)
        // findViewById(R.id.brigtnessProgressView)).setActivity(this);

        adapter = new TabsAdapter2(this, tabFragments);
        pager = (ViewPager)

                findViewById(R.id.pager);

        if (Android6.canWrite(this)) {
            pager.setAdapter(adapter);
        }

        pager.setOffscreenPageLimit(5);
        pager.addOnPageChangeListener(onPageChangeListener);

        drawerLayout.addDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerStateChanged(int arg0) {
                LOG.d("drawerLayout-onDrawerStateChanged", arg0);

            }

            @Override
            public void onDrawerSlide(View arg0, float arg1) {
                LOG.d("drawerLayout-onDrawerSlide");
                if (BookCSS.get().isEnableSync) {
                    swipeRefreshLayout.setEnabled(false);
                }

            }

            @Override
            public void onDrawerOpened(View arg0) {
                LOG.d("drawerLayout-onDrawerOpened");
                if (BookCSS.get().isEnableSync) {
                    swipeRefreshLayout.setEnabled(false);
                }

            }

            @Override
            public void onDrawerClosed(View arg0) {
                LOG.d("drawerLayout-onDrawerClosed");
                try {
                    tabFragments.get(pager.getCurrentItem()).onSelectFragment();

                    if (BookCSS.get().isEnableSync) {
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.setColorSchemeColors(TintUtil.color);

                    }
                    TintUtil.setDrawableTint(fab.getBackground().getCurrent(), TintUtil.color);


                } catch (Exception e) {
                    LOG.e(e);
                }

            }
        });

        if (AppState.get().tapPositionTop) {
            indicator = (SlidingTabLayout) findViewById(R.id.slidingTabs1);
        } else {
            indicator = (SlidingTabLayout) findViewById(R.id.slidingTabs2);
        }
        indicator.addSwipeRefreshLayout(swipeRefreshLayout);
        indicator.setVisibility(View.VISIBLE);
        indicator.init();

        indicator.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                LOG.d("OnFocusChangeListener", hasFocus);
            }
        });


        indicator.setViewPager(pager);

        indicator.setDividerColors(getResources().getColor(R.color.tint_divider));
        indicator.setSelectedIndicatorColors(Color.WHITE);
        indicator.setBackgroundColor(TintUtil.color);

        if (!AppState.get().tapPositionTop) {
            imageMenu.setVisibility(View.GONE);
            indicator.setDividerColors(Color.TRANSPARENT);
            indicator.setSelectedIndicatorColors(Color.TRANSPARENT);
            for (int i = 0; i < indicator.getmTabStrip().getChildCount(); i++) {
                View child = indicator.getmTabStrip().getChildAt(i);
                child.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        imageMenu.performClick();
                        return true;
                    }
                });
            }
        }

        if (AppState.get().appTheme == AppState.THEME_INK) {
            TintUtil.setTintImageNoAlpha(imageMenu, TintUtil.color);
            indicator.setSelectedIndicatorColors(TintUtil.color);
            indicator.setDividerColors(TintUtil.color);
            indicator.setBackgroundColor(Color.TRANSPARENT);
            imageMenuParent.setBackgroundColor(Color.TRANSPARENT);

        }


        Android6.checkPermissions(this, true);
        // Analytics.onStart(this);

        List<String> actions = Arrays.asList("android.intent.action.PROCESS_TEXT", "android.intent.action.SEARCH", "android.intent.action.SEND");
        List<String> extras = Arrays.asList(Intent.EXTRA_PROCESS_TEXT_READONLY, Intent.EXTRA_PROCESS_TEXT, SearchManager.QUERY, Intent.EXTRA_TEXT);
        if (getIntent() != null && getIntent().getAction() != null) {
            if (actions.contains(getIntent().getAction())) {
                for (String extra : extras) {
                    final String text = getIntent().getStringExtra(extra);
                    if (TxtUtils.isNotEmpty(text)) {
                        AppTemp.get().lastClosedActivity = null;
                        pager.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                ((SearchFragment2) tabFragments.get(0)).searchAndOrderExteral(text);
                            }
                        }, 250);
                        break;
                    }
                }

            }

        }

        /*
        try {
            LOG.d("checkForNewBeta");
            if (AppState.get().isShowWhatIsNewDialog) {
                AndroidWhatsNew.checkForNewBeta(this);
            }
        } catch (Exception e) {
            LOG.e(e);
        }
        */

        if (Android6.canWrite(this)) {
            FontExtractor.extractFonts(this);
        }
        EventBus.getDefault().register(this);

        boolean showTabs = getIntent().getBooleanExtra(EXTRA_SHOW_TABS, false);
        LOG.d("EXTRA_SHOW_TABS", showTabs, AppTemp.get().lastMode);
        if (showTabs == false && AppState.get().isOpenLastBook) {
            LOG.d("Open lastBookPath", AppTemp.get().lastBookPath);
            if (AppTemp.get().lastBookPath == null || !new File(AppTemp.get().lastBookPath).isFile()) {
                LOG.d("Open Last book not found");
                return;
            }
            AppTemp.get().lastClosedActivity = null;

            Safe.run(new Runnable() {

                @Override
                public void run() {
                    boolean isEasyMode = HorizontalViewActivity.class.getSimpleName().equals(AppTemp.get().lastMode);
                    Intent intent = new Intent(MainTabs2.this, isEasyMode ? HorizontalViewActivity.class : VerticalViewActivity.class);
                    intent.putExtra(PasswordDialog.EXTRA_APP_PASSWORD, getIntent().getStringExtra(PasswordDialog.EXTRA_APP_PASSWORD));
                    intent.setData(Uri.fromFile(new File(AppTemp.get().lastBookPath)));
                    startActivity(intent);
                }
            });
        } else if (!AppState.get().isOpenLastBook) {
            LOG.d("Open book lastA", AppTemp.get().lastClosedActivity);

            if (AppTemp.get().lastBookPath == null || !new File(AppTemp.get().lastBookPath).isFile()) {
                LOG.d("Open Last book not found");
                return;
            }
            final String saveMode = AppTemp.get().lastClosedActivity;
            Safe.run(new Runnable() {

                @Override
                public void run() {

                    if (HorizontalViewActivity.class.getSimpleName().equals(saveMode)) {
                        Intent intent = new Intent(MainTabs2.this, HorizontalViewActivity.class);
                        intent.setData(Uri.fromFile(new File(AppTemp.get().lastBookPath)));
                        startActivity(intent);
                        LOG.d("Start lastA", saveMode);
                    } else if (VerticalViewActivity.class.getSimpleName().equals(saveMode)) {
                        Intent intent = new Intent(MainTabs2.this, VerticalViewActivity.class);
                        intent.setData(Uri.fromFile(new File(AppTemp.get().lastBookPath)));
                        startActivity(intent);
                        LOG.d("Start lastA", saveMode);
                    }

                }
            });

        } else {
            RecentUpates.updateAll(this);
        }

        checkGoToPage(getIntent());

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onShowSycn(MessageSync msg) {

        try {
            if (msg.state == MessageSync.STATE_VISIBLE) {
                if (BookCSS.get().isShowSyncWheel) {
                    fab.setVisibility(View.VISIBLE);
                }
                swipeRefreshLayout.setRefreshing(false);
            } else if (msg.state == MessageSync.STATE_FAILE) {
                if (BookCSS.get().isShowSyncWheel) {
                    fab.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, getString(R.string.sync_error), Toast.LENGTH_LONG).show();
            } else {
                if (BookCSS.get().isShowSyncWheel) {
                    fab.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(false);

            }
        } catch (Exception e) {
            LOG.e(e);
        }
    }

    @Subscribe
    public void onMessegeBrightness(MessegeBrightness msg) {
        BrightnessHelper.onMessegeBrightness(handler, msg, toastBrightnessText, overlay);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra(EXTRA_PAGE_NUMBER, -1);
            if (pos != -1) {
                if (pos >= 0) {
                    pager.setCurrentItem(pos);
                }

                if (intent.getBooleanExtra(EXTRA_NOTIFY_REFRESH, false)) {
                    onResume();
                }

            } else {
                if (AppState.get().appTheme == AppState.THEME_INK) {
                    TintUtil.setTintImageNoAlpha(imageMenu, TintUtil.color);
                    indicator.setSelectedIndicatorColors(TintUtil.color);
                    indicator.setDividerColors(TintUtil.color);
                    indicator.updateIcons(pager.getCurrentItem());
                } else {
                    indicator.setBackgroundColor(TintUtil.color);
                    imageMenuParent.setBackgroundColor(TintUtil.color);
                }
            }
        }

    };

    public void checkGoToPage(Intent intent) {
        int pos = intent.getIntExtra(EXTRA_PAGE_NUMBER, -1);
        if (pos != -1) {
            pager.setCurrentItem(pos);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppsConfig.isCloudsEnable = UITab.isShowCloudsPreferences();

        LOG.d(TAG, "onResume");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DocumentController.chooseFullScreen(this, AppState.get().isFullScreenMain);
        TintUtil.updateAll();
        AppTemp.get().lastClosedActivity = MainTabs2.class.getSimpleName();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(UIFragment.INTENT_TINT_CHANGE));
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(BookCSS.get().isEnableSync && GoogleSignIn.getLastSignedInAccount(this) != null);
        }

        try {
            if(pager!=null) {
                final UIFragment uiFragment = tabFragments.get(pager.getCurrentItem());
                uiFragment.onSelectFragment();
            }


        } catch (Exception e) {
            LOG.e(e);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        BrightnessHelper.applyBrigtness(this);
        BrightnessHelper.updateOverlay(overlay);
    }

    ;

    boolean isMyKey = false;

    public void updateCurrentFragment() {
        tabFragments.get(pager.getCurrentItem()).onSelectFragment();
    }

    @Override
    public boolean onKeyDown(int keyCode1, KeyEvent event) {
        if (!isEink) {
            return super.onKeyDown(keyCode1, event);
        }

        int keyCode = event.getKeyCode();
        if (keyCode == 0) {
            keyCode = event.getScanCode();
        }
        isMyKey = false;
        if (tabFragments.get(pager.getCurrentItem()).onKeyDown(keyCode)) {
            isMyKey = true;
            return true;
        }

        return super.onKeyDown(keyCode1, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!isEink) {
            return super.onKeyUp(keyCode, event);
        }

        if (isMyKey) {
            return true;
        }
        // TODO Auto-generated method stub
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        ImageLoader.getInstance().clearAllTasks();

        AppProfile.save(this);

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GFile.runSyncService(this);

        LOG.d(TAG, "onDestroy");
        if (pager != null) {
            try {
                pager.setAdapter(null);
            } catch (Exception e) {
                LOG.e(e);
            }
        }
        // Analytics.onStop(this);
        CacheDir.ZipApp.removeCacheContent();
        // ImageExtractor.clearErrors();
        // ImageExtractor.clearCodeDocument();

        if (AppState.get().isAutomaticExport && Android6.canWrite(this) && !PrefDialogs.isBookSeriviceIsRunning(this)) {
            try {
                File root = new File(BookCSS.get().backupPath);
                if (!root.isDirectory()) {
                    root.mkdirs();
                }
                File file = new File(root, Apps.getApplicationName(this) + "-" + Apps.getVersionName(this) + "-backup-export-all.JSON.txt");
                LOG.d("isAutomaticExport", file);
                //ExportSettingsManager.getInstance(this).exportAll(file);
            } catch (Exception e) {
                LOG.e(e);
            }
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String language = newConfig.locale.getLanguage();
        float fontScale = newConfig.fontScale;

        LOG.d("ContextWrapper ConfigChanged", language, fontScale);

        if (pager != null) {
            int currentItem = pager.getCurrentItem();
            //pager.setAdapter(adapter); //WHY???
            pager.setCurrentItem(currentItem);
            IMG.clearMemoryCache();
        }
        activateAds();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Android6.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {
        UIFragment uiFragment = null;

        @Override
        public void onPageSelected(int pos) {
            uiFragment = tabFragments.get(pos);
            uiFragment.onSelectFragment();
            TempHolder.get().currentTab = pos;

            LOG.d("onPageSelected", uiFragment);


        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (BookCSS.get().isEnableSync && swipeRefreshLayout != null) {
                swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
            }
            LOG.d("onPageSelected onPageScrollStateChanged", state);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                check();
            }

        }

        public void check() {
            if (BookCSS.get().isEnableSync && swipeRefreshLayout != null) {
                if (uiFragment instanceof PrefFragment2) {
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    swipeRefreshLayout.setEnabled(true);
                }
            }
        }
    };
    private SlidingTabLayout indicator;

    @Override
    public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {
        if (CloseAppDialog.checkLongPress(this, event)) {
            CloseAppDialog.show(this, closeActivityRunnable);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onFinishActivity() {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isInterstialShown()) {
            onFinishActivity();
            return;
        }

        if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START, AppState.get().appTheme != AppState.THEME_INK);
            return;
        }

        if (tabFragments != null) {
            if (!tabFragments.isEmpty() && tabFragments.get(pager.getCurrentItem()).isBackPressed()) {
                return;
            }

            CloseAppDialog.show(this, closeActivityRunnable);
        } else {
            finish();
        }
    }

    Runnable closeActivityRunnable = new Runnable() {

        @Override
        public void run() {
            showInterstial();
        }
    };

    private DrawerLayout drawerLayout;

    public static void startActivity(Activity c, int tab) {
        AppTemp.get().lastClosedActivity = null;
        final Intent intent = new Intent(c, MainTabs2.class);
        intent.putExtra(MainTabs2.EXTRA_SHOW_TABS, true);
        intent.putExtra(MainTabs2.EXTRA_PAGE_NUMBER, tab);
        intent.putExtra(PasswordDialog.EXTRA_APP_PASSWORD, c.getIntent().getStringExtra(PasswordDialog.EXTRA_APP_PASSWORD));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
        c.overridePendingTransition(0, 0);

    }

    @Subscribe
    public void onCloseAppMsg(MsgCloseMainTabs event) {
        onFinishActivity();
    }

    public static void closeApp(Context c) {
        if (c == null) {
            return;
        }
        EventBus.getDefault().post(new MsgCloseMainTabs());
    }
}
