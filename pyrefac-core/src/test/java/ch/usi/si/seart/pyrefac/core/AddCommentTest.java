package ch.usi.si.seart.pyrefac.core;

import com.intellij.openapi.application.ApplicationManager;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AddCommentTest extends RefactoringTest {

    @Test
    public void testThrows() {
        Assertions.assertThrows(NullPointerException.class, () -> new AddComment("Stub", null, "Random comment"));
        Assertions.assertThrows(NullPointerException.class, () -> new AddComment("Stub", "__init__", null));
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
