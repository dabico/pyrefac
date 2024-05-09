package ch.usi.si.seart.pyrefac.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RenameLiteralTest extends RefactoringTest {

    @Test
    public void testThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RenameLiteral(null, null, "old_name", "new_name"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RenameLiteral(null, "__init__", null, "new_name"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new RenameLiteral(null, "__init__", "old_name", null));
    }
}
