package com.falconxrobotics.website.application.googlesheets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.springframework.stereotype.Repository;

import io.github.cdimascio.dotenv.DotEnvException;
import io.github.cdimascio.dotenv.Dotenv;

@Repository
public class GoogleSheetRepository {

    private final String APPLICATION_NAME = "website";
    private final String SHEET_ID = "1FbbEs4_8v8LqqpAmLqAcVGUFeK4rRlentKwJbzEIgP8";
    private final String SHEET_TOKEN_PATH = "./src/main/resources/secrets/token";
    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Dotenv dotenv;
    private String SHEET_CREDENTIALS;
    private Sheets service;

    public GoogleSheetRepository() throws GeneralSecurityException, IOException {
        dotenv = Dotenv.load();
        try {
            SHEET_CREDENTIALS = fallback(dotenv.get("SHEET_CREDENTIALS"), System.getenv("SHEET_CREDENTIALS"));
        } catch (Exception e) {
            if (e.getClass() == DotEnvException.class) {
                SHEET_CREDENTIALS = System.getenv("SHEET_CREDENTIALS");
            } else {
                throw e;
            }
        }

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
    }

    private String getFromEnvFile(String key) {
        try {
            Dotenv dotenv = Dotenv.load();
            return fallback(dotenv.get(key), System.getenv(key));
        } catch (Exception e) {
            if (e.getClass() == DotEnvException.class) {
                return System.getenv(key);
            } else {
                throw e;
            }
        }
    }

    private String fallback(String arg1, String arg2) {
        return arg1 == null ? arg2 : arg1;
    }

    private File createStoredCredentialFile(String toWrite) {
        File file = new File(SHEET_TOKEN_PATH);
        try (FileWriter fileWriter = new FileWriter(file)) {
            file.createNewFile();
            fileWriter.write(toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new ByteArrayInputStream(SHEET_CREDENTIALS.getBytes())));

        File storedCredentialFile = createStoredCredentialFile("STORED_CREDENTIALS");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(storedCredentialFile))
                        .setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public ValueRange get(String range, String valueRenderOption) {
        try {
            return service.spreadsheets().values().get(SHEET_ID, range).setValueRenderOption(valueRenderOption)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BatchGetValuesResponse batchGet(List<String> ranges, String valueRenderOption) {
        try {
            return service.spreadsheets().values().batchGet(SHEET_ID).setRanges(ranges)
                    .setValueRenderOption(valueRenderOption).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UpdateValuesResponse update(String range, List<List<Object>> values, String valueInputOption) {
        try {
            ValueRange body = new ValueRange().setValues(values);
            return service.spreadsheets().values().update(SHEET_ID, range, body).setValueInputOption(valueInputOption)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public BatchUpdateValuesResponse batchUpdate(String range, List<List<Object>> values, String valueInputOption) {
        try {
            List<ValueRange> data = new ArrayList<ValueRange>();
            data.add(new ValueRange().setRange(range).setValues(values));
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest().setValueInputOption(valueInputOption)
                    .setData(data);
            return service.spreadsheets().values().batchUpdate(SHEET_ID, body).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AppendValuesResponse append(String range, List<List<Object>> values, String valueInputOption) {
        try {
            ValueRange body = new ValueRange().setValues(values);
            return service.spreadsheets().values().append(SHEET_ID, range, body).setValueInputOption(valueInputOption)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}