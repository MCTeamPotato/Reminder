package me.kall.reminder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraftforge.eventbus.api.Event;

public final class ActivityRegistryEvent extends Event {
    public void addDaily(int hour, int minute, int second, String activityKey) {
        RemindConfig.DAILY.computeIfAbsent(new RemindConfig.Day(hour, minute, second), key -> new ObjectArrayList<>()).add(activityKey);
    }

    public void addWeekly(int dayOfWeek, int hour, int minute, int second, String activityKey) {
        RemindConfig.WEEKLY.computeIfAbsent(new RemindConfig.Week(dayOfWeek, hour, minute, second), key -> new ObjectArrayList<>()).add(activityKey);
    }

    public void addMonthly(int dayOfMonth, int hour, int minute, int second, String activityKey) {
        RemindConfig.MONTHLY.computeIfAbsent(new RemindConfig.Month(dayOfMonth, hour, minute, second), key -> new ObjectArrayList<>()).add(activityKey);
    }

    public void addYearly(int month, int dayOfMonth, int hour, int minute, int second, String activityKey) {
        RemindConfig.YEARLY.computeIfAbsent(new RemindConfig.Year(month, dayOfMonth, hour, minute, second), key -> new ObjectArrayList<>()).add(activityKey);
    }
}
