package io.github.veron_santiago.backend.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dzq1ckp3c",
                "api_key", "894671154669417",
                "api_secret", "pXvs3GBVLsh0KwvmEQ-atHQg0tA"));
    }
}