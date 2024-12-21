# Apache Flink Table API

## What is the Table API?

The **Table API** is a high-level API in Flink designed for working with structured data in both **batch** and **streaming** environments. It allows you to perform SQL-like queries and operations on data tables (collections of rows and columns). This abstraction is particularly helpful for users who prefer working with data in tabular form rather than dealing with low-level streams or datasets directly.

### Why Use the Table API?

- **Declarative Querying**: You can write SQL-like queries to process data without worrying about the underlying complexity of stream or batch processing.
- **Unified API**: Works for both stream and batch processing, allowing you to write consistent code for both types of workloads.
- **Interoperability**: The Table API allows you to use SQL and connect with other systems like relational databases, message queues, etc.
- **Optimized Execution**: Flink optimizes the execution of queries for performance, even for large datasets and high throughput.

## Core Concepts of the Table API

1. **Table**:
   A Table represents a stream or a batch of data, similar to a table in a relational database. It has rows (records) and columns (attributes).

2. **Table Environment**:
   The `TableEnvironment` is the central interface to interact with the Table API. It is used to create tables, register data sources, and execute queries.

3. **Table API vs. SQL API**:
   The Table API is a Java/Scala API, and the SQL API allows you to use SQL queries. Both are designed for the same underlying system, so you can often switch between them based on your preference.

## Basic Operations with the Table API

### 1. Setting up the TableEnvironment

The first step is to set up a `TableEnvironment`, which acts as the main entry point for interacting with the Table API.

```java
TableEnvironment tableEnv = TableEnvironment.create(EnvironmentSettings.newInstance().inStreamingMode().build());
```

click the link for more information
[Table API](https://nightlies.apache.org/flink/flink-docs-release-1.17/docs/dev/table/overview/)

![evening_work_setup Picture from www.stockcake.com](../../../images/evening_work_setup.jpg)
