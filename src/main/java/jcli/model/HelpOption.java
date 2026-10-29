package jcli.model;

/**
 * The HelpOption class.
 */
public final class HelpOption {

    /**
     * The name.
     */
    public final String name;
    /**
     * The need.
     */
    public final String need;
    /**
     * The type.
     */
    public final String type;
    /**
     * The description.
     */
    public final String description;
    /**
     * The defaultValue.
     */
    public final String defaultValue;
    /**
     * The group.
     */
    public final String group;
    /**
     * The deprecated.
     */
    public final boolean deprecated;

    /**
     * HelpOption.
      * @param name the name
      * @param need the need
      * @param type the type
     * @param description the description
     * @param defaultValue the defaultValue
     */
    public HelpOption(final String name, final String need, final String type, final String description
            , final String defaultValue) {
        this(name, need, type, description, defaultValue, "", false);
    }

    /**
     * HelpOption.
      * @param name the name
      * @param need the need
      * @param type the type
     * @param description the description
     * @param defaultValue the defaultValue
     * @param group the group
     */
    public HelpOption(final String name, final String need, final String type, final String description
            , final String defaultValue, final String group) {
        this(name, need, type, description, defaultValue, group, false);
    }

    /**
     * HelpOption.
      * @param name the name
      * @param need the need
      * @param type the type
     * @param description the description
     * @param defaultValue the defaultValue
     * @param group the group
     * @param deprecated the deprecated
     */
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
