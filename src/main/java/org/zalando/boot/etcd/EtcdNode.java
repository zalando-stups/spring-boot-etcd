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

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The etcd node object for keyspace operations as described in the client API.
 * 
 * @see https://coreos.com/etcd/docs/2.1.0/api.html
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtcdNode {

    /**
     * key.
     */
    private String key;

    /**
     * value.
     */
    private String value;

    /**
     * time to live.
     */
    private Long ttl;

    /**
     * indicates whether the node is a directory node.
     */
    private boolean dir = false;

    /**
     * created index.
     */
    private int createdIndex;

    /**
     * modified index.
     */
    private int modifiedIndex;

    /**
     * expiration.
     */
    private Date expiration;

    /**
     * child nodes.
     */
    private List<EtcdNode> nodes;

}
