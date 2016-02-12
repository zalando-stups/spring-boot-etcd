/*
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.boot.etcd.autoconfig;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.zalando.boot.etcd.EtcdClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The auto configuration of the etcd client usign either an initial list of
 * locations or a service name for DNS discovery.
 */
@Configuration
@EnableConfigurationProperties(EtcdClientProperties.class)
@ConditionalOnClass(ObjectMapper.class)
@ConditionalOnProperty(prefix = "zalando.etcd", name = "enabled", matchIfMissing = true)
public class EtcdClientAutoConfiguration {

	@Configuration
	@ConditionalOnProperty(prefix = "zalando.etcd", name = "location")
	protected static class StaticDiscoveryConfiguration {

		@Autowired
		private EtcdClientProperties properties = new EtcdClientProperties();

		@Bean
		public EtcdClient etcdService() {
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
	protected static class DnsDiscoveryConfiguration {

		@Autowired
		private EtcdClientProperties properties = new EtcdClientProperties();

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
		public EtcdClient etcdService() throws NamingException {
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
