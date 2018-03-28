# SKIL Benchmarking Suite
A suite of tools focused on benchmarking performance of the SKIL platform

## Benchmarking the SKIL Model Server

The overall goal is to use a series of architectures (MLP, CNN, LSTM) at different parameter sizes to collect REST inference response times to build a chart similar to:

https://www.mysql.com/why-mysql/benchmarks/

We define this metric as "Inferences Per Second" (IPS), similar in nature to a RDBM "Queries Per Second" (QPS). We define this term in the spirit of defining clear and reproducible metrics to communicate a true production value to the customer.

We use an untrained model because the actual parameter values are orthogonal to the time it takes activations to travel through a network or what the REST response overhead measures to be.

We provide a client for each architecture and then a notebook to create each untrained neural network and push it into the SKIL model server.

## Application Architecture Topology

There are multiple ways to look at SKIL model server performance. Below detail how each one is setup.
1. single application server, single SKIL model server instance
2. multiple application servers, single SKIL model server instance
3. multiple application servers, multiple SKIL model servers (scale-out mode)

### Single App Server, Single Model Server

For this intial test we're simulating a 3-tier application architecture (client, app server, SKIL Model Server) where a single application server queries a single SKIL model server as web browser client requests come in. Our client in this test simulates load by acting as a pseudo-application server in this context.

### Multi App Server, Single Model Server

* todo

### Multi App Server, Multi Model Server

* todo

## Network Architectures

* MLP
   * Notebooks to create MLP models for parameters of { 10k, 100k, 1MM, 10MM }
* CNN
   * ResNet - [ todo ]
* LSTM

## Running the Model Creation Notebooks

* The model creation notebooks live in the notebook folder: https://github.com/SkymindIO/SKILBenchmarkSuite/tree/master/notebooks

### Running the Client Benchmarking Code

* [ todo ]

## Hardware Used for the Tests

* [ todo ]
