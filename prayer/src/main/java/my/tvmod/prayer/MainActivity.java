package my.tvmod.prayer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.tvmod.common.Config;
import my.tvmod.common.Http;
import my.tvmod.common.Ui;

public class MainActivity extends Activity {
    private static final String[] NAMES = {"Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak"};
    private static final int GREEN = Color.parseColor("#4CC38A");

    private final Handler h = new Handler(Looper.getMainLooper());
    private final TextView[] nameV = new TextView[6];
    private final TextView[] timeV = new TextView[6];
    private TextView head, next, foot;
    private String[] times; // "HH:mm" x6
    private String source = "";
    private long lastFetch = 0;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (times == null || System.currentTimeMillis() - lastFetch > 3 * 3600_000L) fetch();
            render();
            h.postDelayed(this, 20_000);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this);
        head = Ui.text(this, root, Config.CITY, 22, Ui.gray());
        next = Ui.text(this, root, "Memuatkan…", 40, Ui.white());
        for (int i = 0; i < 6; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 10));
            nameV[i] = new TextView(this);
            nameV[i].setText(NAMES[i]);
            nameV[i].setTextSize(30);
            timeV[i] = new TextView(this);
            timeV[i].setText("--:--");
            timeV[i].setTextSize(30);
            row.addView(nameV[i], new LinearLayout.LayoutParams(Ui.dp(this, 260), -2));
            row.addView(timeV[i], new LinearLayout.LayoutParams(-2, -2));
            root.addView(row);
        }
        foot = Ui.text(this, root, "", 16, Ui.gray());
    }

    @Override protected void onResume() { super.onResume(); h.post(tick); }
    @Override protected void onPause() { super.onPause(); h.removeCallbacks(tick); }

    private void fetch() {
        lastFetch = System.currentTimeMillis();
        new Thread(() -> {
            String[] t = null;
            String src = "";
            try {
                JSONObject o = new JSONObject(Http.get(
                        "https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone=" + Config.ZONE));
                JSONObject p = o.getJSONArray("prayerTime").getJSONObject(0);
                t = new String[]{p.getString("fajr"), p.getString("syuruk"), p.getString("dhuhr"),
                        p.getString("asr"), p.getString("maghrib"), p.getString("isha")};
                src = "Sumber: JAKIM e-Solat, zon " + Config.ZONE;
            } catch (Exception e1) {
                try {
                    JSONObject o = new JSONObject(Http.get(
                            "https://api.aladhan.com/v1/timings?latitude=" + Config.LAT
                                    + "&longitude=" + Config.LON + "&method=17"))
                            .getJSONObject("data").getJSONObject("timings");
                    t = new String[]{o.getString("Fajr"), o.getString("Sunrise"), o.getString("Dhuhr"),
                            o.getString("Asr"), o.getString("Maghrib"), o.getString("Isha")};
                    src = "Sumber: Aladhan (kaedah JAKIM), JAKIM tidak dapat dicapai";
                } catch (Exception e2) {
                    src = "Gagal memuat turun waktu solat. Semak sambungan internet.";
                }
            }
            if (t != null) for (int i = 0; i < 6; i++) t[i] = t[i].substring(0, 5);
            final String[] ft = t;
            final String fs = src;
            h.post(() -> {
                if (ft != null) times = ft; else lastFetch = 0;
                source = fs;
                render();
            });
        }).start();
    }

    private static int mins(String hhmm) {
        return Integer.parseInt(hhmm.substring(0, 2)) * 60 + Integer.parseInt(hhmm.substring(3, 5));
    }

    private void render() {
        head.setText(Config.CITY + "  •  " + new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("ms", "MY")).format(new Date()));
        foot.setText(source);
        if (times == null) return;
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int idx = -1;
        for (int i = 0; i < 6; i++) if (mins(times[i]) > now) { idx = i; break; }
        int diff;
        String label;
        if (idx == -1) { idx = 0; diff = mins(times[0]) + 1440 - now; } else { diff = mins(times[idx]) - now; }
        label = NAMES[idx];
        next.setText(label + " dalam " + (diff / 60) + "j " + (diff % 60) + "m");
        for (int i = 0; i < 6; i++) {
            timeV[i].setText(times[i]);
            int col = (i == idx) ? GREEN : (i == 1 ? Ui.gray() : Ui.white());
            nameV[i].setTextColor(col);
            timeV[i].setTextColor(col);
        }
    }
}
