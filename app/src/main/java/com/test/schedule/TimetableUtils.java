package com.test.schedule;

import android.util.Log;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.Locale;

class TimetableUtils {

    static LocalDate getStartDate() {
        int weekday = LocalDate.now().getDayOfWeek();
        if (weekday == DateTimeConstants.SATURDAY || weekday == DateTimeConstants.SUNDAY) {
            return LocalDate.now().plusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY);
        } else {
            return LocalDate.now();
        }
    }

    static LocalDate getWeekStart() {
        return LocalDate.now().plusDays(2).withDayOfWeek(DateTimeConstants.MONDAY);
    }

    static int getDayCount() {
        int dayOfWeek = LocalDate.now().getDayOfWeek();
        int res = 10 - (Days.daysBetween(getWeekStart(), LocalDate.now()).getDays());
        return dayOfWeek == DateTimeConstants.SATURDAY || dayOfWeek == DateTimeConstants.SUNDAY ? res - 2 : res;
    }

    static String getTabTitle(int index) {
        return getTitle(getTabDate(index));
    }

    static LocalDate getTabDate(int index) {
        LocalDate startDate = getStartDate();
        int weekDay = startDate.getDayOfWeek();
        if (weekDay + index > DateTimeConstants.FRIDAY) {
            index += 2;
        }
        return startDate.plusDays(index);
    }

    static String getTitle(LocalDate date) {

        final String TAG = "schedule:log";

        LocalDate now = LocalDate.now();
        int diff = Days.daysBetween(now, date).getDays();

        Log.d(TAG, "getTitle: diff= " + diff);
        if (diff == -1) {
            return "Wczoraj";
        } else if (diff == 0) {
            return "Dzisiaj";
        } else if (diff == 1) {
            return "Jutro";
        } else if (date.withDayOfWeek(1).getDayOfYear() == getStartDate().withDayOfWeek(1).getDayOfYear()) {
            return date.dayOfWeek().getAsText(new Locale("pl"));
        } else {
            return date.toString("d MMM.", new Locale("pl"));
        }
    }
}
