package com.test.schedule;

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
        return Days.daysBetween(getStartDate(), getWeekStart().plusWeeks(2)).getDays() - 4;
    }

    static String getTabTitle(int index, boolean displayDates, boolean useRelativeTabNames) {
        return getTitle(getTabDate(index), displayDates, useRelativeTabNames);
    }

    static LocalDate getTabDate(int index) {
        LocalDate startDate = getStartDate();
        int weekDay = startDate.getDayOfWeek();
        if (weekDay + index > DateTimeConstants.FRIDAY) {
            index += 2;
        }
        return startDate.plusDays(index);
    }

    private static String getTitle(LocalDate date, boolean displayDates, boolean useRelativeTabNames) {

        int diff = Days.daysBetween(LocalDate.now(), date).getDays();
        if (useRelativeTabNames) {
            if (diff == -1) {
                return "Wczoraj";
            } else if (diff == 0) {
                return "Dzisiaj";
            } else if (diff == 1) {
                return "Jutro";
            }
        }

        boolean sameWeek = date.withDayOfWeek(1).getDayOfYear() == getStartDate().withDayOfWeek(1).getDayOfYear();

        if (sameWeek || !displayDates) {
            return date.dayOfWeek().getAsText(new Locale("pl"));
        } else {
            return date.toString("d MMM.", new Locale("pl"));
        }

    }
}

