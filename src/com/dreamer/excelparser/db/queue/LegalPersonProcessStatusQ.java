package com.dreamer.excelparser.db.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LegalPersonProcessStatusQ {
    private static LegalPersonProcessStatusQ instance = new LegalPersonProcessStatusQ();

    private BlockingQueue<String> queue;

    private LegalPersonProcessStatusQ() {
	queue = new LinkedBlockingQueue<String>();
    }

    public static LegalPersonProcessStatusQ getInstance() {
	return instance;
    }

    public void offer(String processStatus) {
	queue.offer(processStatus);
    }

    public String getProcessStatus() throws InterruptedException {
	return queue.take();
    }
}
