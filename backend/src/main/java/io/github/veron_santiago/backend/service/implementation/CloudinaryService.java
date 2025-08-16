package io.github.veron_santiago.backend.service.implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadField(MultipartFile file, String folder) throws IOException {
        @SuppressWarnings("unchecked")
        Map<String, Object> res = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folder));
        return res.get("secure_url").toString();
    }

}
