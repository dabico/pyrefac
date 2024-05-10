package ch.usi.si.seart.pyrefac.core;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PyNamedParameterNotFoundException;
import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.RunMethodInEdt;
import com.intellij.testFramework.junit5.TestApplication;
import com.jetbrains.python.psi.PyFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@TestApplication
@RunInEdt(allMethods = false)
class RenameFunctionParametersTest extends RefactoringTestCase {

    private static Stream<Arguments> instantiations() {
        Executable nullFunction = () -> new RenameFunctionParameters(null, null, "old_name", "new_name");
        Executable nullOldName = () -> new RenameFunctionParameters(null, "__init__", null, "new_name");
        Executable nullNewName = () -> new RenameFunctionParameters(null, "__init__", "old_name", null);
        return Stream.of(
                Arguments.of(nullFunction),
                Arguments.of(nullOldName),
                Arguments.of(nullNewName)
        );
    }

    @MethodSource("instantiations")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Constructors throw IAE given null arguments")
    void testThrows(Executable executable) {
        Assertions.assertThrows(IllegalArgumentException.class, executable);
    }

    private static Stream<Arguments> refactorings() {
        return Stream.of(
                Arguments.of(
                        PyNamedParameterNotFoundException.class,
                        new RenameFunctionParameters(null, "func", "baz", "_")
                ),
                Arguments.of(
                        NameAlreadyInUseException.class,
                        new RenameFunctionParameters(null, "func", "foo", "bar")
                )
        );
    }

    @RunMethodInEdt
    @MethodSource("refactorings")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Refactoring throws exceptions")
    void testThrows(Class<? extends Throwable> throwable, Refactoring refactoring) {
        String content = """
                def func(foo, bar):
                    return foo + bar
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Assertions.assertThrows(throwable, () -> refactoring.perform(file));
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Parameter not renamed when function is not found")
    void testNoop() {
        String content = """
                def func(foo, bar):
                    return foo + bar
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Refactoring refactoring = new RenameFunctionParameters(null, "noop", "foo", "_");
        refactoring.perform(file);
        Assertions.assertEquals(content, file.getText(), "There should be no changes");
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Function parameter renamed")
    void testRename() {
        String content = """
                def func(foo, bar):
                    return foo + bar
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Refactoring refactoring = new RenameFunctionParameters(null, "func", "foo", "baz");
        refactoring.perform(file);
        String expected = """
                def func(baz, bar):
                    return baz + bar
                """;
        Assertions.assertEquals(expected, file.getText(), "Parameter should be renamed");
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Refactoring can be chained")
    void testChained() {
        String content = """
                def func(foo, bar):
                    return foo + bar
                """;
        Refactoring refactoring;
        PyFile file = createLightPythonFile(NAME, content);
        refactoring = new RenameFunctionParameters(null, "func", "foo", "baz");
        refactoring.perform(file);
        refactoring = new RenameFunctionParameters(null, "func", "baz", "qux");
        refactoring.perform(file);
        String expected = """
                def func(qux, bar):
                    return qux + bar
                """;
        Assertions.assertEquals(expected, file.getText(), "Parameter should be renamed");
    }
}
