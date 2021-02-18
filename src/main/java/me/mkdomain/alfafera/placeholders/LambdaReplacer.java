package me.mkdomain.alfafera.placeholders;

import java.util.function.Supplier;

/**
 * Olyan placeholder lecserélő ami a lecserélt szöveget egy Supplier-ből kapja meg
 */
public class LambdaReplacer implements PlaceholderProvider {

    private final String name;
    private final Supplier<String> replacer;

    public LambdaReplacer(String name, Supplier<String> replacer) {
        this.name = name;
        this.replacer = replacer;
    }

    @Override
    public String replace() {
        return replacer.get();
    }

    @Override
    public String name() {
        return name;
    }
}
