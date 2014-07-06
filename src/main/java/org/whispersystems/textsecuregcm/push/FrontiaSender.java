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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import org.whispersystems.textsecuregcm.entities.EncryptedOutgoingMessage;

import com.baidu.yun.channel.auth.ChannelKeyPair;
import com.baidu.yun.channel.client.BaiduChannelClient;
import com.baidu.yun.channel.exception.ChannelClientException;
import com.baidu.yun.channel.exception.ChannelServerException;
import com.baidu.yun.channel.model.PushUnicastMessageRequest;
import com.baidu.yun.channel.model.PushUnicastMessageResponse;
import com.baidu.yun.core.log.YunLogEvent;
import com.baidu.yun.core.log.YunLogHandler;

import java.io.IOException;

import static com.codahale.metrics.MetricRegistry.name;

public class FrontiaSender {

  private final MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(org.whispersystems.textsecuregcm.util.Constants.METRICS_NAME);
  private final Meter          success        = metricRegistry.meter(name(getClass(), "sent", "success"));
  private final Meter          failure        = metricRegistry.meter(name(getClass(), "sent", "failure"));

//  private final Sender sender;

  private String apiKey = "vL8vhak2sGljMZXOxpyYWUP0";
  private String secretKey = "LfY74eWNbT5A51XGwHISh6Kibcu5u02s";
  private ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);
  private BaiduChannelClient channelClient = new BaiduChannelClient(pair);

  public FrontiaSender() {
  }

  public void sendMessage(long channelId, String userId, EncryptedOutgoingMessage outgoingMessage) 
		  throws NotPushRegisteredException, TransientPushFailureException {
	// 3. ��Ҫ�˽⽻��ϸ�ڣ���ע��YunLogHandler��
      channelClient.setChannelLogHandler(new YunLogHandler() {
          @Override
          public void onHandle(YunLogEvent event) {
              System.out.println(event.getMessage());
          }
      });

      try {
	 // Message frontiaMessage = new Message.Builder().addData("type", "message")
         //                                       .addData("message", outgoingMessage.serialize())
         //                                       .build();
          // 4. �������������
          // �ֻ�˵�ChannelId�� �ֻ�˵�UserId�� ����1111111111111���棬�û����滻Ϊ�Լ���
          PushUnicastMessageRequest request = new PushUnicastMessageRequest();
          request.setDeviceType(3); // device_type => 1: web 2: pc 3:android
                                    // 4:ios 5:wp
          request.setChannelId(channelId);
          request.setUserId(userId);

          request.setMessage("Hello Channel");

          // 5. ����pushMessage�ӿ�
          PushUnicastMessageResponse response = channelClient
                  .pushUnicastMessage(request);

          // 6. ��֤���ͳɹ�
          success.mark();
          System.out.println("push amount : " + response.getSuccessAmount());
      } catch (ChannelClientException e) {
          // ����ͻ��˴����쳣
    	  failure.mark();
          e.printStackTrace();
      } catch (ChannelServerException e) {
          // �������˴����쳣
    	  failure.mark();
          System.out.println(String.format(
                  "request_id: %d, error_code: %d, error_message: %s",
                  e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
      }

  }
  
//  public String sendMessage(String gcmRegistrationId, EncryptedOutgoingMessage outgoingMessage)
//      throws NotPushRegisteredException, TransientPushFailureException
//  {
//    try {
//      Message gcmMessage = new Message.Builder().addData("type", "message")
//                                                .addData("message", outgoingMessage.serialize())
//                                                .build();
//
//      Result  result = sender.send(gcmMessage, gcmRegistrationId, 5);
//
//      if (result.getMessageId() != null) {
//        success.mark();
//        return result.getCanonicalRegistrationId();
//      } else {
//        failure.mark();
//        if (result.getErrorCodeName().equals(Constants.ERROR_NOT_REGISTERED)) {
//          throw new NotPushRegisteredException("Device no longer registered with GCM.");
//        } else {
//          throw new TransientPushFailureException("GCM Failed: " + result.getErrorCodeName());
//        }
//      }
//    } catch (IOException e) {
//      throw new TransientPushFailureException(e);
//    }
//  }
}
