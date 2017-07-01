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

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.Source;
import org.apache.flume.conf.Configurable;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.source.AbstractSource;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Flume Source that connects to an {@link AnalyticInterceptor} and periodically emits its results
 */
public class PeriodicEmissionSource extends AbstractSource implements EventDrivenSource,
        Configurable {

    private static final Logger LOG = Logger.getLogger(PeriodicEmissionSource.class);
    private static final String EMIT_FREQ_MS = "emitFreqMS";
    private static final String INTERCEPTOR_CLASS = "interceptorClass";
    private int emitFreqMS;
    private Class<?> interceptorClass;
    private ExecutorService service;

    /** {@inheritDoc} */
    @Override
    public void configure(Context context) {
        this.emitFreqMS = context.getInteger(EMIT_FREQ_MS);
        try {
            this.interceptorClass = Class.forName(context.getString(INTERCEPTOR_CLASS));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        if (!AnalyticInterceptor.class.isAssignableFrom(interceptorClass)) {
            throw new IllegalArgumentException(
                    "interceptorClass must implement the AnalyticInterceptor interface");
        }
        LOG.info(String.format(
                "Initializing PeriodicEmissionSource: emitFreqMS=%d, interceptorClass=%s", emitFreqMS,
                interceptorClass));
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void start() {
        service = Executors.newSingleThreadExecutor();
        Runnable handler = new PeriodicHandler(this, this.emitFreqMS, this.interceptorClass);
        service.execute(handler);
    }

    public static class PeriodicHandler implements Runnable {
        private Source source;
        private int emitFreqMS;
        private Class<? extends AnalyticInterceptor> interceptorClass;

        @SuppressWarnings("unchecked")
        public PeriodicHandler(Source source, int emitFreqMS, Class<?> interceptorClass) {
            this.source = source;
            this.emitFreqMS = emitFreqMS;
            this.interceptorClass = (Class<? extends AnalyticInterceptor>) interceptorClass;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            while (true) {
                sleep();
                Set<? extends Interceptor> interceptors =
                        InterceptorRegistry.getInstances(interceptorClass);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Emitting results for %d interceptors", interceptors.size()));
                }
                for (Interceptor i : interceptors) {
                    for (Event e : ((AnalyticInterceptor) i).getStatsEvents()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Emit: Header: %s, Body: %s", e.getHeaders(),
                                    new String(e.getBody())));
                        }
                        source.getChannelProcessor().processEvent(e);
                    }
                }
            }
        }

        private void sleep() {
            try {
                Thread.sleep(emitFreqMS);
            } catch (InterruptedException e) {
                LOG.error(e);
            }
        }
    }
}
