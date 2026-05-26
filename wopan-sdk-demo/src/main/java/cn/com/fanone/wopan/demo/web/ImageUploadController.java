package cn.com.fanone.wopan.demo.web;

import cn.com.fanone.wopan.WoPanClient;
import cn.com.fanone.wopan.demo.config.WoPanConfigStore;
import cn.com.fanone.wopan.demo.config.WoPanRuntimeConfig;
import cn.com.fanone.wopan.model.DownloadLink;
import cn.com.fanone.wopan.model.UploadRequest;
import cn.com.fanone.wopan.model.UploadResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/images")
public class ImageUploadController {

    private static final long MAX_IMAGE_BYTES = 50L * 1024 * 1024;

    private final WoPanConfigStore configStore;

    public ImageUploadController(WoPanConfigStore configStore) {
        this.configStore = configStore;
    }

    @PostMapping
    public ImageUploadResponse upload(@RequestParam("image") MultipartFile image) throws Exception {
        WoPanRuntimeConfig config = configStore.current();
        if (!config.isUsable()) {
            throw new BadRequestException("请先保存 accessToken 和 refreshToken");
        }
        if (image.isEmpty()) {
            throw new BadRequestException("请选择一张图片");
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("图片不能超过 50MB");
        }

        String fileName = firstNonBlank(image.getOriginalFilename(), "wopan-demo-image");
        String contentType = firstNonBlank(image.getContentType(), URLConnection.guessContentTypeFromName(fileName), "application/octet-stream");
        if (!isImage(fileName, contentType)) {
            throw new BadRequestException("当前 demo 只允许上传图片文件");
        }

        try (WoPanClient client = config.newClient();
             InputStream content = image.getInputStream()) {
            UploadResult result = client.uploadFile(UploadRequest.builder()
                    .spaceType(config.spaceType())
                    .parentId(config.parentId())
                    .familyId(config.familyId())
                    .fileName(fileName)
                    .content(content)
                    .contentType(contentType)
                    .size(image.getSize())
                    .onProgress((uploaded, total) -> System.out.printf(Locale.ROOT, "upload %s: %d/%d%n", fileName, uploaded, total))
                    .onRetry((attempt, uploadedBytes, error) -> System.out.printf(Locale.ROOT, "retry %s attempt=%d uploaded=%d error=%s%n", fileName, attempt, uploadedBytes, error.getMessage()))
                    .build());
            List<DownloadLink> links = client.getDownloadLinks(List.of(result.getFid()));
            String previewUrl = links.isEmpty() ? "" : links.get(0).getUrl();
            return new ImageUploadResponse(
                    result.getFid(),
                    result.getBatchNo(),
                    fileName,
                    image.getSize(),
                    contentType,
                    previewUrl
            );
        }
    }

    private static boolean isImage(String fileName, String contentType) {
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }
        String lowerName = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(".jpg")
                || lowerName.endsWith(".jpeg")
                || lowerName.endsWith(".png")
                || lowerName.endsWith(".gif")
                || lowerName.endsWith(".webp")
                || lowerName.endsWith(".bmp")
                || lowerName.endsWith(".avif");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    public record ImageUploadResponse(
            String fid,
            String batchNo,
            String fileName,
            long size,
            String contentType,
            String previewUrl
    ) {
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static final class BadRequestException extends RuntimeException {
        private BadRequestException(String message) {
            super(message);
        }
    }
}
