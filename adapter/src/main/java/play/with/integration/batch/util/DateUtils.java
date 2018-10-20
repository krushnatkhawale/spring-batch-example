package play.with.integration.batch.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    public static String diff(Date startTime, Date endTime) {
        LocalDateTime fromDateTime = LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
        LocalDateTime toDateTime = LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault());

        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);


        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);


        long hours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);

        seconds += (hours == 0 && minutes == 0 && seconds == 0) ? 0 : 1;

        return String.format("%s days %s hours %s minutes %s seconds", days, hours, minutes, seconds);
    }
}
