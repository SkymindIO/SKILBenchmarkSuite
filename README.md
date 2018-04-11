# SKIL Benchmarking Suite

Tools for calculating throughput on a SKIL deployment server. The most accepted metric for calculating throughput is
TPS, also known as "requests per second". These benchmarks have been designed to be comparable to both
[tensorflow-serving-benchmark](https://github.com/dwyatte/tensorflow-serving-benchmark) and realistic scenarios where
full round-trip time is necessary to measure.

The round-trip time includes request creation, inference time, response and JSON deserialization.

## Known Results

| Context | Hardware | Model | Concurrency | Average Roundtrip  |  Throughput |
|---|---|---|---|---|---|
| Dedicated, SKIL Docker | Intel(R) Core(TM) i7-6850K CPU @ 3.60GHz | Default | 12  | 2 ms  | 5180.91  |
| Azure, SKIL Docker | Intel(R) Xeon(R) Platinum 8168 CPU @ 2.70GHz | Default | 12  | 2 ms  | 3515.81 TPS  |

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

To build the client:

```
git clone https://github.com/skymindio/SKILBenchmarkSuite.git
mvn clean compile assembly:single
```

To run the client:

`java -cp ./target/SKILBenchmarkSuite-1.0-SNAPSHOT-jar-with-dependencies.jar ai.skymind.BenchmarkRoundTrip --endint http://localhost:9008/endpoints/benchmark/model/benchmarkidentity/default/ --number_calls 4000 --concurrency 4`

where the endpoint parameter is where you created the model in the model server via the notebook in the notebook/ subdirectory. You can also configure how many calls will be performed from the test client and averaged together. Client will report something like:

`Average inference round trip (100 trips total) took: 160 ms`
