package com.mercadolibre.flow.control.tool.client.config;

import static com.mercadolibre.flow.control.tool.client.config.RestPool.BACKLOG;
import static com.mercadolibre.flow.control.tool.client.config.RestPool.PLANNING_MODEL_API;

import com.mercadolibre.restclient.MeliRESTPool;
import com.mercadolibre.restclient.MeliRestClient;
import com.mercadolibre.restclient.RESTPool;
import com.mercadolibre.restclient.cache.local.RESTLocalCache;
import com.mercadolibre.restclient.interceptor.AddTimeInterceptor;
import com.mercadolibre.restclient.retry.SimpleRetryStrategy;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Create the Rest Client using the given properties and the RestPool names.
 */
@AllArgsConstructor
@Configuration
@EnableConfigurationProperties({
    RestClientConfig.BacklogApiClientProperties.class,
    RestClientConfig.PlanningModelApiClientProperties.class
})

public class RestClientConfig {

  private BacklogApiClientProperties backlogApiClientProperties;
  private PlanningModelApiClientProperties planningModelApiClientProperties;

  /**
   * Tries to build a MeLiRestClient with required REST pool.
   *
   * @return meli REST Client
   * @throws IOException when it fails for any reason.
   */
  @Bean
  public MeliRestClient restClient() throws IOException {
    return MeliRestClient
        .builder()
        .withPool(
            restPool(BACKLOG.name(), backlogApiClientProperties),
            restPool(PLANNING_MODEL_API.name(), planningModelApiClientProperties)
        )
        .build();
  }

  /**
   * Build a RESTPool with given name and properties.
   *
   * @param name       name for pooling
   * @param properties properties to be applied
   * @return meli RESTPool
   */
  private RESTPool restPool(final String name,
                            final RestClientProperties properties
  ) {

    RESTPool.Builder restPoolBuilder = MeliRESTPool.builder()
        .withName(name)
        .withBaseURL(properties.getBaseUrl())
        .withMaxPoolWait(properties.getMaxPoolWait())
        .withConnectionTimeout(properties.getConnectionTimeout())
        .withSocketTimeout(properties.getSocketTimeout())
        .withMaxTotal(properties.getMaxTotal())
        .withMaxPerRoute(properties.getMaxTotal())
        .withWorkerThreads(properties.getWorkerThreads())
        .withMaxIdleTime(properties.getMaxIdleTime())
        .withRetryStrategy(new SimpleRetryStrategy(
            properties.getMaxRetries(),
            properties.getRetriesDelay()))
        .withValidationOnInactivity(properties.getValidationOnInactivity())
        .addInterceptorLast(AddTimeInterceptor.INSTANCE)
        .addInterceptorLast(RestClientLoggingInterceptor.INSTANCE);

    if (properties.getCacheSize() > 0) {
      restPoolBuilder = restPoolBuilder.withCache(
          new RESTLocalCache(name + "-cache", properties.getCacheSize()));
    }

    return restPoolBuilder.build();
  }

  /**
   * Binds rest client properties to BacklogApiClient from configuration file.
   */
  @ConfigurationProperties("restclient.pool.backlog")
  public static class BacklogApiClientProperties extends RestClientProperties {
  }

  @ConfigurationProperties("restclient.pool.planning-model-api")
  public static class PlanningModelApiClientProperties extends RestClientProperties {
  }
}
