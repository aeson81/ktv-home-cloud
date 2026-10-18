package com.homektv.web;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 二维码生成（P1.19，详设§11.1）。
 * 内容为 H5 点歌地址 http://<NAS_IP>:8080/m?room=default，供 TV 待机页扫码。
 *
 * QR code generation (P1.19, detailed design §11.1).
 * The content is the H5 song-selection URL http://<NAS_IP>:8080/m?room=default,
 * displayed on the TV standby page for scanning.
 */
@RestController
@RequestMapping("/api")
public class QrController {

    private final ServerAddressService addressService;

    public QrController(ServerAddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * 返回 PNG 二维码。
     *
     * Returns a PNG QR code image.
     * @param room 房间号，默认 default / room identifier, defaults to "default"
     * @param size 边长像素，默认 480，钳制 [120, 1080] / side length in pixels, defaults to 480, clamped to [120, 1080]
     * @return PNG 格式的二维码图片字节流 / PNG-format QR code image byte stream
     */
    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(
            @RequestParam(defaultValue = "default") String room,
            @RequestParam(defaultValue = "480") int size,
            HttpServletRequest request) throws Exception {

        int px = Math.max(120, Math.min(1080, size));
        String content = addressService.h5Url(room, request.getServerName(), request.getServerPort());

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix matrix = new QRCodeWriter()
                .encode(content, BarcodeFormat.QR_CODE, px, px, hints);

        BufferedImage img = new BufferedImage(px, px, BufferedImage.TYPE_INT_RGB);
        int black = 0x000000, white = 0xFFFFFF;
        for (int y = 0; y < px; y++) {
            for (int x = 0; x < px; x++) {
                img.setRGB(x, y, matrix.get(x, y) ? black : white);
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", bos);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                // 地址可能变（DHCP/手填），缓存短一些
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(bos.toByteArray());
    }
}
