package ai.skymind;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.base64.Nd4jBase64;

import static org.nd4j.linalg.indexing.NDArrayIndex.interval;


import com.mashape.unirest.http.Unirest;

import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;


import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 * Benchmarks the total round trip for an inference request to SKIL and calculates total
 * transactions per second (TPS).
 *
 * @author Justin Long (crockpotveggies)
 *
 */
public class BenchmarkRoundTrip {

    private static class Args {
        @Parameter(names="--endpoint", description="Endpoint for classification", required=true)
        private String skilInferenceEndpoint = ""; // EXAMPLE: "http://localhost:9008/endpoints/yolo/model/yolo/default/";

        @Parameter(names="--number_calls", description="Number of client calls to measure", required=false)
        private int numberCalls = 8000;

        @Parameter(names="--concurrency", description="Number of concurrent calls", required=false)
        private int concurrency = 11;

        @Parameter(names="--array_shape", description="Shape of the input array", required=false)
        private String arrayShape = "1";
    }

    static Args args = new Args();
	String auth_token = null;
	String inputBase64 = null;
	long totalBenchTime = 0L;

    public BenchmarkRoundTrip() throws IOException {
        // get shape from launch args
        String[] dimensions = args.arrayShape.split(",");
        int[] shape = new int[dimensions.length];
        for(int i = 0; i < dimensions.length; i++)
            shape[i] = Integer.parseInt(dimensions[i]);

        INDArray input_array = Nd4j.create(shape);
        inputBase64 = Nd4jBase64.base64String( input_array );
    }

    public static void main( String[] args ) throws Exception {
  		BenchmarkRoundTrip m = new BenchmarkRoundTrip();
        JCommander.newBuilder()
                .addObject(BenchmarkRoundTrip.args)
                .build()
                .parse(args);

        m.runBenchmarks();
    }

    public void runBenchmarks() throws Exception, IOException {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);

    	System.out.println( "Running main benchmark routine..." ); 
    	System.out.println( "Benchmarking single model server performance (calls: " + args.numberCalls + ")" ); 
    	System.out.println( "Endpoint: " + args.skilInferenceEndpoint ); 


        if (auth_token == null) {
            Authorization auth = new Authorization(args.skilInferenceEndpoint);
            long time = System.nanoTime();
            auth_token = auth.getAuthToken( "admin", "admin" );
            time = System.nanoTime() - time;
            System.out.println("Getting the auth token took: " + time / 1000000 + " ms");
            System.out.println( "auth token: " + auth_token + "\n" );
        }

        parallelInference(auth_token);

        long avg_time = totalBenchTime / args.numberCalls;
        double throughput = 1e+9 / avg_time * Long.valueOf(args.concurrency);

        System.out.println("Average inference round trip (" + args.numberCalls + " trips total) took: " + avg_time / 1000000 + " ms");
        System.out.println("Total throughput: " + throughput + " TPS");
    }

    private void parallelInference( String auth_token ) throws InterruptedException, ExecutionException {
        Collection<Callable<Long>> tasks = new ArrayList();
        for(int i = 0; i < args.numberCalls; i++){
            tasks.add(new Task(auth_token));
        }

        ExecutorService executor = Executors.newFixedThreadPool(args.concurrency);
        List<Future<Long>> results = executor.invokeAll(tasks);
        for(Future<Long> result : results){
            totalBenchTime += result.get();
        }
        executor.shutdown(); //always reclaim resources
    }


    /** Try to ping a URL. Return true only if successful. */
    private final class Task implements Callable<Long> {
        Task(String token){
            authToken = token;
        }
        /** Access a URL, and see if you get a healthy response. */
        @Override public Long call() throws Exception {
            return makeInferenceRequest(authToken);
        }
        private final String authToken;
    }

    private long makeInferenceRequest( String auth_token ) throws Exception {
        long time = System.nanoTime();
        JSONObject returnJSONObject =
                Unirest.post( args.skilInferenceEndpoint + "predict" )
                        .header("accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header( "Authorization", "Bearer " + auth_token)
                        .body(new JSONObject() //Using this because the field functions couldn't get translated to an acceptable json
                                .put( "id", "some_id" )
                                .put("prediction", new JSONObject().put("array", inputBase64))
                                .toString())
                        .asJson()
                        .getBody().getObject(); //.toString();

        returnJSONObject.getJSONObject("prediction").getString("array");
        time = System.nanoTime() - time;

        return time;
    }

}
