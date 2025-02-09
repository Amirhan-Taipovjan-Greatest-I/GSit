package dev.geco.gsit.manager.mm;

import java.util.regex.*;

import org.bukkit.command.*;
import org.bukkit.entity.*;

import net.kyori.adventure.text.*;
import net.kyori.adventure.text.minimessage.*;
import net.kyori.adventure.text.serializer.json.*;
import net.kyori.adventure.text.serializer.legacy.*;

import dev.geco.gsit.GSitMain;
import dev.geco.gsit.manager.*;

public class MPaperManager extends MManager {

    protected final LegacyComponentSerializer legacyComponentSerializer;
    protected Object jsonComponentSerializer;
    protected final MiniMessage miniMessage;
    protected final Pattern HEX_PATTERN = Pattern.compile(LegacyComponentSerializer.HEX_CHAR + "([a-fA-F0-9]{6})");

    public MPaperManager(GSitMain GPluginMain) {
        super(GPluginMain);
        legacyComponentSerializer = LegacyComponentSerializer.builder().character(LegacyComponentSerializer.AMPERSAND_CHAR).hexColors().build();
        if(GPluginMain.getSVManager().isNewerOrVersion(20, 0)) jsonComponentSerializer = JSONComponentSerializer.json();
        miniMessage = MiniMessage.miniMessage();
    }

    public String getAsJSON(String Text, Object... RawReplaceList) {
        if(jsonComponentSerializer == null) return super.getAsJSON(Text, RawReplaceList);
        return ((JSONComponentSerializer) jsonComponentSerializer).serialize(toFormattedComponent(Text, RawReplaceList));
    }

    public String toFormattedMessage(String Text, Object... RawReplaceList) { return legacyComponentSerializer.serialize(toFormattedComponent(Text, RawReplaceList)); }

    public void sendMessage(CommandSender Target, String Message, Object... ReplaceList) { Target.sendMessage(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    public void sendActionBarMessage(Player Target, String Message, Object... ReplaceList) { Target.sendActionBar(getLanguageComponent(Message, getLanguage(Target), ReplaceList)); }

    private Component getLanguageComponent(String Message, String LanguageCode, Object... ReplaceList) { return toFormattedComponent(getRawLanguageMessage(Message, LanguageCode, ReplaceList)); }

    private Component toFormattedComponent(String Text, Object... RawReplaceList) {
        Component component = miniMessage.deserialize(Text);
        Text = legacyComponentSerializer.serialize(component);
        Text = replaceHexColors(Text);
        component = legacyComponentSerializer.deserialize(Text);
        if(RawReplaceList.length > 0 && RawReplaceList.length % 2 == 0) component = applyRawReplacements(component, RawReplaceList);
        return component;
    }

    private String replaceHexColors(String Text) {
        Matcher matcher = HEX_PATTERN.matcher(Text);
        StringBuilder result = new StringBuilder(Text.length());
        int lastIndex = 0;
        while(matcher.find()) {
            result.append(Text, lastIndex, matcher.start()).append(LegacyComponentSerializer.AMPERSAND_CHAR).append(matcher.group());
            lastIndex = matcher.end();
        }
        result.append(Text.substring(lastIndex));
        return result.toString();
    }

    private Component applyRawReplacements(Component TextComponent, Object... RawReplaceList) {
        for(int count = 0; count < RawReplaceList.length; count += 2) {
            if(RawReplaceList[count] == null || RawReplaceList[count + 1] == null) continue;
            int finalCount = count;
            TextComponent = TextComponent.replaceText(b -> b.matchLiteral(RawReplaceList[finalCount].toString()).replacement(Component.text(RawReplaceList[finalCount + 1].toString())));
        }
        return TextComponent;
    }

}