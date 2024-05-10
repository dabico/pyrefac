package ch.usi.si.seart.pyrefac.core;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PsiNamedElementNotFoundException;
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
class RenameLiteralTest extends RefactoringTestCase {

    private static Stream<Arguments> instantiations() {
        Executable nullFunction = () -> new RenameLiteral(null, null, "old_name", "new_name");
        Executable nullOldName = () -> new RenameLiteral(null, "__init__", null, "new_name");
        Executable nullNewName = () -> new RenameLiteral(null, "__init__", "old_name", null);
        return Stream.of(
                Arguments.of(IllegalArgumentException.class, nullFunction),
                Arguments.of(IllegalArgumentException.class, nullOldName),
                Arguments.of(IllegalArgumentException.class, nullNewName)
        );
    }

    @MethodSource("instantiations")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Constructors throw IAE given null arguments")
    void testThrows(Class<? extends Throwable> throwable, Executable executable) {
        Assertions.assertThrows(throwable, executable);
    }

    private static Stream<Arguments> refactorings() {
        return Stream.of(
                Arguments.of(
                        PsiNamedElementNotFoundException.class,
                        new RenameLiteral(null, "func", "d", "_")
                ),
                Arguments.of(
                        NameAlreadyInUseException.class,
                        new RenameLiteral(null, "func", "a", "b")
                )
        );
    }

    @RunMethodInEdt
    @MethodSource("refactorings")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Refactoring throws exceptions")
    void testThrows(Class<? extends Throwable> throwable, Refactoring refactoring) {
        String content = """
                def func():
                    a = 1
                    b = 2
                    return a + b
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Assertions.assertThrows(throwable, () -> refactoring.perform(file));
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Literal not renamed when function is not found")
    void testNoop() {
        String content = """
                def func():
                    a = 1
                    b = 2
                    return a + b
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Refactoring refactoring = new RenameFunctionParameters(null, "noop", "a", "_");
        refactoring.perform(file);
        Assertions.assertEquals(content, file.getText(), "There should be no changes");
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Literal renamed in function")
    void testRename() {
        String content = """
                def func():
                    a = 1
                    b = 2
                    return a + b
                """;
        PyFile file = createLightPythonFile(NAME, content);
        Refactoring refactoring = new RenameLiteral(null, "func", "a", "_");
        refactoring.perform(file);
        String expected = """
                def func():
                    _ = 1
                    b = 2
                    return _ + b
                """;
        Assertions.assertEquals(expected, file.getText(), "Literal should be renamed");
    }

    @Test
    @RunMethodInEdt
    @DisplayName("Refactoring can be chained")
    void testChaining() {
        String content = """
                def func():
                    a = 1
                    b = 2
                    return a + b
                """;
        Refactoring refactoring;
        PyFile file = createLightPythonFile(NAME, content);
        refactoring = new RenameLiteral(null, "func", "a", "x");
        refactoring.perform(file);
        refactoring = new RenameLiteral(null, "func", "x", "y");
        refactoring.perform(file);
        String expected = """
                def func():
                    y = 1
                    b = 2
                    return y + b
                """;
        Assertions.assertEquals(expected, file.getText(), "Literal should be renamed");
    }
}
