/**
 * Copyright (C) 2013 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecuregcm;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.whispersystems.textsecuregcm.configuration.ApnConfiguration;
import org.whispersystems.textsecuregcm.configuration.FederationConfiguration;
import org.whispersystems.textsecuregcm.configuration.GcmConfiguration;
import org.whispersystems.textsecuregcm.configuration.GetuiConfiguration;
import org.whispersystems.textsecuregcm.configuration.GraphiteConfiguration;
import org.whispersystems.textsecuregcm.configuration.MemcacheConfiguration;
import org.whispersystems.textsecuregcm.configuration.MetricsConfiguration;
import org.whispersystems.textsecuregcm.configuration.NexmoConfiguration;
import org.whispersystems.textsecuregcm.configuration.RateLimitsConfiguration;
import org.whispersystems.textsecuregcm.configuration.RedisConfiguration;
import org.whispersystems.textsecuregcm.configuration.S3Configuration;
import org.whispersystems.textsecuregcm.configuration.SwiftConfiguration;
import org.whispersystems.textsecuregcm.configuration.TwilioConfiguration;
import org.whispersystems.textsecuregcm.configuration.WebsocketConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

public class WhisperServerConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty
  private TwilioConfiguration twilio;

  @JsonProperty
  private NexmoConfiguration nexmo;

  @NotNull
  @JsonProperty
  private GcmConfiguration gcm;

  @NotNull
  @Valid
  @JsonProperty
  private S3Configuration s3;
  
  @NotNull
  @Valid
  @JsonProperty
  private SwiftConfiguration swift;

  @Valid
  @JsonProperty
  private GetuiConfiguration getui;
  
  @NotNull
  @Valid
  @JsonProperty
  private MemcacheConfiguration memcache;

  @NotNull
  @Valid
  @JsonProperty
  private RedisConfiguration redis;

  @JsonProperty
  private ApnConfiguration apn = new ApnConfiguration();

  @Valid
  @JsonProperty
  private FederationConfiguration federation = new FederationConfiguration();

  @Valid
  @NotNull
  @JsonProperty
  private DataSourceFactory database = new DataSourceFactory();

  @Valid
  @NotNull
  @JsonProperty
  private RateLimitsConfiguration limits = new RateLimitsConfiguration();

  @Valid
  @JsonProperty
  private GraphiteConfiguration graphite = new GraphiteConfiguration();

  @Valid
  @JsonProperty
  private MetricsConfiguration viz = new MetricsConfiguration();

  @Valid
  @JsonProperty
  private WebsocketConfiguration websocket = new WebsocketConfiguration();

  public WebsocketConfiguration getWebsocketConfiguration() {
    return websocket;
  }

  public TwilioConfiguration getTwilioConfiguration() {
    return twilio;
  }

  public NexmoConfiguration getNexmoConfiguration() {
    return nexmo;
  }

  public GcmConfiguration getGcmConfiguration() {
    return gcm;
  }

  public ApnConfiguration getApnConfiguration() {
    return apn;
  }

  public S3Configuration getS3Configuration() {
    return s3;
  }

  public MemcacheConfiguration getMemcacheConfiguration() {
    return memcache;
  }

  public RedisConfiguration getRedisConfiguration() {
    return redis;
  }

  public DataSourceFactory getDataSourceFactory() {
    return database;
  }

  public RateLimitsConfiguration getLimitsConfiguration() {
    return limits;
  }

  public FederationConfiguration getFederationConfiguration() {
    return federation;
  }

  public GraphiteConfiguration getGraphiteConfiguration() {
    return graphite;
  }

  public MetricsConfiguration getMetricsConfiguration() {
    return viz;
  }

  public SwiftConfiguration getSwiftConfiguration() {
    return swift;
  }

public GetuiConfiguration getGetuiConfiguration() {
    return getui;
}
  
}
