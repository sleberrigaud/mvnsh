/*
 * Copyright (C) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sonatype.maven.shell.nexus.internal.wink;

import org.apache.wink.client.ClientRequest;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.handlers.ClientHandler;
import org.apache.wink.client.handlers.HandlerContext;
import org.slf4j.Logger;
import org.sonatype.gshell.util.PrintBuffer;

import java.util.List;
import java.util.Map;

/**
 * A logging handler.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 0.9
 */
public class LoggingHandler
    implements ClientHandler
{
    private static final String NOTIFICATION_PREFIX = "* ";

    private static final String REQUEST_PREFIX = "> ";

    private static final String RESPONSE_PREFIX = "< ";

    private final Logger logger;

    private final boolean trace;

    private long id = 0;

    public LoggingHandler(final Logger logger, final boolean trace) {
        assert logger != null;
        this.logger = logger;
        this.trace = trace;
    }

    private void log(final PrintBuffer buff) {
        if (trace) {
            logger.trace(buff.toString());
        }
        else {
            logger.debug(buff.toString());
        }
    }

    private PrintBuffer prefixId(final PrintBuffer buff, final long id) {
        buff.print(id);
        buff.print(" ");
        return buff;
    }

    public ClientResponse handle(final ClientRequest request, final HandlerContext context) throws Exception {
        assert request != null;
        assert context != null;

        long id = ++this.id;
        logRequest(id, request);
        ClientResponse response = context.doChain(request);
        logResponse(id, response);

        return response;
    }

    private void logRequest(final long id, final ClientRequest request) {
        assert request != null;

        PrintBuffer buff = new PrintBuffer();

        prefixId(buff, id).append(NOTIFICATION_PREFIX).println("Client out-bound request");
        prefixId(buff, id).append(REQUEST_PREFIX).append(request.getMethod()).append(" ").println(request.getURI().toASCIIString());

        for (Map.Entry<String, List<String>> e : request.getHeaders().entrySet()) {
            String header = e.getKey();
            for (Object value : e.getValue()) {
                prefixId(buff, id).append(REQUEST_PREFIX).append(header).append(": ").println(value);
            }
        }
        prefixId(buff, id).println(REQUEST_PREFIX);

        Object entity = request.getEntity();
        if (entity != null) {
            buff.println(entity);
        }

        log(buff);
    }

    private void logResponse(final long id, final ClientResponse response) {
        assert response != null;

        PrintBuffer buff = new PrintBuffer();

        prefixId(buff, id).append(NOTIFICATION_PREFIX).println("Client in-bound response");
        prefixId(buff, id).append(RESPONSE_PREFIX).println(response.getStatusCode());

        for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
            String header = e.getKey();
            for (String value : e.getValue()) {
                prefixId(buff, id).append(RESPONSE_PREFIX).append(header).append(": ").println(value);
            }
        }
        prefixId(buff, id).println(RESPONSE_PREFIX);

        String entity = response.getEntity(String.class);
        if (entity != null && entity.length() != 0) {
            buff.println(entity);
        }

        log(buff);
    }
}