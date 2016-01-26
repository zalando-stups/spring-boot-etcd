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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for the etcd client.
 */
@ConfigurationProperties(prefix = "zalando.etcd")
@Data
public class EtcdClientProperties {
	
	/**
	 * DNS SRV name used to lookup etcd nodes
	 */
	private String serviceName;
	
	/**
	 * location of etcd nodes
	 */
	private String[] location;

	/**
	 * indicates whether the etcd client is enabled
	 */
	private boolean enabled = true;

	/**
	 * the connect timeout for a request to the etcd cluster node
	 */
	private int connectTimeout = 1000;

	/**
	 * the read timeout for a request to the etcd cluster node
	 */
	private int readTimeout = 3000;

	/**
	 * number of retries the client should do before giving up
	 */
	private int retryCount = 0;

	/**
	 * maximum duration the client should retry before giving up
	 */
	private int retryDuration = 0;
	
	/**
	 * indicates whether location information should be updated using the members API
	 */
	private boolean updateLocations = true;

}
