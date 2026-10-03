package com.example.agri_detect;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    // ====== ADAPTED TO YOUR DATABASE ======
    private static final String DB_URL = "url.firebaseio.com";
    private static final String DB_PATH = "path";
    private static final String[] TEMP_KEYS = {"temperature", "temp", "t"};
    private static final String[] HUM_KEYS = {"humidity", "hum", "h"};
    // Added "air_quality" and "status" to catch the strings currently in your database
    private static final String[] GAS_KEYS = {"gas_detected", "gasdetected", "gas", "gaz", "gas_value", "gasvalue", "mq2", "air_quality", "status"};
    private static final double GAS_THRESHOLD = 1.0;
    // ==========================================

    // Cozy Greenery Palette (Matches colors.xml)
    private static final int ACCENT = 0xFF2E7D6B;      // Deep Green
    private static final int WATER = 0xFF6C8EA0;       // Slate Blue
    private static final int AMBER = 0xFFFFB547;       // Warm Amber
    private static final int RED = 0xFFFF5D4D;         // Soft Red
    private static final int TEXT_PRIMARY = 0xFF1F2937; // Dark Grey
    private static final int TEXT_SECONDARY = 0xFF6B7280; // Muted Grey
    private static final int ON_ACCENT = 0xFFFFFFFF;   // White text on dark green

    private static final long[] RANGES_MS = {
            30L * 60L * 1000L, 6L * 60L * 60L * 1000L, 24L * 60L * 60L * 1000L
    };

    // views
    private FrameLayout segment;
    private View segThumb, dotLive;
    private TextView tabLive, tabHistory, tvTitle, tvLive;
    private TextView tvStatusTitle, tvStatusSub, tvDebug;
    private TextView chip30, chip6, chip24, tvTempStats, tvHumStats, tvGasSummary;
    private View pageLive, pageHistory;
    private CardView cardStatus;
    private ArcGaugeView gaugeTemp;
    private WaveOrbView orbHumidity;
    private StatusOrbView orbStatus;
    private LineChartView chartTemp, chartHum;
    private GasStripView stripGas;
    private GradientDrawable dotDrawable;

    // state
    private HistoryStore store;
    private DatabaseReference dataRef;
    private DatabaseReference connectedRef;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean connected = false;
    private long lastDataTime = 0;
    private long sessionStart = 0;
    private boolean historyVisible = false;
    private int thumbWidth = 0;
    private int selectedRange = 0;
    private int lastStatus = StatusOrbView.UNKNOWN;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateLiveLabel();
            handler.postDelayed(this, 1000);
        }
    };

    private final ValueEventListener dataListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            Double temp = toDouble(findValue(snapshot, 0, TEMP_KEYS));
            Double hum = toDouble(findValue(snapshot, 0, HUM_KEYS));
            Boolean gas = parseGas(findValue(snapshot, 0, GAS_KEYS));

            float t = temp != null ? temp.floatValue() : Float.NaN;
            float h = hum != null ? hum.floatValue() : Float.NaN;

            gaugeTemp.setValueCelsius(t);
            orbHumidity.setPercent(h);
            showStatus(gas);

            if (temp != null || hum != null || gas != null) {
                lastDataTime = System.currentTimeMillis();
                pulseDot();
                updateLiveLabel();
                boolean added = store.add(lastDataTime, t, h, gas != null && gas);
                if (added && historyVisible) refreshHistory(false);
            }

            tvDebug.setText("raw: " + snapshot.getValue()
                    + "\ntemp=" + temp + "  hum=" + hum + "  gas=" + gas);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            tvLive.setText("Error: " + error.getMessage());
            tvDebug.setText("onCancelled: " + error.getCode() + " / " + error.getMessage());
        }
    };

    private final ValueEventListener connectionListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
            updateLiveLabel();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            // ignored
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setupWindow();
        setContentView(R.layout.activity_dashboard);

        View root = findViewById(R.id.root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Add padding for status bar and navigation bar, plus a little extra for the cozy look
            v.setPadding(bars.left, bars.top + 16, bars.right, bars.bottom + 16);
            return insets;
        });

        bindViews();
        setupTabs();
        setupRangeChips();

        store = new HistoryStore(getApplicationContext());
        sessionStart = System.currentTimeMillis();

        FirebaseDatabase db = DB_URL.isEmpty()
                ? FirebaseDatabase.getInstance()
                : FirebaseDatabase.getInstance(DB_URL);
        dataRef = DB_PATH.isEmpty() ? db.getReference() : db.getReference(DB_PATH);
        connectedRef = db.getReference(".info/connected");

        tvTitle.setOnLongClickListener(v -> {
            tvDebug.setVisibility(tvDebug.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            return true;
        });
    }

    private void setupWindow() {
        Window w = getWindow();
        WindowCompat.setDecorFitsSystemWindows(w, false);
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(w, w.getDecorView());
        // Set to true for dark icons on light background
        c.setAppearanceLightStatusBars(true);
        c.setAppearanceLightNavigationBars(true);
    }

    private void bindViews() {
        segment = findViewById(R.id.segment);
        segThumb = findViewById(R.id.segThumb);
        dotLive = findViewById(R.id.dotLive);
        tabLive = findViewById(R.id.tabLive);
        tabHistory = findViewById(R.id.tabHistory);
        tvTitle = findViewById(R.id.tvTitle);
        tvLive = findViewById(R.id.tvLive);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusSub = findViewById(R.id.tvStatusSub);
        tvDebug = findViewById(R.id.tvDebug);
        chip30 = findViewById(R.id.chip30);
        chip6 = findViewById(R.id.chip6);
        chip24 = findViewById(R.id.chip24);
        tvTempStats = findViewById(R.id.tvTempStats);
        tvHumStats = findViewById(R.id.tvHumStats);
        tvGasSummary = findViewById(R.id.tvGasSummary);
        pageLive = findViewById(R.id.pageLive);
        pageHistory = findViewById(R.id.pageHistory);
        cardStatus = findViewById(R.id.cardStatus);
        gaugeTemp = findViewById(R.id.gaugeTemp);
        orbHumidity = findViewById(R.id.orbHumidity);
        orbStatus = findViewById(R.id.orbStatus);
        chartTemp = findViewById(R.id.chartTemp);
        chartHum = findViewById(R.id.chartHum);
        stripGas = findViewById(R.id.stripGas);

        dotLive.setBackground(dotLive.getBackground().mutate());
        dotDrawable = (GradientDrawable) dotLive.getBackground();
    }

    // ---------------------------------------------------------------- tabs

    private void setupTabs() {
        tabLive.setOnClickListener(v -> selectTab(false));
        tabHistory.setOnClickListener(v -> selectTab(true));
        segment.post(() -> {
            int inner = segment.getWidth() - segment.getPaddingLeft() - segment.getPaddingRight();
            thumbWidth = inner / 2;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) segThumb.getLayoutParams();
            lp.width = thumbWidth;
            segThumb.setLayoutParams(lp);
            segThumb.setTranslationX(historyVisible ? thumbWidth : 0);
        });
    }

    private void selectTab(boolean history) {
        if (history == historyVisible) return;
        historyVisible = history;

        tabLive.setTextColor(history ? TEXT_SECONDARY : TEXT_PRIMARY);
        tabHistory.setTextColor(history ? TEXT_PRIMARY : TEXT_SECONDARY);
        segThumb.animate().translationX(history ? thumbWidth : 0)
                .setDuration(240).setInterpolator(new DecelerateInterpolator()).start();

        final View in = history ? pageHistory : pageLive;
        final View out = history ? pageLive : pageHistory;
        out.animate().cancel();
        in.animate().cancel();
        out.animate().alpha(0f).setDuration(120).withEndAction(() -> {
            out.setVisibility(View.GONE);
            in.setAlpha(0f);
            in.setVisibility(View.VISIBLE);
            in.animate().alpha(1f).setDuration(200).start();
        }).start();

        if (history) refreshHistory(true);
    }

    private void setupRangeChips() {
        chip30.setOnClickListener(v -> selectRange(0));
        chip6.setOnClickListener(v -> selectRange(1));
        chip24.setOnClickListener(v -> selectRange(2));
        applyChipStyles();
    }

    private void selectRange(int idx) {
        if (idx == selectedRange) return;
        selectedRange = idx;
        applyChipStyles();
        refreshHistory(true);
    }

    private void applyChipStyles() {
        TextView[] chips = {chip30, chip6, chip24};
        for (int i = 0; i < chips.length; i++) {
            boolean on = i == selectedRange;
            chips[i].setBackgroundResource(on ? R.drawable.chip_on : R.drawable.chip_off);
            chips[i].setTextColor(on ? ON_ACCENT : TEXT_SECONDARY);
        }
    }

    // ---------------------------------------------------------------- lifecycle

    @Override
    protected void onStart() {
        super.onStart();
        sessionStart = System.currentTimeMillis();
        dataRef.addValueEventListener(dataListener);
        connectedRef.addValueEventListener(connectionListener);
        handler.post(ticker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dataRef.removeEventListener(dataListener);
        connectedRef.removeEventListener(connectionListener);
        handler.removeCallbacks(ticker);
        store.save();
    }

    // ---------------------------------------------------------------- UI updates

    private void updateLiveLabel() {
        long now = System.currentTimeMillis();
        String text;
        int color;
        if (!connected) {
            if (now - sessionStart < 6000) {
                text = "Connecting…";
                color = AMBER;
            } else {
                text = "Offline";
                color = RED;
            }
        } else if (lastDataTime == 0) {
            text = "Connected · waiting for data";
            color = AMBER;
        } else {
            long s = (now - lastDataTime) / 1000;
            if (s < 60) {
                text = s <= 2 ? "Live · just now" : "Live · updated " + s + "s ago";
                color = ACCENT;
            } else {
                text = "No new data for " + (s / 60) + " min";
                color = AMBER;
            }
        }
        tvLive.setText(text);
        dotDrawable.setColor(color);
    }

    private void pulseDot() {
        dotLive.animate().cancel();
        dotLive.setScaleX(1.9f);
        dotLive.setScaleY(1.9f);
        dotLive.animate().scaleX(1f).scaleY(1f).setDuration(500)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private void showStatus(Boolean gas) {
        int state = gas == null ? StatusOrbView.UNKNOWN
                : (gas ? StatusOrbView.GAS : StatusOrbView.NORMAL);

        if (state == StatusOrbView.GAS && lastStatus != StatusOrbView.GAS) {
            orbStatus.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        }
        lastStatus = state;
        orbStatus.setState(state);

        // Updated colors to match the new Cozy Greenery palette
        switch (state) {
            case StatusOrbView.GAS:
                cardStatus.setCardBackgroundColor(RED);
                tvStatusTitle.setText("Gas detected");
                tvStatusTitle.setTextColor(Color.WHITE);
                tvStatusSub.setText("Check the area and ventilate");
                tvStatusSub.setTextColor(Color.WHITE);
                break;
            case StatusOrbView.NORMAL:
                cardStatus.setCardBackgroundColor(Color.WHITE);
                tvStatusTitle.setText("Normal air quality");
                tvStatusTitle.setTextColor(TEXT_PRIMARY);
                tvStatusSub.setText("No gas detected");
                tvStatusSub.setTextColor(TEXT_SECONDARY);
                break;
            default:
                cardStatus.setCardBackgroundColor(Color.WHITE);
                tvStatusTitle.setText("Waiting for data");
                tvStatusTitle.setTextColor(TEXT_PRIMARY);
                tvStatusSub.setText("Listening to your ESP32");
                tvStatusSub.setTextColor(TEXT_SECONDARY);
                break;
        }
    }

    private void refreshHistory(boolean animate) {
        HistoryStore.Series s = store.query(RANGES_MS[selectedRange], 160);

        chartTemp.setData(s.t, s.temp, s.size, ACCENT, "°C", 4f, animate);
        chartHum.setData(s.t, s.hum, s.size, WATER, "%", 10f, animate);
        stripGas.setData(s.t, s.gas, s.size);

        tvTempStats.setText(stats(s.tempMin, s.tempAvg, s.tempMax, "°C", 1));
        tvHumStats.setText(stats(s.humMin, s.humAvg, s.humMax, "%", 0));

        if (s.size == 0) {
            tvGasSummary.setText("No data yet");
            tvGasSummary.setTextColor(TEXT_SECONDARY);
        } else if (s.gasEvents == 0) {
            tvGasSummary.setText("No gas detected in this period");
            tvGasSummary.setTextColor(TEXT_PRIMARY);
        } else {
            tvGasSummary.setText(s.gasEvents + (s.gasEvents == 1 ? " gas event" : " gas events")
                    + " in this period");
            tvGasSummary.setTextColor(RED);
        }
    }

    private String stats(float min, float avg, float max, String unit, int decimals) {
        if (Float.isNaN(min)) return "No data yet";
        String f = "%." + decimals + "f%s";
        return "Min " + String.format(Locale.US, f, min, unit)
                + "     Avg " + String.format(Locale.US, f, avg, unit)
                + "     Max " + String.format(Locale.US, f, max, unit);
    }

    // ---------------------------------------------------------------- data helpers

    private Object findValue(DataSnapshot node, int depth, String[] names) {
        List<DataSnapshot> children = new ArrayList<>();
        for (DataSnapshot child : node.getChildren()) {
            children.add(child);
        }
        Collections.reverse(children);

        for (DataSnapshot child : children) {
            String key = child.getKey();
            if (key == null) continue;
            for (String name : names) {
                if (key.equalsIgnoreCase(name) && !child.hasChildren()) {
                    return child.getValue();
                }
            }
        }
        if (depth < 3) {
            for (DataSnapshot child : children) {
                if (child.hasChildren()) {
                    Object found = findValue(child, depth + 1, names);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Boolean parseGas(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() >= GAS_THRESHOLD;
        }
        if (value instanceof String) {
            String s = ((String) value).trim().toLowerCase(Locale.ROOT);
            Double numeric = toDouble(s);
            if (numeric != null) return numeric >= GAS_THRESHOLD;

            // Added "gaz detected" and "normal air quality" to match your DB
            List<String> yes = Arrays.asList("true", "detected", "yes", "gas detected", "gaz detected", "hazardous", "bad");
            List<String> no = Arrays.asList("false", "normal", "no", "ok", "none", "normal air quality", "safe");

            if (yes.contains(s)) return true;
            if (no.contains(s)) return false;
        }
        return null;
    }
}