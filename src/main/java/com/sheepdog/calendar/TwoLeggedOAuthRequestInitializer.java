package com.sheepdog.calendar;

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.CommonGoogleClientRequestInitializer;

import java.io.IOException;
import java.util.Collections;

class TwoLeggedOAuthRequestInitializer extends CommonGoogleClientRequestInitializer {
    private final String emailAddress;

    public TwoLeggedOAuthRequestInitializer(String apiKey, String emailAddress) {
        super(apiKey);
        this.emailAddress = emailAddress;
    }

    @Override
    public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
        request.setUnknownKeys(Collections.singletonMap("xoauth_requestor_id", (Object) emailAddress));
    }
}
