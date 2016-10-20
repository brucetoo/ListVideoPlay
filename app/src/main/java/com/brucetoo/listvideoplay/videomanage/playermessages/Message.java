package com.brucetoo.listvideoplay.videomanage.playermessages;

/**
 * This generic interface for messages
 */
public interface Message {
    void runMessage();
    void polledFromQueue();
    void messageFinished();
}
