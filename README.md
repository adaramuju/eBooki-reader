
# eBooki - eBook Reader all format (librera fork)

eBooki Reader is an e-book reader for Android devices; 
it supports the following formats: PDF, EPUB, MOBI, DjVu, FB2, TXT, RTF, AZW, AZW3, HTML, CBZ, CBR, and OPDS Catalogs

## Build eBooki

~~~~
sudo apt-get install mesa-common-dev libxcursor-dev  libxrandr-dev libxinerama-dev pkg-config

/Builder/link_to_mupdf_1.11.sh (Change the paths to mupdf and jniLibs folders)
./gradlew assebleLibrera
~~~~

## eBooki depends on

MuPDF - (AGPL License) https://mupdf.com/downloads/archive/ (mupdf-1.11-source.tar.xz)

MuPDF changed source ./eBookiReader/Builder/jni-1.11/

* EbookDroid
* djvulibre
* hpx
* junrar
* Universal Image Loader
* libmobi
* commons-compress
* eventbus
* greendao
* jsoup
* juniversalchardet
* commons-compress
* okhttp3
* okhttp-digest
* okio
* rtfparserkit
* java-mammoth

eBooki is distributed under the GPL

## License

See the [LICENSE](LICENSE.txt) file for license rights and limitations (GPL v.3).
