package com.example.agri_detect;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryStore {

    private static final String PREF_NAME = "agri_detect_history_prefs";
    private static final String KEY_SAMPLES = "samples_json";
    private static final int MAX_SAMPLES = 3000;

    public static class Sample {
        public long t;
        public float temp;
        public float hum;
        public boolean gas;

        public Sample(long t, float temp, float hum, boolean gas) {
            this.t = t;
            this.temp = temp;
            this.hum = hum;
            this.gas = gas;
        }
    }

    public static class Series {
        public long[] t;
        public float[] temp;
        public float[] hum;
        public boolean[] gas;
        public int size = 0;

        public float tempMin = Float.NaN;
        public float tempAvg = Float.NaN;
        public float tempMax = Float.NaN;

        public float humMin = Float.NaN;
        public float humAvg = Float.NaN;
        public float humMax = Float.NaN;

        public int gasEvents = 0;

        public Series(int capacity) {
            t = new long[capacity];
            temp = new float[capacity];
            hum = new float[capacity];
            gas = new boolean[capacity];
        }
    }

    private final Context context;
    private final List<Sample> samples = new ArrayList<>();

    public HistoryStore(Context context) {
        this.context = context.getApplicationContext();
        load();
    }

    public synchronized boolean add(long timestamp, float temp, float hum, boolean gas) {
        if (!samples.isEmpty()) {
            Sample last = samples.get(samples.size() - 1);
            if (timestamp - last.t < 2000) {
                return false; // deduplicate within 2s
            }
        }
        samples.add(new Sample(timestamp, temp, hum, gas));
        if (samples.size() > MAX_SAMPLES) {
            samples.remove(0);
        }
        return true;
    }

    public synchronized void save() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (Sample s : samples) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("t", s.t);
                obj.put("temp", Float.isNaN(s.temp) ? JSONObject.NULL : (double) s.temp);
                obj.put("hum", Float.isNaN(s.hum) ? JSONObject.NULL : (double) s.hum);
                obj.put("gas", s.gas);
                array.put(obj);
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_SAMPLES, array.toString()).apply();
    }

    private synchronized void load() {
        samples.clear();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SAMPLES, null);
        if (json == null) return;

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                long t = obj.optLong("t", 0);
                float temp = obj.isNull("temp") ? Float.NaN : (float) obj.optDouble("temp", Float.NaN);
                float hum = obj.isNull("hum") ? Float.NaN : (float) obj.optDouble("hum", Float.NaN);
                boolean gas = obj.optBoolean("gas", false);
                samples.add(new Sample(t, temp, hum, gas));
            }
        } catch (JSONException ignored) {
        }
    }

    public synchronized Series query(long durationMs, int maxPoints) {
        long now = System.currentTimeMillis();
        long cutoff = now - durationMs;

        List<Sample> filtered = new ArrayList<>();
        for (Sample s : samples) {
            if (s.t >= cutoff) {
                filtered.add(s);
            }
        }

        if (filtered.isEmpty()) {
            return new Series(0);
        }

        // Statistics
        float tempMin = Float.MAX_VALUE, tempMax = -Float.MAX_VALUE;
        double tempSum = 0;
        int tempCount = 0;

        float humMin = Float.MAX_VALUE, humMax = -Float.MAX_VALUE;
        double humSum = 0;
        int humCount = 0;

        int gasEvents = 0;
        boolean lastGasState = false;

        for (Sample s : filtered) {
            if (!Float.isNaN(s.temp)) {
                if (s.temp < tempMin) tempMin = s.temp;
                if (s.temp > tempMax) tempMax = s.temp;
                tempSum += s.temp;
                tempCount++;
            }
            if (!Float.isNaN(s.hum)) {
                if (s.hum < humMin) humMin = s.hum;
                if (s.hum > humMax) humMax = s.hum;
                humSum += s.hum;
                humCount++;
            }
            if (s.gas && !lastGasState) {
                gasEvents++;
            }
            lastGasState = s.gas;
        }

        int sampleCount = filtered.size();
        int bucketCount = Math.min(maxPoints, sampleCount);
        Series series = new Series(bucketCount);

        series.tempMin = tempCount > 0 ? tempMin : Float.NaN;
        series.tempMax = tempCount > 0 ? tempMax : Float.NaN;
        series.tempAvg = tempCount > 0 ? (float) (tempSum / tempCount) : Float.NaN;

        series.humMin = humCount > 0 ? humMin : Float.NaN;
        series.humMax = humCount > 0 ? humMax : Float.NaN;
        series.humAvg = humCount > 0 ? (float) (humSum / humCount) : Float.NaN;

        series.gasEvents = gasEvents;

        float samplesPerBucket = (float) sampleCount / bucketCount;

        for (int i = 0; i < bucketCount; i++) {
            int startIdx = Math.round(i * samplesPerBucket);
            int endIdx = Math.min(sampleCount, Math.round((i + 1) * samplesPerBucket));
            if (startIdx >= endIdx) startIdx = Math.max(0, endIdx - 1);

            long tSum = 0;
            double tValSum = 0;
            int tValCnt = 0;
            double hValSum = 0;
            int hValCnt = 0;
            boolean anyGas = false;
            int countInBucket = 0;

            for (int j = startIdx; j < endIdx; j++) {
                Sample s = filtered.get(j);
                tSum += s.t;
                if (!Float.isNaN(s.temp)) {
                    tValSum += s.temp;
                    tValCnt++;
                }
                if (!Float.isNaN(s.hum)) {
                    hValSum += s.hum;
                    hValCnt++;
                }
                if (s.gas) anyGas = true;
                countInBucket++;
            }

            series.t[i] = countInBucket > 0 ? tSum / countInBucket : 0;
            series.temp[i] = tValCnt > 0 ? (float) (tValSum / tValCnt) : Float.NaN;
            series.hum[i] = hValCnt > 0 ? (float) (hValSum / hValCnt) : Float.NaN;
            series.gas[i] = anyGas;
        }

        series.size = bucketCount;
        return series;
    }
}
