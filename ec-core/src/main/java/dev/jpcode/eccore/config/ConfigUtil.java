package dev.jpcode.eccore.config;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static dev.jpcode.eccore.config.Config.LOGGER;
import static dev.jpcode.eccore.util.TextUtil.parseText;

/**
 * Various parsers, etc.
 */
public final class ConfigUtil {

    private ConfigUtil() {}

    // TODO do not delclair serializer objects out here. Pretty sure is bad for concurrent parsing.
    private static final Style.Serializer styleJsonDeserializer = new Style.Serializer();

    public static Style parseStyleOrDefault(String styleStr, String defaultStyleStr) {
        Style outStyle = null;
        if (styleStr != null) {
            outStyle = parseStyle(styleStr);
        }

        if (outStyle == null) {
            outStyle = parseStyle(defaultStyleStr);
            LOGGER.log(
                    Level.WARN,
                    String.format("Could not load malformed style: '%s'. Using default, '%s'.", styleStr, defaultStyleStr)
            );
        }
        return outStyle;
    }

    public static Style parseStyle(String styleStr) {
        Style outStyle = null;
        Formatting formatting = Formatting.byName(styleStr);
        if (formatting != null) {
            outStyle = Style.EMPTY.withFormatting(formatting);
        }

        if (outStyle == null) {
            try {
                outStyle = styleJsonDeserializer.deserialize(
                        JsonParser.parseString(styleStr),
                        null, null
                );
            } catch (JsonSyntaxException e) {
                LOGGER.log(Level.ERROR, String.format(
                        "Malformed Style JSON in config: %s", styleStr
                ));
            }

        }

        return outStyle;
    }

    public static Text parseTextOrDefault(String textStr, String defaultTextStr) {
        Text outText = null;
        if (textStr != null) {
            outText = parseText(textStr);
        }

        if (outText == null) {
            outText = parseText(defaultTextStr);
            LOGGER.log(
                    Level.WARN,
                    String.format("Could not load malformed Text: '%s'. Using default, '%s'.", textStr, defaultTextStr)
            );
        }
        return outText;
    }

    public static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            logNumberParseError(s, "int");
        }
        return -1;
    }

    public static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            logNumberParseError(s, "double");
        }
        return -1;
    }

    @Contract(pure = true)
    public static <T> @NotNull ValueParser<List<T>> csvParser(ValueParser<T> valueParser) {
        return (String value) -> parseCsv(value, valueParser);
    }

    public static <T> List<T> parseCsv(@NotNull String csvString, @NotNull ValueParser<T> valueParser) {
        return Arrays.stream(csvString.split(",")).sequential().map(String::trim)
                .map(valueParser::parseValue).collect(Collectors.toList());
    }

    @Contract(pure = true)
    public static <T> @NotNull ValueParser<List<T>> arrayParser(ValueParser<T> valueParser) {
        return (String value) -> parseArray(value, valueParser);
    }

    public static <T> List<T> parseArray(@NotNull String arrayString, @NotNull ValueParser<T> valueParser) {
        int endIdx = arrayString.indexOf(']');
        return parseCsv(
                arrayString.substring(arrayString.indexOf('[') + 1, endIdx == -1 ? arrayString.length() : endIdx),
                valueParser
        );
    }

    private static void logNumberParseError(String num, String type) {
        Config.LOGGER.log(Level.WARN, String.format(
                "Invalid number format for type '%s' in config. Value provided: '%s'", type, num
        ));
    }

    public static String serializeStyle(Style style) {
        return String.valueOf(styleJsonDeserializer.serialize(style, null, null));
    }
}
