package io.runtime.mcumgr.transfer;

import android.os.ConditionVariable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

import io.runtime.mcumgr.exception.InsufficientMtuException;
import io.runtime.mcumgr.exception.McuMgrException;

public class TransferCallable implements Callable<Transfer>, TransferController {

    private enum State {
        NONE, TRANSFER, PAUSED, CLOSED
    }

    private Transfer mTransfer;
    private State mState;
    private final ConditionVariable mPauseLock = new ConditionVariable(true);

    public TransferCallable(@NotNull Transfer transfer) {
        mTransfer = transfer;
        mState = State.NONE;
    }

    public Transfer getTransfer() {
        return mTransfer;
    }

    public TransferCallable.State getState() {
        return mState;
    }

    @Override
    public synchronized void pause() {
        if (mState == State.TRANSFER) {
            mPauseLock.close();
        }
    }

    @Override
    public synchronized void resume() {
        if (mState == State.PAUSED) {
            mPauseLock.open();
        }
    }

    @Override
    public synchronized void cancel() {
        mState = State.CLOSED;
        mTransfer.onCanceled();
    }

    private synchronized void failTransfer(McuMgrException e) {
        mState = State.CLOSED;
        mTransfer.onFailed(e);
    }

    private synchronized void completeTransfer() {
        mState = State.CLOSED;
        mTransfer.onCompleted();
    }

    @Override
    public Transfer call() throws InsufficientMtuException {
        if (mState == State.CLOSED) {
            return mTransfer;
        }
        while (!mTransfer.isFinished()) {
            // Block if the transfer has been paused
            mPauseLock.block();

            // Send the next packet
            try {
                mTransfer.sendNext();
            } catch (McuMgrException e) {
                if (e instanceof InsufficientMtuException) {
                    throw (InsufficientMtuException) e;
                }
                failTransfer(e);
                return mTransfer;
            }

            synchronized (this) {
                // Check if upload hasn't been cancelled.
                if (mState == State.CLOSED) {
                    return mTransfer;
                }

                if (mTransfer.getData() == null) {
                    throw new NullPointerException("Transfer data is null!");
                }

                // Call the progress callback.
                mTransfer.onProgressChanged(mTransfer.getOffset(), mTransfer.getData().length,
                        System.currentTimeMillis());
            }
        }
        completeTransfer();
        return mTransfer;
    }
}
