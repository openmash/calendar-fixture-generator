package com.sheepdog.calendar;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CalendarClient {
    private static final String OAUTH_APPLICATION_NAME = "OpenMash";

    private final HttpTransport transport = new NetHttpTransport();
    private final JacksonFactory jsonFactory = new JacksonFactory();

    private final String oauthApiKey;
    private final String oauthConsumerKey;
    private final String oauthConsumerSecret;

    public CalendarClient(File propertiesFile) throws IOException {
        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        properties.load(inputStream);
        inputStream.close();

        oauthApiKey = properties.getProperty("google.apiKey");
        oauthConsumerKey = properties.getProperty("google.oauth.consumerKey");
        oauthConsumerSecret = properties.getProperty("google.oauth.consumerSecret");
    }

    private String getOauthApplicationName() {
        return OAUTH_APPLICATION_NAME;
    }

    private String getOauthApiKey() {
        return oauthApiKey;
    }

    private String getOauthConsumerKey() {
        return oauthConsumerKey;
    }

    private String getOauthConsumerSecret() {
        return oauthConsumerSecret;
    }

    private OAuthParameters getClientOAuthParameters() {
        OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = getOauthConsumerSecret();

        OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.version = "1.0";
        oauthParameters.consumerKey = getOauthConsumerKey();
        oauthParameters.signer = signer;
        return oauthParameters;
    }

    public Calendar getCalendar(final String emailAddress) {
        GoogleClientRequestInitializer requestInitializer =
                new TwoLeggedOAuthRequestInitializer(getOauthApiKey(), emailAddress);

        return new Calendar.Builder(transport, jsonFactory, getClientOAuthParameters())
                .setApplicationName(getOauthApplicationName())
                .setGoogleClientRequestInitializer(requestInitializer)
                .build();
    }
}
