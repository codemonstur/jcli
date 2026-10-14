package jcli.model;

public final class HelpOption {

    public final String name;
    public final String need;
    public final String type;
    public final String description;
    public final String defaultValue;
    public final String group;
    public final boolean deprecated;

    public HelpOption(final String name, final String need, final String type, final String description
            , final String defaultValue) {
        this(name, need, type, description, defaultValue, "", false);
    }

    public HelpOption(final String name, final String need, final String type, final String description
            , final String defaultValue, final String group) {
        this(name, need, type, description, defaultValue, group, false);
    }

    public HelpOption(final String name, final String need, final String type, final String description
            , final String defaultValue, final String group, final boolean deprecated) {
        this.name = name;
        this.need = need;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
        this.group = group;
        this.deprecated = deprecated;
    }
}
