package ai.skymind;

import ai.skymind.benchmarks.IBench;
import ai.skymind.benchmarks.RoundTripBench;
import io.atomix.AtomixReplica;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.atomix.copycat.server.storage.Storage;
import io.atomix.group.DistributedGroup;
import io.atomix.group.LocalMember;
import io.atomix.group.messaging.MessageConsumer;
import io.atomix.group.messaging.MessageProducer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClusterManager {

    public List<String> seeds;
    public AtomixReplica replica;
    public DistributedGroup cluster;
    public LocalMember localMember;
    public MessageProducer<ClusterCommand> producer;
    public MessageConsumer<ClusterCommand> consumer;
    final private String clusterName = "benchmark-default";
    final private String clusterCommandChannel = "benchmark-command";
    final public WorkManager workManager = new WorkManager();

    public ClusterManager(List<String> peers) {
        this.seeds = peers;
    }

    public void setup() {
        AtomixReplica replica = AtomixReplica.builder(new Address("localhost", 8700))
                .withStorage(new Storage())
                .withTransport(new NettyTransport())
                .build();
        this.replica = replica;

        if(seeds.size()>0) {
            log.info("Seeking "+seeds.size()+" peers");
            List<Address> addresses = new ArrayList<>();

            for (int i = 0; i < seeds.size(); i++) {
                String[] els = seeds.get(i).split(":");
                Address addr = new Address(els[0], Integer.parseInt(els[1]));
                addresses.add(addr);
                replica.join(addresses);
            }
        } else {
            log.info("Bootstrapping new cluster, no peers");
            replica.bootstrap();
        }

        setupGroup();
        setupMessaging();
    }

    private void setupGroup() {
        replica.getGroup(clusterName).thenAccept(group -> {
            this.cluster = group;

            group.join().thenAccept(member -> {
                this.localMember = member;
                log.info("Joined "+clusterName+" with member ID: " + member.id());
                log.info("Found "+group.members().size()+ " peers in cluster");
            });
        });

        cluster.onJoin(member -> {
             log.info(member.id() + " joined the cluster");
        });

        cluster.onLeave(member -> {
            log.info(member.id() + " left the cluster");
        });
    }

    private void setupMessaging() {
        this.producer = cluster.messaging().producer(clusterCommandChannel);
        this.consumer = localMember.messaging().consumer(clusterCommandChannel);

        consumer.onMessage(clusterCommandMessage -> handleCommand(clusterCommandMessage.message()));
    }

    public void sendCommand(ClusterCommand command) {
        producer.send(command).thenRun(() -> log.info("Command delivered to "+cluster.members().size()+" members"));
    }

    public void handleCommand(ClusterCommand command) {
        switch(command.type) {
            case BENCH:
                try {
                    IBench bench = new RoundTripBench(command.roundTripArgs);
                    workManager.startWork(bench);

                } catch(IllegalStateException e) {
                    log.error("There was a problem with the workload", e);
                } catch(IOException e) {
                    log.error("Could not set up benchmark", e);
                } catch(Exception e) {
                    log.error("Benchmark failed", e);
                }
                break;

            case HALT:
                workManager.stopWork();
                break;

            default:
                throw new IllegalArgumentException("Command is not recognized");
        }
    }
}
