package android.support.v7.widget;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.os.Build;

public class MyRoundRectDrawable extends RoundRectDrawable {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyRoundRectDrawable(int backgroundColor, float radius) {
        super(ColorStateList.valueOf(backgroundColor), radius);
    }

}