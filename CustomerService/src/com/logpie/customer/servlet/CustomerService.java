package com.logpie.customer.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logpie.customer.logic.CustomerManager;
import com.logpie.service.common.helper.CommonServiceLog;

public class CustomerService extends HttpServlet
{

    private static final String TAG = CustomerService.class.getName();
    private static final long serialVersionUID = 1L;

    public void init()
    {
        CommonServiceLog.d(TAG, "Start initializing...");
        ServletContext serviceContext = getServletContext();
        CustomerManager.initialize(serviceContext);
        // load properties from disk, do be used by subsequent doGet() calls
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO: we should remove in the future
        // Currently we can just keep it.
    	System.out.println("Get your request!");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {

    }

}
