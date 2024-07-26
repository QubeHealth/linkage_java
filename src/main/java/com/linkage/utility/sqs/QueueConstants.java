package com.linkage.utility.sqs;

public class QueueConstants {
    public static final QueueInfo LINKAGE = new QueueInfo("linkage_queue", "common_exchange");

    public static class QueueInfo {
        public final String queue;
        public final String exchange;

        public QueueInfo(String queue, String exchange) {
            this.queue = queue;
            this.exchange = exchange;
        }
    }
}