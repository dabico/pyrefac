package ch.usi.si.seart.pyrefac.core;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RefactoringTest extends BasePlatformTestCase {

    protected final static String NAME = ".py";

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setUp();
    }
}
