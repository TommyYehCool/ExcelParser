package com.dreamer.excelparser.db.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class OthersProcessStatusQ {
    private static OthersProcessStatusQ instance = new OthersProcessStatusQ();

    private BlockingQueue<String> queue;

    private OthersProcessStatusQ() {
	queue = new LinkedBlockingQueue<String>();
    }

    public static OthersProcessStatusQ getInstance() {
	return instance;
    }

    public void offer(String processStatus) {
	queue.offer(processStatus);
    }

    public String getProcessStatus() throws InterruptedException {
	return queue.take();
    }
}
