package com.linkage.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linkage.LinkageConfiguration;

public abstract class BaseService {

    protected final LinkageConfiguration configuration;

    protected static final Logger logger = LoggerFactory.getLogger(BaseService.class);

    protected BaseService(LinkageConfiguration configuration) {
        this.configuration = configuration;
    }

}
