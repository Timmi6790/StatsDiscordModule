package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.property.properties.MinArgCommandProperty;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

public class JavaGameAliasCommand extends AbstractJavaStatsCommand {
    public JavaGameAliasCommand() {
        super("aliasGame", "Game Alias", "<game> <alias>", "ag");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        this.getMineplexStatsModule().getMpStatsRestClient().addJavaGameAlias(game.getName(), commandParameters.getArgs()[1]);
        this.getMineplexStatsModule().loadJavaGames();
        sendTimedMessage(
                commandParameters,
                getEmbedBuilder(commandParameters)
                        .setTitle("Added Game Alias")
                        .setDescription("Added new game alias " + MarkdownUtil.monospace(commandParameters.getArgs()[0])),
                90
        );

        return CommandResult.SUCCESS;
    }
}
