package me.kall.reminder;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class RemindConfig {
    public static final ModConfigSpec INSTANCE;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DAILY_ACTIVITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WEEKLY_ACTIVITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MONTHLY_ACTIVITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> YEARLY_ACTIVITIES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("Reminder");
        Predicate<Object> alwaysTrue = Predicates.alwaysTrue();
        DAILY_ACTIVITIES = builder.comment("Daily activities in format HH:MM:SS->activityLangKey", "Example: 12:00:00->info.time.to.lunch").defineListAllowEmpty("DailyActivities", Lists.newArrayList(), () -> "HH:MM:SS->activityLangKey", alwaysTrue);
        WEEKLY_ACTIVITIES = builder.comment("Weekly activities in format WEEKDAY:HH:MM:SS->activityLangKey", "Example: MONDAY:08:00:00->info.time.to.getup.for.class", "Valid WEEKDAY: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY").defineListAllowEmpty("WeeklyActivities", Lists.newArrayList(), () -> "WEEKDAY:HH:MM:SS->activityLangKey", alwaysTrue);
        MONTHLY_ACTIVITIES = builder.comment("Monthly activities in format DAY:HH:MM:SS->activityLangKey", "Example: 30:12:00:00->info.last.day.of.the.month").defineListAllowEmpty("MonthlyActivities", Lists.newArrayList(), () -> "DAY:HH:MM:SS->activityLangKey", alwaysTrue);
        YEARLY_ACTIVITIES = builder.comment("Yearly activities in format MONTH:DAY:HH:MM:SS->activityLangKey", "Example: 12:25:08:00:00->info.christmas").defineListAllowEmpty("YearlyActivities", Lists.newArrayList(), () -> "MONTH:DAY:HH:MM:SS->activityLangKey", alwaysTrue);
        builder.pop();
        INSTANCE = builder.build();
    }

    public static final Map<Day, List<String>> DAILY = new Object2ObjectOpenHashMap<>();
    public static final Map<Week, List<String>> WEEKLY = new Object2ObjectOpenHashMap<>();
    public static final Map<Month, List<String>> MONTHLY = new Object2ObjectOpenHashMap<>();
    public static final Map<Year, List<String>> YEARLY = new Object2ObjectOpenHashMap<>();

    public static @Nullable List<List<String>> getActivities(int hour, int minute, int second, int dayOfWeek, int dayOfMonth, int monthValue) {
        Day day = new Day(hour, minute, second);
        Week week = new Week(dayOfWeek, hour, minute, second);
        Month month = new Month(dayOfMonth, hour, minute, second);
        Year year = new Year(monthValue, dayOfMonth, hour, minute, second);
        List<List<String>> activities = new ObjectArrayList<>(4);
        List<String> daily = DAILY.remove(day);
        List<String> weekly = WEEKLY.remove(week);
        List<String> monthly = MONTHLY.remove(month);
        List<String> yearly = YEARLY.remove(year);
        if (daily != null) activities.add(daily);
        if (weekly != null) activities.add(weekly);
        if (monthly != null) activities.add(monthly);
        if (yearly != null) activities.add(yearly);
        return activities.isEmpty() ? null : activities;
    }

    public static void validateConfig() {
        DAILY.clear();
        for (String entry : DAILY_ACTIVITIES.get()) {
            String[] parts = entry.split("->");
            String[] time = parts[0].split(":");
            DAILY.computeIfAbsent(new Day(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2])), key -> new ObjectArrayList<>()).add(parts[1]);
        }

        WEEKLY.clear();
        for (String entry : WEEKLY_ACTIVITIES.get()) {
            String[] parts = entry.split("->");
            String[] time = parts[0].split(":");
            WEEKLY.computeIfAbsent(new Week(DayOfWeek.valueOf(time[0]).getValue(), Integer.parseInt(time[1]), Integer.parseInt(time[2]), Integer.parseInt(time[3])), key -> new ObjectArrayList<>()).add(parts[1]);
        }

        MONTHLY.clear();
        for (String entry : MONTHLY_ACTIVITIES.get()) {
            String[] parts = entry.split("->");
            String[] time = parts[0].split(":");
            MONTHLY.computeIfAbsent(new Month(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]), Integer.parseInt(time[3])), key -> new ObjectArrayList<>()).add(parts[1]);
        }

        YEARLY.clear();
        for (String entry : YEARLY_ACTIVITIES.get()) {
            String[] parts = entry.split("->");
            String[] time = parts[0].split(":");
            YEARLY.computeIfAbsent(new Year(Integer.parseInt(time[0]), Integer.parseInt(time[1]), Integer.parseInt(time[2]), Integer.parseInt(time[3]), Integer.parseInt(time[4])), key -> new ObjectArrayList<>()).add(parts[1]);
        }
    }

    public static void reload(ModConfigEvent.@NotNull Reloading event) {
        if (event.getConfig().getModId().equals(Reminder.MOD_ID)) {
            validateConfig();
        }
    }

    public record Day(int hour, int minute, int second) {}
    public record Week(int dayOfWeek, int hour, int minute, int second) {}
    public record Month(int dayOfMonth, int hour, int minute, int second) {}
    public record Year(int month, int dayOfMonth, int hour, int minute, int second) {}
}
