package de.timmi6790.mineplex.stats.bedrock.commands.player;

import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.parameters.SlashCommandParameters;
import de.timmi6790.mpstats.api.client.bedrock.player.models.BedrockPlayer;
import de.timmi6790.mpstats.api.client.common.BaseApiClient;
import de.timmi6790.mpstats.api.client.common.filter.models.Reason;

import java.util.EnumSet;
import java.util.Set;

public class UnfilteredBedrockPlayerCommand extends BedrockPlayerCommand {
    public UnfilteredBedrockPlayerCommand(final BaseApiClient<BedrockPlayer> baseApiClient, final SlashCommandModule commandModule) {
        super(
                baseApiClient,
                commandModule,
                "unfilteredBedrockPlayer",
                "Check the unfiltered player stats",
                "ubpl"
        );
    }

    @Override
    protected Set<Reason> getFilterReasons(final SlashCommandParameters commandParameters) {
        return EnumSet.noneOf(Reason.class);
    }
}
