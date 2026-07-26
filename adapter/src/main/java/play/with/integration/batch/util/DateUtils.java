package play.with.integration.batch.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils {

    public static String diff(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime tempDateTime = LocalDateTime.from(startTime);

        long days = tempDateTime.until(endTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = tempDateTime.until(endTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(endTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(endTime, ChronoUnit.SECONDS);

        seconds += (hours == 0 && minutes == 0 && seconds == 0) ? 0 : 1;

        return String.format("%s days %s hours %s minutes %s seconds", days, hours, minutes, seconds);
    }
}
