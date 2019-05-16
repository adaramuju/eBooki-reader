package net.autogroup.ui2;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaSessionCompat;

import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.dao2.FileMeta;
import net.autogroup.drive.GFile;
import net.autogroup.ext.CacheZipUtils.CacheDir;
import net.autogroup.ext.EbookMeta;
import net.autogroup.model.AppData;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppTemp;
import net.autogroup.model.SimpleMeta;
import net.autogroup.model.TagData;
import net.autogroup.pdf.info.Clouds;
import net.autogroup.pdf.info.ExportConverter;
import net.autogroup.pdf.info.ExportSettingsManager;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.io.SearchCore;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.pdf.search.activity.msg.MessageSync;
import net.autogroup.pdf.search.activity.msg.MessageSyncFinish;
import net.autogroup.pdf.search.activity.msg.UpdateAllFragments;
import net.autogroup.sys.ImageExtractor;
import net.autogroup.sys.TempHolder;

import org.ebookdroid.common.settings.books.SharedBooks;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class BooksService extends IntentService {
    private MediaSessionCompat mediaSessionCompat;

    public BooksService() {
        super("BooksService");
        handler = new Handler();
        LOG.d("BooksService", "Create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.d("BooksService", "onDestroy");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        AppProfile.init(this);
    }

    public static String TAG = "BooksService";

    Handler handler;

    public static String INTENT_NAME = "BooksServiceIntent";
    public static String ACTION_SEARCH_ALL = "ACTION_SEARCH_ALL";
    public static String ACTION_REMOVE_DELETED = "ACTION_REMOVE_DELETED";
    public static String ACTION_SYNC_DROPBOX = "ACTION_SYNC_DROPBOX";
    public static String ACTION_RUN_SYNCRONICATION = "ACTION_RUN_SYNCRONICATION";

    public static String RESULT_SYNC_FINISH = "RESULT_SYNC_FINISH";
    public static String RESULT_SEARCH_FINISH = "RESULT_SEARCH_FINISH";
    public static String RESULT_BUILD_LIBRARY = "RESULT_BUILD_LIBRARY";
    public static String RESULT_SEARCH_COUNT = "RESULT_SEARCH_COUNT";

    private List<FileMeta> itemsMeta = new LinkedList<FileMeta>();

    public static volatile boolean isRunning = false;

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            if (isRunning) {
                LOG.d(TAG, "BooksService", "Is-running");
                return;
            }

            isRunning = true;
            LOG.d(TAG, "BooksService", "Action", intent.getAction());

            //TESET


            File oldConfig = new File(AppProfile.DOWNLOADS_DIR, "eBooki/backup-8.0.json");
            if (!oldConfig.exists()) {
                oldConfig.getParentFile().mkdirs();
                AppDB.get().open(this, AppDB.DB_NAME);
                AppProfile.SYNC_FOLDER_ROOT.mkdirs();
                ExportSettingsManager.exportAll(this, oldConfig);
                try {
                    ExportConverter.covertJSONtoNew(this, oldConfig);
                    ExportConverter.copyPlaylists();
                } catch (Exception e) {
                    LOG.e(e);
                }
                AppDB.get().open(this, AppProfile.getCurrent(this));
            }


            if (ACTION_RUN_SYNCRONICATION.equals(intent.getAction())) {
                if (BookCSS.get().isEnableSync) {


                    AppProfile.save(this);


                    try {
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_VISIBLE));
                        AppTemp.get().syncTimeStatus = MessageSync.STATE_VISIBLE;
                        GFile.sycnronizeAll(this);

                        AppTemp.get().syncTime = System.currentTimeMillis();
                        AppTemp.get().syncTimeStatus = MessageSync.STATE_SUCCESS;
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_SUCCESS));

                    } catch (Exception e) {
                        AppTemp.get().syncTimeStatus = MessageSync.STATE_FAILE;
                        EventBus.getDefault().post(new MessageSync(MessageSync.STATE_FAILE));
                        LOG.e(e);
                    }

                    if (GFile.isNeedUpdate) {
                        LOG.d("GFILE-isNeedUpdate", GFile.isNeedUpdate);
                        TempHolder.get().listHash++;
                        EventBus.getDefault().post(new UpdateAllFragments());
                    }

                    //onHandleIntent(new Intent(this, BooksService.class).setAction(BooksService.ACTION_SEARCH_ALL));
                }

            }


            if (ACTION_REMOVE_DELETED.equals(intent.getAction())) {
                List<FileMeta> list = AppDB.get().getAll();
                for (FileMeta meta : list) {
                    if (meta == null) {
                        continue;
                    }

                    if (Clouds.isCloud(meta.getPath())) {
                        continue;
                    }

                    File bookFile = new File(meta.getPath());
                    if (ExtUtils.isMounted(bookFile)) {
                        LOG.d("isMounted", bookFile);
                        if (!bookFile.exists()) {
                            AppDB.get().delete(meta);
                            LOG.d(TAG, "Delete-setIsSearchBook", meta.getPath());
                        }
                    }

                }
                sendFinishMessage();

                LOG.d("BooksService , searchDate", AppTemp.get().searchDate, BookCSS.get().searchPaths);
                if (AppTemp.get().searchDate != 0) {

                    List<FileMeta> localMeta = new LinkedList<FileMeta>();

                    for (final String path : BookCSS.get().searchPaths.split(",")) {
                        if (path != null && path.trim().length() > 0) {
                            final File root = new File(path);
                            if (root.isDirectory()) {
                                LOG.d(TAG, "Searcin in " + root.getPath());
                                SearchCore.search(localMeta, root, ExtUtils.seachExts);
                            }
                        }
                    }

                    for (FileMeta meta : localMeta) {

                        File file = new File(meta.getPath());

                        if (file.lastModified() >= AppTemp.get().searchDate) {
                            if (AppDB.get().getDao().hasKey(meta)) {
                                LOG.d(TAG, "Skip book", file.getPath());
                                continue;
                            }

                            FileMetaCore.createMetaIfNeed(meta.getPath(), true);
                            LOG.d(TAG, "BooksService", "insert", meta.getPath());
                        } else {
                            //LOG.d("BooksService file old", file.getPath(), file.lastModified(), AppTemp.get().searchDate);
                        }

                    }

                    SharedBooks.updateProgress(list,true);
                    AppDB.get().updateAll(list);

                    AppTemp.get().searchDate = System.currentTimeMillis();
                    sendFinishMessage();
                }

                Clouds.get().syncronizeGet();
                sendFinishMessage();

            } else if (ACTION_SEARCH_ALL.equals(intent.getAction())) {
                LOG.d(ACTION_SEARCH_ALL);

                AppProfile.init(this);

                IMG.clearDiscCache();
                IMG.clearMemoryCache();
                ImageExtractor.clearErrors();


                AppDB.get().deleteAllData();
                itemsMeta.clear();

                handler.post(timer);

                for (final String path : BookCSS.get().searchPaths.split(",")) {
                    if (path != null && path.trim().length() > 0) {
                        final File root = new File(path);
                        if (root.isDirectory()) {
                            LOG.d("Searcin in " + root.getPath());
                            SearchCore.search(itemsMeta, root, ExtUtils.seachExts);
                        }
                    }
                }
                AppTemp.get().searchDate = System.currentTimeMillis();


                for (FileMeta meta : itemsMeta) {
                    meta.setIsSearchBook(true);
                }

                final List<SimpleMeta> allExcluded = AppData.get().getAllExcluded();

                if (TxtUtils.isListNotEmpty(allExcluded)) {
                    for (FileMeta meta : itemsMeta) {
                        if (allExcluded.contains(SimpleMeta.SyncSimpleMeta(meta.getPath()))) {
                            meta.setIsSearchBook(false);
                        }
                    }
                }

                final List<FileMeta> allSyncBooks = AppData.get().getAllSyncBooks();
                if (TxtUtils.isListNotEmpty(allSyncBooks)) {
                    for (FileMeta meta : itemsMeta) {
                        for (FileMeta sync : allSyncBooks) {
                            if (meta.getTitle().equals(sync.getTitle()) && !meta.getPath().equals(sync.getPath())) {
                                meta.setIsSearchBook(false);
                                LOG.d(TAG, "remove-dublicate", meta.getPath());
                            }
                        }

                    }
                }


                itemsMeta.addAll(AppData.get().getAllFavoriteFiles());
                itemsMeta.addAll(AppData.get().getAllFavoriteFolders());


                AppDB.get().saveAll(itemsMeta);

                handler.removeCallbacks(timer);

                sendFinishMessage();

                handler.post(timer2);

                for (FileMeta meta : itemsMeta) {
                    File file = new File(meta.getPath());
                    FileMetaCore.get().upadteBasicMeta(meta, file);
                }

                AppDB.get().updateAll(itemsMeta);
                sendFinishMessage();

                for (FileMeta meta : itemsMeta) {
                    EbookMeta ebookMeta = FileMetaCore.get().getEbookMeta(meta.getPath(), CacheDir.ZipService, true);
                    LOG.d("BooksService getAuthor", ebookMeta.getAuthor());
                    FileMetaCore.get().udpateFullMeta(meta, ebookMeta);
                }

                SharedBooks.updateProgress(itemsMeta,true);
                AppDB.get().updateAll(itemsMeta);


                itemsMeta.clear();

                handler.removeCallbacks(timer2);
                sendFinishMessage();
                CacheDir.ZipService.removeCacheContent();

                Clouds.get().syncronizeGet();

                TagData.restoreTags();

                sendFinishMessage();

            } else if (ACTION_SYNC_DROPBOX.equals(intent.getAction())) {
                Clouds.get().syncronizeGet();
                sendFinishMessage();
            }


        } finally {
            isRunning = false;
        }

    }

    Runnable timer = new Runnable() {

        @Override
        public void run() {
            sendProggressMessage();
            handler.postDelayed(timer, 250);
        }
    };

    Runnable timer2 = new Runnable() {

        @Override
        public void run() {
            sendBuildingLibrary();
            handler.postDelayed(timer2, 250);
        }
    };

    private void sendFinishMessage() {
        try {
            AppDB.get().getDao().detachAll();
        } catch (Exception e) {
            LOG.e(e);
        }

        sendFinishMessage(this);
        EventBus.getDefault().post(new MessageSyncFinish());
    }

    public static void sendFinishMessage(Context c) {
        Intent intent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_SEARCH_FINISH);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    private void sendProggressMessage() {
        Intent itent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_SEARCH_COUNT).putExtra(Intent.EXTRA_INDEX, itemsMeta.size());
        LocalBroadcastManager.getInstance(this).sendBroadcast(itent);
    }

    private void sendBuildingLibrary() {
        Intent itent = new Intent(INTENT_NAME).putExtra(Intent.EXTRA_TEXT, RESULT_BUILD_LIBRARY);
        LocalBroadcastManager.getInstance(this).sendBroadcast(itent);
    }

}
