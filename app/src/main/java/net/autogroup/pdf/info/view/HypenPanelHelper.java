package net.autogroup.pdf.info.view;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import net.autogroup.android.utils.TxtUtils;
import net.autogroup.dao2.FileMeta;
import net.autogroup.hypen.HyphenPattern;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;
import net.autogroup.pdf.info.R;
import net.autogroup.pdf.info.Urls;
import net.autogroup.pdf.info.model.BookCSS;
import net.autogroup.pdf.info.widget.DialogTranslateFromTo;
import net.autogroup.pdf.info.wrapper.DocumentController;
import net.autogroup.ui2.AppDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HypenPanelHelper {


    public static void init(View parent, DocumentController dc) {
        View hyphenPanel = parent.findViewById(R.id.showHypenLangPanel);
        hyphenPanel.setVisibility(TxtUtils.visibleIf(dc.isTextFormat() && BookCSS.get().isAutoHypens && TxtUtils.isEmpty(AppTemp.get().hypenLang)));


        final TextView hypenLang = (TextView) parent.findViewById(R.id.hypenLang);
        final TextView hypenApply = (TextView) parent.findViewById(R.id.hypenApply);

        hypenLang.setText(R.string.choose_);

        TxtUtils.underlineTextView(hypenLang);
        TxtUtils.underlineTextView(hypenApply);

        hypenLang.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

                HyphenPattern[] values = HyphenPattern.values();

                List<String> all = new ArrayList<String>();

                for (HyphenPattern p : values) {
                    String e1 = DialogTranslateFromTo.getLanuageByCode(p.lang) + ":" + p.lang;
                    all.add(e1);

                }
                Collections.sort(all);
                if(TxtUtils.isEmpty(AppTemp.get().lastBookLang)){
                    AppTemp.get().lastBookLang = AppState.get().appLang.equals(AppState.MY_SYSTEM_LANG) ? Urls.getLangCode() : AppState.get().appLang;
                }
                String e = DialogTranslateFromTo.getLanuageByCode(AppTemp.get().lastBookLang) + ":" + AppTemp.get().lastBookLang;
                all.add(0, e);

                for (final String langFull : all) {
                    String[] split = langFull.split(":");
                    final String titleLang = split[0];
                    final String code = split[1];
                    popupMenu.getMenu().add(titleLang).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            AppTemp.get().hypenLang = code;
                            AppTemp.get().lastBookLang = code;
                            hypenLang.setText(titleLang);
                            TxtUtils.underlineTextView(hypenLang);
                            FileMeta load = AppDB.get().load(dc.getCurrentBook().getPath());
                            if (load != null) {
                                load.setLang(code);
                                AppDB.get().update(load);
                            }
                            dc.restartActivity();
                            return false;
                        }
                    });
                }
                popupMenu.show();

            }
        });
        hypenApply.setVisibility(View.GONE);
        hypenApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TxtUtils.isNotEmpty(AppTemp.get().hypenLang)) {
                    dc.restartActivity();
                }
            }
        });

    }
}
