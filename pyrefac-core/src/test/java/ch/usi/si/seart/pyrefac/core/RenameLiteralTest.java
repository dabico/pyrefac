package ch.usi.si.seart.pyrefac.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RenameLiteralTest extends RefactoringTest {

    private static Stream<Arguments> executables() {
        return Stream.of(
                Arguments.of((Executable) () -> new RenameLiteral(null, null, "old_name", "new_name")),
                Arguments.of((Executable) () -> new RenameLiteral(null, "__init__", null, "new_name")),
                Arguments.of((Executable) () -> new RenameLiteral(null, "__init__", "old_name", null))
        );
    }

    @MethodSource("executables")
    @ParameterizedTest(name = "Test {index}")
    public void testThrows(Executable action) {
        Assertions.assertThrows(IllegalArgumentException.class, action);
    }
}
