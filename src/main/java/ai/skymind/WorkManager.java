package ai.skymind;

import ai.skymind.benchmarks.IBench;

public class WorkManager {

    private IBench currentWork = null;

    public boolean hasWork() {
        if(currentWork == null) return true;
        else return false;
    }

    public void startWork(IBench bench) throws Exception {
        if(currentWork == null) {
            currentWork = bench;
            bench.run();
            currentWork = null;
        } else {
            throw new IllegalStateException("Cannot run work when other work exists");
        }
    }

    public boolean stopWork() {
        if(currentWork==null) {
            return false;
        } else {
            currentWork.stop();
            currentWork = null;
            return true;
        }
    }
}
