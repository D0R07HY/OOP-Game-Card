public interface ServerContext {
    
    void processClientAction(String action, ClientHandler sender);

    void removeClient(ClientHandler handler);
}
