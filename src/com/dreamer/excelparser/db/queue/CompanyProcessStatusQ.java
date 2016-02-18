package com.dreamer.excelparser.db.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CompanyProcessStatusQ {
    private static CompanyProcessStatusQ instance = new CompanyProcessStatusQ();

    private BlockingQueue<String> queue;

    private CompanyProcessStatusQ() {
	queue = new LinkedBlockingQueue<String>();
    }

    public static CompanyProcessStatusQ getInstance() {
	return instance;
    }

    public void offer(String processStatus) {
	queue.offer(processStatus);
    }

    public String getProcessStatus() throws InterruptedException {
	return queue.take();
    }
}
