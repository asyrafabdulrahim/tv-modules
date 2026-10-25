package my.tvmod.air;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import my.tvmod.common.Config;
import my.tvmod.common.Http;
import my.tvmod.common.Ui;

public class MainActivity extends Activity {
    private final Handler h = new Handler(Looper.getMainLooper());
    private TextView head, aqi, cat, d1, note;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            fetch();
            h.postDelayed(this, 30 * 60_000L);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this);
        head = Ui.text(this, root, Config.CITY, 22, Ui.gray());
        aqi = Ui.text(this, root, "--", 110, Ui.white());
        cat = Ui.text(this, root, "Memuatkan…", 40, Ui.white());
        d1 = Ui.text(this, root, "", 28, Ui.white());
        note = Ui.text(this, root, "Anggaran model (US AQI, Open-Meteo), BUKAN bacaan IPU rasmi Jabatan Alam Sekitar.", 16, Ui.gray());
        d1.setPadding(0, Ui.dp(this, 12), 0, Ui.dp(this, 12));
    }

    @Override protected void onResume() { super.onResume(); h.post(tick); }
    @Override protected void onPause() { super.onPause(); h.removeCallbacks(tick); }

    private void fetch() {
        new Thread(() -> {
            try {
                JSONObject c = new JSONObject(Http.get(
                        "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=" + Config.LAT
                                + "&longitude=" + Config.LON + "&current=us_aqi,pm2_5,pm10&timezone=auto"))
                        .getJSONObject("current");
                final int v = (int) Math.round(c.getDouble("us_aqi"));
                final String pm = "PM2.5 " + Math.round(c.getDouble("pm2_5")) + " µg/m³   PM10 "
                        + Math.round(c.getDouble("pm10")) + " µg/m³";
                final String name;
                final int col;
                if (v <= 50) { name = "Baik"; col = Color.parseColor("#4CC38A"); }
                else if (v <= 100) { name = "Sederhana"; col = Color.parseColor("#E8D44D"); }
                else if (v <= 150) { name = "Tidak sihat untuk golongan sensitif"; col = Color.parseColor("#F29B38"); }
                else if (v <= 200) { name = "Tidak sihat"; col = Color.parseColor("#E5534B"); }
                else if (v <= 300) { name = "Sangat tidak sihat"; col = Color.parseColor("#A855F7"); }
                else { name = "Berbahaya"; col = Color.parseColor("#9B1C31"); }
                h.post(() -> { aqi.setText(String.valueOf(v)); aqi.setTextColor(col); cat.setText(name); d1.setText(pm); });
            } catch (Exception e) {
                h.post(() -> cat.setText("Gagal memuat data. Semak sambungan internet."));
            }
        }).start();
    }
}
