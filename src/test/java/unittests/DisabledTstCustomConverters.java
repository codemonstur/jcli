package unittests;

public class DisabledTstCustomConverters {

/*
    Custom converters are implemented: CliParserBuilder.addClassConverter(Class, StringToType) registers
    a converter for a type, and it is used for both options and positionals.
    See TestCustomConverters for the tests.

    Still open is the annotation driven variant:

    One way:
    @Parameter(shortName = 'o', mappedBy = PositiveNumberMapper.class, optional = true)
    abstract Optional<Integer> optionalNumber();

    Second way:
    Add a Map of interfaces that accept a Class<T> and String and spit out T
*/
}
