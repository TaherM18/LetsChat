package com.example.letschat.model;

public class BarChartModel {
    private int dayOfMonth;
    private float usageHours;

    public BarChartModel(int dayOfMonth, float usageHours) {
        this.dayOfMonth = dayOfMonth;
        this.usageHours = usageHours;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public float getUsageHours() {
        return usageHours;
    }
}
