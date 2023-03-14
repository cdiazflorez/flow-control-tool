package com.mercadolibre.flow.control.tool.client.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base RestClientProperties for Rest Client.
 */
@Data
@NoArgsConstructor
public class RestClientProperties {

  private String baseUrl = "";

  private long maxPoolWait = 200;

  private long connectionTimeout = 200;

  private long socketTimeout = 2000;

  private int maxTotal = 20;

  private int workerThreads = Runtime.getRuntime().availableProcessors();

  private long maxIdleTime = 5000;

  private int maxRetries = 1;

  private long retriesDelay = 100;

  private int cacheSize;

  private int validationOnInactivity = -1;

  private long poolingInterval = 1000;

  private boolean connectionMetrics = true;
}
