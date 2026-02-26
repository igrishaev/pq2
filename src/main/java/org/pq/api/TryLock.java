package org.pq.api;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TryLock implements AutoCloseable {

    private final Lock lock;

    public TryLock() {
        this.lock = new ReentrantLock();
    }

    public TryLock lock() {
        lock.lock();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
