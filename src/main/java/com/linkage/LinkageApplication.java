package com.linkage;

import com.linkage.controller.BefiscController;
import com.linkage.controller.DigitapController;
import com.linkage.controller.FirebaseController;
import com.linkage.controller.HereController;
import com.linkage.controller.WatiController;
import com.linkage.controller.WebhookController;
import com.linkage.utility.AuthFilter;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class LinkageApplication extends Application<LinkageConfiguration> {

    public static void main(final String[] args) throws Exception {
        new LinkageApplication().run(args);
    }

    @Override
    public String getName() {
        return "Linkage";
    }

    @Override
    public void run(LinkageConfiguration configuration, Environment environment) throws Exception {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        Validator validator = validatorFactory.getValidator();

        AuthFilter authFilter = new AuthFilter(configuration.getxApiKey(), configuration.getAuthorizationKey());

        environment.servlets().addFilter("auth-filter", authFilter)
                .addMappingForUrlPatterns(null, true, "/api/*");

        BefiscController befiscController = new BefiscController(configuration, validator);
        WebhookController webhookController = new WebhookController(configuration, validator);
        FirebaseController firebaseController = new FirebaseController(configuration, validator);
        WatiController watiController = new WatiController(configuration, validator);
        DigitapController digitapController = new DigitapController(configuration, validator);
        HereController hereController= new HereController(configuration, validator);

        environment.jersey().register(befiscController);
        environment.jersey().register(webhookController);
        environment.jersey().register(firebaseController);
        environment.jersey().register(watiController);
        environment.jersey().register(digitapController);
        environment.jersey().register(hereController);

    }
}
