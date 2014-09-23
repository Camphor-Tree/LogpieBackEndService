package com.logpie.service.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logpie.service.logic.CustomerManager;
import com.logpie.service.util.ServiceLog;

public class CustomerService extends HttpServlet
{

    private static final String TAG = CustomerService.class.getName();
    private static final long serialVersionUID = 1L;

    @Override
    public void init()
    {
        ServiceLog.d(TAG, "Customer service servlet starts initializing...");
        ServletContext serviceContext = getServletContext();
        CustomerManager.initialize(serviceContext);
        // load properties from disk, do be used by subsequent doGet() calls
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO: we should remove in the future. Currently we can just keep it.
        ServiceLog.d(TAG, "Customer service received the request by GET...");
        doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        ServiceLog.d(TAG, "Customer service received the request by POST...");
        CustomerManager customerManager = CustomerManager.getInstance();
        ServiceLog.d(TAG, "Customer service is handling the request...");
        customerManager.handleRequest(request, response);
        ServiceLog.d(TAG, "Customer service finished the request.");
    }

}
