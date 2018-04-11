# SKIL Benchmarking Suite

Tools for calculating throughput on a SKIL deployment server. The most accepted metric for calculating throughput is
TPS, also known as "requests per second". These benchmarks have been designed to be comparable to both
[tensorflow-serving-benchmark](https://github.com/dwyatte/tensorflow-serving-benchmark) and realistic scenarios where
full round-trip time is necessary to measure.

The round-trip time includes request creation, inference time, response and JSON deserialization.

## Benchmarking the SKIL Model Server

The overall goal is to use a series of architectures (MLP, CNN, LSTM) at different parameter sizes to collect REST inference response times to build a chart similar to:

https://www.mysql.com/why-mysql/benchmarks/

We define this metric as "Transactions Per Second" (TPS), similar in nature to a RDBM "Queries Per Second" (QPS). We define this term in the spirit of defining clear and reproducible metrics to communicate a true production value to the customer.

We use an untrained model because the actual parameter values are orthogonal to the time it takes activations to travel through a network or what the REST response overhead measures to be.

We provide a client for each architecture and then a notebook to create each untrained neural network and push it into the SKIL model server.

## Application Architecture Topology

There are multiple ways to look at SKIL model server performance. Below detail how each one is setup.
1. single application server, single SKIL model server instance
2. multiple application servers, single SKIL model server instance
3. multiple application servers, multiple SKIL model servers (scale-out mode)

### Single App Server, Single Model Server

For this intial test we're simulating a 3-tier application architecture (client, app server, SKIL Model Server) where a single application server queries a single SKIL model server as web browser client requests come in. Our client in this test simulates load by acting as a pseudo-application server in this context.

## Network Architectures

* Default
    * Single layer network with identity activation function. Matches [tensorflow-serving-benchmark](https://github.com/dwyatte/tensorflow-serving-benchmark) architecture.
* MLP
    * Notebooks to create MLP models for parameters of { 10k, 100k, 1MM, 10MM }.

## Running the Model Creation Notebooks

* The model creation notebooks live in the notebook folder: https://github.com/SkymindIO/SKILBenchmarkSuite/tree/master/notebooks

### Running the Client Benchmarking Code

Currently the client is meant to simulate a basic single application server calling a single SKIL model server.

To test the client:

```
git clone https://github.com/skymindio/SKILBenchmarkSuite.git
mvn package
```

To run the client:

`java -jar ./target/SKILBenchmarkSuite-1.0-SNAPSHOT.jar --endpoint http://localhost:9008/endpoints/skil_benchmarks_deploy/model/benchmarks100kmodel/default/ --number_calls 4000 --concurrency 12`

where the endpoint parameter is where you created the model in the model server via the notebook in the notebook/ subdirectory. You can also configure how many calls will be performed from the test client and averaged together. Client will report something like:

`Average inference round trip (100 trips total) took: 160 ms`


## Known Results

| Hardware | Concurrency | Average Roundtrip  |  Throughput |
|---|---|---|---|
|  Intel i7-6700K | 12  | 2 ms  | 5180.915407308458 TPS  |
