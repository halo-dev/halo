package run.halo.app.extension.index;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Implementation of {@link IndexerTransaction}.
 *
 * @author guqing
 * @since 2.12.0
 */
public class IndexerTransactionImpl implements IndexerTransaction {
    private Deque<ChangeRecord> changeRecords;
    private boolean inTransaction = false;
    private Long threadId;

    @Override
    public synchronized void begin() {
        if (inTransaction) {
            throw new IllegalStateException("Transaction already active");
        }
        threadId = Thread.currentThread().getId();
        this.changeRecords = new ArrayDeque<>();
        inTransaction = true;
    }

    @Override
    public synchronized void commit() {
        checkThread();
        if (!inTransaction) {
            throw new IllegalStateException("Transaction not started");
        }
        Deque<ChangeRecord> committedRecords = new ArrayDeque<>();
        try {
            while (!changeRecords.isEmpty()) {
                var changeRecord = changeRecords.pop();
                applyChange(changeRecord);
                committedRecords.push(changeRecord);
            }
            // Reset threadId after transaction ends
            inTransaction = false;
            // Reset threadId after transaction ends
            threadId = null;
        } catch (Exception e) {
            // Rollback the changes that were committed before the error occurred
            while (!committedRecords.isEmpty()) {
                var changeRecord = committedRecords.pop();
                revertChange(changeRecord);
            }
            throw e;
        }
    }

    @Override
    public synchronized void rollback() {
        checkThread();
        if (!inTransaction) {
            throw new IllegalStateException("Transaction not started");
        }
        changeRecords.clear();
        inTransaction = false;
        // Reset threadId after transaction ends
        threadId = null;
    }

    @Override
    public synchronized void add(ChangeRecord changeRecord) {
        if (inTransaction) {
            changeRecords.push(changeRecord);
        } else {
            throw new IllegalStateException("No active transaction to add changes");
        }
    }

    private void applyChange(ChangeRecord changeRecord) {
        var indexEntry = changeRecord.indexEntry();
        var key = changeRecord.key();
        var value = changeRecord.value();
        var isAdd = changeRecord.isAdd();
        if (isAdd) {
            indexEntry.addEntry(List.of(key), value);
        } else {
            indexEntry.removeEntry(key, value);
        }
    }

    private void revertChange(ChangeRecord changeRecord) {
        var indexEntry = changeRecord.indexEntry();
        var key = changeRecord.key();
        var value = changeRecord.value();
        var isAdd = changeRecord.isAdd();
        if (isAdd) {
            indexEntry.removeEntry(key, value);
        } else {
            indexEntry.addEntry(List.of(key), value);
        }
    }

    private void checkThread() {
        if (threadId != null && !threadId.equals(Thread.currentThread().getId())) {
            throw new IllegalStateException("Transaction cannot span multiple threads!");
        }
    }
}
