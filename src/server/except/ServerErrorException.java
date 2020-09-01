package server.except;

import java.io.IOException;

public class ServerErrorException extends Exception {

    public ServerErrorException(String msg) {
        super(msg);
    }

}
