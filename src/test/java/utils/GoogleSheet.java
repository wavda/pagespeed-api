package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class GoogleSheet {
    private static final String APPLICATION_NAME = "Google Sheet";
    private static final String SPREADSHEET_ID = "xxxxxxxxxxxx";
    private static Sheets sheetsService;

    public static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = GoogleSheet.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new InputStreamReader(in)
        );

        List <String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(
                        new FileDataStoreFactory(
                                new java.io.File("tokens"))).setAccessType("offline").build();

        return new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");
    }

    public static Sheets getSheetsService() throws GeneralSecurityException, IOException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                credential).setApplicationName(APPLICATION_NAME).build();
    }

    public static void appendData(String date_time, String build, String url, String strategy, String performance,
                                  String fcp, String lcp, String cls, String interactive, String blocking_time,
                                  String speed_index, String environment) throws GeneralSecurityException, IOException {
        sheetsService = getSheetsService();

        ValueRange appendBody = new ValueRange().setValues(
                Arrays.asList(Arrays.asList(date_time, build, url, strategy, performance, fcp, lcp, cls,
                        interactive, blocking_time, speed_index, environment)));

        sheetsService.spreadsheets().values()
                .append(SPREADSHEET_ID, "report", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
    }

    public ValueRange getData(String range) throws GeneralSecurityException, IOException {
        Sheets sheetsService = GoogleSheet.getSheetsService();
        return sheetsService.spreadsheets().values().get(SPREADSHEET_ID, range).execute();
    }
}
