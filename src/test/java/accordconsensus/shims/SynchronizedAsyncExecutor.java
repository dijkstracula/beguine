package accordconsensus.shims;

import accord.api.Agent;
import accord.impl.basic.TaskExecutorService;
import accord.impl.list.ListAgent;
import accord.local.AgentExecutor;
import accord.utils.async.AsyncChain;
import accord.utils.async.AsyncExecutor;

import java.util.List;
import java.util.concurrent.*;

// I have no idea what I want here.
public class SynchronizedAsyncExecutor extends TaskExecutorService implements AgentExecutor {
    private Agent agent;

    public SynchronizedAsyncExecutor(Agent agent) {
        this.agent = agent;
    }

    @Override
    public Agent agent() {
        return agent;
    }

    @Override
    protected void execute(Task<?> task) {
        //TODO 
    }
}
