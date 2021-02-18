package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.utils.DiscordWebhook;
import org.jetbrains.annotations.NotNull;

public class ContactPostHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String name = ctx.formParam("name");
        String subject = ctx.formParam("subject");
        String email = ctx.formParam("email");
        String message = ctx.formParam("message");
        DiscordWebhook dw = new DiscordWebhook("DISCORD WEBHOOK URL");
        dw.addEmbed(new DiscordWebhook.EmbedObject().setTitle("Kapcsolat")
                .addField("Név", name, true)
                .addField("Tárgy", subject, true)
                .addField("Email", email, true)
                .addField("Üzenet", message, false));
        dw.execute();
        ctx.redirect("/kapcsolat?success=true");
    }
}
