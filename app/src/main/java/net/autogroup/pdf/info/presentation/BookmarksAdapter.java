package net.autogroup.pdf.info.presentation;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.autogroup.android.utils.TxtUtils;
import net.autogroup.model.AppBookmark;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.BookmarksData;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.wrapper.DocumentController;

import java.util.List;

public class BookmarksAdapter extends BaseAdapter {

    private final List<AppBookmark> objects;
    private final boolean submenu;
    private final Context context;

    private int muxnumberOfLines = 3;
    private String higlightText;
    private DocumentController controller;

    public BookmarksAdapter(Context context, List<AppBookmark> objects, boolean submenu, DocumentController controller) {
        this.context = context;
        this.objects = objects;
        this.submenu = submenu;
        this.controller = controller;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView == null ? LayoutInflater.from(context).inflate(R.layout.bookmark_item, parent, false) : convertView;

        final AppBookmark bookmark = objects.get(position);

        final TextView textView = (TextView) view.findViewById(R.id.text);
        final TextView pageView = (TextView) view.findViewById(R.id.page);
        final TextView titleView = (TextView) view.findViewById(R.id.title);
        final ImageView image = (ImageView) view.findViewById(R.id.image);
        final ImageView cloudImage = (ImageView) view.findViewById(R.id.cloudImage);
        cloudImage.setVisibility(View.GONE);
        final View deleteView = view.findViewById(R.id.remove2);
        deleteView.setVisibility(View.VISIBLE);
        view.findViewById(R.id.remove).setVisibility(View.GONE);


        ((View) image.getParent()).setVisibility(View.GONE);
        ViewCompat.setElevation(((View) image.getParent()), 0);
        view.setBackgroundColor(Color.TRANSPARENT);

        String pageNumber = TxtUtils.deltaPage(AppTemp.get().isCut ? bookmark.getPage(controller.getPageCount()) * 2 : bookmark.getPage(controller.getPageCount()));
        titleView.setVisibility(View.GONE);
        textView.setText(bookmark.getPage(controller.getPageCount()) + ": " + bookmark.text);

        pageView.setText(pageNumber);

        if (AppState.get().appTheme == AppState.THEME_INK) {
            TxtUtils.bold(textView);
            TxtUtils.bold(pageView);
            textView.setTextColor(Color.BLACK);
            pageView.setTextColor(Color.BLACK);
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pageView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        pageView.setBackgroundColor(Color.TRANSPARENT);
        pageView.setLayoutParams(layoutParams);
        pageView.setTextColor(textView.getCurrentTextColor());

        deleteView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                BookmarksData.get().remove(bookmark);
                objects.remove(bookmark);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getMuxnumberOfLines() {
        return muxnumberOfLines;
    }

    public void setMuxnumberOfLines(int muxnumberOfLines) {
        this.muxnumberOfLines = muxnumberOfLines;
    }

    public String getHiglightText() {
        return higlightText;
    }

    public void setHiglightText(String higlightText) {
        this.higlightText = higlightText;
    }

}
