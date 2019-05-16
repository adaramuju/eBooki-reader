package net.autogroup.sys;

import net.autogroup.android.utils.Dips;
import net.autogroup.model.AppState;
import net.autogroup.model.AppTemp;

public class ClickUtils {
    private int WIDHT_X, HEIGHT_Y;
    private float border_side = 0;
    private float border_top = 0;

    public ClickUtils() {
        init();
    }

    public void init() {
        WIDHT_X = Dips.screenWidth();
        HEIGHT_Y = Dips.screenHeight();

        border_side = Dips.screenWidth() / 100 * AppState.get().tapzoneSize;
        border_top = Dips.screenHeight() / 100 * AppState.get().tapzoneSize;
    }


    public void initMusician() {
        WIDHT_X = Dips.screenWidth();
        HEIGHT_Y = Dips.screenHeight();
        border_side = AppTemp.get().readingMode == AppState.READING_MODE_MUSICIAN ? 0 : Dips.screenWidth() / 100 * AppState.get().tapzoneSize;
        border_top = AppTemp.get().readingMode == AppState.READING_MODE_MUSICIAN ? 0 : Dips.screenHeight() / 100 * AppState.get().tapzoneSize;
    }

    public boolean isClickCenter(float x, float y) {
        boolean isX = border_side < x && x < (WIDHT_X - border_side);
        boolean isY = border_top < y && y < (HEIGHT_Y - border_top);
        return isX && isY;

    }

    public boolean isClickTop(float x, float y) {
        return y < border_top;
    }

    public boolean isClickBottom(float x, float y) {
        return y > (HEIGHT_Y - border_top);
    }

    public boolean isClickLeft(float x, float y) {
        return x < border_side;
    }

    public boolean isClickRight(float x, float y) {
        return x > (WIDHT_X - border_side);
    }

}
