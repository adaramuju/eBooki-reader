package net.autogroup.pdf.search.activity.msg;

public class MessageSync {
     public int state;
     public static int STATE_SUCCESS = 0;
    public static int STATE_VISIBLE = 1;
    public static int STATE_FAILE = 2;

    public MessageSync(int state) {
        super();
        this.state = state;
    }




}
