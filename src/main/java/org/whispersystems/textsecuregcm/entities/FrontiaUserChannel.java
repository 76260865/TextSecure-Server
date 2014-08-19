package org.whispersystems.textsecuregcm.entities;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FrontiaUserChannel {

    @JsonProperty
    @NotEmpty
    private String userId;

    @JsonProperty
    @NotEmpty
    private String channelId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

}
