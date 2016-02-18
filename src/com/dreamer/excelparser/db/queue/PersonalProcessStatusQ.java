package com.dreamer.excelparser.db.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PersonalProcessStatusQ {
    private static PersonalProcessStatusQ instance = new PersonalProcessStatusQ();

    private BlockingQueue<String> queue;

    private PersonalProcessStatusQ() {
	queue = new LinkedBlockingQueue<String>();
    }

    public static PersonalProcessStatusQ getInstance() {
	return instance;
    }

    public void offer(String processStatus) {
	queue.offer(processStatus);
    }

    public String getProcessStatus() throws InterruptedException {
	return queue.take();
    }
}
