package com.linkage;

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

    }

}
