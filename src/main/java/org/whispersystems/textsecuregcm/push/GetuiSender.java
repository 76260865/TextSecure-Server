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
package org.whispersystems.textsecuregcm.push;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.TransmissionTemplate;

import org.whispersystems.textsecuregcm.configuration.GetuiConfiguration;
import org.whispersystems.textsecuregcm.entities.EncryptedOutgoingMessage;
import org.whispersystems.textsecuregcm.storage.Device;

import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codahale.metrics.MetricRegistry.name;

public class GetuiSender {
    private final Logger logger = LoggerFactory.getLogger(GetuiSender.class);
    private final MetricRegistry metricRegistry = SharedMetricRegistries
            .getOrCreate(org.whispersystems.textsecuregcm.util.Constants.METRICS_NAME);
    private final Meter success = metricRegistry.meter(name(getClass(), "sent",
            "success"));
    private final Meter failure = metricRegistry.meter(name(getClass(), "sent",
            "failure"));


      private String AppID;
      private String AppKey;
      private String AppSecret;
      private String MasterSecret;
      private String host;

    public GetuiSender(GetuiConfiguration getuiConfiguration) {
        this.AppID = getuiConfiguration.getAppID();
        this.AppKey = getuiConfiguration.getAppKey();
        this.AppSecret = getuiConfiguration.getAppSecret();
        this.MasterSecret = getuiConfiguration.getMasterSecret();
        this.host = getuiConfiguration.getHost();
    }

    public void sendMessage(String getuicid,
            EncryptedOutgoingMessage outgoingMessage)
            throws NotPushRegisteredException, TransientPushFailureException {        
        try {
            
            IGtPush push = new IGtPush(host, AppKey, MasterSecret);
            push.connect();
      
            SingleMessage message = new SingleMessage();
            message.setOffline(true);
                    //离线有效时间，单位为毫秒，可选
            message.setOfflineExpireTime(24 * 3600 * 1000);
            message.setData(TransmissionTemplate(outgoingMessage.serialize()));
      
            //List targets = new ArrayList();
            Target target1 = new Target();
            //Target target2 = new Target();
      
            target1.setAppId(AppID);
            target1.setClientId(getuicid);
      
            IPushResult ret = push.pushMessageToSingle(message, target1);
            
            
//            PushUnicastMessageRequest request = new PushUnicastMessageRequest();
//            request.setDeviceType(3); // device_type => 1: web 2: pc 3:android
//                                        // 4:ios 5:wp
//            request.setChannelId(channelId);
//            request.setUserId(userId);
//            logger.warn("FrontiaSender sendMessage channelId:" + channelId + "  userId:"
//                    + userId);
//
//            request.setMessage(outgoingMessage.serialize());
//            request.setMsgKey(System.currentTimeMillis()+"");
//
//            PushUnicastMessageResponse response = channelClient
//                    .pushUnicastMessage(request);

            success.mark();
            //logger.warn("push amount : " + response.getSuccessAmount());
            System.out.println(ret.getResponse().toString());
        } catch (IOException e) {
            failure.mark();
            e.printStackTrace();
        }

    }
    
    private TransmissionTemplate TransmissionTemplate(String content) {
        TransmissionTemplate template = new TransmissionTemplate();
        template.setAppId(AppID);
        template.setAppkey(AppKey);
        template.setTransmissionType(2);
        template.setTransmissionContent(content);
        return template;
    }
}
