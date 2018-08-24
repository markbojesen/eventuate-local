package io.eventuate.local.unified.cdc.pipeline.polling.configuration;

import io.eventuate.local.java.common.broker.DataProducerFactory;
import io.eventuate.local.unified.cdc.pipeline.common.factory.CdcPipelineFactory;
import io.eventuate.local.unified.cdc.pipeline.polling.factory.PollingCdcPipelineFactory;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PollingCdcPipelineFactoryConfiguration {
  @Bean("evenutateLocalPolling")
  public CdcPipelineFactory pollingCdcPipelineFactory(CuratorFramework curatorFramework,
                                                      DataProducerFactory dataProducerFactory) {

    return new PollingCdcPipelineFactory(curatorFramework, dataProducerFactory);
  }
}