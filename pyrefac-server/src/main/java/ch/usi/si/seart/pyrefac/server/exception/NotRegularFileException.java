package ch.usi.si.seart.pyrefac.server.exception;

import java.nio.file.FileSystemException;

public class NotRegularFileException extends FileSystemException {

    public NotRegularFileException(String file) {
        super(file);
    }
}
