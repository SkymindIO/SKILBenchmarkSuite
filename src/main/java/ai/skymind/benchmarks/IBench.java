package ai.skymind.benchmarks;

import com.beust.jcommander.Parameter;

public interface IBench {
    void run() throws Exception;
    void stop();

    public static class Args {
        @Parameter(names="--endpoint", description="Endpoint for classification", required=true)
        public String skilInferenceEndpoint = ""; // EXAMPLE: "http://localhost:9008/endpoints/yolo/model/yolo/default/";

        @Parameter(names="--number_calls", description="Number of client calls to measure", required=false)
        public int numberCalls = 8000;

        @Parameter(names="--concurrency", description="Number of concurrent calls", required=false)
        public int concurrency = 11;

        @Parameter(names="--array_shape", description="Shape of the input array", required=false)
        public String arrayShape = "1";
    }
}
