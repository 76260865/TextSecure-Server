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
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.AuthenticationCredentials;
import org.whispersystems.textsecuregcm.auth.AuthorizationHeader;
import org.whispersystems.textsecuregcm.auth.InvalidAuthorizationHeaderException;
import org.whispersystems.textsecuregcm.entities.AccountAttributes;
import org.whispersystems.textsecuregcm.entities.AccountInfo;
import org.whispersystems.textsecuregcm.entities.ApnRegistrationId;
import org.whispersystems.textsecuregcm.entities.FrontiaUserChannel;
import org.whispersystems.textsecuregcm.entities.GcmRegistrationId;
import org.whispersystems.textsecuregcm.entities.GetuiModel;
import org.whispersystems.textsecuregcm.entities.IncomingMessage;
import org.whispersystems.textsecuregcm.entities.IncomingMessageList;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.sms.SmsSender;
import org.whispersystems.textsecuregcm.sms.TwilioSmsSender;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountInfoManage;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.storage.PendingAccountsManager;
import org.whispersystems.textsecuregcm.util.Util;
import org.whispersystems.textsecuregcm.util.VerificationCode;
import org.whispersystems.textsecuregcm.push.FrontiaSender;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Vector;

import org.whispersystems.textsecuregcm.push.NotPushRegisteredException;
import org.whispersystems.textsecuregcm.push.TransientPushFailureException;

import io.dropwizard.auth.Auth;

@Path("/v1/accounts")
public class AccountController {

  private final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final PendingAccountsManager pendingAccounts;
    private final AccountsManager        accounts;
    private final RateLimiters           rateLimiters;
    private final SmsSender              smsSender;
    private final AccountInfoManage      accountinfomanage;
    private final MessageController      mc;

    public AccountController(PendingAccountsManager pendingAccounts,
            AccountsManager accounts,
            RateLimiters rateLimiters,
            SmsSender smsSenderFactory,
            AccountInfoManage accountinfo,
            MessageController mc)
    {
        this.pendingAccounts   = pendingAccounts;
        this.accounts          = accounts;
        this.rateLimiters      = rateLimiters;
        this.smsSender         = smsSenderFactory;
        this.accountinfomanage = accountinfo;
        this.mc = mc;
    }

  @Timed
  @GET
  @Path("/{transport}/code/{number}")
  public Response createAccount(@PathParam("transport") String transport,
                                @PathParam("number")    String number)
      throws IOException, RateLimitExceededException, NotPushRegisteredException, TransientPushFailureException
  {
    if (!Util.isValidNumber(number)) {
      logger.debug("Invalid number: " + number);
      throw new WebApplicationException(Response.status(400).build());
    }

    switch (transport) {
      case "sms":
        rateLimiters.getSmsDestinationLimiter().validate(number);
        break;
      case "voice":
        rateLimiters.getVoiceDestinationLimiter().validate(number);
        break;
      default:
        throw new WebApplicationException(Response.status(422).build());
    }

    VerificationCode verificationCode = generateVerificationCode();
    pendingAccounts.store(number, verificationCode.getVerificationCode());

    if (transport.equals("sms")) {
      //smsSender.deliverSmsVerification(number, verificationCode.getVerificationCodeDisplay());
    } else if (transport.equals("voice")) {
      smsSender.deliverVoxVerification(number, verificationCode.getVerificationCodeSpeech());
    }

    return Response.ok().build();
  }

  /**
   * FIXME Current we use the verificationCode to get the channelid and userid of baidu service temporary
   * and verificationCode = channelId:userId
   */
  @Timed
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/code/{verification_code}")
  public void verifyAccount(@PathParam("verification_code") String verificationCode,
                            @HeaderParam("Authorization")   String authorizationHeader,
                            @Valid                          AccountAttributes accountAttributes)
      throws RateLimitExceededException
  {
    try {
        logger.warn("verificationCode begin");
      AuthorizationHeader header = AuthorizationHeader.fromFullHeader(authorizationHeader);
      String number              = header.getNumber();
      String password            = header.getPassword();

      rateLimiters.getVerifyLimiter().validate(number);

      Optional<String> storedVerificationCode = pendingAccounts.getCodeForNumber(number);

      // Comment by wei.he assume the vertification code is the same with our db
      // TODO need use a sms gateway in future
//      if (!storedVerificationCode.isPresent() ||
//          !verificationCode.equals(storedVerificationCode.get()))
//      {
//        throw new WebApplicationException(Response.status(403).build());
//      }

      if (accounts.isRelayListed(number)) {
        throw new WebApplicationException(Response.status(417).build());
      }

      if (Util.isEmpty(verificationCode)) {
          //TODO: throw an exception
          //verificationCode = "123156465:1545457478";
          throw new WebApplicationException(Response.status(403).build());
      }

      logger.warn("verificationCode :" + verificationCode);
      String getuicid = verificationCode;
      
      Device device = new Device();
      device.setId(Device.MASTER_ID);
      device.setAuthenticationCredentials(new AuthenticationCredentials(password));
      device.setSignalingKey(accountAttributes.getSignalingKey());
      device.setFetchesMessages(accountAttributes.getFetchesMessages());
      device.setRegistrationId(accountAttributes.getRegistrationId());
      device.setGetuicid(getuicid);

      Account account = new Account();
      account.setNumber(number);
      account.setSupportsSms(accountAttributes.getSupportsSms());
      account.addDevice(device);
//      FrontiaSender sender = new FrontiaSender();
//      sender.sendMessage(channelId, userId, null);
      accounts.create(account);

      pendingAccounts.remove(number);

      logger.debug("Stored device...");
    } catch (InvalidAuthorizationHeaderException e) {
      logger.info("Bad Authorization Header", e);
      throw new WebApplicationException(Response.status(401).build());
    } catch (NumberFormatException ex) {
        logger.info("Bad baiduParams", ex);
        throw new WebApplicationException(Response.status(401).build());
    }
//    catch (NotPushRegisteredException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    } catch (TransientPushFailureException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
  }
  /**
   * Added by wei.he for saving the channel id and user id of frontia
   * @param account
   * @param registrationId
   */
  @Timed
  @PUT
  @Path("/frontia/")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setFrontiaChannelAndUserId(@Auth Account account, @Valid FrontiaUserChannel userChannel)  {
    Device device = account.getAuthenticatedDevice().get();
    device.setChannelId(Long.valueOf(userChannel.getChannelId()));
    device.setUserId(userChannel.getUserId());
    accounts.update(account);
  }
  
  
  /**
   * Added by chenzhen for saving the getui client id
   * @param account
   * @param registrationId
   */
  @Timed
  @PUT
  @Path("/getui/")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setGetuiClientId(@Auth Account account, @Valid  GetuiModel gtclientid)  {
    Device device = account.getAuthenticatedDevice().get();
    device.setGetuicid(gtclientid.getClientId());
    accounts.update(account);
  }

