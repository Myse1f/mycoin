/**
 * Created By Yufan Wu
 * 2019/4/18
 */
package main;

import com.google.common.collect.Sets;
import net.NetworkParameters;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"core", "utils", "restapi", "persistence"})
@EnableSwagger2
public class Main {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
    }

    @Bean("network_params")
    public NetworkParameters testNet() {
        return new NetworkParameters(NetworkParameters.ID_TESTNET);
    }

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .protocols(Sets.newHashSet("http")) //协议，http或https
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("restapi")) //一定要写对，会在这个路径下扫描controller定义
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("REST接口定义")
                .version("1.0")
                .description("用于测试RESTful API")
                .build();
    }
}
