package grupo4.fastbuyback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FastBuyBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(FastBuyBackApplication.class, args);
    }

}
