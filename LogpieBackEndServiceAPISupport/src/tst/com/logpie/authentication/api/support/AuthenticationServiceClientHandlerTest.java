package tst.com.logpie.authentication.api.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import com.logpie.authentication.api.support.AuthenticationServiceClientHandler;
import com.logpie.authentication.api.support.ServiceCall;

public class AuthenticationServiceClientHandlerTest extends TestCase
{
    private static ServiceCall mockCall = mock(ServiceCall.class);

    public void testGetAuthenticateResult()
    {
        when(mockCall.call()).thenReturn("b");

        AuthenticationServiceClientHandler handler = new AuthenticationServiceClientHandler(
                mockCall);
        assertEquals(handler.getAuthenticateResult(), "b");
    }
}