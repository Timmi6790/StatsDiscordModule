package de.timmi6790.mineplex_stats.commands.bedrock;

import de.timmi6790.discord_framework.modules.command.AbstractCommand;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.exceptions.CommandReturnException;
import de.timmi6790.mineplex_stats.commands.AbstractStatsCommand;
import de.timmi6790.mineplex_stats.commands.bedrock.info.BedrockGamesCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractBedrockStatsCommand extends AbstractStatsCommand {
    private static final Pattern NAME_PATTERN = Pattern.compile("^.{3,32}$");

    public AbstractBedrockStatsCommand(final String name, final String description, final String syntax, final String... aliasNames) {
        super(name, "MineplexStats - Bedrock", description, syntax, aliasNames);
    }

    protected BedrockGame getGame(final CommandParameters commandParameters, final int argPos) {
        final String name = commandParameters.getArgs()[argPos];
        final Optional<BedrockGame> game = this.getMineplexStatsModule().getBedrockGame(name);
        if (game.isPresent()) {
            return game.get();
        }

        final List<BedrockGame> similarGames = new ArrayList<>(this.getMineplexStatsModule().getSimilarBedrockGames(name, 0.6, 3));
        if (!similarGames.isEmpty() && commandParameters.getUserDb().hasAutoCorrection()) {
            return similarGames.get(0);
        }

        final AbstractCommand command = this.getMineplexStatsModule().getModuleOrThrow(CommandModule.class).getCommand(BedrockGamesCommand.class).orElse(null);
        this.sendHelpMessage(commandParameters, name, argPos, "game", command, new String[0], similarGames.stream().map(BedrockGame::getName).collect(Collectors.toList()));

        throw new CommandReturnException();
    }

    protected String getPlayer(final CommandParameters commandParameters, final int startPos) {
        final String name = String.join(" ", Arrays.copyOfRange(commandParameters.getArgs(), startPos, commandParameters.getArgs().length));
        if (NAME_PATTERN.matcher(name).find()) {
            return name;
        }

        throw new CommandReturnException(
                getEmbedBuilder(commandParameters)
                        .setTitle("Invalid Name")
                        .setDescription(MarkdownUtil.monospace(name) + " is not a minecraft name.")
        );
    }
}
