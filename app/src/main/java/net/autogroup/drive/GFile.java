package net.autogroup.drive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import net.autogroup.android.utils.Apps;
import net.autogroup.android.utils.IO;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.dao2.FileMeta;
import net.autogroup.model.AppProfile;
import net.autogroup.model.AppTemp;
import net.autogroup.model.TagData;
import net.autogroup.pdf.info.Clouds;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.ui2.AppDB;
import net.autogroup.ui2.BooksService;
import net.autogroup.ui2.FileMetaCore;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.ebookdroid.common.settings.books.SharedBooks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GFile {
    public static final int REQUEST_CODE_SIGN_IN = 1110;

    public static final String MIME_FOLDER = "application/vnd.google-apps.folder";

    public static final String TAG = "GFile";
    public static final int PAGE_SIZE = 1000;
    public static final String SKIP = "skip";
    public static final String MY_SCOPE = DriveScopes.DRIVE_FILE;
    public static final String LASTMODIFIED = "lastmodified2";

    public static com.google.api.services.drive.Drive googleDriveService;

    public static String debugOut = new String();


    public static String getDisplayInfo(Context c) {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(c);
        if (account == null) {
            return "";
        }
        return TxtUtils.nullToEmpty(account.getDisplayName()) + " (" + account.getEmail() + ")";

    }

    public static void logout(Context c) {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(MY_SCOPE))
                        .build();
        GoogleSignInClient client = GoogleSignIn.getClient(c, signInOptions);
        client.signOut();
        googleDriveService = null;
        AppTemp.get().syncTime = 0;

    }

    public static void init(Activity c) {

        logout(c);

        if (googleDriveService != null) {
            return;
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(c);

        if (account == null) {


            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(new Scope(MY_SCOPE))
                            .build();
            GoogleSignInClient client = GoogleSignIn.getClient(c, signInOptions);

            // The result of the sign-in Intent is handled in onActivityResult.
            c.startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        } else {

            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            c, Collections.singleton(MY_SCOPE));
            credential.setSelectedAccount(account.getAccount());
            googleDriveService =
                    new com.google.api.services.drive.Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName(Apps.getApplicationName(c))
                            .build();
        }
        sp = c.getSharedPreferences(LASTMODIFIED, Context.MODE_PRIVATE);

    }

    static SharedPreferences sp;

    public static void buildDriveService(Context c) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(c);
        if (account == null) {
            LOG.d(TAG, "buildDriveService", " account is null");
            return;
        }

        if (googleDriveService != null) {
            LOG.d(TAG, "googleDriveService", " has already inited");
            return;
        }


        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        c, Collections.singleton(MY_SCOPE));
        credential.setSelectedAccount(account.getAccount());
        googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(Apps.getApplicationName(c))
                        .build();

        LOG.d(TAG, "googleDriveService", " build");
        sp = c.getSharedPreferences(LASTMODIFIED, Context.MODE_PRIVATE);

    }

    public static List<File> exeQF(String q, String... args) throws IOException {
        return exeQ(String.format(q, args));
    }

    public static List<File> exeQ(String q) throws IOException {
        //LOG.d(TAG, "exeQ", q);
        String nextPageToken = "";
        List<File> res = new ArrayList<File>();
        do {
            //debugOut += "\n:" + q;

            final FileList list = googleDriveService.files().list().setQ(q).setPageToken(nextPageToken).setFields("nextPageToken, files(*)").setPageSize(PAGE_SIZE).setOrderBy("modifiedTime").execute();
            nextPageToken = list.getNextPageToken();
            res.addAll(list.getFiles());
            debugOut += "\nGet remote files info: " + list.getFiles().size();
            //debugPrint(list.getFiles());
        } while (nextPageToken != null);
        return res;
    }

    public static List<File> getFiles(String rootId) throws Exception {

        //String time = new DateTime(lastModifiedTime).toString();
        LOG.d("getFiles-by", rootId);
        final String txt = "('%s' in parents and trashed = false) or ('%s' in parents and trashed = false and mimeType = '%s')";
        return exeQF(txt, rootId, rootId, MIME_FOLDER);
    }

    public static List<File> getFilesAll(boolean withTrashed) throws Exception {
        return withTrashed ? exeQ("") : exeQ("trashed = false or");
    }

    public static File findLibreraSync() throws Exception {

        final List<File> files = exeQF("name = 'eBooki' and 'root' in parents and mimeType = '%s' and trashed = false", MIME_FOLDER);
        debugPrint(files);
        if (files.size() > 0) {
            return files.get(0);
        } else {
            return null;
        }
    }

    public static void debugPrint(List<File> list) {

        LOG.d(TAG, list.size());
        for (File f : list) {
            LOG.d(TAG, f.getId(), f.getName(), f.getMimeType(), f.getParents(), f.getCreatedTime(), f.getModifiedTime(), "trashed", f.getTrashed());
            LOG.d(f);
        }
    }

    public static File getFileById(String roodId, String name) throws IOException {
        LOG.d(TAG, "Get file", roodId, name);
        name = name.replace("'", "\\'");
        final List<File> files = exeQF("'%s' in parents and name='%s' and trashed = false", roodId, name);
        if (files != null && files.size() >= 1) {
            final File file = files.get(0);
            return file;
        }

        return null;
    }

    public static File getOrCreateLock(String roodId, long modifiedTime) throws IOException {
        File file = getFileById(roodId, "lock");
        if (file == null) {
            File metadata = new File()
                    .setParents(Collections.singletonList(roodId))
                    .setModifiedTime(new DateTime(modifiedTime))
                    .setMimeType("text/plain")
                    .setName("lock");

            LOG.d(TAG, "Create lock", roodId, "lock");
            debugOut += "\nCreate lock: " + new DateTime(modifiedTime).toStringRfc3339();
            file = googleDriveService.files().create(metadata).execute();
        }
        return file;
    }

    public static void updateLock(String roodId, long modifiedTime) throws IOException {
        File file = getOrCreateLock(roodId, modifiedTime);
        File metadata = new File().setModifiedTime(new DateTime(modifiedTime));

        debugOut += "\nUpdate lock: " + new DateTime(modifiedTime).toStringRfc3339();
        GFile.googleDriveService.files().update(file.getId(), metadata).execute();
    }

    public static File createFile(String roodId, String name, String content, long lastModifiedtime) throws IOException {
        File file = getFileById(roodId, name);
        if (file == null) {
            File metadata = new File()
                    .setParents(Collections.singletonList(roodId))
                    .setModifiedTime(new DateTime(lastModifiedtime))
                    .setMimeType("text/plain")
                    .setName(name);

            LOG.d(TAG, "Create file", roodId, name);
            file = googleDriveService.files().create(metadata).execute();
        }

        File metadata = new File().setName(name).setModifiedTime(new DateTime(lastModifiedtime));
        ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
        LOG.d(TAG, "Create file with content", roodId, name);
        GFile.googleDriveService.files().update(file.getId(), metadata, contentStream).execute();

        return file;
    }


    public static File getFileInfo(String roodId, final java.io.File inFile) throws IOException {
        File file = getFileById(roodId, inFile.getName());
        if (file == null) {
            File metadata = new File()
                    .setParents(Collections.singletonList(roodId))
                    .setMimeType(ExtUtils.getMimeType(inFile))
                    .setModifiedTime(new DateTime(getLastModified(inFile)))
                    .setName(inFile.getName());

            LOG.d(TAG, "Create file", roodId, inFile.getName());
            file = googleDriveService.files().create(metadata).execute();
        }
        return file;

    }

    public static File createFirstTime(String roodId, final java.io.File inFile) throws IOException {
        File metadata = new File()
                .setParents(Collections.singletonList(roodId))
                .setMimeType(ExtUtils.getMimeType(inFile))
                .setModifiedTime(new DateTime(getLastModified(inFile)))
                .setName(inFile.getName());

        LOG.d(TAG, "Create file", roodId, inFile.getName());
        return googleDriveService.files().create(metadata).execute();
    }


    public static void uploadFile(String roodId, File file, final java.io.File inFile) throws IOException {
        debugOut += "\nUpload: " + inFile.getParentFile().getName() + "/" + inFile.getName();

        setLastModifiedTime(inFile, inFile.lastModified());
        File metadata = new File().setName(inFile.getName()).setModifiedTime(new DateTime(getLastModified(inFile)));
        FileContent contentStream = new FileContent("text/plain", inFile);


        file.setModifiedTime(new DateTime(inFile.lastModified()));
        googleDriveService.files().update(file.getId(), metadata, contentStream).execute();


        LOG.d(TAG, "Upload: " + inFile.getParentFile().getName() + "/" + inFile.getName());


    }


    public static String readFileAsString(String fileId) throws IOException {

        LOG.d(TAG, "read file as string", fileId);
        //File metadata = googleDriveService.files().get(fileId).execute();
        //String name = metadata.getName();

        // Stream the file contents to a String.
        try (InputStream is = googleDriveService.files().get(fileId).executeMediaAsInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String contents = stringBuilder.toString();

            return contents;
        }


    }


    public static void downloadFile(String fileId, java.io.File file, long lastModified) throws IOException {
        LOG.d(TAG, "Download: " + file.getParentFile().getName() + "/" + file.getName());
        debugOut += "\nDownload: " + file.getParentFile().getName() + "/" + file.getName();
        InputStream is = null;
        //if (!file.getPath().endsWith("json")) {
        //    is = googleDriveService.files().get(fileId).executeMediaAsInputStream();
        //} else {
        // }
        java.io.File temp = new java.io.File(file.getPath() + ".temp");
        try {
            try {
                is = googleDriveService.files().get(fileId).executeMediaAsInputStream();
            } catch (IOException e) {
                is = googleDriveService.files().get(fileId).executeAsInputStream();
            }


            final boolean result = IO.copyFile(is, temp);
            if (result) {
                IO.copyFile(temp, file);
                setLastModifiedTime(file, lastModified);

                if (Clouds.isLibreraSyncFile(file.getPath())) {
                    IMG.clearCache(file.getPath());
                    FileMeta meta = AppDB.get().getOrCreate(file.getPath());
                    FileMetaCore.createMetaIfNeed(file.getPath(), true);
                    IMG.loadCoverPageWithEffect(meta.getPath(), IMG.getImageSize());
                }
                SharedBooks.cache.clear();

            }
        } finally {
            temp.delete();
        }

        //LOG.d(TAG, "downloadFile-lastModified after", file.lastModified(), lastModified, file.getName());

    }

    public static void downloadTemp(String fileId, java.io.File file) throws IOException {
        LOG.d(TAG, "Download: " + file.getParentFile().getName() + "/" + file.getName());
        debugOut += "\nDownload: " + file.getParentFile().getName() + "/" + file.getName();
        InputStream is = null;
        java.io.File temp = new java.io.File(file.getPath() + ".temp");
        try {
            try {
                is = googleDriveService.files().get(fileId).executeMediaAsInputStream();
            } catch (IOException e) {
                is = googleDriveService.files().get(fileId).executeAsInputStream();
            }

            final boolean result = IO.copyFile(is, temp);
            if (result) {
                IO.copyFile(temp, file);
            }
        } finally {
            temp.delete();
        }
    }

    public static void setLastModifiedTime(java.io.File file, long lastModified) {
        if (file.isFile()) {
            for (String key : sp.getAll().keySet()) {
                if (key.startsWith(file.getPath())) {
                    sp.edit().remove(key).commit();
                    LOG.d("hasLastModified remove", key);
                }
            }
        }
        sp.edit().putLong(file.getPath() + file.lastModified(), lastModified).commit();
        LOG.d("hasLastModified put", file.getPath() + file.lastModified(), lastModified);

    }

    public static boolean hasLastModified(java.io.File file) {
        for (String key : sp.getAll().keySet()) {
            if (key.startsWith(file.getPath())) {
                return true;
            }
        }
        return false;
    }

    public static long getLastModified(java.io.File file) {
        if (file.lastModified() == 0) {
            return 0;
        }
        return sp.getLong(file.getPath() + file.lastModified(), file.lastModified());
    }


    private static void deleteFile(File file, long lastModified) throws IOException {
        File metadata = new File().setTrashedTime(new DateTime(lastModified)).setModifiedTime(new DateTime(lastModified)).setTrashed(true);
        LOG.d("Delete", file.getName());
        debugOut += "\nDelete: " + file.getName();
        googleDriveService.files().update(file.getId(), metadata).execute();

    }


    public static File createFolder(String roodId, String name) throws IOException {
        File folder = getFileById(roodId, name);
        if (folder != null) {
            return folder;
        }
        LOG.d(TAG, "Create folder", roodId, name);
        debugOut += "\nCreate remote folder: " + name;
        File metadata = new File()
                .setParents(Collections.singletonList(roodId))
                //.setModifiedTime(new DateTime(lastModified))
                .setMimeType(MIME_FOLDER)
                .setName(name);

        return googleDriveService.files().create(metadata).execute();

    }


    public static volatile boolean isNeedUpdate = false;

    public static synchronized void sycnronizeAll(final Context c) throws Exception {

        try {
            isNeedUpdate = false;
            debugOut = "\nBegin: " + DateFormat.getTimeInstance().format(new Date());
            buildDriveService(c);
            LOG.d(TAG, "sycnronizeAll", "begin");
            if (TxtUtils.isEmpty(BookCSS.get().syncRootID)) {
                File syncRoot = GFile.findLibreraSync();
                LOG.d(TAG, "findeBookiSync finded", syncRoot);
                if (syncRoot == null) {
                    syncRoot = GFile.createFolder("root", "eBooki");
                }
                BookCSS.get().syncRootID = syncRoot.getId();
                AppProfile.save(c);
            }
            if (!AppProfile.SYNC_FOLDER_ROOT.exists()) {
                sp.edit().clear().commit();
                AppProfile.SYNC_FOLDER_ROOT.mkdirs();
                debugOut += "/n Create root folder";
            }


            LOG.d("Begin");

            sync(BookCSS.get().syncRootID, AppProfile.SYNC_FOLDER_ROOT);

            //updateLock(AppState.get().syncRootID, beginTime);

            LOG.d(TAG, "sycnronizeAll", "finished");
            debugOut += "\nEnd: " + DateFormat.getTimeInstance().format(new Date());


            TagData.restoreTags();


        } catch (IOException e) {
            debugOut += "\nException: " + e.getMessage();
            LOG.e(e);
            throw e;
        }
    }

    public static boolean deleteRemoteFile(final java.io.File ioFile) {
        try {
            final File file = map2.get(ioFile);
            if (file != null) {
                deleteFile(file, System.currentTimeMillis());
                //Thread.sleep(5000);
                return true;
            }
        } catch (Exception e) {
            LOG.e(e);
        }
        return false;
    }

    static Map<java.io.File, File> map2 = new HashMap<>();

    private static void sync(String syncId, final java.io.File ioRoot) throws Exception {
        final List<File> driveFiles = getFilesAll(true);
        LOG.d(TAG, "getFilesAll", "end");

        Map<String, File> map = new HashMap<>();
        map2.clear();


        for (File file : driveFiles) {
            map.put(file.getId(), file);
        }

        for (File file : driveFiles) {
            final String filePath = findFile(file, map);

            if (filePath.startsWith(SKIP)) {
                continue;
            }

            java.io.File local = new java.io.File(ioRoot, filePath);

            final File other = map2.get(local);
            if (other == null) {
                map2.put(local, file);
                LOG.d(TAG, "map2-put", file.getName(), file.getId(), file.getTrashed());
            } else if (other.getTrashed() == true) {
                map2.put(local, file);
                LOG.d(TAG, "map2-put", file.getName(), file.getId(), file.getTrashed());
            }
        }

        for (java.io.File local : map2.keySet()) {
            File remote = map2.get(local);
            LOG.d("CHECK-to-REMOVE", local.getPath(), remote.getModifiedTime().getValue(), getLastModified(local));
            if (remote.getTrashed() && local.exists() && compareModifiedTime(remote, local) > 0) {
                debugOut += "\nDelete local: " + local.getPath();
                LOG.d(TAG, "Delete local", local.getPath());
                ExtUtils.deleteRecursive(local);
                isNeedUpdate = true;
            }

        }


        //upload second files
        for (File remote : driveFiles) {
            if (remote.getTrashed()) {
                LOG.d(TAG, "Skip trashed", remote.getName());
                continue;
            }
            boolean skip = false;
            if (!MIME_FOLDER.equals(remote.getMimeType())) {
                final String filePath = findFile(remote, map);
                if (filePath.startsWith(SKIP)) {
                    LOG.d(TAG, "Skip", filePath);
                    continue;
                }

                java.io.File local = new java.io.File(ioRoot, filePath);

                if (!hasLastModified(local) && local.length() == remote.getSize().longValue()) {
                    setLastModifiedTime(local, remote.getModifiedTime().getValue());
                    skip = true;
                    debugOut += "\n skip: " + local.getName();
                }
//                } else if (local.getName().endsWith(AppProfile.APP_PROGRESS_JSON) || local.getName().endsWith(AppProfile.APP_BOOKMARKS_JSON)) {
//                    if (local.length() != remote.getSize().longValue()) {
//                        LOG.d("merge-" + local.getName());
//                        debugOut += "\n merge: " + local.getName();
//                        java.io.File merge = new java.io.File(local.getPath() + ".[merge]");
//                        try {
//                            downloadTemp(remote.getId(), merge);
//                            //merge
//                            if (local.getName().endsWith(AppProfile.APP_PROGRESS_JSON)) {
//                                ExportConverter.mergeBookProgrss(merge, local);
//                            } else if (local.getName().endsWith(AppProfile.APP_BOOKMARKS_JSON)) {
//                                ExportConverter.mergeBookmarks(merge, local);
//                            }
//                            uploadFile(syncId, remote, local);
//                            isNeedUpdate = true;
//                        } finally {
//                            merge.delete();
//                        }
//                        skip = true;
//                    }
//                }


                if (!skip && compareModifiedTime(remote, local) > 0) {
                    final java.io.File parentFile = local.getParentFile();
                    if (parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    downloadFile(remote.getId(), local, remote.getModifiedTime().getValue());
                    isNeedUpdate = true;
                }
            }
        }
        syncUpload(syncId, ioRoot, map2);
    }

    public static long compareModifiedTime(File remote, java.io.File local) {
        if (!remote.getName().endsWith("json")) {
            if (remote.getSize()!=null && remote.getSize().longValue() == local.length()) {
                return 0;
            }
        }

        return remote.getModifiedTime().getValue() - getLastModified(local);
    }

    private static void syncUpload(String syncId, java.io.File ioRoot, Map<java.io.File, File> map2) throws IOException {
        java.io.File[] files = ioRoot.listFiles();
        if (files == null) {
            return;
        }
        for (java.io.File local : files) {
            File remote = map2.get(local);
            if (remote != null && remote.getTrashed() == true) {
                remote = null;
            }
            if (local.isDirectory()) {
                if (remote == null) {
                    remote = createFolder(syncId, local.getName());
                }
                syncUpload(remote.getId(), local, map2);
            } else {
                if (remote == null) {
                    File add = createFirstTime(syncId, local);
                    uploadFile(syncId, add, local);
                } else if (compareModifiedTime(remote, local) < 0) {
                    uploadFile(syncId, remote, local);
                }


            }
        }
    }

    public static void upload(java.io.File local) throws IOException {
        final File remoteParent = map2.get(local.getParentFile());
        final File firstTime = createFirstTime(remoteParent.getId(), local);
        uploadFile(remoteParent.getId(), firstTime, local);
    }


    private static String findFile(File file, Map<String, File> map) {
        if (file == null) {
            return SKIP;
        }
        if (file.getParents() == null) {
            return SKIP;
        }

        if (file.getId().equals(BookCSS.get().syncRootID)) {
            return "";
        }

        return findFile(map.get(file.getParents().get(0)), map) + "/" + file.getName();
    }


    public static void runSyncService(Activity a) {
        runSyncService(a, false);

    }

    public static void runSyncService(Activity a, boolean force) {


        if (BookCSS.get().isEnableSync && !BooksService.isRunning) {
            if (!force && BookCSS.get().isSyncManualOnly) {
                LOG.d("runSyncService", "manual sync only");
                return;
            }
            if (BookCSS.get().isSyncWifiOnly && !Apps.isWifiEnabled(a)) {
                LOG.d("runSyncService", "wifi not available");
                return;
            }

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(a);
            if (account != null) {
                GFile.buildDriveService(a);
                a.startService(new Intent(a, BooksService.class).setAction(BooksService.ACTION_RUN_SYNCRONICATION));
            }
        }

    }

}

