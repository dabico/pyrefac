package ch.usi.si.seart.pyrefac;

import ch.usi.si.seart.pyrefac.core.exception.NameAlreadyInUseException;
import ch.usi.si.seart.pyrefac.core.exception.PsiNamedElementNotFoundException;
import ch.usi.si.seart.pyrefac.core.jackson.PyRefacModule;
import ch.usi.si.seart.pyrefac.server.RefactoringHandler;
import ch.usi.si.seart.pyrefac.server.exception.NotRegularFileException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intellij.openapi.application.ApplicationStarter;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
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
        Javalin.create(config -> {
                    config.showJavalinBanner = false;
                    JavalinJackson mapper = new JavalinJackson();
                    SimpleModule module = new PyRefacModule();
                    mapper.updateMapper(delegate -> delegate.registerModule(module));
                    config.jsonMapper(mapper);
                })
                .get("/", ctx -> ctx.status(200))
                .post(RefactoringHandler.PATH, new RefactoringHandler())
                .exception(IllegalArgumentException.class, (ex, ctx) -> ctx.status(400))
                .exception(JsonParseException.class, (ex, ctx) -> ctx.status(400))
                .exception(NotRegularFileException.class, (ex, ctx) -> ctx.status(400))
                .exception(NullPointerException.class, (ex, ctx) -> ctx.status(400))
                .exception(FileNotFoundException.class, (ex, ctx) -> ctx.status(404))
                .exception(PsiNamedElementNotFoundException.class, (ex, ctx) -> ctx.status(404))
                .exception(NameAlreadyInUseException.class, (ex, ctx) -> ctx.status(409))
                .exception(Exception.class, (ex, ctx) -> ctx.status(500))
                .exception(UnsupportedOperationException.class, (ex, ctx) -> ctx.status(501))
                .start(port);
    }
}
