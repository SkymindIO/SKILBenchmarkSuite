package ai.skymind;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BenchmarkNode {

    @Parameter(names="--peer", description="Address of peer in form [host]:[port]; can pass multiple.", required=false)
    private List<String> peers = new ArrayList();

    private ClusterManager cluster;

    public static void main(String[] args) {
        log.info("Starting SKIL benchmark suite...");
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(args);

        new BenchmarkNode().run();
    }

    public void run() {
        log.info("Setting up cluster replica...");
        this.cluster = new ClusterManager(peers);
        cluster.setup();

        log.info("Setting up API...");
        new HttpInterface(cluster).run();
    }

}
