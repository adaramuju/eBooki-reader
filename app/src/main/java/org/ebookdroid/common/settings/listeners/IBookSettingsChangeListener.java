package org.ebookdroid.common.settings.listeners;

import net.autogroup.model.AppBook;

public interface IBookSettingsChangeListener {

    void onBookSettingsChanged(AppBook oldSettings, AppBook newSettings, AppBook.Diff diff);

}
