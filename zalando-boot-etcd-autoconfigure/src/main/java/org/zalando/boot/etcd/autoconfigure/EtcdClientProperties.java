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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration properties for the etcd client.
 */
@Data
@ConfigurationProperties(prefix = "zalando.etcd")
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
