package org.ebookdroid.droids;

import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.ext.CacheZipUtils;
import net.autogroup.hypen.HypenUtils;
import net.autogroup.mobi.parser.IOUtils;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.model.BookCSS;

import org.ebookdroid.core.codec.CodecDocument;
import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;
import org.ebookdroid.droids.mupdf.codec.PdfContext;
import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;
import org.zwobble.mammoth.images.Image;
import org.zwobble.mammoth.images.ImageConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DocxContext extends PdfContext {

    File cacheFile;

    @Override
    public File getCacheFileName(String fileNameOriginal) {
        fileNameOriginal = fileNameOriginal + BookCSS.get().isAutoHypens + AppTemp.get().hypenLang + AppTemp.get().isDouble + AppState.get().isAccurateFontSize + BookCSS.get().isCapitalLetter;
        cacheFile = new File(CacheZipUtils.CACHE_BOOK_DIR, fileNameOriginal.hashCode() + ".html");
        return cacheFile;
    }

    @Override
    public CodecDocument openDocumentInner(String fileName, String password) {
        if (!cacheFile.isFile()) {
            DocumentConverter converter = new DocumentConverter().
            imageConverter(new ImageConverter.ImgElement() {
                @Override
                public Map<String, String> convert(Image image) throws IOException {


                    String imageName = cacheFile.getName()+"+"+image.hashCode()+"." +image.getContentType().replace("image/","");
                    LOG.d("ImageConverter name", imageName);

                    FileOutputStream out = new FileOutputStream(new File(cacheFile.getParent(),imageName));
                    IOUtils.copyClose(image.getInputStream(), out);


                    Map<String, String> map = new HashMap<>();
                    map.put("src",imageName);
                    return map;
                }
            });


            Result<String> result = null;
            try {
                result = converter.convertToHtml(new File(fileName));

                String html = result.getValue();
                if(BookCSS.get().isAutoHypens && TxtUtils.isNotEmpty(AppTemp.get().hypenLang)){
                    LOG.d("docx-isAutoHypens", BookCSS.get().isAutoHypens);
                    HypenUtils.applyLanguage(AppTemp.get().hypenLang);
                    HypenUtils.resetTokenizer();
                    html= HypenUtils.applyHypnes(html);
                }

                FileOutputStream out = new FileOutputStream(cacheFile);
                out.write("<html><head></head><body>".getBytes());
                out.write(html.getBytes());
                out.write("</body></html>".getBytes());
                out.close();

                MuPdfDocument muPdfDocument = new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, cacheFile.getPath(), password);
                return muPdfDocument;

            } catch (IOException e) {
                LOG.e(e);
                return null;
            }

        }
        MuPdfDocument muPdfDocument = new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, cacheFile.getPath(), password);
        return muPdfDocument;


    }
}
