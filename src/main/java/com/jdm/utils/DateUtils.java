package com.jdm.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date operations
 */
public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter DISPLAY_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
    
    /**
     * Parse a date string in the format dd-MM-yyyy
     * 
     * @param dateStr Date string
     * @return LocalDate object
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try alternate format
            return LocalDate.parse(dateStr);
        }
    }
    
    /**
     * Parse a date-time string in the format dd-MM-yyyy HH:mm
     * 
     * @param dateTimeStr Date-time string
     * @return LocalDateTime object
     * @throws DateTimeParseException if parsing fails
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try alternate format
            return LocalDateTime.parse(dateTimeStr);
        }
    }
    
    /**
     * Format a date for display (MMM d, yyyy)
     * 
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDateForDisplay(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMATTER);
    }
    
    /**
     * Format a date-time for display (MMM d, yyyy HH:mm)
     * 
     * @param dateTime Date-time to format
     * @return Formatted date-time string
     */
    public static String formatDateTimeForDisplay(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_DATE_TIME_FORMATTER);
    }
    
    /**
     * Get a list of dates between start and end dates (inclusive)
     * 
     * @param startDate Start date
     * @param endDate End date
     * @return List of dates
     */
    public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);
        
        for (int i = 0; i <= numOfDays; i++) {
            dates.add(startDate.plusDays(i));
        }
        
        return dates;
    }
    
    /**
     * Get a list of dates for the last N days from today
     * 
     * @param days Number of days
     * @return List of dates
     */
    public static List<LocalDate> getLastNDays(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return getDatesBetween(startDate, endDate);
    }
    
    /**
     * Get a list of dates for the last N months from today (first day of each month)
     * 
     * @param months Number of months
     * @return List of dates (first day of each month)
     */
    public static List<LocalDate> getLastNMonths(int months) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < months; i++) {
            LocalDate date = now.minusMonths(i).withDayOfMonth(1);
            dates.add(date);
        }
        
        // Reverse to get chronological order
        List<LocalDate> chronological = new ArrayList<>(dates);
        java.util.Collections.reverse(chronological);
        return chronological;
    }
    
    /**
     * Clean up date string from CMAS.csv which may contain special characters
     * 
     * @param dateStr Raw date string
     * @return Cleaned date string
     */
    public static String cleanupDateString(String dateStr) {
        // Replace special encoding with normal characters
        return dateStr.replace("+AC0-", "-");
    }
    
    /**
     * Get start of month for a given date
     * 
     * @param date Date
     * @return First day of the month
     */
    public static LocalDate getStartOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }
    
    /**
     * Get end of month for a given date
     * 
     * @param date Date
     * @return Last day of the month
     */
    public static LocalDate getEndOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    
    /**
     * Check if a date is between two other dates (inclusive)
     * 
     * @param date Date to check
     * @param startDate Start date
     * @param endDate End date
     * @return true if the date is between startDate and endDate (inclusive), false otherwise
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Check if a date-time is between two other date-times (inclusive)
     * 
     * @param dateTime Date-time to check
     * @param startDateTime Start date-time
     * @param endDateTime End date-time
     * @return true if the date-time is between startDateTime and endDateTime (inclusive), false otherwise
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }
}
