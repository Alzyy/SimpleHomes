package it.alzy.simplehomes.records;

public record Database(String host, String username, String password, int port, String databaseName, int maxPool) {
    
    public String getJDBC() {
        return "jdbc:mysql://" + host + ":" + port + "/" + databaseName;
    }
}
