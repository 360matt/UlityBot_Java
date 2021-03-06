package fr.ulity.bot;

import fr.ulity.bot.api.CommandBuilder;
import fr.ulity.bot.api.CommandManager;
import fr.ulity.bot.api.Config;
import fr.ulity.bot.api.Lang;
import fr.ulity.bot.commands.PingCMD;
import fr.ulity.bot.module.Updater;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class MainDiscordApi {
    public static Config config;
    public static JDA jda;
    public static File path;
    public static String pathString;

    public static void start (File path) throws LoginException, IOException, URISyntaxException {
        MainDiscordApi.path = path;
        MainDiscordApi.pathString = path.getPath();

        config = new Config();
        Lang.reload();

        if (!path.equals(new File("")))
            Updater.update();



        // register commands
        CommandManager.register(new PingCMD());


        jda = JDABuilder.createDefault(config.getString("bot.token")).build();
        jda.addEventListener(new CommandManager());
    }

    public static boolean isRunning () { return jda != null; }

    public static void registerChecker (Class checker) { CommandManager.checkers.add(checker); }
    public static void unregisterChecker (Class checker) { CommandManager.checkers.remove(checker); }
    public static void registerCommand (CommandBuilder cmd) { CommandManager.register(cmd); }
    public static void unregisterCommand (CommandBuilder cmd) { CommandManager.unregister(cmd); }

    public static void addLangTemplate (URL template) { Lang.extraLangTemplate.add(template); }
    public static void removeLangTemplate (URL template) { Lang.extraLangTemplate.remove(template); }
    public static void reloadLang () throws IOException, URISyntaxException { Lang.reload(); }

}
