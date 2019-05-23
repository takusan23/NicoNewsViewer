package io.github.takusan23.niconewsviewer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DarkModeSupport {
    private Context context;
    private int nightMode;
    private Activity activity;
    private SharedPreferences pref_setting;

    /*テキストビューの染色だけならここからどうぞ*/
    public DarkModeSupport(Context context) {
        this.context = context;
        pref_setting = PreferenceManager.getDefaultSharedPreferences(context);
        setDarkmode();
    }

    //ダークモード設定
    private void setDarkmode() {
        //ダークモード処理
        Configuration conf = context.getResources().getConfiguration();
        nightMode = conf.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //Android Q以前の場合はメニューから切り替えを行う
        if (Build.VERSION.SDK_INT <= 28 && !Build.VERSION.CODENAME.equals("Q")) {
            if (pref_setting.getBoolean("darkmode", false)) {
                nightMode = Configuration.UI_MODE_NIGHT_YES;
            } else {
                nightMode = Configuration.UI_MODE_NIGHT_NO;
            }
        }
    }

    /*テーマを適用する*/
    public void setActivityTheme(Activity activity) {
        this.activity = activity;
        setDarkmode();
        //テーマ切り替え
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                if (activity instanceof MainActivity) {
                    activity.setTheme(R.style.AppTheme_NoActionBar);
                } else {
                    activity.setTheme(R.style.AppTheme);
                }
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                if (activity instanceof MainActivity) {
                    activity.setTheme(R.style.DarkMode_NoActionBar);
                } else {
                    activity.setTheme(R.style.DarkMode);
                }
                break;
        }
    }

    /*背景ダークモード*/
    public void setBackgroundDarkMode(LinearLayout linearLayout) {
        setDarkmode();
        if (linearLayout instanceof LinearLayout) {
            switch (nightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    linearLayout.setBackgroundColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    linearLayout.setBackgroundColor(context.getResources().getColor(android.R.color.black, context.getTheme()));
                    break;
            }
        }
    }

    //染色する
    public void setTextViewThemeColor(TextView textView) {
        setDarkmode();
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                textView.setTextColor(context.getColor(android.R.color.black));
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.black, context.getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                textView.setTextColor(context.getColor(android.R.color.white));
                textView.setCompoundDrawableTintList(context.getResources().getColorStateList(android.R.color.white, context.getTheme()));
                break;
        }
    }

    //Linearlayoutから子View全取得してTextViewなら染色を行う
    public void setLayoutAllThemeColor(LinearLayout layout) {
        setBackgroundDarkMode(layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof LinearLayout) {
                setLayoutAllThemeColor((LinearLayout) layout.getChildAt(i));
            }
            if (layout.getChildAt(i) instanceof TextView) {
                setTextViewThemeColor((TextView) layout.getChildAt(i));
            }
        }
    }

    public int getNightMode() {
        return nightMode;
    }
}
