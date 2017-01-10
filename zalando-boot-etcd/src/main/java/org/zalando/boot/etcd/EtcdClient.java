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
package org.zalando.boot.etcd;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A service that encapsulates the communication with an etcd cluster.
 * 
 * @see <a href="https://coreos.com/etcd/docs/2.1.0/api.html">https://coreos.com
 *      /etcd/docs/2.1.0/api.html</a>
 */
@Slf4j
public class EtcdClient implements InitializingBean, DisposableBean {

	/**
	 * base path
	 */
	private static final String BASE_PATH = "{location}/v2";

	/**
	 * key space containing all nodes with key-value pairs
	 */
	private static final String KEYSPACE = BASE_PATH + "/keys";

	/**
	 * member space
	 */
	private static final String MEMBERSPACE = BASE_PATH + "/members";

	/**
	 * request converter
	 */
	private AllEncompassingFormHttpMessageConverter requestConverter = new AllEncompassingFormHttpMessageConverter();

	/**
	 * response converter
	 */
	private MappingJackson2HttpMessageConverter responseConverter = new MappingJackson2HttpMessageConverter();

	/**
	 * request factory
	 */
	@Getter
	@Setter
	private ClientHttpRequestFactory requestFactory;

	/**
	 * template.
	 */
	private RestTemplate template;

	/**
	 * number of retries
	 */
	@Getter
	@Setter
	private int retryCount = 0;

	/**
	 * duration of retries
	 */
	@Getter
	@Setter
	private int retryDuration = 0;

	/**
	 * locations
	 */
	private String[] locations;

	/**
	 * indicates whether the location updater is enabled
	 */
	private boolean locationUpdaterEnabled = true;

	/**
	 * current location
	 */
	private int locationIndex = 0;

