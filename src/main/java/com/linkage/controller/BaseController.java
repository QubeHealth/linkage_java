package com.linkage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.LinkageConfiguration;

import jakarta.validation.Validator;

public abstract class BaseController {
    protected final LinkageConfiguration configuration;
    protected final Validator validator;
    protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected BaseController(LinkageConfiguration configuration, Validator validator) {
        this.configuration = configuration;
        this.validator = validator;

    }
}