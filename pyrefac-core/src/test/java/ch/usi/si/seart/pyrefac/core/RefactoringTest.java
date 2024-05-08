package ch.usi.si.seart.pyrefac.core;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RefactoringTest extends BasePlatformTestCase {

    protected static final String original =
"""
def stub(foo, bar):
    print(foo)
    print(bar)

class Example:
    def __init__(self, foo, bar):
        self.foo = foo
        self.bar = bar

    def __str__(self):
        return f"foo: {self.foo}, bar: {self.bar}"

def documented():
    \"""This is a documented function.\"""
    pass

class Stub:

    def __init__(self, foo, bar):
        self.foo = foo
        self.bar = bar

    def __str__(self):
        return f"foo: {self.foo}, bar: {self.bar}"

""";

    @Override
    @BeforeAll
    protected void setUp() throws Exception {
        super.setUp();
    }
}
