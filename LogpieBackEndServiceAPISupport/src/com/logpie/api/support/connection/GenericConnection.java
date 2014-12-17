package com.logpie.api.support.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;

import com.logpie.api.support.connection.EndPoint.ServiceURL;
import com.logpie.authentication.api.support.exception.BadRequestException;
import com.logpie.authentication.api.support.exception.BadResponseException;
import com.logpie.authentication.api.support.exception.ConnectionException;
import com.logpie.authentication.api.support.exception.InvalidParameterException;
import com.logpie.authentication.api.support.exception.ServerInternalException;

public class GenericConnection
{
    // If the service call has the response value, can use this key to get
    // the result
    public static final String KEY_RESPONSE_DATA = "com.logpie.connection.response.key";
    // If the service call doesn't need return value, can use this key to get
    // the boolean result
    public static final String KEY_BOOLEAN_RESULT = "com.logpie.connection.result.boolean";
    public static final String KEY_REQUEST_ID = "request_id";

    private HttpURLConnection mHttpURLConnection;

    private ServiceURL mServiceURL;
    private int mTimeout = 100 * 1000;
    // logpie default verb is post
    private String mHttpVerb = "POST";
    private JSONObject mRequestData;
    private String mResponseString;
    private AuthType mAuthType;

    // This variable indicate whether the connection is retryable when meeting a
    // token expiration(Http 401 error). Notes: We should never retry for the
    // second time, since it may cause infinite-loop.
    private boolean mIsRetriable;

    private String mAccessToken;
    private String mRefreshToken;
    private String mUID;

