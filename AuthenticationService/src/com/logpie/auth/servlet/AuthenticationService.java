/*
 * Copyright (c) 2014 logpie.com
 * All rights reserved.
 */

package com.logpie.auth.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.logpie.auth.config.AuthConfig;
import com.logpie.auth.logic.AuthenticationManager;
import com.logpie.service.common.helper.CommonServiceLog;

public class AuthenticationService extends HttpServlet
{
    private static final String TAG = AuthenticationService.class.getName();
    private static final long serialVersionUID = 1L;

    @Override
    public void init()
    {
    	CommonServiceLog.setLogFilePath(AuthConfig.LogPath);
    	CommonServiceLog.d(TAG, "Start initializing...");
        ServletContext serviceContext = getServletContext();
        AuthenticationManager.initialize(serviceContext);
        // load properties from disk, do be used by subsequent doGet() calls
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        // TODO: we should remove in the future
        // Currently we can just keep it.
        doPost(request, response);

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        AuthenticationManager.getInstance().handleAuthenticationRequest(request, response);
    }

}
