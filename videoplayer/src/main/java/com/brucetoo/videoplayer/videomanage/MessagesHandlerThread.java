package com.brucetoo.videoplayer.videomanage;


import com.brucetoo.videoplayer.utils.Logger;
import com.brucetoo.videoplayer.videomanage.messages.Message;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is designed to process a message queue.
 * It calls very specific methods of {@link Message} in very specific times.
 * <p>
 * 1. When message is polled from queue it calls {@link Message#polledFromQueue()}
 * 2. When message should be run it calls {@link Message#runMessage()}
 * 3. When message finished running it calls {@link Message#messageFinished()}
 */
public class MessagesHandlerThread {

    private static final String TAG = MessagesHandlerThread.class.getSimpleName();

    private final Queue<Message> mPlayerMessagesQueue = new ConcurrentLinkedQueue<>();
    private final PlayerQueueLock mQueueLock = new PlayerQueueLock();
    private final Executor mQueueProcessingThread = Executors.newSingleThreadExecutor();
//    private AtomicBoolean mCanProcess = new AtomicBoolean(true);

    private AtomicBoolean mTerminated = new AtomicBoolean(false); // TODO: use it
    private Message mLastMessage;

    public MessagesHandlerThread() {
        mQueueProcessingThread.execute(new Runnable() {
            @Override
            public void run() {

                Logger.v(TAG, "start worker thread");
                do {

                    mQueueLock.lock(TAG);
                    Logger.v(TAG, "mPlayerMessagesQueue " + mPlayerMessagesQueue);

                    if (mPlayerMessagesQueue.isEmpty()) {
                        try {
                            Logger.v(TAG, "queue is empty, wait for new messages");

                            mQueueLock.wait(TAG);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            throw new RuntimeException("InterruptedException");
                        }
                    }

                    mLastMessage = mPlayerMessagesQueue.poll();

                    if (mLastMessage == null) break;
                    mLastMessage.polledFromQueue();
                    Logger.v(TAG, "poll mLastMessage " + mLastMessage);
                    mQueueLock.unlock(TAG);

                    Logger.v(TAG, "run, mLastMessage " + mLastMessage);
                    mLastMessage.runMessage();

                    mQueueLock.lock(TAG);
                    mLastMessage.messageFinished();
                    mQueueLock.unlock(TAG);

                } while (!mTerminated.get());

            }
        });
    }

    /**
     * Use it if you need to add a single message
     */
    public void addMessage(Message message) {

        Logger.v(TAG, ">> addMessage, lock " + message);
        mQueueLock.lock(TAG);

        mPlayerMessagesQueue.add(message);
        mQueueLock.notify(TAG);

        Logger.v(TAG, "<< addMessage, unlock " + message);
        mQueueLock.unlock(TAG);
    }

    /**
     * Use it if you need to add a multiple messages
     */
    public void addMessages(List<? extends Message> messages) {
        Logger.v(TAG, ">> addMessages, lock " + messages);
        mQueueLock.lock(TAG);

        mPlayerMessagesQueue.addAll(messages);
        mQueueLock.notify(TAG);

        Logger.v(TAG, "<< addMessages, unlock " + messages);
        mQueueLock.unlock(TAG);
    }

    public void pauseQueueProcessing(String outer) {
        Logger.v(TAG, "pauseQueueProcessing, lock " + mQueueLock);
        mQueueLock.lock(outer);
    }

    public void resumeQueueProcessing(String outer) {
        Logger.v(TAG, "resumeQueueProcessing, unlock " + mQueueLock);
        mQueueLock.unlock(outer);
    }

    public void clearAllPendingMessages(String outer) {
        Logger.v(TAG, ">> clearAllPendingMessages, mPlayerMessagesQueue " + mPlayerMessagesQueue);

        if (mQueueLock.isLocked(outer)) {
            mPlayerMessagesQueue.clear();
        } else {
            throw new RuntimeException("cannot perform action, you are not holding a lock");
        }
        Logger.v(TAG, "<< clearAllPendingMessages, mPlayerMessagesQueue " + mPlayerMessagesQueue);
    }

    public void terminate() {
        mTerminated.set(true);
    }
}
