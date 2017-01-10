/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Zalando SE
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.zalando.boot.etcd.autoconfigure;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.zalando.boot.etcd.EtcdClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The auto configuration of the etcd client using either an initial list of
 * locations or a service name for DNS discovery.
 */
@Configuration
@ConditionalOnProperty(prefix = "zalando.etcd", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(ObjectMapper.class)
@ConditionalOnMissingBean(EtcdClient.class)
public class EtcdClientAutoConfiguration {

	@Configuration
	@ConditionalOnProperty(prefix = "zalando.etcd", name = "location")
	@EnableConfigurationProperties(EtcdClientProperties.class)
	protected static class StaticDiscoveryConfiguration {

		@Autowired
		private EtcdClientProperties properties;

		@Bean
		public EtcdClient etcdClient() {
			EtcdClient client = new EtcdClient(properties.getLocation());

			client.setRetryCount(properties.getRetryCount());
			client.setRetryDuration(properties.getRetryDuration());
			client.setLocationUpdaterEnabled(properties.isUpdateLocations());

			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(properties.getConnectTimeout());
			requestFactory.setReadTimeout(properties.getReadTimeout());
			client.setRequestFactory(requestFactory);

			return client;
		}
	}

	@Configuration
	@ConditionalOnProperty(prefix = "zalando.etcd", name = "serviceName")
	@EnableConfigurationProperties(EtcdClientProperties.class)
	protected static class DnsDiscoveryConfiguration {

		@Autowired
		private EtcdClientProperties properties;

		private List<String> discoverNodes(String serviceName) throws NamingException {
			List<String> locations = new ArrayList<>();

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			env.put("java.naming.provider.url", "dns:");

			DirContext context = new InitialDirContext(env);
			Attributes attributes = context.getAttributes(serviceName, new String[] { "SRV" });
			for (NamingEnumeration<? extends Attribute> records = attributes.getAll(); records.hasMore();) {
				Attribute record = records.next();
				NamingEnumeration<String> values = (NamingEnumeration<String>) record.getAll();
				while (values.hasMore()) {
					String dns = values.next();
					String[] split = dns.split(" ");
					String host = split[3];
					if (host.endsWith(".")) {
						host = host.substring(0, host.length() - 1);
					}

					String location = "http://" + host + ":2379";
					locations.add(location);
				}
			}
			return locations;
		}

		@Bean
		public EtcdClient etcdClient() throws NamingException {
			List<String> locations = discoverNodes("_etcd-server._tcp." + properties.getServiceName());

			EtcdClient client = new EtcdClient(locations.get(0));

			client.setRetryCount(properties.getRetryCount());
			client.setRetryDuration(properties.getRetryDuration());
			client.setLocationUpdaterEnabled(properties.isUpdateLocations());

			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(properties.getConnectTimeout());
			requestFactory.setReadTimeout(properties.getReadTimeout());
			client.setRequestFactory(requestFactory);

			return client;
		}
	}
}
