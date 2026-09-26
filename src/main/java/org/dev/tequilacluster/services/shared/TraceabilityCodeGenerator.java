package org.dev.tequilacluster.services.shared;

import org.dev.tequilacluster.repositories.shared.BatchRepository;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * FR-05 / RB-004: generates the unique {@code TRZ-YYYY-NNNNN} traceability code assigned to
 * every batch at creation, regardless of stage. NNNNN is a 5-digit sequence within the current
 * year, checked against existing codes so restarts never reuse a number.
 */
@Component
public class TraceabilityCodeGenerator {

    private static final String PREFIX = "TRZ";

    private final BatchRepository batchRepository;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile int cachedYear = -1;

    public TraceabilityCodeGenerator(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public String next() {
        lock.lock();
        try {
            int year = Year.now().getValue();
            if (year != cachedYear) {
                cachedYear = year;
                sequence.set(0);
            }

            String candidate;
            do {
                int next = sequence.incrementAndGet();
                candidate = "%s-%d-%05d".formatted(PREFIX, cachedYear, next);
            } while (batchRepository.existsByTraceabilityCode(candidate));

            return candidate;
        } finally {
            lock.unlock();
        }
    }
}
