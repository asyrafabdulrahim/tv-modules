package my.tvmod.prayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.tvmod.common.Config;
import my.tvmod.common.Http;
import my.tvmod.common.Ui;

public class MainActivity extends Activity {
    private static final String[] NAMES = {"Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak"};
    private static final String[] KEYS = {"fajr", "syuruk", "dhuhr", "asr", "maghrib", "isha"};
    private static final int GLOW = 0xFF4CC38A;

    // {kod zon, negeri, kawasan}
    private static final String[][] ZONES = {
        {"JHR01", "Johor", "Pulau Aur, Pulau Pemanggil"},
        {"JHR02", "Johor", "Johor Bahru, Kota Tinggi, Mersing, Kulai"},
        {"JHR03", "Johor", "Kluang, Pontian"},
        {"JHR04", "Johor", "Batu Pahat, Muar, Segamat, Tangkak"},
        {"KDH01", "Kedah", "Kota Setar, Kubang Pasu, Pokok Sena"},
        {"KDH02", "Kedah", "Kuala Muda, Yan, Pendang"},
        {"KDH03", "Kedah", "Padang Terap, Sik"},
        {"KDH04", "Kedah", "Baling"},
        {"KDH05", "Kedah", "Bandar Baharu, Kulim"},
        {"KDH06", "Kedah", "Langkawi"},
        {"KDH07", "Kedah", "Gunung Jerai"},
        {"KTN01", "Kelantan", "Kota Bharu, Bachok, Machang, Pasir Mas, Tumpat"},
        {"KTN03", "Kelantan", "Jeli, Gua Musang"},
        {"MLK01", "Melaka", "Seluruh Melaka"},
        {"NGS01", "Negeri Sembilan", "Tampin, Jempol"},
        {"NGS02", "Negeri Sembilan", "Jelebu, Kuala Pilah, Rembau"},
        {"NGS03", "Negeri Sembilan", "Port Dickson, Seremban"},
        {"PHG01", "Pahang", "Pulau Tioman"},
        {"PHG02", "Pahang", "Kuantan, Pekan, Rompin"},
        {"PHG03", "Pahang", "Jerantut, Temerloh, Maran, Bera"},
        {"PHG04", "Pahang", "Bentong, Lipis, Raub"},
        {"PHG05", "Pahang", "Genting Sempah, Janda Baik, Bukit Tinggi"},
        {"PHG06", "Pahang", "Cameron Highlands, Bukit Fraser"},
        {"PLS01", "Perlis", "Kangar, Padang Besar, Arau"},
        {"PNG01", "Pulau Pinang", "Seluruh Pulau Pinang"},
        {"PRK01", "Perak", "Tapah, Slim River, Tanjung Malim"},
        {"PRK02", "Perak", "Ipoh, Kuala Kangsar, Batu Gajah, Kampar"},
        {"PRK03", "Perak", "Lenggong, Pengkalan Hulu, Grik"},
        {"PRK04", "Perak", "Temengor, Belum"},
        {"PRK05", "Perak", "Teluk Intan, Bagan Datuk, Lumut, Sitiawan"},
        {"PRK06", "Perak", "Taiping, Selama, Parit Buntar"},
        {"PRK07", "Perak", "Bukit Larut"},
        {"SBH01", "Sabah", "Sandakan, Bukit Garam, Semawang"},
        {"SBH02", "Sabah", "Beluran, Telupid, Kuamut"},
        {"SBH03", "Sabah", "Lahad Datu, Kunak, Semporna"},
        {"SBH04", "Sabah", "Tawau, Kalabakan"},
        {"SBH05", "Sabah", "Kudat, Kota Marudu, Pitas"},
        {"SBH06", "Sabah", "Gunung Kinabalu"},
        {"SBH07", "Sabah", "Kota Kinabalu, Ranau, Tuaran, Papar"},
        {"SBH08", "Sabah", "Pensiangan, Keningau, Tambunan"},
        {"SBH09", "Sabah", "Beaufort, Sipitang, Tenom"},
        {"SGR01", "Selangor", "Shah Alam, Petaling, Gombak, Sepang, Hulu Langat, Rawang"},
        {"SGR02", "Selangor", "Kuala Selangor, Sabak Bernam"},
        {"SGR03", "Selangor", "Klang, Kuala Langat"},
        {"SWK01", "Sarawak", "Limbang, Lawas"},
        {"SWK02", "Sarawak", "Miri, Niah, Sibuti"},
        {"SWK03", "Sarawak", "Bintulu, Belaga, Tatau"},
        {"SWK04", "Sarawak", "Sibu, Mukah, Kapit"},
        {"SWK05", "Sarawak", "Sarikei, Matu, Julau"},
        {"SWK06", "Sarawak", "Sri Aman, Betong, Saratok, Lubok Antu"},
        {"SWK07", "Sarawak", "Serian, Samarahan"},
        {"SWK08", "Sarawak", "Kuching, Bau, Lundu"},
        {"SWK09", "Sarawak", "Zon khas (Kampung Patarikan)"},
        {"TRG01", "Terengganu", "Kuala Terengganu, Marang, Kuala Nerus"},
        {"TRG02", "Terengganu", "Besut, Setiu"},
        {"TRG03", "Terengganu", "Hulu Terengganu"},
        {"TRG04", "Terengganu", "Dungun, Kemaman"},
        {"WLY01", "Wilayah Persekutuan", "Kuala Lumpur, Putrajaya"},
        {"WLY02", "Wilayah Persekutuan", "Labuan"},
    };

