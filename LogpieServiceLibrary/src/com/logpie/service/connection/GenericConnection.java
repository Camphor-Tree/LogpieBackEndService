package com.logpie.service.connection;

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

import com.logpie.service.connection.EndPoint.ServiceURL;
import com.logpie.service.util.ServiceCallback;
import com.logpie.service.util.ServiceLog;

public class GenericConnection
{
    public static final String KEY_RESPONSE_DATA = "com.logpie.connection.response.key";
    public static final String KEY_REQUEST_ID = "request_id";
    public static final String STATIC_REQUEST_ID = "5U2VydmljZSRVc2VyU2VydmljZSR";

    private static final String TAG = GenericConnection.class.getName();

    private HttpURLConnection mHttpURLConnection;
    private ServiceURL mServiceURL;
    private int mTimeout = 10 * 1000;
    // logpie default verb is post
    private String mHttpVerb = "POST";
    private JSONObject mRequestData;

    public void initialize(ServiceURL serviceURL)
    {

        try
        {
            mServiceURL = serviceURL;
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
            mHttpURLConnection.setChunkedStreamingMode(0);
            // set the timeout
            mHttpURLConnection.setConnectTimeout(mTimeout);
            // set http verb
            mHttpURLConnection.setRequestMethod(mHttpVerb);
            // set charset, we should use UTF-8
            mHttpURLConnection.setRequestProperty("Charset", "UTF-8");
            // set Content-Type, logpie's default sending data format is JSON

            mHttpURLConnection.setRequestProperty("Content-Type", "application/json");

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
     * @param callback
     *            Logpie callback.
     */
    public void send(ServiceCallback callback)
    {
        try
        {
            mRequestData.put(KEY_REQUEST_ID, UUID.randomUUID().toString());
        } catch (JSONException e1)
        {
            // Do nothing if cannot add requestID
            ServiceLog.e(TAG, "JSONException when putting request_id. Putting empty request_id");
        }
        String data = mRequestData.toString();
        if (data == null)
        {
            // TODO: we should put the error message in one place
            JSONObject error = new JSONObject();
            try
            {
                error.put("error", "cannot send empty data");
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when set the error message");
                e.printStackTrace();
            }
            callback.onError(error);
            return;
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
            ServiceLog.e(TAG, "geOutputStream occured error");

            handleCallback(false, "IOException when trying to output the data", callback);
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
                ServiceLog.e(TAG, "error when try to close BufferedWriter");
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
                    String responseData = inputStringReader(mHttpURLConnection.getInputStream());
                    ServiceLog.i(TAG, "Response from server:" + responseData);
                    handleCallbackWithResponseData(responseData, callback);
                }
                else
                {
                    handleCallback(true, "succesfully sending data to server", callback);
                    ServiceLog.i(TAG, "successful sending data to: " + mServiceURL.getServiceName()
                            + "<--->hitting url:" + mServiceURL.getURL().toString());
                }
            }
            else if (responsecode >= 300 && responsecode < 400)
            {
                handleCallback(false, "redirection happen when sending data to server. error code:"
                        + responsecode, callback);
                ServiceLog.e(TAG, "redirection happen when sending data to server. error code:"
                        + responsecode);
            }
            else if (responsecode >= 400 && responsecode < 500)
            {
                handleCallback(false,
                        "client error happen when sending data to server. error code:"
                                + responsecode, callback);
                ServiceLog.e(TAG, "client error happen when sending data to server. error code:"
                        + responsecode);
            }
            else if (responsecode >= 500)
            {
                handleCallback(false, "server error when sending data to server. error code:"
                        + responsecode, callback);
                ServiceLog.e(TAG, "server error when sending data to server. error code:"
                        + responsecode);
            }
            else if (responsecode == -1)
            {
                handleCallback(false,
                        "no valid response code when sending data to server. error code:"
                                + responsecode, callback);
                ServiceLog.e(TAG, "no valid response code when sending data to server. error code:"
                        + responsecode);
            }
            else
            {
                handleCallback(false, "unknown error when sending data to server. error code:"
                        + responsecode, callback);
                ServiceLog.e(TAG, "unknown error when sending data to server. error code:"
                        + responsecode);
            }
        } catch (IOException e)
        {
            handleCallback(false, "IOException when sending data to server and getresponseCode",
                    callback);
            e.printStackTrace();
        }
    }

    private void handleCallback(boolean isSuccess, String message, ServiceCallback callback)
    {
        JSONObject returnMessage = new JSONObject();
        if (isSuccess)
        {
            try
            {
                returnMessage.put("success", message);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when putting success message");
                e.printStackTrace();
            }
            callback.onSuccess(returnMessage);
        }
        else
        {
            try
            {
                returnMessage.put("error", message);
            } catch (JSONException e)
            {
                ServiceLog.e(TAG, "JSONException when putting error message");
                e.printStackTrace();
            }
            callback.onError(returnMessage);
        }

    }

    private void handleCallbackWithResponseData(final String responseString,
            final ServiceCallback callback)
    {
        JSONObject returnMessage = new JSONObject();
        try
        {
            JSONObject responseJSON = new JSONObject(responseString);
            returnMessage.put(KEY_RESPONSE_DATA, responseJSON);
        } catch (JSONException e)
        {
            ServiceLog
                    .e(TAG,
                            "JSON Exception happened when parsing response from server, mainly due to the Server is returning mal-formed data",
                            e);
        }
        callback.onSuccess(returnMessage);
    }

    public JSONObject getResponse()
    {
        return new JSONObject();
    }

    public JSONObject sendAndGetResult(ServiceCallback callback)
    {
        return new JSONObject();
    }

    public int getTimeout()
    {
        return mTimeout;
    }

    public void setTimeout(int timeout)
    {
        mTimeout = timeout;
    }

    public String getHttpVerb()
    {
        return mHttpVerb;
    }

    public void setHttpVerb(String httpVerb)
    {
        mHttpVerb = httpVerb;
    }

    public JSONObject getRequestData()
    {
        return mRequestData;
    }

    public void setRequestData(JSONObject mRequestData)
    {
        this.mRequestData = mRequestData;
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

    private String inputStringReader(InputStream inputStream) throws IOException
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
}
