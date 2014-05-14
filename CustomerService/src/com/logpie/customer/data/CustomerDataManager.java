package com.logpie.customer.data;

import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.Driver;
import com.logpie.customer.tool.CustomerServiceLog;

public class CustomerDataManager
{
    public interface DataCallback
    {
        abstract void onSuccess(JSONObject result);

        abstract void onError(JSONObject error);
    }

    private static final String TAG = CustomerDataManager.class.getName();
    private static CustomerDataManager sAuthDataManager;
    private static Driver sPostgreDriver;

    public static final String KEY_INSERT_RESULT_ID = "com.logpie.auth.insert.id";
    public static final String KEY_CALLBACK_ERROR = "com.logpie.auth.error";

    public synchronized static CustomerDataManager getInstance()
    {
        if (sAuthDataManager == null)
        {
            initializeDB();
            sAuthDataManager = new CustomerDataManager();
        }
        return sAuthDataManager;
    }

    /**
     * initialize the postgreSQL's db driver
     */
    private static void initializeDB()
    {
        try
        {
            sPostgreDriver = (Driver) Class.forName("org.postgresql.Driver").newInstance();
        } catch (InstantiationException e)
        {
            CustomerServiceLog.e(TAG, e.getMessage());
            CustomerServiceLog.e(TAG,
                    "InstantiationException happended when trying to initiliaze postgreSQL driver");
        } catch (IllegalAccessException e)
        {
            CustomerServiceLog.e(TAG, e.getMessage());
            CustomerServiceLog.e(TAG,
                    "IllegalAccessException happended when trying to initiliaze postgreSQL driver");
        } catch (ClassNotFoundException e)
        {
            CustomerServiceLog.e(TAG, e.getMessage());
            CustomerServiceLog.e(TAG,
                    "ClassNotFoundException happended when trying to initiliaze postgreSQL driver");
        }
    }
}
