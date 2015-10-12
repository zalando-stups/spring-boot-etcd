/*
 * 
 */
package org.zalando.boot.etcd.autoconfig;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.boot.etcd.EtcdClient;
import org.zalando.boot.etcd.SampleApplication;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleApplication.class)
public class EtcdClientAutoConfigurationTest {

	@Autowired
	private EtcdClient client;

	@Test
	public void testStartup() {
		Assert.assertNotNull(client);
		Assert.assertEquals("retry-count", 3, client.getRetryCount());
		Assert.assertEquals("retry-duration", 30000, client.getRetryDuration());
		Assert.assertArrayEquals("locations", new String[] { "http://localhost:2379" },
				client.getLocations());
	}
}
