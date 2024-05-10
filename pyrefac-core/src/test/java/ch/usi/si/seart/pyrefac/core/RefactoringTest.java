package ch.usi.si.seart.pyrefac.core;

import com.intellij.testFramework.junit5.RunInEdt;
import com.intellij.testFramework.junit5.TestApplication;
import com.jetbrains.python.psi.PyFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RunInEdt
@TestApplication
class RefactoringTest extends RefactoringTestCase {

    @Test
    @DisplayName("No changes made when using noop")
    void testNoop() {
        String content = "def func(): pass";
        PyFile file = createLightPythonFile(NAME, content);
        Refactoring refactoring = Refactoring.noop();
        refactoring.perform(file);
        Assertions.assertEquals(content, file.getText(), "There should be no changes");
    }
}
