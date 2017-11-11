/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.tool;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Date;

import de.mediathekview.mlib.tool.Functions;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MserverDatumZeit
{

    private static final Logger LOG = LogManager.getLogger(MserverDatumZeit.class);

    private static final DateTimeFormatter DF_DATUM_ZEIT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(Locale.GERMANY);
    private static final DateTimeFormatter DF_DATUM = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMANY);
    private static final DateTimeFormatter DF_DATUM_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private static final DateTimeFormatter DF_OUT_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String getJetzt()
    {
        return DF_DATUM_ZEIT.format(LocalDateTime.now());
    }

    public static String getHeute()
    {
        return DF_DATUM.format(LocalDateTime.now());
    }

    public static long getSecondsUntilNextDay()
    {
        // now
        LocalDateTime now = LocalDateTime.now();
        // tomorrow 0:00
        LocalDateTime future = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1L);
        Duration duration = Duration.between(now, future);
        return duration.getSeconds();
    }

    public static String getHeute_yyyy_MM_dd()
    {
        return DF_DATUM_YYYY_MM_DD.format(LocalDateTime.now());
    }

    public static String getNameAkt(String path)
    {
        // liefert den Namen der Filmliste "akt" von heute
        if (path.isEmpty())
        {
            return "";
        }
        return Functions.addsPfad(path, "Filmliste-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".xz");
    }

    /**
     * formats a date/datetime string to the date format used in DatenFilm
     *
     * @param dateValue          the date/datetime value
     * @param aDateTimeFormatter the format of dateValue
     * @return the formatted date string
     */
    public static String formatDate(String dateValue, DateTimeFormatter aDateTimeFormatter)
    {
        try
        {
            return DF_DATUM.format(aDateTimeFormatter.parse(dateValue));
        } catch (DateTimeParseException ex)
        {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s: %s", dateValue, ex.getMessage()));
        }

        return "";
    }

    /**
     * Parse a date/datetime string.
     *
     * @param dateValue the date/datetime value
     * @return the date
     */
    public static LocalDate parseDate(String dateValue)
    {
        try
        {
            return LocalDate.parse(dateValue, DF_DATUM);
        } catch (DateTimeParseException ex)
        {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s: %s", dateValue, ex.getMessage()));
        }

        return null;
    }

    /**
     * Parse a date/datetime string.
     *
     * @param aDate the date value
     * @param aTime the time value
     * @return the dateTime
     */
    public static LocalDateTime parseDateTime(String aDate, String aTime)
    {
        try
        {
            LocalTime time;
            if (StringUtils.isBlank(aDate))
            {
                return null;
            }

            if (StringUtils.isBlank(aTime))
            {
                time = LocalTime.MIDNIGHT;
            }else
            {
                time = tryToParseTime(aTime);
            }


            return LocalDateTime.of(LocalDate.parse(aDate, DF_DATUM), time);
        } catch (DateTimeParseException ex)
        {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s und der Zeit %s: %s", aDate, aTime, ex.getMessage()));
        }

        return null;
    }

    private static LocalTime tryToParseTime(final String aTime)
    {
        try
        {
            return parseTime(FormatStyle.LONG, aTime);
        } catch (DateTimeParseException dateTimeParseException2)
        {
            LOG.debug(String.format("Can't parse time \"%s\" for german format LONG tying MEDIUM now...",aTime), dateTimeParseException2);
        }
        try
        {
            return parseTime(FormatStyle.MEDIUM, aTime);
        } catch (DateTimeParseException dateTimeParseException3)
        {
            LOG.debug(String.format("Can't parse time \"%s\" for german format MEDIUM tying SHORT now...",aTime), dateTimeParseException3);
            return parseTime(FormatStyle.SHORT, aTime);
        }
    }

    private static LocalTime parseTime(final FormatStyle aFormatStyle, String aTime)
    {
        return LocalTime.parse(aTime, DateTimeFormatter.ofLocalizedTime(aFormatStyle).withLocale(Locale.GERMANY));
    }

    /**
     * formats a datetime string to the time format used in DatenFilm
     *
     * @param dateValue          the datetime value
     * @param aDateTimeFormatter the format of dateValue
     * @return the formatted time string
     */
    public static String formatTime(String dateValue, DateTimeFormatter aDateTimeFormatter)
    {
        try
        {
            return DF_OUT_TIME.format(aDateTimeFormatter.parse(dateValue));
        } catch (DateTimeParseException ex)
        {
            LOG.debug(String.format("Fehler beim Parsen des Datums %s: %s", dateValue, ex.getMessage()));
        }

        return "";
    }

}
