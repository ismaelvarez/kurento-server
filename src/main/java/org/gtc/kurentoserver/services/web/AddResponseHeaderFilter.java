package org.gtc.kurentoserver.services.web;

import org.gtc.kurentoserver.services.authentification.SessionAuthentication;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebFilter("/*")
public class AddResponseHeaderFilter implements Filter {

    @Autowired
    private SessionAuthentication sessions;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        httpServletResponse.setHeader("session-alive", String.valueOf(sessions.getTimeAlive(httpRequest.getHeader("session-id"))));
        chain.doFilter(request, response);
    }
}
