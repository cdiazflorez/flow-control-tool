package com.mercadolibre.flow.control.tool;

import com.mercadolibre.flow.control.tool.util.ScopeUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** 
 * Main class for the App.
 */
@SpringBootApplication
public class FlowControlToolApplication {

  /** 
   * @param args command line arguments for the application.
   */
  public static void main(String[] args) {
    ScopeUtils.calculateScopeSuffix();
    new SpringApplicationBuilder(FlowControlToolApplication.class).registerShutdownHook(true).run(args);
  }

}
