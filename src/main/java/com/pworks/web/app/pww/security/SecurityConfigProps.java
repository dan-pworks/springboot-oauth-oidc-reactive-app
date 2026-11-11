package com.pworks.web.app.pww.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app.security")
public class SecurityConfigProps {
    Map<String, List<String>> endpoints;

    public Map<String, List<String>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, List<String>> endpoints) {
        this.endpoints = endpoints;
    }
}
