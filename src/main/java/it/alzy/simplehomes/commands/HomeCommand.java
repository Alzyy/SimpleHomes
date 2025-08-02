package it.alzy.simplehomes.commands;

import org.bukkit.entity.Player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import it.alzy.simplehomes.SimpleHomes;
import net.kyori.adventure.text.minimessage.MiniMessage;

@CommandAlias("home|homes")
@Description("Main command")
public class HomeCommand extends BaseCommand {
    
    private final SimpleHomes plugin = SimpleHomes.getInstance();


    @Default
    public void root(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gradient:#22C55E:#16A34A><bold>✔ SimpleHomes</bold></gradient> <gray>| Version </gray><white>"
                + plugin.getPluginMeta().getVersion() + "</white>\n"
                + "<gray>Developed with ❤ by </gray>"
                + "<hover:show_text:'Click to view my profile!'><click:open_url:'https://www.spigotmc.org/members/alzyit.1581572/'>"
                + "<gradient:#A1A1AA:#71717A><bold>AlzyIT</bold></gradient></click></hover>"));
    }

    @Default
    @CommandCompletion("@homes")
    public void withHomeName(Player player, String homeName) {

    }



}
