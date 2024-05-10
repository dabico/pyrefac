package ch.usi.si.seart.pyrefac;

import ch.usi.si.seart.pyrefac.server.RefactoringHandler;
import ch.usi.si.seart.pyrefac.server.exception.NotRegularFileException;
import com.intellij.openapi.application.ApplicationStarter;
import io.javalin.Javalin;
import org.eclipse.jgit.errors.TransportException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.List;

public class ServerRunner implements ApplicationStarter {

    @Override
    @Nullable
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public String getCommandName() {
        return "pyrefac-server";
    }

    @Override
    public void main(@NotNull List<String> args) {
        int port = args.stream().skip(1)
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElse(8080);
        Javalin.create()
                .get("/", ctx -> ctx.status(200))
                .get(RefactoringHandler.PATH, new RefactoringHandler())
                .exception(NotRegularFileException.class, (ex, ctx) -> ctx.status(400))
                .exception(TransportException.class, (ex, ctx) -> ctx.status(400))
                .exception(FileNotFoundException.class, (ex, ctx) -> ctx.status(404))
                .exception(Exception.class, (ex, ctx) -> ctx.status(500))
                .start(port);
    }
}