    /**
     * Initialize the HttpURLConnection. It will set all necessary parameter
     * based on the serviceURL and also handle the authentication
     * 
     * @param serviceURL
     * @param authType
     * @param context
     * @throws InvalidParameterException
     */
    public void initialize(final ServiceURL serviceURL, final AuthType authType,
            final String access_token, final String refresh_token, final String uid)
    {
        // Check all the parameters are non-null
        checkParameterAndThrowIfIllegal(serviceURL, authType);
        try
        {
            mAuthType = authType;
            mServiceURL = serviceURL;
            mAccessToken = access_token;
            mRefreshToken = refresh_token;
            mUID = uid;

            // Retriable default to true;
            mIsRetriable = true;

            // initialize the HttpURLConnection based on the url
            URL url = serviceURL.getURL();
            boolean isUsingSSL = serviceURL.isUsingHttps();
            if (isUsingSSL)
            {
                // TODO: Should turn off when release;
                disableSSLClientCertificate();
                mHttpURLConnection = (HttpsURLConnection) url.openConnection();
            }
            else
            {
                mHttpURLConnection = (HttpURLConnection) url.openConnection();
            }

            setInputOutput();
            setRequestParameters();
            handleAuthentication();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Send data to the server, Based on the DoInput attribute to determine
     * whether need to return data or data. If need read response data, it will
     * parse into the callback's onSuccess bundle with Key @link
     * GenericConnection.KEY_RESPONSE_DATA
     * 
     * This method will trigger the service call, so this method will send the
     * task into background thread. If you need a sync result, you can just call
     * LogpieCallbackFuture.get() to blocking wait the result. You need a sync
     * callback, then you also need to call LogpieCallbackFuture.get() to make
     * sure the callback is called in a sync way.
     * 
     * In summary, this api support 3 return modes:
     * 
     * 1. Callback, sync (pass a callback, and also call the return
     * callbackFuture.get())
     * 
     * 2. Callback, async (just pass a callback)
     * 
     * 3. Return value, sync (NOT passing a callback, just get the return
     * callbackFuture, call callbackFuture.get())
     * 
     * @return
     * 
     * @throws ServerInternalException
     * @throws BadResponseException
     * @throws ConnectionException
     * @throws BadRequestException
     * 
     */
    public JSONObject send() throws BadRequestException, ConnectionException, BadResponseException,
            ServerInternalException
    {
        return syncSendDataAndGetResult();
    }

    /**
     * @throws BadRequestException
     * @throws ConnectionException
     * @throws BadResponseException
     * @throws ServerInternalException
     * @throws InvalidParameterException
     */
    private JSONObject syncSendDataAndGetResult() throws BadRequestException, ConnectionException,
            BadResponseException, ServerInternalException
    {

        try
        {
            mRequestData.put(KEY_REQUEST_ID, UUID.randomUUID().toString());
        } catch (JSONException e)
        {
        }
        String data = mRequestData.toString();
        if (data == null)
        {
            throw new IllegalArgumentException("The request data cannot be null!");
        }

        BufferedWriter writer = null;
        try
        {
            OutputStream outputStream = mHttpURLConnection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            writer = new BufferedWriter(outputStreamWriter);
            writer.write(data);
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("IOException happened when sending data");
        } finally
        {
            try
            {
                if (writer != null)
                {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                throw new ConnectionException("IOException happened when closing output writer");
            }
        }

        try
        {
            int responsecode = mHttpURLConnection.getResponseCode();
            if (responsecode >= 200 && responsecode < 300)
            {
                // Check whether the end point service need read the input
                if (mServiceURL.isDoInput())
                {
                    // read the response data from server.
                    mResponseString = inputStringReader(mHttpURLConnection.getInputStream());
                    try
                    {
                        return new JSONObject(mResponseString);
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                        throw new BadResponseException("Bad response from server:"
                                + mResponseString);
                    }
                }
                else
                {
                    return null;
                }
            }
            else if (responsecode >= 300 && responsecode < 400)
            {

            }
            else if (responsecode >= 400 && responsecode < 500)
            {
                /*
                 * When the error code is 401, means the token is invalid. We
                 * need to use refresh token to refresh access token. And we
                 * only do a retry for the first time to avoid potential
                 * infinite loop.
                 */
                if (responsecode == 401 && mIsRetriable)
                {
                    boolean tokenExchangeSuccess = doTokenExchange();
                    /*
                     * If tokenExchangeSucceed, then we should use the new
                     * access_token to retry the connection
                     */
                    if (tokenExchangeSuccess)
                    {
                        retryConnection();
                    }
                }
                throw new BadRequestException(
                        "client error happen when sending data to server. error code:"
                                + responsecode);
            }
            else if (responsecode >= 500)
            {
                throw new ServerInternalException("Service side internal exception! Error code:"
                        + responsecode);
            }
            else if (responsecode == -1)
            {
                throw new ServerInternalException(
                        "no valid response code when sending data to server. error code:"
                                + responsecode);
            }
            else
            {
                throw new ServerInternalException(
                        "unknown error when sending data to server. error code:" + responsecode);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            throw new ConnectionException("IOException happened when getting result from server");
        }
        return null;
    }

    private JSONObject retryConnection() throws BadRequestException, ConnectionException,
            BadResponseException, ServerInternalException
    {
        GenericConnection connection = new GenericConnection();
        connection.initialize(mServiceURL, mAuthType, mAccessToken, mRefreshToken, mUID);
        connection.setRequestData(mRequestData);
        connection.setRetriable(false);
        return connection.syncSendDataAndGetResult();
    }

    public String getResponse()
    {
        return mResponseString;
    }

    public int getTimeout()
    {
        return mTimeout;
    }

    public void setTimeout(final int timeout)
    {
        mTimeout = timeout;
    }

    public String getHttpVerb()
    {
        return mHttpVerb;
    }

    public void setHttpVerb(final String httpVerb)
    {
        mHttpVerb = httpVerb;
    }

    public JSONObject getRequestData()
    {
        return mRequestData;
    }

    public void setRequestData(final JSONObject mRequestData)
    {
        this.mRequestData = mRequestData;
    }

    /**
     * If this is set true, then the connection will automatically retry when
     * meeting the token expriation. It will try to do token exchange first,
     * then retry the connection. But the second connection will not be able to
     * retry again.
     * 
     * @param retriable
     */
    public void setRetriable(final boolean retriable)
    {
        this.mIsRetriable = retriable;
    }

    private void disableSSLClientCertificate()
    {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException
            {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException
            {

            }
        } };

        SSLContext sc;
        try
        {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
            {

                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            });
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String inputStringReader(final InputStream inputStream) throws IOException
    {
        if (inputStream != null)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }
            return builder.toString();
        }
        return null;
    }

    // handle the necessary authentication in request header
    private void handleAuthentication()
    {
        authenticateHttpURLConnection(mHttpURLConnection, mAuthType);
    }

    /**
     * Add the necessary auth header to the connection
     * 
     * @param httpURLConnection
     */
    public void authenticateHttpURLConnection(final HttpURLConnection httpURLConnection,
            final AuthType authType)
    {
        switch (authType)
        {
        case NoAuth:
        {
            return;
        }
        case TokenExchange:
        {

            if (mRefreshToken != null)
            {
                httpURLConnection.setRequestProperty("refresh_token", mRefreshToken);
                httpURLConnection.setRequestProperty("uid", mUID);
            }
            else
            {
                throw new UnsupportedOperationException(
                        "Not supported this operation when user is already logged out");
            }
            return;
        }
        case NormalAuth:
        {
            if (mAccessToken != null)
            {
                httpURLConnection.setRequestProperty("access_token", mAccessToken);
                httpURLConnection.setRequestProperty("uid", mUID);
            }
            else
            {
                throw new UnsupportedOperationException(
                        "Not supported this operation when user is already logged out");
            }
            return;
        }
        }
    }

    private boolean doTokenExchange() throws BadRequestException, ConnectionException,
            BadResponseException, ServerInternalException
    {
        JSONObject tokenExchangeData = new JSONObject();
        try
        {
            GenericConnection connection = new GenericConnection();
            connection.initialize(ServiceURL.AuthenticationService, AuthType.TokenExchange,
                    mAccessToken, mRefreshToken, mUID);
            tokenExchangeData.put(RequestKeys.KEY_REQUEST_TYPE, "TOKEN_EXCHANGE");
            tokenExchangeData.put(RequestKeys.KEY_DECLARE_UID, mUID);
            tokenExchangeData.put(RequestKeys.KEY_ACCESS_TOKEN, mAccessToken);
            tokenExchangeData.put(RequestKeys.KEY_REFRESH_TOKEN, mRefreshToken);
            tokenExchangeData.put(RequestKeys.KEY_REQUEST_ID, UUID.randomUUID().toString());
            connection.setRequestData(tokenExchangeData);
            JSONObject tokenExchangeResultJSON = connection.syncSendDataAndGetResult();
            boolean tokenExchangeSuccess = parseTokenExchangeBundle(tokenExchangeResultJSON);
            if (tokenExchangeSuccess)
            {
                return true;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean parseTokenExchangeBundle(JSONObject result)
    {
        if (result == null)
        {
            return false;
        }

        String tokenExceptionResult;
        try
        {
            tokenExceptionResult = result.getString(ResponseKeys.KEY_AUTHENTICATION_RESULT);
            if (tokenExceptionResult.equals(ResponseKeys.RESULT_SUCCESS))
            {
                String new_access_token = result.getString(ResponseKeys.KEY_ACCESS_TOKEN);
                mAccessToken = new_access_token;
                return true;

            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    private void setInputOutput()
    {
        // check whether need to do input
        if (mServiceURL.needDoOutput())
        {
            mHttpURLConnection.setDoOutput(true);
        }
        else
        {
            mHttpURLConnection.setDoOutput(false);
        }
        // check whether nned to do output
        if (mServiceURL.needDoInput())
        {
            mHttpURLConnection.setDoInput(true);
        }
        else
        {
            mHttpURLConnection.setDoInput(false);
        }
    }

    private void setRequestParameters() throws IOException
    {
        mHttpURLConnection.setChunkedStreamingMode(0);
        // set the timeout
        mHttpURLConnection.setConnectTimeout(mTimeout);
        // set http verb
        mHttpURLConnection.setRequestMethod(mHttpVerb);
        // set charset, we should use UTF-8
        mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
        // set Content-Type, logpie's default sending data format is JSON

        mHttpURLConnection.setRequestProperty("Content-Type", "application/json");
    }

    private void checkParameterAndThrowIfIllegal(final ServiceURL serviceURL,
            final AuthType authType)
    {
        if (serviceURL == null || authType == null)
        {
            throw new IllegalArgumentException(
                    "ServiceURL, authType and context all cannot be null!");
        }
    }
}