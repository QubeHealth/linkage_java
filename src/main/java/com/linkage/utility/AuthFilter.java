package com.linkage.utility;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkage.api.ApiResponse;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthFilter implements Filter {

    private final String apiKey;
    private final String authorizationKey;

    public AuthFilter(String apiKey, String authorizationKey) {
        this.apiKey = apiKey;
        this.authorizationKey = authorizationKey;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

       
        String xApiKey = httpRequest.getHeader("X-API-Key");

        if ( xApiKey == null) {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(httpResponse.getWriter(),
                    new ApiResponse<>(false, "Unauthorised", null));

            return;
        }

        if ( !xApiKey.equals(apiKey)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(httpResponse.getWriter(),
                    new ApiResponse<>(false, "Unauthorised", null));
            return;
        }

        chain.doFilter(request, response);
    }
}
