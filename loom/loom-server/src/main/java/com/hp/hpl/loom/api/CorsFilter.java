/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.loom.api;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A simple Cross-Origin Resource Sharing filter to permit clients running in web browsers not
 * served from the same host as Loom to call the API. The Access-Control-Allow-Origin may be set via
 * the cors.origin property in deployment.properties.
 */

public class CorsFilter extends OncePerRequestFilter {
    private static final Log LOG = LogFactory.getLog(CorsFilter.class);

    /**
     * Only one origin may be set per deployment.
     */
    private static String origin = null;

    /**
     * Set the Access-Control-Allow-Origin to be used across all requests.
     *
     * @param origin new value for Access-Control-Allow-Origin. Must include protocol, e.g.
     *        http://localhost.
     */
    public void setOrigin(final String origin) {
        if (StringUtils.isEmpty(origin)) {
            throw new IllegalArgumentException("Access-Control-Allow-Origin must not be null or an empty string");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting Access-Control-Allow-Origin to " + origin);
        }

        CorsFilter.origin = origin;
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        if (origin == null) {
            LOG.warn("CORS origin has not been set - filter disabled");
        } else {
            String requestOrigin = request.getHeader("Origin");
            String responseOrigin = requestOrigin == null ? origin : requestOrigin;
            response.setHeader("Access-Control-Allow-Origin", responseOrigin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            // If it's a pre-flight check then provide more information.
            if (request.getHeader("Access-Control-Request-Method") != null) {
                response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
                response.setHeader("Access-Control-Allow-Headers",
                        "Accept, Cache-Control, Content-Type, If-Modified-Since, Keep-Alive, Origin, User-Agent, X-Requested-With");
            }

            response.setHeader("Access-Control-Max-Age", "3600");
        }

        MDC.put("sessionId", request.getSession() != null ? request.getSession().getId() : "");

        chain.doFilter(request, response);
    }
}
