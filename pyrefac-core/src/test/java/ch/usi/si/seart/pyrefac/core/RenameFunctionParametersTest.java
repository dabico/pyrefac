package ch.usi.si.seart.pyrefac.core;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PyNamedParameterNotFoundException;
import com.intellij.openapi.application.ApplicationManager;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RenameFunctionParametersTest extends RefactoringTest {

    private static Stream<Arguments> instantiations() {
        Executable nullFunction = () -> new RenameFunctionParameters(null, null, "old_name", "new_name");
        Executable nullOldName = () -> new RenameFunctionParameters(null, "__init__", null, "new_name");
        Executable nullNewName = () -> new RenameFunctionParameters(null, "__init__", "old_name", null);
        return Stream.of(
                Arguments.of(IllegalArgumentException.class, nullFunction),
                Arguments.of(IllegalArgumentException.class, nullOldName),
                Arguments.of(IllegalArgumentException.class, nullNewName)
        );
    }

    @MethodSource("instantiations")
    @ParameterizedTest(name = "{index}: {0}")
    public void testThrows(Class<? extends Throwable> throwable, Executable executable) {
        Assertions.assertThrows(throwable, executable);
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

    @MethodSource("refactorings")
    @ParameterizedTest(name = "{index}: {0}")
    public void testThrows(Class<? extends Throwable> throwable, Refactoring refactoring) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String content = "def func(foo, bar): pass";
            PyFile file = (PyFile) createLightFile(NAME, PythonLanguage.INSTANCE, content);
            Assertions.assertThrows(throwable, () -> refactoring.perform(file));
        });
    }

    @Test
    public void testNoop() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String content = "def func(foo, bar): pass";
            PyFile file = (PyFile) createLightFile(NAME, PythonLanguage.INSTANCE, content);
            Refactoring refactoring = new RenameFunctionParameters(null, "noop", "foo", "_");
            refactoring.perform(file);
            Assertions.assertEquals(content, file.getText(), "There should be no changes");
        });
    }
}
