# spring-boot-etcd

## Purpose

Spring-boot-etcd is a [Spring Boot](http://projects.spring.io/spring-boot/) library that provides an [etcd](https://github.com/coreos/etcd) client to access and manage key-value pairs stored in an etcd cluster. It's useful out-of-the-box.

### Spring-boot-etcd Features

- provides an interface that includes all the functions featured by the etcd API v2
- uses either a list of addresses or a DNS SRV record to help you discover the nodes of your etcd cluster
- includes an automatic update mechanism so that the client can easily connect to nodes in the etcd cluster
- provides auto-configuration so that you can get started without having to write any code

###Inspiration

We created spring-boot-etcd after discovering that current implementations of the etcd client API don’t provide an automatic update mechanism, or use other libraries like [Netty](http://netty.io/) to communicate with the etcd cluster. Also, these implementations are not compatible the version of Netty used by the Cassandra driver.

### Getting Started

Use the following Maven dependency to add this library:

    <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>spring-boot-etcd</artifactId>
      <version>1.4</version>
    </dependency>

You can find the latest version at [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.zalando%22%20a%3A%22spring-boot-etcd%22).

###Configuration

Configure one of these: 
- the addresses of at least one etcd node, OR
- the DNS SRV record name for auto-discovery


    zalando.etcd.location=http://etcd-cluster.example.org:2379
    zalando.etcd.serviceName=etcd-cluster.example.com

### Running It

Then, auto-wire the client into your code:

    @Autowired
    private EtcdClient etcdClient;

## Usage

Start an etcd cluster:

    docker run -d -p 2379:2379 -p 2380:2380 \
      --name etcd quay.io/coreos/etcd:v2.0.8 -name etcd0 \
      -advertise-client-urls http://10.170.0.10:2379,http://127.0.0.1:2379 \
      -listen-client-urls http://0.0.0.0:2379,http://127.0.0.1:2379 \
      -initial-advertise-peer-urls http://10.170.0.10:2380 \
      -listen-peer-urls http://0.0.0.0:2380 \
      -initial-cluster-token etcd-cluster \
      -initial-cluster etcd0=http://10.170.0.10:2380 \
      -initial-cluster-state new

Write a small Spring Boot application and add this project as a dependency. Use an autowired field of type EtcdService to inject the service into your bean. Call the methods to retrieve key-value pairs from etcd.

## Building

    mvn clean install

## Deployment

    mvn release:prepare release:perform

## Contributing

This project accepts contributions from the open-source community, including bug fixes and feature adds. Before submitting a major change or PR, please open an issue describing the change or addition you would like to make.

## License

Copyright 2015 Zalando SE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
