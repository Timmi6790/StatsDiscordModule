package de.timmi6790.mineplex.stats.common;

import de.timmi6790.discord_framework.module.AbstractModule;
import de.timmi6790.discord_framework.module.modules.config.ConfigModule;
import de.timmi6790.discord_framework.module.modules.reactions.button.ButtonReactionModule;
import de.timmi6790.discord_framework.module.modules.setting.SettingModule;
import de.timmi6790.discord_framework.module.modules.slashcommand.SlashCommandModule;
import de.timmi6790.mineplex.stats.common.commands.info.AboutCommand;
import de.timmi6790.mineplex.stats.common.settings.DisclaimerMessagesSetting;
import de.timmi6790.mineplex.stats.common.settings.FilterReasonSetting;
import de.timmi6790.mpstats.api.client.MpStatsApiClient;
import lombok.Getter;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class BaseMineplexStatsModule extends AbstractModule {
    public static String TIME_ZONE = "-05:00";

    public static ZonedDateTime getMineplexTimeZone(final ZonedDateTime zonedDateTime) {
        return zonedDateTime
                .withZoneSameInstant(ZoneOffset.of(BaseMineplexStatsModule.TIME_ZONE));
    }

    @Getter
    private MpStatsApiClient mpStatsApiClient;

    public BaseMineplexStatsModule() {
        super("BaseMineplexStats");

        this.addDependenciesAndLoadAfter(
                SlashCommandModule.class,
                ButtonReactionModule.class,
                ConfigModule.class,
                SettingModule.class
        );
    }

    @Override
    public boolean onInitialize() {
        final Config config = this.getModuleOrThrow(ConfigModule.class).registerAndGetConfig(this, new Config());

        this.mpStatsApiClient = new MpStatsApiClient(
                config.getApi().getUrl(),
                config.getApi().getKey()
        );

        this.getModuleOrThrow(SettingModule.class).registerSettings(
                this,
                new FilterReasonSetting(),
                new DisclaimerMessagesSetting()
        );

        final SlashCommandModule commandModule = this.getModuleOrThrow(SlashCommandModule.class);
        commandModule.registerCommands(
                this,
                new AboutCommand(commandModule)
        );

        return true;
    }
}
