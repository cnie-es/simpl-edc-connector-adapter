package eu.europa.ec.simpl.edcconnectoradapter;

import eu.europa.ec.simpl.data1.common.util.ApplicationUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EDCConnectorAdapterApplication {

    public static void main(String[] args) {
        ApplicationUtil.printBannerAndRun(
                new SpringApplication(EDCConnectorAdapterApplication.class),
                args,
                "banner.txt",
                EDCConnectorAdapterApplication.class);
    }
}
