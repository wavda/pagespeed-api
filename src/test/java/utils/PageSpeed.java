package utils;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PageSpeed {
    static String pagespeed_api_key = System.getProperty("pagespeed_api_key");

    public static String[] getPageSpeedInfo(String strategy, String test_url) {
        String main_url = "https://www.googleapis.com/pagespeedonline/v5/runPagespeed?url=";
        String pagespeed_url = main_url + test_url + "&key=" + pagespeed_api_key + "&strategy=" + strategy;

        RestAssured.baseURI = main_url;
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 30000));
        RequestSpecification request = RestAssured.given().config(config);
        Response response = request.get(pagespeed_url).then().extract().response();

        JSONObject jo = new JSONObject(response.getBody().asString());
        assertEquals(200, response.statusCode());

        JSONObject lighthouseResult = (JSONObject) jo.get("lighthouseResult");
        JSONObject categories = (JSONObject) lighthouseResult.get("categories");
        JSONObject performance = (JSONObject) categories.get("performance");
        float score = Float.parseFloat(performance.get("score").toString()) * 100;

        JSONObject audits = (JSONObject) lighthouseResult.get("audits");
        JSONObject fcp = (JSONObject) audits.get("first-contentful-paint");
        JSONObject lcp = (JSONObject) audits.get("largest-contentful-paint");
        JSONObject cls = (JSONObject) audits.get("cumulative-layout-shift");
        JSONObject interactive = (JSONObject) audits.get("interactive");
        JSONObject blocking_time = (JSONObject) audits.get("total-blocking-time");
        JSONObject speed_index = (JSONObject) audits.get("speed-index");

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String final_fcp = fcp.get("numericValue").toString();
        String final_lcp = lcp.get("numericValue").toString();
        String final_cls = df.format(Float.parseFloat(cls.get("numericValue").toString()));
        String final_interactive = df.format(Float.parseFloat(interactive.get("numericValue").toString()));
        String final_blocking_time = blocking_time.get("numericValue").toString();
        String final_speed_index = df.format(Float.parseFloat(speed_index.get("numericValue").toString()));

        System.out.printf("\nPERFORMANCE SCORE: " + score);
        System.out.printf("\nFCP: " + final_fcp);
        System.out.printf("\nLCP: " + final_lcp);
        System.out.printf("\nCLS: " + final_cls);
        System.out.printf("\nInteractive: " + final_interactive);
        System.out.printf("\nBlocking Time: " + final_blocking_time);
        System.out.printf("\nSpeed Index: " + final_speed_index);

        return new String[]{String.valueOf(score), final_fcp, final_lcp, final_cls, final_interactive, final_blocking_time, final_speed_index};
    }
}
