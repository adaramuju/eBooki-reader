package net.autogroup.model;

import java.io.File;

public class SimpleMeta implements MyPath.RelativePath {

    public static int STATE_NORMAL = 0;
    public static int STATE_DELETE = 1;


    public String path;
    public long time;


    transient public File file;


    public SimpleMeta() {

    }

    public SimpleMeta(String path, long time) {
        this.path = MyPath.toRelative(path);
        this.time = time;
    }

    public SimpleMeta(String path) {
        this.path = MyPath.toRelative(path);
    }

    public static SimpleMeta SyncSimpleMeta(SimpleMeta s) {
        return new SimpleMeta(MyPath.getSyncPath(s.getPath()), s.time);
    }
    public static SimpleMeta SyncSimpleMeta(String path) {
        return new SimpleMeta(MyPath.getSyncPath(path), 0);
    }



    public String getPath() {
        return MyPath.toAbsolute(path);
    }

    public void setPath(String path) {
        this.path = MyPath.toRelative(path);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        final String path1 =((SimpleMeta) obj).path;
        final String path2 = this.path;
        return path1.equals(path2);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleMeta:" + path + ":" + time;

    }



}
