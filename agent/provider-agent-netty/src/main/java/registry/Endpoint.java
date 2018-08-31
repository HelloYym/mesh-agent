package registry;

public class Endpoint {
    private final String host;
    private final int port;
    private final String type;
    private final int weight;
    private final String url;



    public Endpoint(String host,int port, String type){
        this.host = host;
        this.port = port;
        this.type = type;

        switch(type){
            case "provider-small" :
                this.weight = 5;
                break;
            case "provider-medium" :
                this.weight = 8;
                break;
            case "provider-large":
                this.weight = 11;
                break;
            default :
                this.weight = 0;
        }

        this.url =  "http://" + this.host + ":" + this.port;
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;

    }

    public int getWeight() {
        return weight;
    }

    public String getType() {
        return type;
    }

    public int getPort() {
        return port;
    }

    public String toString(){
        return host + ":" + port;
    }

    public boolean equals(Object o){
        if (!(o instanceof Endpoint)){
            return false;
        }
        Endpoint other = (Endpoint) o;
        return other.host.equals(this.host) && other.port == this.port;
    }

    public int hashCode(){
        return host.hashCode() + port;
    }
}
