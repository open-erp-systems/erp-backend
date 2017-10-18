# ERP Backend

[![Build Status](https://travis-ci.org/leeks-and-dragons/leeks-and-dragons.svg?branch=master)](https://travis-ci.org/leeks-and-dragons/leeks-and-dragons) 
[![Coverage Status](https://coveralls.io/repos/github/open-erp-systems/erp-backend/badge.svg?branch=master)](https://coveralls.io/github/open-erp-systems/erp-backend?branch=master)
\
**Backend** of open source ERP (Enterprise-Resource-Planning) system, written in Java.\
\
**License**: Apache 2.0 License (Open Source)\
**Price**: free\
**Phase**: Concept / Planning Phase

## Goals of system

The **main goal** of this system is to provide an intuitive, easy to use ERP system for small and middle companies.\
Open ERP System should be free and very fast.\
Backend is Microservice-orientated and uses [vertx.io](http://vertx.io) for networking and distribution.\
It should be easy to **scale-out** (distributed system).\

Short Mindmap:
![Open ERP System](./docs/Management_Software.png)

## System Requirements

  - Java 9 (JRE)
  - Database:
      * MySQL (free)
      * Cassandra Support is planned
  - Caching
      * [Hazelcast](http://hazelcast.org) (Open Source - free)