package org.ebookdroid.droids;

import net.autogroup.android.utils.LOG;
import net.autogroup.ext.CacheZipUtils;
import net.autogroup.ext.Fb2Extractor;
import net.autogroup.hypen.HypenUtils;
import net.autogroup.libmobi.LibMobi;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.model.BookCSS;

import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;
import org.ebookdroid.droids.mupdf.codec.PdfContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class DocContext extends PdfContext {

    public static String EXT_DOC_HTML = ".doc.html";

    File cacheFile;

    @Override
    public File getCacheFileName(String fileNameOriginal) {
        fileNameOriginal = fileNameOriginal + BookCSS.get().isAutoHypens + AppTemp.get().hypenLang + AppTemp.get().isDouble + AppState.get().isAccurateFontSize + BookCSS.get().isCapitalLetter;
        cacheFile = new File(CacheZipUtils.CACHE_BOOK_DIR, fileNameOriginal.hashCode() + EXT_DOC_HTML);
        return cacheFile;
    }

    @Override
    public CodecDocument openDocumentInner(String fileName, String password) {

        if (!cacheFile.isFile()) {
            String outputTemp = cacheFile.getPath() + ".tmp";
            final int res = LibMobi.convertDocToHtml(fileName, outputTemp);
            LOG.d("convertDocToHtml",res);
            if (res == 0) {
                return new RtfContext().openDocumentInner(fileName, password);
            }


            try {
                FileInputStream in = new FileInputStream(outputTemp);
                OutputStream out = new BufferedOutputStream(new FileOutputStream(cacheFile));

                HypenUtils.applyLanguage(AppTemp.get().hypenLang);
                Fb2Extractor.generateHyphenFileEpub(new InputStreamReader(in), null, out);
                out.close();
                in.close();

            } catch (Exception e) {
                LOG.e(e);
            }


        }

        MuPdfDocument muPdfDocument = new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, cacheFile.getPath(), password);
        return muPdfDocument;
    }


}
