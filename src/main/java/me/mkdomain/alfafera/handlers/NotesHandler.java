package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.placeholders.Placeholder;
import me.mkdomain.alfafera.stats.popularity.Popularity;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * A jegyzetek oldalt tölti be, nem használ cache-t
 */
public class NotesHandler implements Handler {

    private static final String template = "<div class=\"col-12 col-md-6 col-lg-4 jegyzet %filter_categories%\">\n" +
            "                                        <div class=\"clean-product-item\">\n" +
            "                                            <div class=\"image\"><a href=\"%link%\"><img class=\"img-fluid d-block mx-auto\" src=\"/jegyzet/%local%/kep\"></a></div>\n" +
            "                                            <div class=\"product-name\"><a href=\"%link%\" class=\"custom-name\">%name%</a></div>\n" +
            "                                            <div class=\"about\">\n" +
            "                                                <div class=\"price\">\n" +
            "                                                    <h3 class=\"custom-price\">%categories%</h3>\n" +
            "                                                </div>\n" +
            "                                            </div>\n" +
            "                                        </div>\n" +
            "                                    </div> ";

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String in = Placeholder.replace(new String(Files.readAllBytes(Paths.get("html/jegyzetek.html"))));
        final StringBuilder completed = new StringBuilder();

        for (Note value : Popularity.getNotesByPopularityDescending()) {
            completed.append(template
                    .replace("%name%", value.name())
                    .replace("%local%", value.getFile().getFileName().toString())
                    .replace("%categories%", value.categories())
                    .replace("%link%", "jegyzet/" + value.getFile().getFileName().toString())
                    .replace("%filter_categories%", value.getCategories().stream().map(Enum::name).collect(Collectors.joining(" "))))
            ;
        }
        in = in.replace("__ide__", completed.toString());

        ctx.contentType("text/html; charset=utf-8");
        ctx.result(in);
    }
}
