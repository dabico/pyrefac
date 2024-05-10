package ch.usi.si.seart.pyrefac.server;

import ch.usi.si.seart.pyrefac.server.exception.NotRegularFileException;
import com.intellij.openapi.util.io.FileUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RefactoringHandler implements Handler {

    public static final String PATH = "/{owner}/{name}/*";

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        String owner = ctx.pathParam("owner");
        String name = ctx.pathParam("name");
        String prefix = String.format("/%s/%s/", owner, name);
        String url = String.format("https://github.com/%s/%s.git", owner, name);
        String path = ctx.path();
        String file = StringUtils.removeStart(path, prefix);
        try (
                TemporaryDirectory root = TemporaryDirectory.create();
                Git ignored = Git.cloneRepository()
                        .setDirectory(root.path().toFile())
                        .setURI(url)
                        .setDepth(1)
                        .call()
        ) {
            Path absolute = root.path().resolve(file);
            if (!Files.exists(absolute))
                throw new FileNotFoundException(absolute.toString());
            if (!Files.isRegularFile(absolute))
                throw new NotRegularFileException(absolute.toString());
            String contents = Files.readString(absolute);
            ctx.html("<pre><code>" + contents + "</code></pre>");
        }
    }

    private record TemporaryDirectory(Path path) implements AutoCloseable {

        public static TemporaryDirectory create() throws IOException {
            Path path = Files.createTempDirectory("pyrefac-server-");
            return new TemporaryDirectory(path);
        }

        @Override
        public void close() throws IOException {
            FileUtil.delete(path);
        }
    }
}
