package ai.skymind.benchmarks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;
import com.mashape.unirest.http.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.base64.Nd4jBase64;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


/**
 * Benchmarks the total round trip for an inference request to SKIL and calculates total
 * transactions per second (TPS).
 *
 * @author Justin Long (crockpotveggies)
 *
 */
@Slf4j
public class RoundTripBench implements IBench {

    static Args args;
	String auth_token = null;
	String inputBase64 = null;
	long totalBenchTime = 0L;
    ExecutorService executor;

    public RoundTripBench() throws IOException {
        this(new Args());
    }
    
    public RoundTripBench(Args args) throws IOException {
        this.args = args;
        // get shape from launch args
        String[] dimensions = args.arrayShape.split(",");
        int[] shape = new int[dimensions.length];
        for(int i = 0; i < dimensions.length; i++)
            shape[i] = Integer.parseInt(dimensions[i]);

        INDArray input_array = Nd4j.create(shape);
        inputBase64 = Nd4jBase64.base64String( input_array );
    }

    public static void main( String[] args ) throws Exception {
  		RoundTripBench m = new RoundTripBench();
        JCommander.newBuilder()
                .addObject(RoundTripBench.args)
                .build()
                .parse(args);

        m.run();
    }

    public void stop() {
        log.info("Attempting to stop benchmark...");
        executor.shutdown();
    }

    public void run() throws Exception {
        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);

    	log.info( "Running main benchmark routine..." ); 
    	log.info( "Benchmarking single model server performance (calls: " + args.numberCalls + ")" ); 
    	log.info( "Endpoint: " + args.skilInferenceEndpoint ); 


        if (auth_token == null) {
            Authorization auth = new Authorization(args.skilInferenceEndpoint);
            long time = System.nanoTime();
            auth_token = auth.getAuthToken( "admin", "admin" );
            time = System.nanoTime() - time;
            log.info("Getting the auth token took: " + time / 1000000 + " ms");
            log.info( "auth token: " + auth_token + "\n" );
        }

        parallelInference(auth_token);

        long avg_time = totalBenchTime / args.numberCalls;
        double throughput = 1e+9 / avg_time * Long.valueOf(args.concurrency);

        log.info("Average inference round trip (" + args.numberCalls + " trips total) took: " + avg_time / 1000000 + " ms");
        log.info("Total throughput: " + throughput + " TPS");
    }

    private void parallelInference( String auth_token ) throws InterruptedException, ExecutionException {
        Collection<Callable<Long>> tasks = new ArrayList();
        for(int i = 0; i < args.numberCalls; i++){
            tasks.add(new Task(auth_token));
        }

        executor = Executors.newFixedThreadPool(args.concurrency);
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
