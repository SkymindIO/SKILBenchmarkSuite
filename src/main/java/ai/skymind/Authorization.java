package ai.skymind;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.net.URL;
import java.text.MessageFormat;

/**
 Simple helper class to encapsulate some of the raw REST code for making basic authentication calls
 */
class Authorization {

    private String host;
    private String port;

    public Authorization(String skilInferenceEndpoint) throws Exception {
        URL url = new URL(skilInferenceEndpoint);
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