	/**
	 * location updater
	 */
	private ScheduledExecutorService locationUpdater = Executors.newScheduledThreadPool(1, new ThreadFactory() {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "etcd-location-updater");
			t.setDaemon(true);
			return t;
		}
	});

	/**
	 * Creates a new EtcdClient.
	 */
	public EtcdClient() {
		super();
	}

	/**
	 * Creates a new EtcdClient with the given location.
	 * 
	 * @param location
	 *            the location
	 */
	public EtcdClient(String location) {
		this.locations = new String[] { location };
	}

	/**
	 * Creates a new EtcdClient with the given locations.
	 * 
	 * @param locations
	 *            the locations
	 */
	public EtcdClient(String[] locations) {
		this.locations = locations;
	}

	public boolean isLocationUpdaterEnabled() {
		return locationUpdaterEnabled;
	}

	public void setLocationUpdaterEnabled(boolean value) {
		this.locationUpdaterEnabled = value;
	}

	/**
	 * @param value
	 *            the locations
	 */
	public void setLocations(String[] value) {
		this.locations = value == null ? new String[0] : value;
	}

	/**
	 * @return the locations
	 */
	public String[] getLocations() {
		return locations;
	}

	/**
	 * @return the current location
	 */
	protected String getCurrentLocation() {
		return locations[locationIndex];
	}

	/**
	 * Returns the node with the given key from etcd.
	 * 
	 * @param key
	 *            the node's key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse get(String key) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		return execute(builder, HttpMethod.GET, null, EtcdResponse.class);
	}

	/**
	 * Returns the node with the given key from etcd.
	 * 
	 * @param key
	 *            the node's key
	 * @param recursive
	 *            <code>true</code> if child nodes should be returned,
	 *            <code>false</code> otherwise
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse get(String key, boolean recursive) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("recursive", recursive);

		return execute(builder, HttpMethod.GET, null, EtcdResponse.class);
	}

	/**
	 * Sets the value of the node with the given key in etcd. Any previously
	 * existing key-value pair is returned as prevNode in the etcd response.
	 * 
	 * @param key
	 *            the node's key
	 * @param value
	 *            the node's value
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse put(final String key, final String value) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Sets the value of the node with the given key in etcd.
	 * 
	 * @param key
	 *            the node's key
	 * @param value
	 *            the node's value
	 * @param ttl
	 *            the node's time-to-live or <code>-1</code> to unset existing
	 *            ttl
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse put(String key, String value, int ttl) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("ttl", ttl == -1 ? "" : ttl);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Deletes the node with the given key from etcd.
	 * 
	 * @param key
	 *            the node's key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse delete(final String key) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		return execute(builder, HttpMethod.DELETE, null, EtcdResponse.class);
	}

	/**
	 * Creates a new node with the given key-value pair under the node with the
	 * given key.
	 * 
	 * @param key
	 *            the directory node's key
	 * @param value
	 *            the value of the created node
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse create(final String key, final String value) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.POST, payload, EtcdResponse.class);
	}

	/**
	 * Atomically creates or updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param prevExist
	 *            <code>true</code> if the existing node should be updated,
	 *            <code>false</code> of the node should be created
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(final String key, final String value, boolean prevExist) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("prevExist", prevExist);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically creates or updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param ttl
	 *            the time-to-live
	 * @param prevExist
	 *            <code>true</code> if the existing node should be updated,
	 *            <code>false</code> of the node should be created
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(final String key, final String value, int ttl, boolean prevExist)
			throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("ttl", ttl == -1 ? "" : ttl);
		builder.queryParam("prevExist", prevExist);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param prevIndex
	 *            the modified index of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(String key, String value, int prevIndex) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("prevIndex", prevIndex);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param ttl
	 *            the time-to-live
	 * @param prevIndex
	 *            the modified index of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(String key, String value, int ttl, int prevIndex) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("ttl", ttl == -1 ? "" : ttl);
		builder.queryParam("prevIndex", prevIndex);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param prevValue
	 *            the previous value of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(String key, String value, String prevValue) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("prevValue", prevValue);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically updates a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param ttl
	 *            the time-to-live
	 * @param prevValue
	 *            the previous value of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndSwap(String key, String value, int ttl, String prevValue) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("ttl", ttl == -1 ? "" : ttl);
		builder.queryParam("prevValue", prevValue);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("value", value);

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Atomically deletes a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param prevIndex
	 *            the modified index of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndDelete(final String key, int prevIndex) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("prevIndex", prevIndex);

		return execute(builder, HttpMethod.DELETE, null, EtcdResponse.class);
	}

	/**
	 * Atomically deletes a key-value pair in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param prevValue
	 *            the previous value of the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse compareAndDelete(final String key, String prevValue) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("prevValue", prevValue);

		return execute(builder, HttpMethod.DELETE, null, EtcdResponse.class);
	}

	/**
	 * Creates a directory node in etcd.
	 * 
	 * @param key
	 *            the key
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse putDir(final String key) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("dir", "true");

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	/**
	 * Creates a directory node in etcd.
	 * 
	 * @param key
	 *            the key
	 * @param ttl
	 *            the time-to-live
	 * @return the response from etcd with the node
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdResponse putDir(String key, int ttl) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>(1);
		payload.set("dir", "true");
		payload.set("ttl", ttl == -1 ? "" : String.valueOf(ttl));

		return execute(builder, HttpMethod.PUT, payload, EtcdResponse.class);
	}

	public EtcdResponse deleteDir(String key) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("dir", "true");

		return execute(builder, HttpMethod.DELETE, null, EtcdResponse.class);
	}

	public EtcdResponse deleteDir(String key, boolean recursive) throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(KEYSPACE);
		builder.pathSegment(key);
		builder.queryParam("recursive", recursive);

		return execute(builder, HttpMethod.DELETE, null, EtcdResponse.class);
	}

	/**
	 * Returns a representation of all members in the etcd cluster.
	 * 
	 * @return the members
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	public EtcdMemberResponse listMembers() throws EtcdException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(MEMBERSPACE);
		return execute(builder, HttpMethod.GET, null, EtcdMemberResponse.class);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.requestFactory == null) {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(1000);
			requestFactory.setReadTimeout(3000);
			this.requestFactory = requestFactory;
		}

		template = new RestTemplate(this.requestFactory);
		template.setMessageConverters(Arrays.asList(requestConverter, responseConverter));

		if (locationUpdaterEnabled) {
			Runnable worker = new Runnable() {
				@Override
				public void run() {
					updateMembers();
				}
			};
			locationUpdater.scheduleAtFixedRate(worker, 5000, 5000, TimeUnit.MILLISECONDS);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		locationUpdater.shutdownNow();
	}

	/**
	 * Updates the locations of the etcd cluster members.
	 */
	private void updateMembers() {
		try {
			List<String> locations = new ArrayList<String>();

			EtcdMemberResponse response = listMembers();
			EtcdMember[] members = response.getMembers();

			for (EtcdMember member : members) {
				String[] clientUrls = member.getClientURLs();
				if (clientUrls != null) {
					for (String clientUrl : clientUrls) {
						try {
							String version = template.getForObject(clientUrl + "/version", String.class);
							if (version == null) {
								locations.add(clientUrl);
							}
						} catch (RestClientException e) {
							log.debug("ignoring URI " + clientUrl + " because of error.", e);
						}
					}
				}
			}

			if (!locations.isEmpty()) {
				this.locations = locations.toArray(new String[locations.size()]);
			} else {
				log.debug("not updating locations because no location is found");
			}
		} catch (EtcdException e) {
			log.error("Could not update etcd cluster member.", e);
		}
	}

	/**
	 * Executes the given method on the given location using the given request
	 * data.
	 * 
	 * @param uri
	 *            the location
	 * @param method
	 *            the HTTP method
	 * @param requestData
	 *            the request data
	 * @return the etcd response
	 * @throws EtcdException
	 *             in case etcd returned an error
	 */
	private <T> T execute(UriComponentsBuilder uriTemplate, HttpMethod method,
			MultiValueMap<String, String> requestData, Class<T> responseType) throws EtcdException {
		long startTimeMillis = System.currentTimeMillis();
		int retry = -1;

		ResourceAccessException lastException = null;
		do {
			lastException = null;

			URI uri = uriTemplate.buildAndExpand(locations[locationIndex]).toUri();

			RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(requestData, null, method,
					uri);

			try {
				ResponseEntity<T> responseEntity = template.exchange(requestEntity, responseType);
				return responseEntity.getBody();
			} catch (HttpStatusCodeException e) {
				EtcdError error = null;
				try {
					error = responseConverter.getObjectMapper().readValue(e.getResponseBodyAsByteArray(),
							EtcdError.class);
				} catch (IOException ex) {
					error = null;
				}
				throw new EtcdException(error, "Failed to execute " + requestEntity + ".", e);
			} catch (ResourceAccessException e) {
				log.debug("Failed to execute " + requestEntity + ", retrying if possible.", e);

				if (locationIndex == locations.length - 1) {
					locationIndex = 0;
				} else {
					locationIndex++;
				}
				lastException = e;
			}
		} while (retry <= retryCount && System.currentTimeMillis() - startTimeMillis < retryDuration);

		if (lastException != null) {
			throw lastException;
		} else {
			return null;
		}
	}
}