  @Timed
  @PUT
  @Path("/gcm/")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setGcmRegistrationId(@Auth Account account, @Valid GcmRegistrationId registrationId)  {
    Device device = account.getAuthenticatedDevice().get();
    device.setApnId(null);
    device.setGcmId(registrationId.getGcmRegistrationId());
    accounts.update(account);
  }

  @Timed
  @DELETE
  @Path("/gcm/")
  public void deleteGcmRegistrationId(@Auth Account account) {
    Device device = account.getAuthenticatedDevice().get();
    device.setGcmId(null);
    accounts.update(account);
  }

  @Timed
  @PUT
  @Path("/apn/")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setApnRegistrationId(@Auth Account account, @Valid ApnRegistrationId registrationId) {
    Device device = account.getAuthenticatedDevice().get();
    device.setApnId(registrationId.getApnRegistrationId());
    device.setGcmId(null);
    accounts.update(account);
  }

  @Timed
  @DELETE
  @Path("/apn/")
  public void deleteApnRegistrationId(@Auth Account account) {
    Device device = account.getAuthenticatedDevice().get();
    device.setApnId(null);
    accounts.update(account);
  }

  @Timed
  @POST
  @Path("/voice/twiml/{code}")
  @Produces(MediaType.APPLICATION_XML)
  public Response getTwiml(@PathParam("code") String encodedVerificationText) {
    return Response.ok().entity(String.format(TwilioSmsSender.SAY_TWIML,
        encodedVerificationText)).build();
  }

  @VisibleForTesting protected VerificationCode generateVerificationCode() {
    try {
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      int randomInt       = 100000 + random.nextInt(900000);
      return new VerificationCode(randomInt);
    } catch (NoSuchAlgorithmException e) {
      throw new AssertionError(e);
    }
  }

    @Timed
    @PUT
    @Path("/code/saveorupdate/{number}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveOrUpdateAccountInfo(@Auth Account account,@PathParam("number") String number,@Valid AccountInfo info)
            throws RateLimitExceededException
    {
        AccountInfo dbinfo = this.accountinfomanage.getbynumber(number);
        if (dbinfo==null)
        {
            this.accountinfomanage.insert(info);
        }
        else{
            if (info.getNickname() !=null )
            {
                dbinfo.setNickname(info.getNickname());
            }
            else if (info.getGender() != null)
            {
                dbinfo.setGender(info.getGender());
            }
            else if (info.getAge() != null)
            {
                dbinfo.setAge(info.getAge());
            }
            else if (info.getWork() != null)
            {
                dbinfo.setWork(info.getWork());
            }
            else if (info.getImageattachmentid() != null)
            {
                dbinfo.setImageattachmentid(info.getImageattachmentid());
            }
            else if (info.getSign() != null)
            {
                dbinfo.setSign(info.getSign());
            }

            this.accountinfomanage.updatebynumber(dbinfo);
        }
        
        try {
			List<String> friends = info.getFriends();
			if(friends!=null && !friends.isEmpty())
			{
				for (String tempnumber: friends)
				{
					if (tempnumber!=null&&!tempnumber.isEmpty())
					{
						IncomingMessageList mlist = new IncomingMessageList();
						List<IncomingMessage> messages = new Vector<IncomingMessage>();
						IncomingMessage message = new IncomingMessage();
						message.setBody("");
						message.setDestination(tempnumber);
						message.setType(5);
						message.setTimestamp(System.currentTimeMillis());
						messages.add(message);
						mlist.setMessages(messages);
			            mc.sendLocalMessage(account, tempnumber, mlist);
					}
				}
				
			}
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MismatchedDevicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StaleDevicesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    @Timed
    @GET
    @Path("/code/get/{number}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountInfo getAccountInfo(@Auth Account account,@PathParam("number") String number)
            throws RateLimitExceededException
    {
        final AccountInfo info = this.accountinfomanage.getbynumber(number);
        return info;
    }

}