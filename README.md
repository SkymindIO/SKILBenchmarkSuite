# SKIL Benchmarking Suite
A suite of tools focused on benchmarking performance of the SKIL platform

## Benchmarking the SKIL Model Server

The overall goal is to use a series of architectures (MLP, CNN, LSTM) at different parameter sizes to collect REST inference response times to build a chart similar to:

https://www.mysql.com/why-mysql/benchmarks/

We define this metric as "Inferences Per Second" (IPS), similar in nature to a RDBM "Queries Per Second" (QPS). We define this term in the spirit of defining clear and reproducible metrics to communicate a true production value to the customer.

We use an untrained model because the actual parameter values are orthogonal to the time it takes activations to travel through a network or what the REST response overhead measures to be.

We provide a client for each architecture and then a notebook to create each untrained neural network and push it into the SKIL model server.

### Network Architectures

* MLP
   * Notebooks to create MLP models for parameters of { 10k, 100k, 1MM, 10MM }
* CNN
* LSTM

### Running the Model Creation Notebooks

* [ todo ]

### Running the Client Benchmarking Code

* [ todo ]
