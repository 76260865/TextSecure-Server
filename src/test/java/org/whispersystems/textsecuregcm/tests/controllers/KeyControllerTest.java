package org.whispersystems.textsecuregcm.tests.controllers;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.whispersystems.textsecuregcm.controllers.KeysController;
import org.whispersystems.textsecuregcm.entities.PreKey;
import org.whispersystems.textsecuregcm.entities.PreKeyList;
import org.whispersystems.textsecuregcm.entities.PreKeyStatus;
import org.whispersystems.textsecuregcm.entities.UnstructuredPreKeyList;
import org.whispersystems.textsecuregcm.limits.RateLimiter;
import org.whispersystems.textsecuregcm.limits.RateLimiters;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.storage.Keys;
import org.whispersystems.textsecuregcm.tests.util.AuthHelper;

import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

import io.dropwizard.testing.junit.ResourceTestRule;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyControllerTest {

  private static final String EXISTS_NUMBER     = "+14152222222";
  private static String NOT_EXISTS_NUMBER = "+14152222220";

  private static int SAMPLE_REGISTRATION_ID  =  999;
  private static int SAMPLE_REGISTRATION_ID2 = 1002;

  private final PreKey          SAMPLE_KEY    = new PreKey(1, EXISTS_NUMBER, Device.MASTER_ID, 1234, "test1", "test2", false);
  private final PreKey          SAMPLE_KEY2   = new PreKey(2, EXISTS_NUMBER, 2, 5667, "test3", "test4,", false               );
  private final PreKey          SAMPLE_KEY3   = new PreKey(3, EXISTS_NUMBER, 3, 334, "test5", "test6", false                );
  private final Keys            keys          = mock(Keys.class           );
  private final AccountsManager accounts      = mock(AccountsManager.class);
  private final Account         existsAccount = mock(Account.class        );

  private RateLimiters          rateLimiters  = mock(RateLimiters.class);
  private RateLimiter           rateLimiter   = mock(RateLimiter.class );

  @Rule
  public final ResourceTestRule resources = ResourceTestRule.builder()
                                                            .addProvider(AuthHelper.getAuthenticator())
                                                            .addResource(new KeysController(rateLimiters, keys, accounts, null))
                                                            .build();

  @Before
  public void setup() {
    Device  sampleDevice  = mock(Device.class );
    Device  sampleDevice2 = mock(Device.class);
    Device  sampleDevice3 = mock(Device.class);

    when(sampleDevice.getRegistrationId()).thenReturn(SAMPLE_REGISTRATION_ID);
    when(sampleDevice2.getRegistrationId()).thenReturn(SAMPLE_REGISTRATION_ID2);
    when(sampleDevice3.getRegistrationId()).thenReturn(SAMPLE_REGISTRATION_ID2);
    when(sampleDevice.isActive()).thenReturn(true);
    when(sampleDevice2.isActive()).thenReturn(true);
    when(sampleDevice3.isActive()).thenReturn(false);

    when(existsAccount.getDevice(1L)).thenReturn(Optional.of(sampleDevice));
    when(existsAccount.getDevice(2L)).thenReturn(Optional.of(sampleDevice2));
    when(existsAccount.getDevice(3L)).thenReturn(Optional.of(sampleDevice3));
    when(existsAccount.isActive()).thenReturn(true);
    when(existsAccount.getIdentityKey()).thenReturn("existsidentitykey");

    when(accounts.get(EXISTS_NUMBER)).thenReturn(Optional.of(existsAccount));
    when(accounts.get(NOT_EXISTS_NUMBER)).thenReturn(Optional.<Account>absent());

    when(rateLimiters.getPreKeysLimiter()).thenReturn(rateLimiter);

    when(keys.get(eq(EXISTS_NUMBER), eq(1L))).thenAnswer(new Answer<Optional<UnstructuredPreKeyList>>() {
      @Override
      public Optional<UnstructuredPreKeyList> answer(InvocationOnMock invocationOnMock) throws Throwable {
        return Optional.of(new UnstructuredPreKeyList(cloneKey(SAMPLE_KEY)));
      }
    });

    when(keys.get(eq(NOT_EXISTS_NUMBER), eq(1L))).thenReturn(Optional.<UnstructuredPreKeyList>absent());

    when(keys.get(EXISTS_NUMBER)).thenAnswer(new Answer<Optional<UnstructuredPreKeyList>>() {
      @Override
      public Optional<UnstructuredPreKeyList> answer(InvocationOnMock invocationOnMock) throws Throwable {
        List<PreKey> allKeys = new LinkedList<>();
        allKeys.add(cloneKey(SAMPLE_KEY));
        allKeys.add(cloneKey(SAMPLE_KEY2));
        allKeys.add(cloneKey(SAMPLE_KEY3));

        return Optional.of(new UnstructuredPreKeyList(allKeys));
      }
    });

    when(keys.getCount(eq(AuthHelper.VALID_NUMBER), eq(1L))).thenReturn(5);
    when(AuthHelper.VALID_ACCOUNT.getIdentityKey()).thenReturn(null);
  }

  @Test
  public void validKeyStatusTest() throws Exception {
    PreKeyStatus result = resources.client().resource("/v1/keys")
        .header("Authorization",
                AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(PreKeyStatus.class);

    assertThat(result.getCount() == 4);

    verify(keys).getCount(eq(AuthHelper.VALID_NUMBER), eq(1L));
  }

  @Test
  public void validLegacyRequestTest() throws Exception {
    PreKey result = resources.client().resource(String.format("/v1/keys/%s", EXISTS_NUMBER))
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(PreKey.class);

    assertThat(result.getKeyId()).isEqualTo(SAMPLE_KEY.getKeyId());
    assertThat(result.getPublicKey()).isEqualTo(SAMPLE_KEY.getPublicKey());
    assertThat(result.getIdentityKey()).isEqualTo(existsAccount.getIdentityKey());

    assertThat(result.getId() == 0);
    assertThat(result.getNumber() == null);

    verify(keys).get(eq(EXISTS_NUMBER), eq(1L));
    verifyNoMoreInteractions(keys);
  }

  @Test
  public void validMultiRequestTest() throws Exception {
    UnstructuredPreKeyList results = resources.client().resource(String.format("/v1/keys/%s/*", EXISTS_NUMBER))
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(UnstructuredPreKeyList.class);

    assertThat(results.getKeys().size()).isEqualTo(2);

    PreKey result = results.getKeys().get(0);

    assertThat(result.getKeyId()).isEqualTo(SAMPLE_KEY.getKeyId());
    assertThat(result.getPublicKey()).isEqualTo(SAMPLE_KEY.getPublicKey());
    assertThat(result.getIdentityKey()).isEqualTo(existsAccount.getIdentityKey());
    assertThat(result.getRegistrationId()).isEqualTo(SAMPLE_REGISTRATION_ID);

    assertThat(result.getId() == 0);
    assertThat(result.getNumber() == null);

    result = results.getKeys().get(1);
    assertThat(result.getKeyId()).isEqualTo(SAMPLE_KEY2.getKeyId());
    assertThat(result.getPublicKey()).isEqualTo(SAMPLE_KEY2.getPublicKey());
    assertThat(result.getIdentityKey()).isEqualTo(existsAccount.getIdentityKey());
    assertThat(result.getRegistrationId()).isEqualTo(SAMPLE_REGISTRATION_ID2);

    assertThat(result.getId() == 0);
    assertThat(result.getNumber() == null);

    verify(keys).get(eq(EXISTS_NUMBER));
    verifyNoMoreInteractions(keys);
  }


  @Test
  public void invalidRequestTest() throws Exception {
    ClientResponse response = resources.client().resource(String.format("/v1/keys/%s", NOT_EXISTS_NUMBER))
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(ClientResponse.class);

    assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(404);
  }

  @Test
  public void unauthorizedRequestTest() throws Exception {
    ClientResponse response =
        resources.client().resource(String.format("/v1/keys/%s", NOT_EXISTS_NUMBER))
            .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.INVALID_PASSWORD))
            .get(ClientResponse.class);

    assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(401);

    response =
        resources.client().resource(String.format("/v1/keys/%s", NOT_EXISTS_NUMBER))
            .get(ClientResponse.class);

    assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(401);
  }

  @Test
  public void putKeysTest() throws Exception {
    final PreKey newKey        = new PreKey(0, null, 1L, 31337, "foobar", "foobarbaz", false);
    final PreKey lastResortKey = new PreKey(0, null, 1L, 0xFFFFFF, "fooz", "foobarbaz", false);

    List<PreKey> preKeys = new LinkedList<PreKey>() {{
      add(newKey);
    }};

    PreKeyList preKeyList = new PreKeyList();
    preKeyList.setKeys(preKeys);
    preKeyList.setLastResortKey(lastResortKey);

    ClientResponse response =
        resources.client().resource("/v1/keys")
            .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .put(ClientResponse.class, preKeyList);

    assertThat(response.getClientResponseStatus().getStatusCode()).isEqualTo(204);

    ArgumentCaptor<List>   listCaptor       = ArgumentCaptor.forClass(List.class  );
    ArgumentCaptor<PreKey> lastResortCaptor = ArgumentCaptor.forClass(PreKey.class);
    verify(keys).store(eq(AuthHelper.VALID_NUMBER), eq(1L), listCaptor.capture(), lastResortCaptor.capture());

    List<PreKey> capturedList = listCaptor.getValue();
    assertThat(capturedList.size() == 1);
    assertThat(capturedList.get(0).getIdentityKey().equals("foobarbaz"));
    assertThat(capturedList.get(0).getKeyId() == 31337);
    assertThat(capturedList.get(0).getPublicKey().equals("foobar"));

    assertThat(lastResortCaptor.getValue().getPublicKey().equals("fooz"));
    assertThat(lastResortCaptor.getValue().getIdentityKey().equals("foobarbaz"));

    verify(AuthHelper.VALID_ACCOUNT).setIdentityKey(eq("foobarbaz"));
    verify(accounts).update(AuthHelper.VALID_ACCOUNT);
  }

  private PreKey cloneKey(PreKey source) {
    return new PreKey(source.getId(), source.getNumber(), source.getDeviceId(), source.getKeyId(),
                      source.getPublicKey(), source.getIdentityKey(), source.isLastResort());
  }

}