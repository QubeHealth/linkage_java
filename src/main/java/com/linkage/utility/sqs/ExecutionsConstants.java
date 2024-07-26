package com.linkage.utility.sqs;

public class ExecutionsConstants {

    public static final ExecutionInfo PREFUNDED_NOTIFICATON = new ExecutionInfo("LINKAGE.PREFUNDED_NOTIFICATON", "Kokilaben flow Notificaton Service");

        public static class ExecutionInfo {
                public final String key;
                public final String description;

                public ExecutionInfo(String key, String description) {
                        this.key = key;
                        this.description = description;
                }
        }
}

