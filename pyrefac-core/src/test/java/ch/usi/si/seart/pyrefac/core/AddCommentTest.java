package ch.usi.si.seart.pyrefac.core;

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
class AddCommentTest extends RefactoringTest {

    private static Stream<Arguments> instantiations() {
        Executable nullFunction = () -> new AddComment(null, null, "Comment");
        Executable nullComment = () -> new AddComment(null, "__init__", null);
        return Stream.of(
                Arguments.of(IllegalArgumentException.class, nullFunction),
                Arguments.of(IllegalArgumentException.class, nullComment)
        );
    }

    @MethodSource("instantiations")
    @ParameterizedTest(name = "{index}: {0}")
    @DisplayName("Constructors throw IAE given null arguments")
    void testThrows(Class<? extends Throwable> throwable, Executable executable) {
        Assertions.assertThrows(throwable, executable);
    }

    @Test
    @DisplayName("Comment not added when function is not found")
    void testNoop() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            String content = "def func(): pass";
            PyFile file = createLightPythonFile(NAME, content);
            Refactoring refactoring = new AddComment(null, "noop", "This should appear in the file");
            refactoring.perform(file);
            Assertions.assertEquals(content, file.getText(), "There should be no changes");
        });
    }
}
