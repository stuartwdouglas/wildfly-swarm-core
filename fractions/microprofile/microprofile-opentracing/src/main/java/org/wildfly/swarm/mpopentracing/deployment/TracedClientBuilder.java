/**
 * Copyright 2015-2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.mpopentracing.deployment;

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import java.util.concurrent.Executors;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * {@link javax.ws.rs.client.ClientBuilder} with installed tracing components.
 *
 * @author Pavol Loffay
 */
public class TracedClientBuilder extends ResteasyClientBuilder {

  private final Tracer tracer;

  public TracedClientBuilder() {
    Instance<Tracer> tracerInstance = CDI.current().select(Tracer.class);
    this.tracer = tracerInstance.get();
    super.register(new ClientTracingFeature.Builder(tracer)
        .build());
  }

  @Override
  public ResteasyClient build() {
    if (asyncExecutor == null) {
      cleanupExecutor = true;
      asyncExecutor = Executors.newFixedThreadPool(10);
    }
    this.asyncExecutor = new TracedExecutorService(asyncExecutor, tracer);
    return super.build();
  }
}
