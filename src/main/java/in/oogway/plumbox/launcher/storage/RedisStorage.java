package in.oogway.plumbox.launcher.storage;

import in.oogway.plumbox.launcher.config.Config;
import redis.clients.jedis.Jedis;

import java.nio.charset.Charset;

/*
*   @author talina06 on 2/7/18
*/
public interface RedisStorage  extends StorageDriver {

    Jedis jedis = new Jedis(Config.getDirPath("redis_server_address"));

    /**
     * @param key A key whose value is to be read. Eg: source_id: <source yaml file as value>
     * @return byte array of the yaml file contents.
     */
    @Override
    default byte[] read(String key)  {
        String contents = jedis.get(key);
        return contents.getBytes(Charset.forName("UTF-8"));
    }
}
