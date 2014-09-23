package com.logpie.service.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logpie.service.logic.ActivityManager;
import com.logpie.service.util.ServiceLog;

public class ActivityService extends HttpServlet
{

    private static final String TAG = ActivityService.class.getName();
    private static final long serialVersionUID = 1L;

    @Override
    public void init()
    {
        ServiceLog.d(TAG, "Activity service servlet starts initializing...");
        ServletContext serviceContext = getServletContext();
        ActivityManager.initialize(serviceContext);
        // load properties from disk, do be used by subsequent doGet() calls
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO: we should remove in the future
        // Currently we can just keep it.
        ServiceLog.d(TAG, "Activity service received the request by GET...");
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        ServiceLog.d(TAG, "Activity service received the request by POST...");
        ActivityManager activityManager = ActivityManager.getInstance();
        ServiceLog.d(TAG, "Activity service is handling the request...");
        activityManager.handleRequest(request, response);
        ServiceLog.d(TAG, "Activity service finished the request.");
    }

}
