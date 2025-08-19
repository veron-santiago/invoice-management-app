package io.github.veron_santiago.backend.service.implementation;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadField(MultipartFile file, String folder, String name, boolean isPdf) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("folder", folder);
        map.put("public_id", name);
        if (isPdf) {
            map.put("resource_type", "raw");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> res = cloudinary.uploader().upload(file.getBytes(), map);
        return res.get("secure_url").toString();
    }

}
