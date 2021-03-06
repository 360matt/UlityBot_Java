package fr.ulity.bot.api;

import fr.ulity.bot.MainDiscordApi;
import fr.ulity.bot.commandCheckers.CheckerCooldow;
import fr.ulity.bot.commandCheckers.CheckerDM;
import fr.ulity.bot.commandCheckers.CheckerLevel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    public static HashMap<String, CommandBuilder> commandMap = new HashMap<>();
    public static HashMap<String, String> aliases = new HashMap<>();

    public static List<Class> checkers = Arrays.asList(
        CheckerLevel.class, // permissions, simplified
                CheckerDM.class, // DM command state
                CheckerCooldow.class // respect of the cooldown of the command

        // checkers likes: permissions, cooldown, etc
    );


    public static void register (CommandBuilder cmd) {
        commandMap.put(cmd.name, cmd);
        for (String aliase : cmd.aliases)
            aliases.put(aliase, cmd.name);
    }

    public static void unregister (CommandBuilder cmd) {
        commandMap.remove(cmd.name);
        for (String aliase : cmd.aliases)
            aliases.remove(aliase);
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String prefix = MainDiscordApi.config.getString("bot.prefix");
        String content = event.getMessage().getContentRaw();

        if (content.startsWith(prefix)) {
            String presumedCMD = content
                    .replace(prefix, "")
                    .split(" ")
                    [0]
                    .toLowerCase();

            // if is legitime command
            if (commandMap.containsKey(presumedCMD)) {
                processCMD(event, presumedCMD);

            // if is aliase of command
            } else if (aliases.containsKey(presumedCMD)) {
                String trueCMD = aliases.get(presumedCMD);
                if (commandMap.containsKey(trueCMD)) {
                    processCMD(event, trueCMD);
                }
            }
        }
    }

    private static void processCMD (MessageReceivedEvent event, String command) {
        CommandBuilder cmdObj = commandMap.get(command);

        // loop all checkers class ...
        for (Class x : checkers) {
            try {
                // and invoke the check() method of their class
                if (x.getDeclaredMethod("check", MessageReceivedEvent.class, CommandBuilder.class)
                        .invoke(null, event, cmdObj)
                        .equals(Boolean.FALSE))
                    // if false is returned, command will don't be launched, then return void to stop it.
                    return;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        String content = event.getMessage().getContentRaw();
        String args = content.substring(content.indexOf(' ') + 1);

        // execute command with channel, author, and args

        CommandBuilder privateObj = cmdObj;
        privateObj.channel = event.getChannel();
        privateObj.user = event.getAuthor();
        privateObj.args = args.split(" ");
        privateObj.run();


    }
}
