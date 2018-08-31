package registry;

import java.util.List;

public interface IRegistry {

    // 注册服务
    void register(String serviceName, int port, String type) throws Exception;

}
