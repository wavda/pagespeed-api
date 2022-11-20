package utils;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Slack {
    static String webhook_url = System.getProperty("slack_webhook_url");

    public static void slackSend(String page_url, String strategy, String score, String build) {
        RestAssured.baseURI = webhook_url;
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 30000));
        RequestSpecification request = RestAssured.given().config(config);

        JSONObject requestParams = new JSONObject();
        requestParams.put("page_url", page_url);
        requestParams.put("strategy", strategy);
        requestParams.put("score", score);
        requestParams.put("build", build);

        request.body(requestParams.toString());
        Response response = request.post().then().extract().response();
        JSONObject jo = new JSONObject(response.getBody().asString());
        assertEquals(200, response.statusCode());
    }
}
