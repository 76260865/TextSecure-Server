package org.whispersystems.textsecuregcm.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SwiftConfiguration {
    @JsonProperty
    private String host;
    @JsonProperty
    private String secret;
    @JsonProperty
    private String imagepath;
    public String getHost() {
        return host;
    }
    public String getSecret() {
        return secret;
    }
    public String getImagepath() {
        return imagepath;
    }
}
