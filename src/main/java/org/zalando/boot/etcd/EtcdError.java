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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The etcd error object for keyspace operations as described in the client API.
 * 
 * @see <a href="https://coreos.com/etcd/docs/2.1.0/api.html">https://coreos.com
 *      /etcd/docs/2.1.0/api.html</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtcdError {

	/**
	 * error code
	 */
	private int errorCode;

	/**
	 * message
	 */
	private String message;

	/**
	 * cause
	 */
	private String cause;

	/**
	 * index
	 */
	private int index;
}
