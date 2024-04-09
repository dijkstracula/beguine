package accordconsensus.shims;

import accord.api.DataStore;
import accord.local.Node;
import accord.local.SafeCommandStore;
import accord.primitives.Ranges;
import accord.primitives.SyncPoint;
import accord.primitives.Timestamp;
import accord.utils.async.AsyncResults;

public class Store implements DataStore {

    static class NonAsyncSuccessfulFetch extends AsyncResults.SettableResult<Ranges> implements FetchResult
    {
        NonAsyncSuccessfulFetch(Ranges ranges) { setSuccess(ranges); }
        @Override public void abort(Ranges abort) { }
    }

    @Override
    public FetchResult fetch(Node node, SafeCommandStore safeStore, Ranges ranges, SyncPoint syncPoint, FetchRanges callback)
    {
        callback.starting(ranges).started(Timestamp.NONE);
        callback.fetched(ranges);
        return new NonAsyncSuccessfulFetch(ranges);
    }

}
