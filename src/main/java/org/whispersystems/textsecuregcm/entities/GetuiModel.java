package org.whispersystems.textsecuregcm.entities;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetuiModel {

    @JsonProperty
    @NotEmpty
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String getuiclientid) {
        this.clientId = getuiclientid;
    }


}
