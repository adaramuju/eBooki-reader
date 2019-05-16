package net.autogroup.libmobi;

public class LibMobi {
    // static {
    // System.loadLibrary("mobi");
    // }

    public static native int convertToEpub(String input, String output);

    public static native int convertDocToHtml(String input, String output);
}
