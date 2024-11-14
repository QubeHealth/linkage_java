package com.linkage;

import com.linkage.controller.BefiscController;
import com.linkage.controller.DigitapController;
import com.linkage.controller.ElasticrunController;
import com.linkage.controller.ErupeeController;
import com.linkage.controller.FirebaseController;
import com.linkage.controller.GoogleMapsController;
import com.linkage.controller.MessageProviderController;
import com.linkage.controller.SubscriptionController;
import com.linkage.controller.VendorController;
import com.linkage.controller.WebengageController;
import com.linkage.controller.SmsController;
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
        MessageProviderController messageProviderController = new MessageProviderController(configuration, validator);
        DigitapController digitapController = new DigitapController(configuration, validator);
        SubscriptionController subscriptionController = new SubscriptionController(configuration, validator);
        SmsController smsController = new SmsController(configuration, validator);
        ErupeeController erupeeController = new ErupeeController(configuration, validator);
        GoogleMapsController googleMapsController=new GoogleMapsController(configuration, validator);
        VendorController vendorController=new VendorController(configuration, validator);
        WebengageController webengageController = new WebengageController(configuration, validator);
        ElasticrunController elasticrunController = new ElasticrunController(configuration, validator);

        environment.jersey().register(befiscController);
        environment.jersey().register(webhookController);
        environment.jersey().register(firebaseController);
        environment.jersey().register(messageProviderController);
        environment.jersey().register(digitapController);
        environment.jersey().register(subscriptionController);
        environment.jersey().register(erupeeController);
        environment.jersey().register(googleMapsController);
        environment.jersey().register(smsController);
        environment.jersey().register(vendorController);
        environment.jersey().register(webengageController);
        environment.jersey().register(elasticrunController);

    }
}
