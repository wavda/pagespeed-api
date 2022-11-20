package test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import utils.GoogleSheet;
import utils.PageSpeed;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static utils.Slack.slackSend;

@Execution(ExecutionMode.CONCURRENT)
class TestPageSpeed {
    static String build_number = System.getProperty("build_number", "N/A");
    static String nowAsiaJakarta;
    String branch_name = System.getProperty("env", "uat");
    String range;
    String test_url;
    String score;
    String fcp;
    String lcp;
    String cls;
    String interactive;
    String blocking_time;
    String speed_index;
    String environment;

    @BeforeAll
    static void beforeAll() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        nowAsiaJakarta = df.format(date);
    }

    @BeforeEach
    public void startTest() {
        if (branch_name.contains("master")) {
            range = "test_urls!A2:A";
            environment = "Production";
        } else {
            range = "test_urls!B:B";
            environment = "UAT";
        }
    }

    @Test
    @DisplayName("Check Mobile Performance Score")
    void testPageSpeedMobile() throws GeneralSecurityException, IOException {
        checkPerformance("mobile");
    }

    @Test
    @DisplayName("Check Desktop Performance Score")
    void testPageSpeedDesktop() throws GeneralSecurityException, IOException {
        checkPerformance("desktop");
    }

    public void checkPerformance(String strategy) throws GeneralSecurityException, IOException {
        // Get values from Google Sheet
        List <List <Object>> values = new GoogleSheet().getData(range).getValues();

        // Check if row has value
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                test_url = row.get(0).toString();
                System.out.printf("%s \n", test_url);

                try {
                    // Measure Core Web Vital
                    String[] result = PageSpeed.getPageSpeedInfo(strategy, test_url);
                    score = result[0];
                    fcp = result[1];
                    lcp = result[2];
                    cls = result[3];
                    interactive = result[4];
                    blocking_time = result[5];
                    speed_index = result[6];

                    // Add result as new row in Google Sheet
                    GoogleSheet.appendData(nowAsiaJakarta, build_number, test_url, strategy, score, fcp,
                            lcp, cls, interactive, blocking_time, speed_index, environment);

                    // Send notification to Slack if performance score is low
                    if (strategy.contains("mobile") && Integer.parseInt(score) < 50) {
                        slackSend(test_url, strategy, score, build_number);
                    } else if (strategy.contains("desktop") && Integer.parseInt(score) < 80) {
                        slackSend(test_url, strategy, score, build_number);
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }
}
