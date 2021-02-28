package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.utils.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * A statisztikákat mutatja (/stats)
 */
public class StatisticsHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.contentType("text/html; charset=utf-8");
        ctx.result(
                "Összes memória: " + Utils.humanReadableByteCountBin(Runtime.getRuntime().totalMemory()) + "<br>" +
                        "Használt memória: " + Utils.humanReadableByteCountBin(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "<br>" +
                        "Cachelt adat: " + Utils.humanReadableByteCountBin(Main.getManager().getCachedData()) + "<br>" +
                        "Kiszolgált adat: " + Utils.humanReadableByteCountBin(Main.getManager().getServedData()) + "<br>"
        );
    }
}
