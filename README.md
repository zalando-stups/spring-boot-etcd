# spring-boot-etcd

[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/zalando-boot-etcd-starter.svg)](https://github.com/zalando/spring-boot-etcd)
[![Build Status](https://travis-ci.org/zalando/spring-boot-etcd.svg?branch=master)](https://travis-ci.org/zalando/spring-boot-etcd)

## Purpose

Spring-boot-etcd is a [Spring Boot](http://projects.spring.io/spring-boot/) library that provides an [etcd](https://github.com/coreos/etcd) client to access and manage key-value pairs stored in an etcd cluster. It's useful out-of-the-box.

### Spring-boot-etcd Features

- provides an interface that includes all the functions featured by the etcd API v2
- uses either a list of addresses or a DNS SRV record to help you discover the nodes of your etcd cluster
- includes an automatic update mechanism so that the client can easily connect to nodes in the etcd cluster
- provides auto-configuration so that you can get started without having to write any code

### Inspiration

We created zalando-boot-etcd after discovering that current implementations of the etcd client API don’t provide an automatic update mechanism, or use other libraries like [Netty](http://netty.io/) to communicate with the etcd cluster. Also, these implementations are not compatible the version of Netty used by the Cassandra driver.

### Getting Started

Use the following Maven dependency to add this library:

    <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>zalando-boot-etcd-starter</artifactId>
      <version>RELEASE</version>
    </dependency>

You can find the latest version at [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.zalando%22%20a%3A%22zalando-boot-etcd-starter%22).

### Configuration

Configure one of these: 

- the addresses of at least one etcd node, OR
- the DNS SRV record name for auto-discovery

Do: 

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

This project accepts contributions from the open-source community, including bug fixes and feature adds.

Before making a contribution, please let us know by posting a comment to the relevant issue. And if you would like to propose a new feature, do start a new issue explaining the feature you’d like to contribute.

## Licensing
The MIT License (MIT)

Copyright (c) 2015 Compose, Zalando SE

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
