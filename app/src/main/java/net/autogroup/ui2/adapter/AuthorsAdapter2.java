package net.autogroup.ui2.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.model.AppState;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.ui2.AppRecycleAdapter;
import net.autogroup.ui2.adapter.AuthorsAdapter2.AuthorViewHolder;
import net.autogroup.ui2.fast.FastScroller;

import java.util.Locale;

public class AuthorsAdapter2 extends AppRecycleAdapter<String, AuthorViewHolder> implements FastScroller.SectionIndexer {

    public class AuthorViewHolder extends RecyclerView.ViewHolder {
        public TextView letter, text;
        public View parent;

        public AuthorViewHolder(View view) {
            super(view);
            letter = (TextView) view.findViewById(R.id.image1);
            text = (TextView) view.findViewById(R.id.text1);
            parent = view;
        }

        public void onBindViewHolder(final AuthorViewHolder holder, String string) {
            holder.text.setText(string);
            holder.letter.setText(TxtUtils.getFirstLetter(string));

            if (holder.letter.getTag() == null) {
                holder.letter.setTag(TintUtil.randomColor(string.hashCode()));
            }

            GradientDrawable background = (GradientDrawable) holder.letter.getBackground();
            background.setColor((Integer) holder.letter.getTag());

            bindItemClickAndLongClickListeners(holder.parent, string);
            if (!AppState.get().isBorderAndShadow) {
                holder.parent.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    @Override
    public AuthorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_author, parent, false);
        return new AuthorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AuthorViewHolder holder, final int position) {
        final String string = getItem(position);
        holder.onBindViewHolder(holder, string);
    }

    @Override
    public String getSectionText(int position) {
        try {
            return TxtUtils.getFirstLetter(items.get(position)).toUpperCase(Locale.US);
        } catch (Exception e) {
            LOG.e(e);
            return "";
        }
    }

}