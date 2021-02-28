package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.utils.DiscordWebhook;
import org.jetbrains.annotations.NotNull;

/**
 * A kapcsolat űrlapot kezeli
 */
public class ContactPostHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        final String name = ctx.formParam("name");
        final String subject = ctx.formParam("subject");
        final String email = ctx.formParam("email");
        final String message = ctx.formParam("message");
        final DiscordWebhook dw = new DiscordWebhook(Main.getConfiguration().get("discord-webhook-url"));
        dw.addEmbed(new DiscordWebhook.EmbedObject().setTitle("Kapcsolat")
                .addField("Név", name, true)
                .addField("Tárgy", subject, true)
                .addField("Email", email, true)
                .addField("Üzenet", message, false));
        dw.execute();
        ctx.redirect("/kapcsolat?success=true");
    }
}
