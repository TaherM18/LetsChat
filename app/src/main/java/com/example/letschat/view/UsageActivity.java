package com.example.letschat.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.letschat.R;
import com.example.letschat.databinding.ActivityUsageBinding;
import com.example.letschat.model.AppUsageModel;
import com.example.letschat.model.BarChartModel;
import com.example.letschat.utils.AndroidUtil;
import com.example.letschat.utils.FirebaseUtil;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Usage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UsageActivity extends BaseActivity {

    private ActivityUsageBinding binding;
    private List<BarEntry> entries = new LinkedList<>();
    private List<AppUsageModel> appUsageModelList = new LinkedList<>();
    private List<BarChartModel> barChartModelList;
    private List<String> xValues;
    private FirebaseAuth firebaseAuth;
    private LocalDate currentWeekStartDate, earliestStartDate, latestStartDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage);

        // INITIALIZATION ==========================================================================

        binding = DataBindingUtil.setContentView(this, R.layout.activity_usage);
        setSupportActionBar(binding.materialToolbar);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentWeekStartDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        xValues = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        // binding.barChart.highlightValue();

        // SETUP ===================================================================================

        setAppUsageModelList();

        // EVENT LISTENERS =============================================================================

        binding.imgBtnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreviousWeekReport();
            }
        });

        binding.imgBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextWeekReport();
            }
        });
    }


    // FUNCTIONS ===================================================================================

    private void setAppUsageModelList() {
        AndroidUtil.setProgressBar(binding.progressBar, true);

        firestore.collection("app_usage")
                .whereEqualTo("userId", firebaseAuth.getUid())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for ( QueryDocumentSnapshot snapshot : task.getResult() ) {
                                AppUsageModel model = snapshot.toObject(AppUsageModel.class);
                                appUsageModelList.add(model);
                            }
                            setEarliestStartDate();
                            setLatestStartDate();

                            barChartModelList = getWeeklyUsageReport();

//                            String str = "";
//                            for (BarChartModel model : barChartModelList) {
//                                str += model.getUsageHours() + ", ";
//                            }
//                            Toast.makeText(UsageActivity.this, str, Toast.LENGTH_SHORT).show();

                            setupBarChart();

                            AndroidUtil.setProgressBar(binding.progressBar, false);
                        }
                        else {
                            Toast.makeText(UsageActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("UsageActivity", task.getException().getMessage());
                        }
                    }
                });
    }


    private void setEarliestStartDate() {
        // Convert milliseconds to Instant
        Instant instant = Instant.ofEpochMilli( appUsageModelList.get(0).getStartTime() );
        // Convert Instant to LocalDate
        earliestStartDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setLatestStartDate() {
        // Convert milliseconds to Instant
        Instant instant = Instant.ofEpochMilli( appUsageModelList.get(appUsageModelList.size()-1).getStartTime() );
        // Convert Instant to LocalDate
        latestStartDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public float calculateUsageDuration(LocalDate date) {
        // Convert LocalDate to LocalDateTime for the start of the day
        LocalDateTime startOfDay = date.atStartOfDay();

        // Calculate the start and end of the day in milliseconds
        long startOfDayMillis = startOfDay.toInstant(ZoneOffset.UTC).toEpochMilli();
        long endOfDayMillis = startOfDay.plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli();

        long earliestStartTime = Long.MAX_VALUE;
        long latestStartTime = Long.MIN_VALUE;

        // Find the earliest and latest start times within the given date
        for (AppUsageModel data : appUsageModelList) {
            long startTime = data.getStartTime();
            if (startTime >= startOfDayMillis && startTime < endOfDayMillis) {
                earliestStartTime = Math.min(earliestStartTime, startTime);
                latestStartTime = Math.max(latestStartTime, startTime);
            }
        }

        // Calculate the duration in milliseconds
        long duration = latestStartTime - earliestStartTime;

        // Convert milliseconds to hours
        return (float) (duration / (1000.0 * 60 * 60));
    }

    public List<BarChartModel> getWeeklyUsageReport() {
        List<BarChartModel> weeklyUsage = new LinkedList<>();

        // Calculate the end date of the current week (Saturday)
        LocalDate currentWeekEndDate = currentWeekStartDate.plusDays(6);

        // Iterate through each date of the current week
        LocalDate dateIterator = currentWeekStartDate;
        while (!dateIterator.isAfter(currentWeekEndDate)) {
            // Calculate the usage duration for the current date
            float usageHours = calculateUsageDuration(dateIterator);
            // TODO: check the result of usage hours
            usageHours = ((float)Math.round(usageHours * 100))/100;

            // Add the usage duration to the list
            weeklyUsage.add(new BarChartModel(dateIterator.getDayOfMonth(), usageHours));

            // Move to the next date
            dateIterator = dateIterator.plusDays(1);
        }

        return weeklyUsage;
    }

    public void setPreviousWeekReport() {
        if (canShowPreviousWeek()) {
            // Move to the previous week
            currentWeekStartDate = currentWeekStartDate.minusWeeks(1);

            // Generate the usage report for the new week
            barChartModelList = getWeeklyUsageReport();

            setupBarChart();
        }
        else {
            Toast.makeText(this, "No Previous Data to Show", Toast.LENGTH_SHORT).show();
        }
    }

    public void setNextWeekReport() {
        if (canShowNextWeek()) {
            // Move to the next week
            currentWeekStartDate = currentWeekStartDate.plusWeeks(1);

            // Generate the usage report for the new week
            barChartModelList = getWeeklyUsageReport();

            setupBarChart();
        }
        else {
            Toast.makeText(this, "No Further Data to Show", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean canShowPreviousWeek() {
        // Check if moving to the previous week would result in a date before one week from the earliest date
        return currentWeekStartDate.minusWeeks(1)
                .isAfter(earliestStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))
                || currentWeekStartDate.minusWeeks(1).equals(earliestStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
    }

    public boolean canShowNextWeek() {
        // Check if moving to the next week would result in a date after the current date
        return currentWeekStartDate.plusWeeks(1)
                .isBefore(latestStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).plusWeeks(1));
    }

    private void setupBarChart() {
        entries.clear();

        YAxis yAxis = binding.barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setTextSize(14f);
//        yAxis.setAxisMaximum(barChartModelList.get(barChartModelList.size()-1).getUsageHours()+1f);
//        yAxis.setAxisLineWidth(2f);
//        yAxis.setAxisLineColor(R.color.black);
//        yAxis.setLabelCount(10);

        String test = "";

        for (BarChartModel model : barChartModelList) {
            entries.add(new BarEntry(model.getDayOfMonth(), model.getUsageHours()));
            // test += entries.get(entries.size()-1).toString() + ",\n";
        }

        // For Debugging:
        // Toast.makeText(this, test, Toast.LENGTH_SHORT).show();

        BarDataSet barDataSet = new BarDataSet(entries, "Hours");

        int colorAccent = ContextCompat.getColor(UsageActivity.this, R.color.color_accent);
        barDataSet.setColor(colorAccent);
        barDataSet.setValueTextColor(R.color.black);
        barDataSet.setValueTextSize(12f);

        BarData barData = new BarData(barDataSet);

        binding.barChart.getAxisRight().setDrawLabels(false);
        binding.barChart.getXAxis().setTextSize(14f);
        binding.barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        binding.barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xValues));
//        binding.barChart.getXAxis().setGranularity(1f);
//        binding.barChart.getXAxis().setGranularityEnabled(true);

        binding.barChart.setFitBars(true);
        binding.barChart.setData(barData);
        binding.barChart.getDescription().setText("Daily App Usage in Hours");
        binding.barChart.getDescription().setTextSize(16f);
        // Set the position above the chart
        binding.barChart.getDescription().setPosition( binding.barChart.getWidth()/2f, 30f);
        binding.barChart.animateY(1500);
    }
}