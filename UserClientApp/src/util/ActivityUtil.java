package util;

import controller.MainMenuController;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityUtil {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());

    private static String PATH_TO_CSV;

    static {
        try {
            PATH_TO_CSV = ConfigUtil.getActivityCsvPath();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Config file - cannot read CSV file");
            PATH_TO_CSV = "csvActivityRecords";
        }
    }

    private static final String[] HEADERS = { "TOKEN", "LOGIN_TIME", "LOGOUT_TIME", "TOTAL_TIME" };

    public static LocalDateTime loginTime;
    public static LocalDateTime logoutTime;

    public static void addActivity()
    {
        try {
            List<String[]> strings = null;
            try {
                strings = readActivities();
            } catch (Exception exception){
                strings = new ArrayList<>();
            }

            FileWriter out = new FileWriter(PATH_TO_CSV);
            String loginStr = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(loginTime);
            String logoutStr = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(logoutTime);
            String intervalStr = intervalGenerator(loginTime, logoutTime);

            try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                    .withHeader(HEADERS))) {
                for (String[] elem : strings){
                    printer.printRecord(elem);
                }
                printer.printRecord(new String[]{CurrentUser.getToken(), loginStr, logoutStr, intervalStr});
            }
        }
        catch (IOException e) {
            LOGGER.log(Level.WARNING, "Cannot read activities.", e);
            e.printStackTrace();
        }
    }

    public static List<String[]> readActivities() throws IOException {
            Reader in = new FileReader(PATH_TO_CSV);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withHeader(HEADERS)
                    .withFirstRecordAsHeader()
                    .parse(in);
            List<String[]> result = new ArrayList<>();
            for (CSVRecord record : records) {
                String TOKEN = record.get("TOKEN");
                String LOGIN_TIME = record.get("LOGIN_TIME");
                String LOGOUT_TIME = record.get("LOGOUT_TIME");
                String TOTAL_TIME = record.get("TOTAL_TIME");
                String[] array = {TOKEN, LOGIN_TIME, LOGOUT_TIME, TOTAL_TIME};
                result.add(array);
            }
            return result;
    }

    private static String intervalGenerator(LocalDateTime before, LocalDateTime after){
        LocalDateTime tempDateTime = LocalDateTime.from(before);

        long years = tempDateTime.until(after, ChronoUnit.YEARS );
        tempDateTime = tempDateTime.plusYears( years );

        long months = tempDateTime.until(after, ChronoUnit.MONTHS );
        tempDateTime = tempDateTime.plusMonths(months);

        long days = tempDateTime.until(after, ChronoUnit.DAYS );
        tempDateTime = tempDateTime.plusDays( days );

        long hours = tempDateTime.until(after, ChronoUnit.HOURS );
        tempDateTime = tempDateTime.plusHours( hours );

        long minutes = tempDateTime.until(after, ChronoUnit.MINUTES );
        tempDateTime = tempDateTime.plusMinutes( minutes );

        long seconds = tempDateTime.until(after, ChronoUnit.SECONDS );

        String intervalStr = "";
        intervalStr += years==0 ? "" : (years+"Y ");
        intervalStr += months==0 ? "" : (months+"M ");
        intervalStr += days==0 ? "" : (days+"D ");
        intervalStr += hours==0 ? "" : (hours+"H ");
        intervalStr += minutes==0 ? "" : (minutes+"M ");
        intervalStr += seconds==0 ? "" : (seconds+"S");

        return intervalStr;
    }
}
