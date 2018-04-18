package ai.skymind;

import com.google.gson.Gson;

import static spark.Spark.get;
import static spark.Spark.post;

public class HttpInterface {

    private ClusterManager cluster;
    final private Gson gson = new Gson();

    public HttpInterface(ClusterManager cluster) {
        this.cluster = cluster;
    }

    public void run() {
        get("/", (req, res) -> "Working: "+ Boolean.toString(cluster.workManager.hasWork()));
        post("/benchmark", (req, res) -> {
            ClusterCommand cmd = gson.fromJson(req.body(), ClusterCommand.class);
            cluster.sendCommand(cmd);
            return "Success";
        });
    }
}
