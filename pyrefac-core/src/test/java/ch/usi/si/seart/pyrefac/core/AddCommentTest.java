package ch.usi.si.seart.pyrefac.core;

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

public class AddCommentTest extends RefactoringTest {

    private static Stream<Arguments> executables() {
        return Stream.of(
                Arguments.of((Executable) () -> new AddComment(null, null, "Random comment")),
                Arguments.of((Executable) () -> new AddComment(null, "__init__", null))
        );
    }

    @MethodSource("executables")
    @ParameterizedTest(name = "Test {index}")
    public void testThrows(Executable action) {
        Assertions.assertThrows(IllegalArgumentException.class, action);
    }

    @Test
    public void testNoop() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            PyFile file = (PyFile) createLightFile(".py", PythonLanguage.INSTANCE, original);
            Refactoring refactoring = new AddComment(null, "noop", "This should appear in the file");
            refactoring.perform(file);
            Assertions.assertEquals(original, file.getText(), "There should be no changes");
        });
    }
}
