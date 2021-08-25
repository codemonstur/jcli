package unittests;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
import static com.tngtech.archunit.library.GeneralCodingRules.*;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = { "jcli" })
public class TestArchitecture {

    @ArchTest
    private final ArchRule no_accesses_to_upper_package = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

    @ArchTest
    private final ArchRule no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

    @ArchTest
    private final ArchRule no_jodatime = NO_CLASSES_SHOULD_USE_JODATIME;

    @ArchTest
    private final ArchRule no_field_injection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;


    @ArchTest
    private final ArchRule interfaces_should_not_have_names_ending_with_the_word_interface =
        noClasses().that().areInterfaces().should().haveNameMatching(".*Interface");

    @ArchTest
    private final ArchRule no_classes_have_name_ending_in_impl =
        noClasses().should().haveNameMatching(".*Impl");

    @ArchTest
    private final ArchRule no_interfaces_have_name_starting_with_i =
        noClasses().that().areInterfaces().should().haveNameMatching("I[A-Z].*");

    @ArchTest
    private final ArchRule no_underscores_in_method_names = noMethods().should().haveNameMatching(".*_.*");

    @ArchTest
    private final ArchRule no_underscores_in_field_names = noFields().that()
        .doNotHaveModifier(FINAL).or().doNotHaveModifier(STATIC).should().haveNameMatching(".*_.*");

    @ArchTest
    private final ArchRule no_static_and_not_final_fields = noFields().that()
        .doNotHaveModifier(FINAL).should().haveModifier(STATIC);

    @ArchTest
    private final ArchRule no_classes_named_abstract = noClasses().should().haveNameMatching("Abstract.*");

}
