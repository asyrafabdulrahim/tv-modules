package my.tvmod.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    private Ui() {}

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static LinearLayout root(Activity a) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(0xFF0E1216);
        l.setPadding(dp(a, 56), dp(a, 36), dp(a, 56), dp(a, 36));
        a.setContentView(l);
        return l;
    }

    public static TextView text(Activity a, LinearLayout parent, String t, int sp, int color) {
        TextView v = new TextView(a);
        v.setText(t);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setGravity(Gravity.START);
        parent.addView(v, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return v;
    }

    public static int gray() { return Color.parseColor("#9AA5B1"); }
    public static int white() { return Color.WHITE; }
}