    private final Handler h = new Handler(Looper.getMainLooper());
    private final LinearLayout[] cols = new LinearLayout[6];
    private final TextView[] nameV = new TextView[6];
    private final TextView[] timeV = new TextView[6];
    private TextView head, countdown, foot;
    private SharedPreferences sp;
    private String zone;
    private String[] times;
    private String dataDay = "";
    private String status = "";
    private long lastAttempt = 0;
    private int shownIdx = -2;
    private boolean dirty = true;
    private boolean fetching = false;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!fetching && !today().equals(dataDay)
                    && System.currentTimeMillis() - lastAttempt > 60_000L) fetch();
            render();
            h.postDelayed(this, 1000);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("prayer", MODE_PRIVATE);
        zone = sp.getString("zone", Config.ZONE);
        loadCache();

        LinearLayout root = Ui.root(this);
        head = Ui.text(this, root, "", 22, Ui.gray());
        countdown = Ui.text(this, root, "Memuatkan…", 40, Ui.white());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 230));
        rp.topMargin = Ui.dp(this, 28);
        root.addView(row, rp);

        for (int i = 0; i < 6; i++) {
            LinearLayout c = new LinearLayout(this);
            c.setOrientation(LinearLayout.VERTICAL);
            c.setGravity(Gravity.CENTER);
            nameV[i] = new TextView(this);
            nameV[i].setText(NAMES[i]);
            nameV[i].setTextSize(26);
            nameV[i].setGravity(Gravity.CENTER);
            timeV[i] = new TextView(this);
            timeV[i].setText("--:--");
            timeV[i].setTextSize(40);
            timeV[i].setGravity(Gravity.CENTER);
            timeV[i].setPadding(0, Ui.dp(this, 10), 0, 0);
            c.addView(nameV[i], new LinearLayout.LayoutParams(-2, -2));
            c.addView(timeV[i], new LinearLayout.LayoutParams(-2, -2));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, -1, 1f);
            cp.setMargins(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
            row.addView(c, cp);
            cols[i] = c;
        }
        foot = Ui.text(this, root, "", 16, Ui.gray());
        foot.setPadding(0, Ui.dp(this, 28), 0, 0);
    }

    @Override protected void onResume() { super.onResume(); h.post(tick); }
    @Override protected void onPause() { super.onPause(); h.removeCallbacks(tick); }

    @Override public boolean onKeyDown(int k, KeyEvent e) {
        if (k == KeyEvent.KEYCODE_DPAD_CENTER || k == KeyEvent.KEYCODE_ENTER || k == KeyEvent.KEYCODE_MENU) {
            showStates();
            return true;
        }
        return super.onKeyDown(k, e);
    }

    private void showStates() {
        ArrayList<String> st = new ArrayList<>();
        for (String[] z : ZONES) if (!st.contains(z[1])) st.add(z[1]);
        final String[] arr = st.toArray(new String[0]);
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Pilih negeri")
                .setItems(arr, (d, w) -> showZones(arr[w]))
                .show();
    }

    private void showZones(String state) {
        final ArrayList<String[]> zs = new ArrayList<>();
        for (String[] z : ZONES) if (z[1].equals(state)) zs.add(z);
        String[] labels = new String[zs.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = zs.get(i)[0] + "  " + zs.get(i)[2];
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle(state + ": pilih bandar/kawasan")
                .setItems(labels, (d, w) -> setZone(zs.get(w)[0]))
                .show();
    }

    private void setZone(String code) {
        zone = code;
        sp.edit().putString("zone", code).remove("ct").remove("cz").remove("cd").apply();
        times = null;
        dataDay = "";
        status = "";
        dirty = true;
        lastAttempt = 0;
        render();
    }

    private void loadCache() {
        String cz = sp.getString("cz", "");
        String ct = sp.getString("ct", "");
        String cd = sp.getString("cd", "");
        if (cz.equals(zone) && ct.length() > 0) {
            String[] p = ct.split(",");
            if (p.length == 6) { times = p; dataDay = cd; }
        }
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    private void fetch() {
        fetching = true;
        lastAttempt = System.currentTimeMillis();
        final String z = zone;
        final String day = today();
        new Thread(() -> {
            String[] t;
            String msg;
            try {
                JSONObject o = new JSONObject(Http.get(
                        "https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone=" + z));
                JSONObject p = o.getJSONArray("prayerTime").getJSONObject(0);
                t = new String[6];
                for (int i = 0; i < 6; i++) t[i] = p.getString(KEYS[i]);
                msg = "";
            } catch (Exception e) {
                t = null;
                msg = "Tiada sambungan ke JAKIM.";
            }
            final String[] ft = t;
            final String fm = msg;
            h.post(() -> {
                fetching = false;
                if (!z.equals(zone)) return;
                if (ft != null) {
                    times = ft;
                    dataDay = day;
                    status = "";
                    dirty = true;
                    sp.edit().putString("cz", z).putString("ct", TextUtils.join(",", ft))
                            .putString("cd", day).apply();
                } else {
                    status = fm;
                }
                render();
            });
        }).start();
    }

    private static int sec(String t) {
        int s = Integer.parseInt(t.substring(0, 2)) * 3600 + Integer.parseInt(t.substring(3, 5)) * 60;
        if (t.length() >= 8) s += Integer.parseInt(t.substring(6, 8));
        return s;
    }

    private static String two(int n) { return n < 10 ? "0" + n : String.valueOf(n); }

    private String zoneLabel() {
        for (String[] z : ZONES) if (z[0].equals(zone)) return z[1] + " • " + z[2];
        return zone;
    }

    private void style(int i, boolean current) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(Ui.dp(this, 24));
        if (current) {
            g.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
            g.setColors(new int[]{0x55FFFFFF, 0x1AFFFFFF});
            g.setStroke(Ui.dp(this, 2), 0x88FFFFFF);
        } else {
            g.setColor(0x0DFFFFFF);
        }
        cols[i].setBackground(g);
        nameV[i].setTextColor(current ? 0xFFEAFBF3 : (i == 1 ? Ui.gray() : 0xFFC9D1D9));
        timeV[i].setTextColor(current ? Ui.white() : (i == 1 ? Ui.gray() : 0xFFC9D1D9));
        timeV[i].setShadowLayer(current ? 30f : 0f, 0f, 0f, GLOW);
        float s = current ? 1.06f : 1f;
        cols[i].animate().scaleX(s).scaleY(s).setDuration(300).start();
    }

    private void render() {
        head.setText(zoneLabel() + "  •  "
                + new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("ms", "MY")).format(new Date()));
        String hint = "   [OK] tukar kawasan";
        if (times == null) {
            countdown.setText(status.length() > 0 ? "Tiada data" : "Memuatkan…");
            foot.setText(status + hint);
            return;
        }
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
        int idx = 5;
        for (int i = 0; i < 6; i++) if (sec(times[i]) <= now) idx = i;
        int nxt = (idx + 1) % 6;
        int diff = sec(times[nxt]) - now;
        if (diff <= 0) diff += 86400;
        countdown.setText(NAMES[nxt] + " dalam " + two(diff / 3600) + ":" + two(diff % 3600 / 60) + ":" + two(diff % 60));
        if (idx != shownIdx || dirty) {
            for (int i = 0; i < 6; i++) {
                timeV[i].setText(times[i].substring(0, 5));
                style(i, i == idx);
            }
            shownIdx = idx;
            dirty = false;
        }
        String old = today().equals(dataDay) ? "" : "Data lama, belum dikemas kini.  ";
        foot.setText("Sumber: JAKIM e-Solat  •  " + old + status + hint);
    }
}
