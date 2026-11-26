package pe.edu.idat.essaludmiconsulta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EssaludMiConsultaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EssaludMiConsultaApplication.class, args);
    }

}
