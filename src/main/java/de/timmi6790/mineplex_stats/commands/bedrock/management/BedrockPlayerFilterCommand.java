package de.timmi6790.mineplex_stats.commands.bedrock.management;

import de.timmi6790.discord_framework.datatypes.MapBuilder;
import de.timmi6790.discord_framework.modules.command.CommandParameters;
import de.timmi6790.discord_framework.modules.command.CommandResult;
import de.timmi6790.discord_framework.modules.command.properties.MinArgCommandProperty;
import de.timmi6790.discord_framework.modules.command.properties.RequiredDiscordBotPermsCommandProperty;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.AbstractEmoteReaction;
import de.timmi6790.discord_framework.modules.emote_reaction.emotereactions.EmptyEmoteReaction;
import de.timmi6790.discord_framework.utilities.discord.DiscordEmotes;
import de.timmi6790.mineplex_stats.commands.bedrock.AbstractBedrockStatsCommand;
import de.timmi6790.mineplex_stats.statsapi.models.bedrock.BedrockGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

import java.util.LinkedHashMap;

public class BedrockPlayerFilterCommand extends AbstractBedrockStatsCommand {
    public BedrockPlayerFilterCommand() {
        super("bfilter", "Filter Bedrock Players", "<game> <player>");

        this.addProperties(
                new MinArgCommandProperty(2),
                new RequiredDiscordBotPermsCommandProperty(Permission.MESSAGE_ADD_REACTION)
        );
    }

    @Override
    protected CommandResult onCommand(final CommandParameters commandParameters) {
        final BedrockGame game = this.getGame(commandParameters, 0);
        final String player = this.getPlayer(commandParameters, 1);


        final EmbedBuilder embedBuilder = this.getEmbedBuilder(commandParameters)
                .addField("Player", player, false)
                .addField("Game", game.getName(), false);

        this.sendEmoteMessage(
                commandParameters,
                embedBuilder.setTitle("Filter Confirm")
                        .setDescription("Are you sure that you want to filter this person?"),
                new MapBuilder<String, AbstractEmoteReaction>(() -> new LinkedHashMap<>(2))
                        .put(DiscordEmotes.CHECK_MARK.getEmote(), new AbstractEmoteReaction() {
                            @Override
                            public void onEmote() {
                                BedrockPlayerFilterCommand.this.getStatsModule().getMpStatsRestClient().addBedrockPlayerFilter(player, game.getName());

                                BedrockPlayerFilterCommand.this.sendTimedMessage(
                                        commandParameters,
                                        embedBuilder.setTitle("Successfully Filtered"),
                                        90
                                );
                            }
                        })
                        .put(DiscordEmotes.RED_CROSS_MARK.getEmote(), new EmptyEmoteReaction())
                        .build()
        );

        return CommandResult.SUCCESS;
    }
}
