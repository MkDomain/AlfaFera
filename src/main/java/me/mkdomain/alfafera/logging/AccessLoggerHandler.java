package me.mkdomain.alfafera.logging;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Elérési naplozásért felel
 */
public class AccessLoggerHandler implements Handler {

    private static final List<LogRecord> accesses = new ArrayList<>();

    public static List<LogRecord> getAccesses() {
        return accesses;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        accesses.add(new LogRecord(ctx.ip(), System.currentTimeMillis(), ctx.fullUrl()));
    }

    /**
     * Az eléréseket tárolja (minden fontos adatot)
     */
    public static class LogRecord {
        private final String ip;
        private final long accessDate;
        private final String url;

        public LogRecord(String ip, long accessDate, String url) {
            this.ip = ip;
            this.accessDate = accessDate;
            this.url = url;
        }

        public String getIp() {
            return ip;
        }

        public long getAccessDate() {
            return accessDate;
        }

        public String getUrl() {
            return url;
        }
    }
}
