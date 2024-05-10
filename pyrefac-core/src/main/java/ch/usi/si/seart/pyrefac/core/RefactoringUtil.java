package ch.usi.si.seart.pyrefac.core;

import com.intellij.openapi.util.text.StringUtil;

public final class RefactoringUtil {

    private RefactoringUtil() {
    }

    public static Class<? extends Refactoring> getImplementationClass(String name) {
        String className = StringUtil.toTitleCase(name).replace("_", "");
        String packageName = Refactoring.class.getPackageName();
        String fullyQualifiedName = packageName + "." + className;

        try {
            return Class.forName(fullyQualifiedName).asSubclass(Refactoring.class);
        } catch (ClassNotFoundException ex) {
            throw new UnsupportedOperationException("Unsupported refactoring: " + name, ex);
        } catch (ClassCastException ex) {
            throw new UnsupportedOperationException("Can not be used for refactoring: " + className, ex);
        }
    }
}
