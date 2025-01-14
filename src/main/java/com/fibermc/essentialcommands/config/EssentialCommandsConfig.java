package com.fibermc.essentialcommands.config;

import com.fibermc.essentialcommands.ECPerms;
import dev.jpcode.eccore.config.Config;
import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;
import static dev.jpcode.eccore.config.ConfigUtil.arrayParser;
import static dev.jpcode.eccore.config.ConfigUtil.parseStyle;
import static dev.jpcode.eccore.util.TextUtil.parseText;

public final class EssentialCommandsConfig extends Config {

    @ConfigOption public final Option<Style> FORMATTING_DEFAULT =     new Option<>("formatting_default", parseStyle("gold"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ACCENT =      new Option<>("formatting_accent", parseStyle("light_purple"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ERROR =       new Option<>("formatting_error", parseStyle("red"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Text>  NICKNAME_PREFIX =        new Option<>("nickname_prefix", parseText("{\"text\":\"~\",\"color\":\"red\"}"), TextUtil::parseText, Text.Serializer::toJson);
    @ConfigOption public final Option<Boolean> ENABLE_BACK =            new Option<>("enable_back", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_HOME =            new Option<>("enable_home", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_SPAWN =           new Option<>("enable_spawn", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TPA =             new Option<>("enable_tpa", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WARP =            new Option<>("enable_warp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_NICK =            new Option<>("enable_nick", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_RTP =             new Option<>("enable_rtp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_FLY =             new Option<>("enable_fly", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_INVULN =          new Option<>("enable_invuln", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WORKBENCH =       new Option<>("enable_workbench", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ANVIL =           new Option<>("enable_anvil", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ENDERCHEST =      new Option<>("enable_enderchest", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WASTEBIN =        new Option<>("enable_wastebin", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ESSENTIALSX_CONVERT = new Option<>("enable_experimental_essentialsx_converter", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TOP =             new Option<>("enable_top", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_GAMETIME =        new Option<>("enable_gametime", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_MOTD =            new Option<>("enable_motd", false, Boolean::parseBoolean);
    @ConfigOption public final Option<List<Integer>> HOME_LIMIT =       new Option<>("home_limit", List.of(1, 2, 5), arrayParser(ConfigUtil::parseInt));
    @ConfigOption public final Option<Double>  TELEPORT_COOLDOWN =      new Option<>("teleport_cooldown", 1.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Double>  TELEPORT_DELAY =         new Option<>("teleport_delay", 0.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Boolean> ALLOW_BACK_ON_DEATH =    new Option<>("allow_back_on_death", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> TELEPORT_REQUEST_DURATION = new Option<>("teleport_request_duration", 60, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> USE_PERMISSIONS_API =    new Option<>("use_permissions_api", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> CHECK_FOR_UPDATES =      new Option<>("check_for_updates", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> TELEPORT_INTERRUPT_ON_DAMAGED = new Option<>("teleport_interrupt_on_damaged", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ALLOW_TELEPORT_BETWEEN_DIMENSIONS = new Option<>("allow_teleport_between_dimensions", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> OPS_BYPASS_TELEPORT_RULES =  new Option<>("ops_bypass_teleport_rules", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> NICKNAMES_IN_PLAYER_LIST =   new Option<>("nicknames_in_player_list", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> NICKNAME_MAX_LENGTH =    new Option<>("nickname_max_length", 32, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_RADIUS =             new Option<>("rtp_radius", 1000, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_COOLDOWN =           new Option<>("rtp_cooldown", 30, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_MAX_ATTEMPTS =       new Option<>("rtp_max_attempts", 15, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> BROADCAST_TO_OPS =       new Option<>("broadcast_to_ops", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> NICK_REVEAL_ON_HOVER =   new Option<>("nick_reveal_on_hover", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> GRANT_LOWEST_NUMERIC_BY_DEFAULT = new Option<>("grant_lowest_numeric_by_default", true, Boolean::parseBoolean);
    @ConfigOption public final Option<String> LANGUAGE = new Option<>("language", "en_us", String::toString);
    @ConfigOption public final Option<String> MOTD = new Option<>("motd", "<yellow>Welcome to our server <blue>%player:displayname%</blue>!\nPlease read the rules.</yellow>", String::toString);

    public EssentialCommandsConfig(Path savePath, String displayName, String documentationLink) {
        super(savePath, displayName, documentationLink);
        HOME_LIMIT.changeEvent.register(newValue ->
                ECPerms.Registry.Group.home_limit_group = ECPerms.makeNumericPermissionGroup("essentialcommands.home.limit", newValue)
        );
    }

    public static <T> T getValueSafe(@NotNull Option<T> option, T defaultValue) {
        try {
            return option.getValue();
        } catch (Exception ex) {
            // Someone was getting an error with eccore/config/Option not being found when Option.getValue() was called
            // from within ServerPlayerEntityMixin. I can't reproduce, but /shrug
            // We're actually catching a ClassNotFoundException due to mixin weirdness, I think...
            LOGGER.error(ex);
        }
        return defaultValue;
    }

}
