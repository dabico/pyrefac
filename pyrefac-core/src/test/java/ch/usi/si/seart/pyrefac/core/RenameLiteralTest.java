package ch.usi.si.seart.pyrefac.core;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PsiNamedElementNotFoundException;
import com.intellij.openapi.application.ApplicationManager;
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
class RenameLiteralTest extends RefactoringTest {

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

    @MethodSource("refactorings")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Refactoring throws exceptions")
    void testThrows(Class<? extends Throwable> throwable, Refactoring refactoring) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String content = """
                    def func():
                        a = 1
                        b = 2
                        return a + b
                    """;
            PyFile file = createLightPythonFile(NAME, content);
            Assertions.assertThrows(throwable, () -> refactoring.perform(file));
        });
    }

    @Test
    @DisplayName("Literal not renamed when function is not found")
    void testNoop() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
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
        });
    }
}
