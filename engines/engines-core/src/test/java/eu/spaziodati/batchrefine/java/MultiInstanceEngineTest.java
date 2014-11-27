package eu.spaziodati.batchrefine.java;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiInstanceEngineTest {

    @Test
    public void testDistributesRequests() throws Exception {
        FakeEngine e1 = new FakeEngine(10);
        FakeEngine e2 = new FakeEngine(50);
        FakeEngine e3 = new FakeEngine(100);
        FakeEngine e4 = new FakeEngine(200);

        MultiInstanceEngine engine = new MultiInstanceEngine(new BlockingOfferQueue(100), e1, e2, e3, e4);

        ArrayList<Future<Object>> tasks = new ArrayList<Future<Object>>();

        for (int i = 0; i < 1000; i++) {
            tasks.add(engine.transform(null, null, null, null, new IAsyncTransformEngine.Callback()));
        }

        for (Future<Object> result : tasks) {
            result.get();
        }

        Assert.assertTrue(e1.count > e2.count);
        Assert.assertTrue(e2.count > e3.count);
        Assert.assertTrue(e3.count > e4.count);

        Assert.assertTrue(e1.count != 0);
        Assert.assertTrue(e2.count != 0);
        Assert.assertTrue(e3.count != 0);
        Assert.assertTrue(e4.count != 0);
    }

}

class FakeEngine implements ITransformEngine {

    final AtomicBoolean fBusy = new AtomicBoolean();

    volatile int count;

    final long process;

    public FakeEngine(long time) {
        process = time;
    }

    @Override
    public void transform(URI original, JSONArray transform, URI transformed, Properties exporterOptions) throws IOException, JSONException {

        if (!fBusy.compareAndSet(false, true)) {
            Assert.fail("Non-sequential access to engine.");
        }

        count++;
        try {
            Thread.sleep(process);
        } catch (InterruptedException e) {
            // nothing
        }

        fBusy.set(false);
    }

    @Override
    public void close() throws Exception {
    }
}

class BlockingOfferQueue<E> extends LinkedBlockingQueue<E> {
    public BlockingOfferQueue(int maxSize) {
        super(maxSize);
    }

    @Override
    public boolean offer(E e) {
        // turn offer() and add() into a blocking calls (unless interrupted)
        try {
            put(e);
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

}