package ai.skymind;

import org.datavec.api.util.ClassPathResource;
import org.datavec.spark.transform.model.Base64NDArrayBody;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Broadcast;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.serde.base64.Nd4jBase64;
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastMulOp;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Broadcast;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.interval;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;


import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONObject;
import org.json.JSONArray;

import java.text.MessageFormat;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.InputStream;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import org.apache.commons.io.IOUtils;


import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;


/**
 * Hello world!
 *
 */
public class BasicClient 
{

    private static long globalStartTime = 0;
    private static long globalEndTime = 0;
    //private int numberServerCalls = 5;

    private static class Args {
        @Parameter(names="--endpoint", description="Endpoint for classification", required=true)
        private String skilInferenceEndpoint = ""; // EXAMPLE: "http://localhost:9008/endpoints/yolo/model/yolo/default/";

        @Parameter(names="--number_calls", description="Number of client calls to measure", required=false)
        private int numberCalls = 10; // "https://raw.githubusercontent.com/pjreddie/darknet/master/data/dog.jpg";

    //    @Parameter(names="--camera", description="Camera input device number", required=false)
      //  private int input_camera = -1; // needs to be 0 or larger
    }
    static Args args = new Args();
	String auth_token = null;

    public BasicClient() { }

    public static void main( String[] args ) throws Exception
    {
        


  		BasicClient m = new BasicClient();
        JCommander.newBuilder()
                .addObject(BasicClient.args)
                .build()
                .parse(args);




        // the JavaFX code initializes early, need to parse arguments and store in static variable
//        JCommander.newBuilder()
  //        .addObject(YOLO2_TF_Client.args)
    //      .build()
      //    .parse(args);

        //BasicClient client = new BasicClient();

        //m.run();

        //new JCommander(client, args); // simple one-liner        

        m.runBenchmarks();

    }

    public void runBenchmarks() throws Exception, IOException {

        Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.ERROR);


    	System.out.println( "Running main benchmark routine..." ); 
    	System.out.println( "Benchmarking single model server performance (calls: " + args.numberCalls + ")" ); 
    	System.out.println( "Endpoint: " + args.skilInferenceEndpoint ); 


        if (auth_token == null) {
            Authorization auth = new Authorization();
            long start = System.nanoTime();
            auth_token = auth.getAuthToken( "admin", "admin" );
            long end = System.nanoTime();
            System.out.println("Getting the auth token took: " + (end - start) / 1000000 + " ms");
            //System.out.println( "auth token: " + auth_token );
        }

        long start = System.nanoTime();

        for ( int x = 0; x < args.numberCalls; x++ ) {
        
        	makeInferenceRequest( auth_token );

        }

        long end = System.nanoTime();
        long avg_time = (end - start) / args.numberCalls;

        System.out.println("Average inference round trip (" + args.numberCalls + " trips total) took: " + avg_time / 1000000 + " ms");


        //System.out.println( "Done" );



/*
        Inference.Request request = new Inference.Request(Nd4jBase64.base64String(array));

        final Object response = restTemplate.postForObject(
                inferenceEndpoint,
                request,
                Inference.Response.Classify.class);

        System.out.format("Inference response: %s\n", response.toString());
*/


    }

    private void makeInferenceRequest( String auth_token ) throws IOException {

		int[] shape = new int[] { 1, 100 };

        INDArray input_array = Nd4j.create(shape);


        String inputBase64 = Nd4jBase64.base64String( input_array );



//        System.out.println( "Sending the Classification Payload..." );
        
        try {

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

            try {

                returnJSONObject.getJSONObject("prediction").getString("array");

            } catch (org.json.JSONException je) { 

                System.out.println( "\n\nException\n\nReturn: " + returnJSONObject );
                return;

            }


//            String predict_return_array = returnJSONObject.getJSONObject("prediction").getString("array");
  //          System.out.println( "REST payload return length: " + predict_return_array.length() );

            //networkGlobalOutput = Nd4jBase64.fromBase64( predict_return_array );

        } catch (UnirestException e) {
            e.printStackTrace();
        }    

    }






    /**
        Simple helper class to encapsulate some of the raw REST code for making basic authentication calls
    */
    private class Authorization {

        private String host;
        private String port;

        public Authorization() throws Exception {
            URL url = new URL(args.skilInferenceEndpoint);
            this.host = url.getHost();
            this.port = Integer.toString(url.getPort());
        }

        public Authorization(String host, String port) {
            this.host = host;
            this.port = port;
        }

        public String getAuthToken(String userId, String password) {
            String authToken = null;

            try {
                authToken =
                        Unirest.post(MessageFormat.format("http://{0}:{1}/login", host, port))
                                .header("accept", "application/json")
                                .header("Content-Type", "application/json")
                                .body(new JSONObject() //Using this because the field functions couldn't get translated to an acceptable json
                                        .put("userId", userId)
                                        .put("password", password)
                                        .toString())
                                .asJson()
                                .getBody().getObject().getString("token");
            } catch (UnirestException e) {
                e.printStackTrace();
            }

            return authToken;
        }
    }
/*
    private class Request {
        public Request(String ndArray) {
            this.ndArray = ndArray;
        }

        @JsonProperty("id")
        private String uuid = UUID.randomUUID().toString();

        @JsonIgnore
        private String ndArray;

        @JsonProperty("prediction")
        public Map<String, String> getPrediction() {
            final Map<String, String> prediction = new HashMap<String, String>();
            prediction.put("array", this.ndArray);

            return prediction;
        }
    }

    private interface Response {

        class Classify implements Response {
            @JsonProperty("results")
            private int[] results = null;

            @JsonProperty("probabilities")
            private float[] probabilities = null;

            public String toString() {
                return "Inference.Response.Classify{" +
                        "results{" + Arrays.toString(this.results) + "}, " +
                        "probabilities{" + Arrays.toString(this.probabilities) + "}";
            }
        }

    }

   class MultiClassify implements Response {
        @JsonProperty("rankedOutcomes")
        private List<String[]> rankedOutcomes = null;

        @JsonProperty("maxOutcomes")
        private String[] maxOutcomes = null;

        @JsonProperty("probabilities")
        private List<float[]> probabilities = null;

        public String toString() {
            final StringBuilder sb = new StringBuilder();

            sb.append("Inference.Response.MultiClassify{");
            sb.append("rankedOutcomes{").append(listToString(this.rankedOutcomes)).append("}, ");
            sb.append("maxOutcomes{").append(Arrays.toString(this.maxOutcomes)).append("}, ");
            sb.append("probabilities{").append(listToString(this.probabilities)).append("}");
            sb.append("}");

            return sb.toString();
        }

        private String listToString(List l) {
            if (l == null) {
                return "null";
            }

            return Arrays.deepToString(l.toArray());
        }
    }
*/


}
