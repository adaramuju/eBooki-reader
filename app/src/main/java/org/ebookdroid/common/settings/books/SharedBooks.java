package org.ebookdroid.common.settings.books;

import net.autogroup.android.utils.IO;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.Objects;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.dao2.FileMeta;
import net.autogroup.model.AppBook;
import net.autogroup.model.AppProfile;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.ui2.AppDB;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedBooks {


    public static synchronized void updateProgress(List<FileMeta> list, boolean updateTime) {

        for (FileMeta meta : list) {
            try {
                AppBook book = SharedBooks.load(meta.getPath());
                meta.setIsRecentProgress(book.p);
                if (updateTime) {
                    meta.setIsRecentTime(book.t);
                }
            } catch (Exception e) {
                LOG.e(e);
            }
        }
        AppDB.get().updateAll(list);
    }

    public static Map<String, AppBook> cache = new HashMap<>();

    public static AppBook load(String fileName) {
        LOG.d("SharedBooks-load", fileName);

        if (cache.containsKey(fileName)) {
            LOG.d("SharedBooks-load-from-cache", fileName);
            return cache.get(fileName);
        }

        AppBook res = new AppBook(fileName);
        AppBook original = null;

        for (File file : AppProfile.getAllFiles(AppProfile.APP_PROGRESS_JSON)) {
            final AppBook load = load(IO.readJsonObject(file), fileName);
            load.path = fileName;

            if (file.equals(AppProfile.syncProgress) && load != null) {
                original = load;
            }

            if (load.t >= res.t) {
                res = load;
            }
        }
        if (original != null) {
            original.p = res.p;
            original.t = Math.max(res.t, original.t);
            LOG.d("SharedBooks-load1 original", fileName, res.p);
            cache.put(fileName, original);
            return original;
        }

        LOG.d("SharedBooks-load1 general", fileName, res.p);
        cache.put(fileName, res);
        return res;

    }

    private static AppBook load(JSONObject obj, String fileName) {
        AppBook bs = new AppBook(fileName);
        try {

            LOG.d("SharedBooks-load", bs.path);
            final String key = ExtUtils.getFileName(fileName);
            if (!obj.has(key)) {
                return bs;
            }
            final JSONObject rootObj = obj.getJSONObject(key);
            Objects.loadFromJson(bs, rootObj);
        } catch (Exception e) {
            LOG.e(e);
        }
        return bs;
    }

    public static void save(AppBook bs) {
        save(bs,true);
    }
    public static synchronized void save(AppBook bs, boolean inThread) {
        if (bs == null || TxtUtils.isEmpty(bs.path)) {
            LOG.d("Can't save AppBook");
            return;
        }
        JSONObject obj = IO.readJsonObject(AppProfile.syncProgress);
        try {
            final String fileName = ExtUtils.getFileName(bs.path);
            obj.put(fileName, Objects.toJSONObject(bs));
            cache.put(fileName, bs);
            if(inThread) {
                IO.writeObj(AppProfile.syncProgress, obj);
            }else{
                IO.writeObjAsync(AppProfile.syncProgress, obj);
            }
        } catch (Exception e) {
            LOG.e(e);
        }


    }
}
