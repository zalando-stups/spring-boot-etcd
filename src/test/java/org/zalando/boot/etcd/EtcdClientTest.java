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
package org.zalando.boot.etcd;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

/**
 *
 */
public class EtcdClientTest {

	/**
	 * client
	 */
	private EtcdClient client = new EtcdClient("http://localhost:2379");

	/**
	 * server
	 */
	private MockRestServiceServer server;

	@Before
	public void before() throws Exception {
		RestTemplate template = new RestTemplate();
		server = MockRestServiceServer.createServer(template);

		ReflectionTestUtils.setField(client, "template", template);
	}

	@Test
	public void get() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_get.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.get("sample");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test(expected = EtcdException.class)
	public void getWithResourceAccessException() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND)
						.contentType(MediaType.APPLICATION_JSON)
						.body(new ClassPathResource("EtcdClientTest_get.json")));

		try {
			client.get("sample");
		}
		finally {
			server.verify();
		}
	}

	@Test
	public void getRecursive() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?recursive=true"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_get.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.get("sample", true);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void put() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_set.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.put("sample", "Hello world");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void putWithSetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl=60"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_set.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.put("sample", "Hello world", 60);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void putWithUnsetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl="))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_set.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.put("sample", "Hello world", -1);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void delete() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.delete("sample");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void create() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.create("sample", "Hello world");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevExist() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?prevExist=false"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", false);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevExistAndSetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl=60&prevExist=false"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", 60, false);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevExistAndUnsetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl=&prevExist=false"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", -1, false);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevIndex() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?prevIndex=2"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", 2);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevIndexAndSetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl=60&prevIndex=2"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", 60, 2);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevIndexAndUnsetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?ttl=&prevIndex=2"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", -1, 2);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevValue() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?prevValue=Hello%20etcd"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world",
				"Hello etcd");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevValueAndSetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers.requestTo(
				"http://localhost:2379/v2/keys/sample?ttl=60&prevValue=Hello%20etcd"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", 60,
				"Hello etcd");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndSwapWithPrevValueAndUnsetTtl() throws EtcdException {
		server.expect(MockRestRequestMatchers.requestTo(
				"http://localhost:2379/v2/keys/sample?ttl=&prevValue=Hello%20etcd"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("value=Hello+world"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndSwap("sample", "Hello world", -1,
				"Hello etcd");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndDeleteWithPrevIndex() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?prevIndex=3"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndDelete("sample", 3);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void compareAndDeleteWithPrevValue() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?prevValue=Hello%20etcd"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.compareAndDelete("sample", "Hello etcd");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void putDir() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("dir=true"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.putDir("sample");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void putDirWithSetTtl() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/keys/sample"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.PUT))
				.andExpect(MockRestRequestMatchers.content()
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(MockRestRequestMatchers.content().string("dir=true&ttl=60"))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.putDir("sample", 60);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void deleteDir() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?dir=true"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.deleteDir("sample");
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void deleteDirWithRecursive() throws EtcdException {
		server.expect(MockRestRequestMatchers
				.requestTo("http://localhost:2379/v2/keys/sample?recursive=true"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdResponse response = client.deleteDir("sample", true);
		Assert.assertNotNull("response", response);

		server.verify();
	}

	@Test
	public void listMembers() throws EtcdException {
		server.expect(
				MockRestRequestMatchers.requestTo("http://localhost:2379/v2/members"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withSuccess(
						new ClassPathResource("EtcdClientTest_delete.json"),
						MediaType.APPLICATION_JSON));

		EtcdMemberResponse response = client.listMembers();
		Assert.assertNotNull("response", response);

		server.verify();
	}
}
