package jcli.model;

public final class HelpOption {

    public final String name;
    public final String need;
    public final String type;
    public final String description;
    public final String defaultValue;

    public HelpOption(final String name, final String need, final String type, final String description, final String defaultValue) {
        this.name = name;
        this.need = need;
        this.type = type;
        this.description = description;
        this.defaultValue = defaultValue;
    }
}
