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
import org.whispersystems.textsecuregcm.entities.EncryptedOutgoingMessage;

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

public class FrontiaSender {
	private final Logger logger = LoggerFactory.getLogger(FrontiaSender.class);
	private final MetricRegistry metricRegistry = SharedMetricRegistries
			.getOrCreate(org.whispersystems.textsecuregcm.util.Constants.METRICS_NAME);
	private final Meter success = metricRegistry.meter(name(getClass(), "sent",
			"success"));
	private final Meter failure = metricRegistry.meter(name(getClass(), "sent",
			"failure"));

	// private final Sender sender;

	private String apiKey = "y2CzhlKDrct8dbjKP2DFpHeo";
	private String secretKey = "rhNPOm8G3Et0x2rIBDHTEpPptgCaS9L1";
	private ChannelKeyPair pair = new ChannelKeyPair(apiKey, secretKey);
	private BaiduChannelClient channelClient = new BaiduChannelClient(pair);

	public FrontiaSender() {
	}

	public void sendMessage(long channelId, String userId,
			EncryptedOutgoingMessage outgoingMessage)
			throws NotPushRegisteredException, TransientPushFailureException {

		channelClient.setChannelLogHandler(new YunLogHandler() {
			@Override
			public void onHandle(YunLogEvent event) {
				System.out.println(event.getMessage());
			}
		});

		try {
			PushUnicastMessageRequest request = new PushUnicastMessageRequest();
			request.setDeviceType(3); // device_type => 1: web 2: pc 3:android
										// 4:ios 5:wp
			request.setChannelId(channelId);
			request.setUserId(userId);
			logger.warn("FrontiaSender sendMessage channelId:" + channelId + "  userId:"
					+ userId);

			request.setMessage(outgoingMessage.serialize());
			request.setMsgKey(System.currentTimeMillis()+"");

			PushUnicastMessageResponse response = channelClient
					.pushUnicastMessage(request);

			success.mark();
			logger.warn("push amount : " + response.getSuccessAmount());
		} catch (ChannelClientException e) {
			failure.mark();
			e.printStackTrace();
			logger.warn(e.getMessage());
		} catch (ChannelServerException e) {
			failure.mark();
			logger.warn(String.format(
					"request_id: %d, error_code: %d, error_message: %s",
					e.getRequestId(), e.getErrorCode(), e.getErrorMsg()));
		}

	}
}
