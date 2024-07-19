package com.linkage.controller;

import com.linkage.LinkageConfiguration;

import io.sentry.Sentry;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/sentry")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class SentryController extends BaseController {

    public SentryController(LinkageConfiguration configuration, Validator validator) {
        super(configuration, validator);
    }

    public static void run() {
        // Initialize Sentry
        Sentry.init(options -> {
            options.setDsn(
                    "https://73766306b18141d592ae7de745d7ed0a@o4505482094116864.ingest.sentry.io/4505486810087424");
            options.setTracesSampleRate(1.0);
            options.setDebug(true); // Enable debug mode if needed
        });
        try {
            throw new Exception("This is a test exception");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    @Path("/test")
    public static void main(String[] args) {
        run();
    }
}
