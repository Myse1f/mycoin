/**
 * Created By Yufan Wu
 * 2019/4/18
 */
package main;

import exception.BlockPersistenceException;
import net.NetworkParameters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import persistence.BlockPersistence;
import persistence.LevelDBBlockPersistence;

import java.io.File;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"core", "utils", "restapi"})
public class Main {

    public static void main(String[] args) throws IOException {
        NetworkParameters.setNetworkParameters(NetworkParameters.ID_TESTNET);
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public BlockPersistence levelDB() throws BlockPersistenceException {
        return new LevelDBBlockPersistence(new File("data"));
    }
}
