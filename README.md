# spring-boot-etcd

## Purpose

This project provides a small etcd client to access and store configuration values in an etcd cluster.

## Download

Use the following maven dependency to add this library

    <dependency>
      <groupId>org.zalando</groupId>
      <artifactId>spring-boot-etcd</artifactId>
      <version>1.4</version>
    </dependency>

Then configure location or discovery domain in your application.properties

    zalando.etcd.location=http://etcd-cluster.example.org:2379
    zalando.etcd.serviceName=etcd-cluster.example.com

and use the etcd client in your service

    @Autowired
    private EtcdClient etcdClient;

## Usage

Start an etcd cluster

    docker run -d -p 2379:2379 -p 2380:2380 \
      --name etcd quay.io/coreos/etcd:v2.0.8 -name etcd0 \
      -advertise-client-urls http://10.170.0.10:2379,http://127.0.0.1:2379 \
      -listen-client-urls http://0.0.0.0:2379,http://127.0.0.1:2379 \
      -initial-advertise-peer-urls http://10.170.0.10:2380 \
      -listen-peer-urls http://0.0.0.0:2380 \
      -initial-cluster-token etcd-cluster \
      -initial-cluster etcd0=http://10.170.0.10:2380 \
      -initial-cluster-state new

Write a small spring boot application and add this project as dependency. Use an autowired field of type EtcdService to inject the service into your bean. Call the methods to retrieve key-value pairs from etcd.

## Build

    mvn clean install

## Deployment

    mvn release:prepare release:perform

## Contact

Please open an issue.

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
