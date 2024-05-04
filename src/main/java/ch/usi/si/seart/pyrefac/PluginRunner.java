package ch.usi.si.seart.pyrefac;

import com.intellij.openapi.application.ApplicationStarter;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PluginRunner implements ApplicationStarter {

    private static final Logger LOG = Logger.getInstance(PluginRunner.class);

    @Override
    @Nullable
    public String getCommandName() {
        return "pyrefac";
    }

    @Override
    public void main(@NotNull List<String> arguments) {
        LOG.warn("Running PyRefac with arguments: " + arguments);
        System.exit(0);
    }
}
