package net.autogroup;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import net.autogroup.android.utils.LOG;
import net.autogroup.dao2.FileMeta;
import net.autogroup.ext.CacheZipUtils;
import net.autogroup.mobi.parser.IOUtils;
import net.autogroup.model.AppState;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.R;
import net.autogroup.ui2.FileMetaCore;

import org.ebookdroid.BookType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class OpenerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppState.get().isDayNotInvert) {
            setTheme(R.style.StyledIndicatorsWhite);
        } else {
            setTheme(R.style.StyledIndicatorsBlack);
        }

        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getData() == null) {
            Toast.makeText(this, R.string.msg_unexpected_error, Toast.LENGTH_SHORT).show();
            finish();
        }

        LOG.d("OpenerActivity", getIntent());
        LOG.d("OpenerActivity Data", getIntent().getData());
        LOG.d("OpenerActivity Path", getIntent().getData().getPath());
        LOG.d("OpenerActivity Scheme", getIntent().getScheme());
        LOG.d("OpenerActivity Mime", getIntent().getType());
        //LOG.d("OpenerActivity Mime", getIntent().getData().);

        File file = new File(getIntent().getData().getPath());
        if (!file.isFile()) {
            try {

                BookType mime = BookType.getByMimeType(getIntent().getType());

                if (mime.getExt() == null) {
                    Toast.makeText(this, R.string.msg_unexpected_error, Toast.LENGTH_SHORT).show();
                    finish();
                }

                String name = getIntent().getData().getPath().hashCode() + "." + mime.getExt();

                LOG.d("OpenerActivity", "cache", name);

                if (!CacheZipUtils.CACHE_RECENT.exists()) {
                    CacheZipUtils.CACHE_RECENT.mkdirs();
                }

                file = new File(CacheZipUtils.CACHE_RECENT, name);
                if (!file.isFile()) {
                    FileOutputStream out = new FileOutputStream(file);
                    InputStream inputStream = getContentResolver().openInputStream(getIntent().getData());
                    IOUtils.copyClose(inputStream, out);

                    LOG.d("OpenerActivity", "creatae cache file", file.getPath());
                }
            } catch (Exception e) {
                LOG.e(e);
            }
        }

        FileMeta meta = FileMetaCore.createMetaIfNeed(file.getPath(), false);
        ExtUtils.openFile(this, meta);
        LOG.d("OpenerActivity", "open file", meta.getPath());
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
