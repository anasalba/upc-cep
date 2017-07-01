/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package upc.edu.cep.flume.statistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.interceptor.TimestampInterceptor;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Flume Interceptor that counts how many times a hashtag has appered in twitter status updates in
 * a sliding window.
 * <p>
 * See {@link RollingCountInterceptor} and {@link RollingCounters}.
 */
public class CounterInterceptor extends RollingCountInterceptor<String> {
    private static final Logger LOG = Logger.getLogger(CounterInterceptor.class);
    private static final String STATUS_UPDATE_FIELDNAME = "text";
    private static final String Delimiter = ";";
    private static final String HASHTAG = "#";
    private static final String SERVERS = "servers";
    private final JsonFactory jsonFactory = new JsonFactory();
    private Map<String, Integer> ruleServerNOMap;
    private Map<Integer, String> serverNOServerMap;
    private boolean[] serverFlags;

    public CounterInterceptor(int numBuckets, int windowLenSec, Map<String, Integer> ruleServerNOMap, Map<Integer, String> serverNOServerMap) {
        super(numBuckets, windowLenSec);

        this.ruleServerNOMap = ruleServerNOMap;
        this.serverNOServerMap = serverNOServerMap;
        this.serverFlags = new boolean[this.serverNOServerMap.size()];

    }

    public CounterInterceptor(int numBuckets, int windowLenSec) {
        super(numBuckets, windowLenSec);
    }


    private void flagsReset() {
        for (int i = 0; i < serverFlags.length; i++) {
            serverFlags[i] = false;
        }
    }

    @Override
    public void initialize() {
        InterceptorRegistry.register(CounterInterceptor.class, this);
    }

    @Override
    public void close() {
        InterceptorRegistry.deregister(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getObjectsToCount(Event event) {
        flagsReset();
        List<String> rns = Lists.newArrayList();
        String rules = event.getHeaders().get("Rules");
        if (rules != null && rules != "") {
            String[] ruleArr = rules.split(Delimiter);
            for (String rule : ruleArr) {
                rule = rule.trim();
                if (rule != "") {
                    rns.add(rule);
                    serverFlags[ruleServerNOMap.get(rule)] = true;
                }
            }
        }
        for (int i = 0; i < serverFlags.length; i++) {
            if (serverFlags[i]) {
                rns.add(serverNOServerMap.get(i));
            }
        }

        return rns;
    }

    @Override
    public List<Event> getStatsEvents() {
        List<Event> events = Lists.newArrayList();
        Map<String, Long> counters = getCounters();

        for (String obj : counters.keySet()) {
            Map<String, String> headers = Maps.newHashMap();
            headers.put(obj, String.valueOf(counters.get(obj)));
            headers.put(TimestampInterceptor.Constants.TIMESTAMP,
                    Long.toString(System.currentTimeMillis()));
            events.add(EventBuilder.withBody(new byte[0], headers));
        }

        return events;
    }

    /**
     * Builder which builds new instance of CounterInterceptor.
     */
    public static class Builder implements Interceptor.Builder {
        private int numBuckets;
        private int windowLenSec;
        private Map<String, Integer> ruleServerNOMap;
        private Map<Integer, String> serverNOServerMap;

        @Override
        public void configure(Context context) {
            this.numBuckets = context.getInteger(NUM_BUCKETS);
            this.windowLenSec = context.getInteger(WINDOW_LEN_SEC);
            ruleServerNOMap = new HashMap<>();
            serverNOServerMap = new HashMap<>();
            int counter = 0;

            String servers = context.getString(SERVERS);

            if (servers != null && servers != "") {
                String[] temp1 = servers.split(";");
                for (String server : temp1) {
                    server = server.trim();
                    if (server != "") {
                        serverNOServerMap.put(counter, server);
                        String rules = context.getString(server);
                        if (rules != null && rules != "") {
                            String[] temp2 = rules.split(";");
                            for (String rule : temp2) {
                                rule = rule.trim();
                                if (rule != "") {
                                    ruleServerNOMap.put(rule, counter);
                                }
                            }
                        }
                        counter++;
                    }
                }
            }

            System.out.println("rule server numbers: " + ruleServerNOMap);
            System.out.println("server numbers servers: " + serverNOServerMap);
        }

        @Override
        public Interceptor build() {
            return new CounterInterceptor(numBuckets, windowLenSec, ruleServerNOMap, serverNOServerMap);
        }

    }
}
