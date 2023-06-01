package com.github.dominik48n.party.config;

import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class SwitchServerConfig {

    private final ConfigList whiteList;
    private final ConfigList blackList;

    private SwitchServerConfig(final @NotNull ConfigList whiteList,final @NotNull  ConfigList blackList) {
        this.whiteList = whiteList;
        this.blackList = blackList;
    }

    public boolean whiteEnable() {
        return whiteList.enable();
    }

    public @NotNull List<Pattern> whitePatternList() {
        return this.whiteList.toPatternList();
    }

    public boolean blackEnable() {
        return blackList.enable();
    }

    public @NotNull List<Pattern> blackPatternList() {
        return this.blackList.toPatternList();
    }

    static @NotNull SwitchServerConfig fromDocument(final @NotNull Document document) {
        return new SwitchServerConfig(
                ConfigList.of(document.getDocument("server_white_list")),
                ConfigList.of(document.getDocument("server_black_list"))
        );
    }

    @NotNull Document toDocument() {
        return new Document()
                .append("server_white_list", this.whiteList.toDocument())
                .append("server_black_list", this.blackList.toDocument());
    }

    private record ConfigList(boolean enable, List<String> stringList) {

        private static @NotNull ConfigList of(final @NotNull Document document) {
            return new ConfigList(
                    document.getBoolean("enable", false),
                    document.getList("regex", Collections.singletonList("^Lobby.*"), Document.stringCollector())
            );
        }

        private @NotNull Document toDocument() {
            return new Document()
                    .append("enable", enable)
                    .append("regex", stringList, Document::addJsonConsumer);
        }

        private @NotNull List<Pattern> toPatternList() {
            return stringList().stream().map(Pattern::compile).toList();
        }

    }

}
