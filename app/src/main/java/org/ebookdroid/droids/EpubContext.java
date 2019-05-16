package org.ebookdroid.droids;

import net.autogroup.android.utils.LOG;
import net.autogroup.ext.CacheZipUtils;
import net.autogroup.ext.EpubExtractor;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.JsonHelper;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.sys.TempHolder;

import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;
import org.ebookdroid.droids.mupdf.codec.PdfContext;

import java.io.File;
import java.util.Map;

public class EpubContext extends PdfContext {

    private static final String TAG = "EpubContext";
    File cacheFile;

    @Override
    public File getCacheFileName(String fileNameOriginal) {
        LOG.d(TAG, "getCacheFileName", fileNameOriginal, AppTemp.get().hypenLang);
        cacheFile = new File(CacheZipUtils.CACHE_BOOK_DIR, (fileNameOriginal + AppState.get().isShowFooterNotesInText + AppState.get().isAccurateFontSize + BookCSS.get().isAutoHypens + AppTemp.get().hypenLang).hashCode() + ".epub");
        return cacheFile;
    }

    @Override
    public CodecDocument openDocumentInner(final String fileName, String password) {
        LOG.d(TAG, fileName);

        Map<String, String> notes = null;
        if (AppState.get().isShowFooterNotesInText) {
            notes = getNotes(fileName);
            LOG.d("footer-notes-extracted");
        }

        if ((BookCSS.get().isAutoHypens || AppState.get().isShowFooterNotesInText) && !cacheFile.isFile()) {
            EpubExtractor.proccessHypens(fileName, cacheFile.getPath(), notes);
        }
        if (TempHolder.get().loadingCancelled) {
            removeTempFiles();
            return null;
        }

        String bookPath = (BookCSS.get().isAutoHypens || AppState.get().isShowFooterNotesInText) ? cacheFile.getPath() : fileName;
        final MuPdfDocument muPdfDocument = new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, bookPath, password);

        if (notes != null) {
            muPdfDocument.setFootNotes(notes);
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    muPdfDocument.setMediaAttachment(EpubExtractor.getAttachments(fileName));
                    if (muPdfDocument.getFootNotes() == null) {
                        muPdfDocument.setFootNotes(getNotes(fileName));
                    }
                    removeTempFiles();
                } catch (Exception e) {
                    LOG.e(e);
                }
            };
        }.start();

        return muPdfDocument;
    }

    public Map<String, String> getNotes(String fileName) {
        Map<String, String> notes = null;
        final File jsonFile = new File(cacheFile + ".json");
        if (jsonFile.isFile()) {
            LOG.d("getNotes cache", fileName);
            notes = JsonHelper.fileToMap(jsonFile);
        } else {
            LOG.d("getNotes extract", fileName);
            notes = EpubExtractor.get().getFooterNotes(fileName);
            JsonHelper.mapToFile(jsonFile, notes);
            LOG.d("save notes to file", jsonFile);
        }
        return notes;
    }

}
