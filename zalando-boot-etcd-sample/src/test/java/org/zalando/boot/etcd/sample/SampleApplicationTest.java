package org.zalando.boot.etcd.sample;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.boot.etcd.EtcdClient;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleApplicationTest {

	@Autowired
	private EtcdClient etcdClient;
	
	@Test
	public void contextLoads() {
		Assert.assertNotNull(etcdClient);
	}

}
