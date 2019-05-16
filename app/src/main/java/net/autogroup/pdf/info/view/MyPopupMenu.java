package net.autogroup.pdf.info.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import net.autogroup.android.utils.BaseItemLayoutAdapter;
import net.autogroup.android.utils.Dips;
import net.autogroup.android.utils.Keyboards;
import net.autogroup.android.utils.LOG;
import net.autogroup.android.utils.TxtUtils;
import net.autogroup.model.AppState;
import net.autogroup.pdf.info.ADS;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.TintUtil;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.ui2.MainTabs2;

import java.util.ArrayList;
import java.util.List;

public class MyPopupMenu {
    Context c;
    private View anchor;
    List<Menu> list = new ArrayList<Menu>();
    private boolean isTabsActivity;

    public MyPopupMenu(Context c, View anchor) {
        this.c = c;
        this.anchor = anchor;
        isTabsActivity = c instanceof MainTabs2;
    }

    public MyPopupMenu(View anchor) {
        this.c = anchor.getContext();
        this.anchor = anchor;
        isTabsActivity = c instanceof MainTabs2;
    }

    public class Menu {
        String stringRes;
        int iconRes;
        Drawable drawable;
        String letter;
        OnMenuItemClickListener click;
        OnMenuItemClickListener onLongClick;

        private String fontPath;
        Boolean active;

        String checkboxString;
        boolean checkboxState;
        CompoundButton.OnCheckedChangeListener checkedChangeListener;

        public Menu add(int res) {
            this.stringRes = c.getString(res);
            return this;
        }

        public Menu add(String name) {
            this.stringRes = name;
            return this;
        }

        public Menu addCheckbox(String name, boolean state, CompoundButton.OnCheckedChangeListener listener) {
            this.checkboxString = name;
            this.checkboxState = state;
            this.checkedChangeListener = listener;
            return this;
        }

        public Menu add(String name, String fontPath) {
            this.stringRes = name;
            this.fontPath = fontPath;
            return this;
        }

        public Menu setIcon(int res) {
            this.iconRes = res;
            return this;
        }

        public Menu setDrawable(String letter, Drawable d) {
            this.drawable = d;
            this.letter = letter;
            return this;
        }

        public Menu active(Boolean active) {
            this.active = active;
            return this;
        }

        public Menu setOnMenuItemClickListener(OnMenuItemClickListener onclick) {
            this.click = onclick;
            return this;
        }

        public Menu setOnMenuItemLongClickListener(OnMenuItemClickListener onLongClick) {
            this.onLongClick = onLongClick;
            return this;
        }

    }

    public void show() {
        show(-1);
    }

    private OnDismissListener onDismissListener;

    public void show(int pos) {
        try {
            if (c instanceof MainTabs2) {
                ADS.hideAdsTemp((Activity) c);
            }
        } catch (Exception e) {
            LOG.e(e);
        }

        final ListPopupWindow p1 = new ListPopupWindow(c);

        BaseItemLayoutAdapter<Menu> a = new BaseItemLayoutAdapter<Menu>(c, R.layout.item_dict_line, list) {
            @Override
            public void populateView(View layout, int position, final Menu item) {
                TextView textView = (TextView) layout.findViewById(R.id.text1);
                TextView profileLetter = (TextView) layout.findViewById(R.id.profileLetter);
                profileLetter.setVisibility(View.GONE);

                textView.setText(item.stringRes);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

                if (TxtUtils.isNotEmpty(item.fontPath)) {
                    textView.setTypeface(BookCSS.getTypeFaceForFont(item.fontPath));
                }
                if (AppState.get().appTheme == AppState.THEME_INK) {
                    textView.setTextColor(Color.BLACK);
                }

                CheckBox checkbox1 = (CheckBox) layout.findViewById(R.id.checkbox1);
                if(TxtUtils.isNotEmpty(item.checkboxString)){
                    checkbox1.setVisibility(View.VISIBLE);
                    checkbox1.setText(item.checkboxString);
                    checkbox1.setOnCheckedChangeListener(null);
                    checkbox1.setChecked(item.checkboxState);
                    checkbox1.setOnCheckedChangeListener(item.checkedChangeListener);
                }


                ImageView imageView = (ImageView) layout.findViewById(R.id.image1);
                if (item.iconRes != 0) {
                    imageView.setImageResource(item.iconRes);
                    if (item.iconRes == R.mipmap.icon_pdf_pro || Boolean.TRUE.equals(item.active)) {
                        TintUtil.setNoTintImage(imageView);
                    } else if (Boolean.FALSE.equals(item.active)) {
                        TintUtil.setTintImageWithAlpha(imageView, Color.LTGRAY);
                    } else {
                        if (isTabsActivity) {
                            if (AppState.get().appTheme == AppState.THEME_INK || AppState.get().appTheme == AppState.THEME_LIGHT) {
                                TintUtil.setTintImageWithAlpha(imageView, TintUtil.color);
                            } else {
                                TintUtil.setTintImageWithAlpha(imageView, Color.LTGRAY);
                            }
                        } else {

                            if (AppState.get().isDayNotInvert) {
                                TintUtil.setTintImageWithAlpha(imageView, TintUtil.color);
                            } else {
                                TintUtil.setTintImageWithAlpha(imageView, Color.LTGRAY);
                            }
                        }
                    }
                } else if (item.drawable != null) {
                    profileLetter.setVisibility(View.VISIBLE);
                    profileLetter.setText(item.letter);
                    profileLetter.setBackgroundDrawable(item.drawable);
                    imageView.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
                layout.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(item.click!=null) {
                            item.click.onMenuItemClick(null);
                        }
                        try {
                            p1.dismiss();
                        } catch (Exception e) {
                            LOG.e(e);
                        }
                    }
                });

                layout.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        if (item.onLongClick == null) {
                            return false;
                        }
                        item.onLongClick.onMenuItemClick(null);
                        try {
                            p1.dismiss();
                        } catch (Exception e) {
                            LOG.e(e);
                        }
                        return true;
                    }

                });

            }
        };

        if (anchor != null) {

            p1.setModal(true);
            p1.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    p1.dismiss();
                    if (isTabsActivity) {
                        if (AppState.get().isFullScreenMain) {
                            Keyboards.hideNavigation((Activity) c);
                        }
                    } else {
                        if (AppState.get().isFullScreen) {
                            Keyboards.hideNavigation((Activity) c);
                        }
                    }
                    if (onDismissListener != null) {
                        onDismissListener.onDismiss();
                    }

                }

            });


            p1.setAnchorView(anchor);


            p1.setAdapter(a);

            try {
                p1.setWidth(measureContentWidth(a, c) + Dips.dpToPx(20));
            } catch (Exception e) {
                p1.setWidth(200);
            }

            p1.show();

            if (pos != -1) {
                p1.setSelection(pos - 2);
            }
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(c);
            ListView list = new ListView(c);
            list.setDivider(null);
            list.setAdapter(a);
            builder.setView(list);
            builder.show();
        }

    }

    private int measureContentWidth(ListAdapter listAdapter, Context mContext) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final ListAdapter adapter = listAdapter;
        final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(mContext);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }

    public Menu getMenu() {
        Menu m = new Menu();
        list.add(m);
        return m;
    }

    public Menu getMenu(int icon, int text, Runnable run) {
        Menu m = new Menu();
        m.setIcon(icon);
        m.add(text);
        m.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                run.run();
                return true;
            }
        });

        list.add(m);

        return m;
    }

    public void setAnchor(View anchor) {
        this.anchor = anchor;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

}
