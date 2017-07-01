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
import org.apache.flume.Event;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * A Flume Interceptor that counts objects within an {@link Event} in a sliding window. See
 * {@link RollingCounters}.
 * <p>
 * For example: counting how many times a hashtag has appeared in twitter status updates in the past
 * 30 minutes. In this case, the hashtags are extracted from the tweet, their counts incremented,
 * and their new totals added to the event's header.
 * <p>
 * If the sliding window length is set to 30 minutes and the following tweet was seen 50 times in
 * the past 30 minutes then the emitted event would contain the following headers:
 *
 * <pre>
 * {@code
 * Event header: {"#Hadoop":"50","#BigData":"50"}
 * Event body: {"text":"Follow @ClouderaEng for technical posts, updates, and resources. #Hadoop #BigData"}
 * </pre>
 *
 * For a more scalable approach, the counts should be collected by another source, which can emit
 * them as seperate events that can be multiplexed and routed downstream. See
 * {@link PeriodicEmissionSource}
 * @param <T>
 */
public abstract class RollingCountInterceptor<T> implements AnalyticInterceptor {
    public static final String NUM_BUCKETS = "numBuckets";
    public static final String WINDOW_LEN_SEC = "windowLenSec";
    private static final Logger LOG = Logger.getLogger(RollingCountInterceptor.class);
    private final RollingCounters<T> counters;

    public RollingCountInterceptor(int numBuckets, int windowLenSec) {
        this.counters = new RollingCounters<T>(numBuckets, windowLenSec);
        LOG.info(String.format("Initialising RollingCountInterceptor: buckets=%d, "
                + "window length=%d sec", numBuckets, windowLenSec));
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        // no-op
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        // no-op
    }

    /** {@inheritDoc} */
    @Override
    public Event intercept(Event event) {
        List<T> objects = getObjectsToCount(event);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Identified %d objects to count from event: %s", objects.size(),
                    objects));
        }

        if (!objects.isEmpty()) {
            Map<String, String> headers = event.getHeaders();
            for (T obj : objects) {
                long total = counters.incrementCount(obj);
                headers.put(String.valueOf(obj), String.valueOf(total));
                System.out.println(String.format("Added counter to event header: %s=%d", obj.toString(), total));
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Added counter to event header: %s=%d", obj.toString(), total));
                }
            }
        }
        return event;
    }

    /** {@inheritDoc} */
    @Override
    public List<Event> intercept(List<Event> events) {
        List<Event> intercepted = Lists.newArrayListWithCapacity(events.size());
        for (Event e : events) {
            intercepted.add(intercept(e));
        }
        return intercepted;
    }

    /**
     * Gets the current objects and their totals
     * @return Map of objects to totals
     */
    public Map<T, Long> getCounters() {
        return counters.getCounters();
    }

    /**
     * Extracts the objects to count from the given event
     * @param event
     * @return The objects to count
     */
    public abstract List<T> getObjectsToCount(Event event);
}
