package io.eventuate.local.mysql.binlog;

import io.eventuate.local.db.log.test.util.AbstractDbLogBasedCdcKafkaPublisherTest;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MySqlBinlogCdcIntegrationTestConfiguration.class)
@IntegrationTest
public class MySQLCdcKafkaPublisherTest extends AbstractDbLogBasedCdcKafkaPublisherTest {
}
