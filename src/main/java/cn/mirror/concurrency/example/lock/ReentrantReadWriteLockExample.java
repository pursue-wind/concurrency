package cn.mirror.concurrency.example.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author mirror
 */
@Slf4j
public class ReentrantReadWriteLockExample {
    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();
    private static final Lock READ_LOCK = LOCK.readLock();
    private static final Lock WRITE_LOCK = LOCK.writeLock();
    private static final Map<String, Date> MAP = new HashMap<>();

    public Date get(String key) {
        READ_LOCK.lock();
        try {
            return MAP.get(key);
        } finally {
            READ_LOCK.unlock();
        }
    }

    public Set<String> getAllKeys() {
        READ_LOCK.lock();
        try {
            return MAP.keySet();
        } finally {
            READ_LOCK.unlock();
        }
    }

    public Date put(String key, Date value) {
        WRITE_LOCK.lock();
        try {
            return MAP.put(key, value);
        } finally {
            WRITE_LOCK.unlock();
        }
    }


}
