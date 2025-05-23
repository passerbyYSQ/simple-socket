package top.ysqorz.socket.server;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public interface TcpServer extends Closeable {
    void setup(boolean async) throws IOException;

    void removeClient(String clientId) throws IOException;

    ClientHandler getClient(String clientId);

    List<ClientHandler> getAllClients();

    void broadcast(String text);

    void broadcast(File file);
}
