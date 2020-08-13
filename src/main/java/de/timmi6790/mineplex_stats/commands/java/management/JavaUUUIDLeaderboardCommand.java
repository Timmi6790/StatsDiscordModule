package de.timmi6790.mineplex_stats.commands.java.management;

import de.timmi6790.discord_framework.DiscordBot;
import de.timmi6790.discord_framework.datatypes.ListBuilder;
import de.timmi6790.discord_framework.modules.command.CommandCause;
import de.timmi6790.discord_framework.modules.command.CommandModule;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.EmoteReactionMessage;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.CommandEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.java.AbstractJavaStatsCommand;
import de.timmi6790.mineplex_stats.picture.PictureTable;
import de.timmi6790.mineplex_stats.statsapi.models.ResponseModel;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaBoard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaGame;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaLeaderboard;
import de.timmi6790.mineplex_stats.statsapi.models.java.JavaStat;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaUUUIDLeaderboardCommand extends AbstractJavaStatsCommand {
    private static final int ARG_POS_BOARD_POS = 2;
    private static final int ARG_POS_START_POS = 3;
    private static final int ARG_POS_END_POS = 4;

    private static final int LEADERBOARD_UPPER_LIMIT = 1_000;

    public JavaUUUIDLeaderboardCommand() {
        super("uuidLeaderboard", "Java UUID Leaderboard", "<game> <stat> [board] [start] [end] [date]", "ul");

        this.setCategory("MineplexStats - Java - Management");
        this.addProperties(
                new MinArgCommandProperty(2),
                new RequiredDiscordBotPermsCommandProperty(
                        Permission.MESSAGE_ADD_REACTION
                )
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final JavaGame game = this.getGame(commandParameters, 0);
        final JavaStat stat = this.getStat(game, commandParameters, 1);
        final JavaBoard board = this.getBoard(game, commandParameters, ARG_POS_BOARD_POS);
        final int startPos = this.getStartPosition(commandParameters, ARG_POS_START_POS, LEADERBOARD_UPPER_LIMIT);
        final int endPos = this.getEndPosition(startPos, commandParameters, ARG_POS_END_POS, LEADERBOARD_UPPER_LIMIT, 5);
        final long unixTime = this.getUnixTime(commandParameters, 5);

        final de.timmi6790.mineplex_stats.MineplexStatsModule module = this.getStatsModule();
        final ResponseModel responseModel = module.getMpStatsRestClient().getJavaLeaderboard(game.getName(), stat.getName(), board.getName(), startPos, endPos,
                unixTime, true);
        this.checkApiResponse(commandParameters, responseModel, "No stats available");

        final JavaLeaderboard leaderboardResponse = (JavaLeaderboard) responseModel;
        final JavaLeaderboard.Info leaderboardInfo = leaderboardResponse.getInfo();

        final AtomicInteger index = new AtomicInteger(1);
        final String[][] leaderboard = new ListBuilder<String[]>(() -> new ArrayList<>(leaderboardResponse.getLeaderboard().size() + 1))
                .add(new String[]{"Emote", "UUID", "Player", "Score", "Position"})
                .addAll(leaderboardResponse.getLeaderboard()
                        .stream()
                        .map(data -> new String[]{String.valueOf(index.getAndIncrement()), data.getUuid().toString(), data.getName(),
                                this.getFormattedScore(stat, data.getScore()), String.valueOf(data.getPosition())}))
                .build()
                .toArray(new String[0][3]);

        final String[] header = {leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard()};

        // Emotes
        final Map<String, AbstractEmoteReaction> emotes = new LinkedHashMap<>();

        DiscordBot.getModuleManager().getModuleOrThrow(CommandModule.class)
                .getCommand(JavaPlayerFilterCommand.class)
                .ifPresent(filterCommand -> {
                    final AtomicInteger emoteIndex = new AtomicInteger(1);
                    leaderboardResponse.getLeaderboard().forEach(data -> {
                        final CommandParameters newParameters = new CommandParameters(commandParameters, CommandCause.EMOTES, data.getUuid().toString(), leaderboardInfo.getGame(), leaderboardInfo.getStat(), leaderboardInfo.getBoard());
                        emotes.put(DiscordEmotes.getNumberEmote(emoteIndex.getAndIncrement()).getEmote(), new CommandEmoteReaction(filterCommand, newParameters));
                    });
                });

        // Leaderboard default responses
        final int rowDistance = endPos - startPos;
        final int fastRowDistance = leaderboardInfo.getTotalLength() * 10 / 100;

        // Create a new args array if the old array has no positions
        final CommandParameters newCommandParameters;
        if (Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1 > commandParameters.getArgs().length) {
            final String[] newArgs = new String[Math.max(ARG_POS_END_POS, ARG_POS_START_POS) + 1];
            newArgs[ARG_POS_BOARD_POS] = board.getName();

            System.arraycopy(commandParameters.getArgs(), 0, newArgs, 0, commandParameters.getArgs().length);
            newCommandParameters = new CommandParameters(commandParameters, newArgs);
        } else {
            newCommandParameters = commandParameters;
        }

        emotes.putAll(this.getLeaderboardEmotes(commandParameters, rowDistance, fastRowDistance, startPos, endPos,
                leaderboardInfo.getTotalLength(), ARG_POS_START_POS, ARG_POS_END_POS));

        return this.sendPicture(
                newCommandParameters,
                new PictureTable(header, this.getFormattedUnixTime(leaderboardInfo.getUnix()), leaderboard).getPlayerPicture(),
                String.join("-", header) + "-" + leaderboardInfo.getUnix(),
                new EmoteReactionMessage(
                        emotes,
                        commandParameters.getUser().getIdLong(),
                        commandParameters.getTextChannel().getIdLong()
                )
        );
    }
}
