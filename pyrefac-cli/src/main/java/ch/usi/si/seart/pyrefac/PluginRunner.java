package ch.usi.si.seart.pyrefac;

import ch.usi.si.seart.pyrefac.cli.ExecutionExceptionHandler;
import ch.usi.si.seart.pyrefac.cli.ParameterExceptionHandler;
import ch.usi.si.seart.pyrefac.cli.PyRefac;
import com.intellij.openapi.application.ApplicationStarter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.util.List;

public class PluginRunner implements ApplicationStarter {

    @Override
    @Nullable
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public String getCommandName() {
        return "pyrefac";
    }

    @Override
    public void main(@NotNull List<String> arguments) {
        String[] strings = arguments.stream().skip(1).toArray(String[]::new);
        int code = new CommandLine(new PyRefac())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setParameterExceptionHandler(new ParameterExceptionHandler())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                .execute(strings);
        System.exit(code);
    }
}
