package com.logpie.commonlib;

import java.net.MalformedURLException;
import java.net.URL;

public class EndPoint
{
    private static String TAG = EndPoint.class.getName();

    public enum ServiceURL
    {
        RocektService("RocketService", "https://10.0.0.6:8443/RocketService/servlet", "1.0.0",
                "test", true, false, true),

        AuthenticationService("AuthenticationService",
                "http://10.0.0.19:8080/AuthenticationService/auth", "1.0.0", "test", true, true,
                false),

        CustomerService("CustomerService", "http://10.0.0.19:8080/LogpieService/customer", "1.0.0",
                "test", true, true, false),

        ActivityService("ActivityService", "http://10.0.0.19:8080/LogpieService/activity", "1.0.0",
                "test", true, true, false),

        CommentService("CommentService", "http://10.0.0.19:8080/LogpieService/comment", "1.0.0",
                "test", true, true, false),

        PhotoService("PhotoService", "http://10.0.0.19:8080/LogpieService/photo", "1.0.0", "test",
                true, true, false),
        // EC2 Service URL
        AuthenticationServiceEC2(
                "AuthenticationService",
                "http://ec2-54-68-64-62.us-west-2.compute.amazonaws.com:8080/AuthenticationService/auth",
                "1.0.0", "test", true, true, false),

        CustomerServiceEC2(
                "CustomerService",
                "http://ec2-54-68-64-62.us-west-2.compute.amazonaws.com:8080/LogpieService/customer",
                "1.0.0", "test", true, true, false),

        ActivityServiceEC2(
                "ActivityService",
                "http://ec2-54-68-64-62.us-west-2.compute.amazonaws.com:8080/LogpieService/activity",
                "1.0.0", "test", true, true, false),

        CommentServiceEC2(
                "CommentService",
                "http://ec2-54-68-64-62.us-west-2.compute.amazonaws.com:8080/LogpieService/comment",
                "1.0.0", "test", true, true, false),

        PhotoServiceEC2("PhotoService",
                "http://ec2-54-68-64-62.us-west-2.compute.amazonaws.com:8080/LogpieService/photo",
                "1.0.0", "test", true, true, false);

        String mServiceName;
        String mUrl;
        String mVersion;
        String mEnvironment;
        boolean mDoOutput;
        boolean mDoInput;
        boolean mUseSSL;

        ServiceURL(String name, String url, String version, String environment, boolean doOutput,
                boolean doInput, boolean useSSL)
        {
            mServiceName = name;
            mUrl = url;
            mVersion = version;
            mEnvironment = environment;
            mDoOutput = doOutput;
            mDoInput = doInput;
            mUseSSL = useSSL;
        }

        public String getVersion()
        {
            return mVersion;
        }

        public String getEnvironment()
        {
            return mEnvironment;
        }

        public boolean isDoOutput()
        {
            return mDoOutput;
        }

        public boolean isDoInput()
        {
            return mDoInput;
        }

        public URL getURL()
        {
            try
            {
                return new URL(mUrl);
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public boolean needDoOutput()
        {
            return mDoOutput;
        }

        public boolean needDoInput()
        {
            return mDoInput;
        }

        public String getServiceName()
        {
            return mServiceName;
        }

        public boolean isUsingHttps()
        {
            return mUseSSL;
        }

        public boolean equals(ServiceURL serviceURL)
        {
            if (serviceURL.getServiceName().equals(mServiceName)
                    && serviceURL.getEnvironment().equals(mEnvironment)
                    && serviceURL.getURL().equals(this.getURL())
                    && serviceURL.getVersion().equals(mVersion))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    // Basically the method will only return the first service endpoint satisfy
    // the condition
    // Not recommend to use that
    public static ServiceURL getServiceUrlByName(String name)
    {
        for (ServiceURL serviceUrl : ServiceURL.values())
        {
            if (name.equals(serviceUrl.getServiceName()))
                return serviceUrl;
        }
        return null;
    }

    public static ServiceURL getServiceUrlByCondition(String name, String environment,
            String version)
    {
        for (ServiceURL serviceUrl : ServiceURL.values())
        {
            if (name.equals(serviceUrl.getServiceName())
                    && environment.equals(serviceUrl.getEnvironment())
                    && version.equals(serviceUrl.getVersion()))
                return serviceUrl;
        }
        return null;
    }
}
