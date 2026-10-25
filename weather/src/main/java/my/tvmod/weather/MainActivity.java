package my.tvmod.weather;

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
    private TextView head, temp, desc, d1, d2, d3, foot;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            fetch();
            h.postDelayed(this, 15 * 60_000L);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this);
        head = Ui.text(this, root, Config.CITY, 22, Ui.gray());
        temp = Ui.text(this, root, "--°C", 96, Ui.white());
        desc = Ui.text(this, root, "Memuatkan…", 36, Color.parseColor("#7FB2FF"));
        d1 = Ui.text(this, root, "", 28, Ui.white());
        d2 = Ui.text(this, root, "", 28, Ui.white());
        d3 = Ui.text(this, root, "", 28, Ui.white());
        foot = Ui.text(this, root, "Sumber: Open-Meteo", 16, Ui.gray());
        for (TextView v : new TextView[]{d1, d2, d3}) v.setPadding(0, Ui.dp(this, 8), 0, 0);
    }

    @Override protected void onResume() { super.onResume(); h.post(tick); }
    @Override protected void onPause() { super.onPause(); h.removeCallbacks(tick); }

    private static String describe(int code) {
        if (code == 0) return "Cerah";
        if (code <= 2) return "Sebahagian berawan";
        if (code == 3) return "Mendung";
        if (code == 45 || code == 48) return "Berkabus";
        if (code >= 51 && code <= 57) return "Gerimis";
        if (code >= 61 && code <= 67) return "Hujan";
        if (code >= 71 && code <= 77) return "Salji";
        if (code >= 80 && code <= 82) return "Hujan lebat sekejap";
        if (code >= 95) return "Ribut petir";
        return "Cuaca tidak diketahui";
    }

    private void fetch() {
        new Thread(() -> {
            try {
                JSONObject o = new JSONObject(Http.get(
                        "https://api.open-meteo.com/v1/forecast?latitude=" + Config.LAT + "&longitude=" + Config.LON
                                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m"
                                + "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max"
                                + "&timezone=auto&forecast_days=1"));
                JSONObject c = o.getJSONObject("current");
                JSONObject d = o.getJSONObject("daily");
                final String t = Math.round(c.getDouble("temperature_2m")) + "°C";
                final String ds = describe(c.getInt("weather_code"));
                final String a = "Terasa seperti " + Math.round(c.getDouble("apparent_temperature")) + "°C";
                final String b = "Kelembapan " + Math.round(c.getDouble("relative_humidity_2m")) + "%   Angin "
                        + Math.round(c.getDouble("wind_speed_10m")) + " km/j";
                final String r = "Tertinggi " + Math.round(d.getJSONArray("temperature_2m_max").getDouble(0))
                        + "°  Terendah " + Math.round(d.getJSONArray("temperature_2m_min").getDouble(0))
                        + "°  Hujan " + d.getJSONArray("precipitation_probability_max").getInt(0) + "%";
                h.post(() -> { temp.setText(t); desc.setText(ds); d1.setText(a); d2.setText(b); d3.setText(r); });
            } catch (Exception e) {
                h.post(() -> desc.setText("Gagal memuat cuaca. Semak sambungan internet."));
            }
        }).start();
    }
}
