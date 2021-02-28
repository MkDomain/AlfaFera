package me.mkdomain.alfafera.placeholders;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.stats.popularity.Popularity;
import me.mkdomain.alfafera.utils.Utils;

import java.util.Arrays;
import java.util.List;

/**
 * Olyan szövegrészek amiket ez az osztály automatikusan lecserél
 */
public class Placeholder {

    private static final List<PlaceholderProvider> replacer = Arrays.asList(
            new LambdaReplacer("kiszolgalt_adat", () -> Utils.humanReadableByteCountBin(Main.getManager().getServedData())),
            new LambdaReplacer("cachelt_adat", () -> Utils.humanReadableByteCountBin(Main.getManager().getCachedData())),
            new LambdaReplacer("betoltott_jegyzetek", () -> String.valueOf(NoteStatistics.getALlPoints())),
            new LambdaReplacer("legnepszerubb_jegyzet", () -> String.valueOf(Popularity.getMostPopularNote().name()))
    );

    public static String replace(String context) {
        String ret = context;
        for (PlaceholderProvider replacer : replacer) {
            ret = ret.replace("%" + replacer.name() + "%", replacer.replace());
        }
        return ret;
    }

}
