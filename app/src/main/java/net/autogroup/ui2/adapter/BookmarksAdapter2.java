package net.autogroup.ui2.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.autogroup.android.utils.ResultResponse;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.model.AppBookmark;
import net.autogroup.model.AppState;
import net.autogroup.pdf.info.Clouds;
import net.autogroup.pdf.info.ExtUtils;
import net.autogroup.pdf.info.IMG;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.ui2.AppRecycleAdapter;
import net.autogroup.ui2.adapter.BookmarksAdapter2.BookmarksViewHolder;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class BookmarksAdapter2 extends AppRecycleAdapter<AppBookmark, BookmarksViewHolder> {


    public class BookmarksViewHolder extends RecyclerView.ViewHolder {
        public TextView page, text, title;
        public ImageView remove;
        public CardView parent;
        public ImageView image, cloudImage;

        public BookmarksViewHolder(View view) {
            super(view);
            page = (TextView) view.findViewById(R.id.page);
            title = (TextView) view.findViewById(R.id.title);
            text = (TextView) view.findViewById(R.id.text);
            image = (ImageView) view.findViewById(R.id.image);
            cloudImage = (ImageView) view.findViewById(R.id.cloudImage);
            remove = view.findViewById(R.id.remove);
            parent = (CardView) view;
        }
    }


    @Override
    public BookmarksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarksViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BookmarksViewHolder holder, final int position) {
        final AppBookmark item = getItem(position);

        holder.page.setText(TxtUtils.percentFormatInt(item.getPercent()));
        holder.title.setText(ExtUtils.getFileName(item.getPath()));


        holder.text.setText(item.text);
        holder.remove.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onDeleteClickListener.onResultRecive(item);
            }
        });
        holder.remove.setImageResource(withPageNumber ? R.drawable.glyphicons_208_remove_2 : R.drawable.glyphicons_basic_578_share);
        TintUtil.setTintImageNoAlpha(holder.remove, holder.remove.getResources().getColor(R.color.lt_grey_dima));

        if (withTitle) {
            //holder.title.setVisibility(View.VISIBLE);
            //holder.title.setVisibility(View.GONE);
        } else {
            holder.title.setVisibility(View.GONE);
        }

        TintUtil.setTintBgSimple(holder.page, 240);
        holder.page.setTextColor(Color.WHITE);
        if (withPageNumber) {
            holder.page.setVisibility(View.VISIBLE);
            // holder.remove.setVisibility(View.VISIBLE);
        } else {
            holder.page.setVisibility(View.GONE);
            //holder.remove.setVisibility(View.GONE);
        }

        IMG.getCoverPageWithEffectPos(holder.image, item.getPath(), IMG.getImageSize(), position, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {

            }
        });

        Clouds.showHideCloudImage(holder.cloudImage, item.getPath());


        if (!AppState.get().isBorderAndShadow) {
            holder.parent.setBackgroundColor(Color.TRANSPARENT);
        }

        bindItemClickAndLongClickListeners(holder.parent, getItem(position));

        if (AppState.get().appTheme == AppState.THEME_DARK_OLED) {
            holder.parent.setBackgroundColor(Color.BLACK);
        }
    }

    public void setOnDeleteClickListener(ResultResponse<AppBookmark> onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }


    private ResultResponse<AppBookmark> onDeleteClickListener;


    public boolean withTitle = true;
    public boolean withPageNumber = true;

}