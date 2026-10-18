package com.homektv.library;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class FileHashService {

    public String md5(Path path) {
        try (InputStream in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) >= 0) {
                digest.update(buf, 0, read);
            }
            return toHex(digest.digest());
        } catch (IOException e) {
            throw new IllegalStateException("读取文件失败: " + path, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 不可用", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            sb.append(Character.forDigit((value >> 4) & 0xF, 16));
            sb.append(Character.forDigit(value & 0xF, 16));
        }
        return sb.toString();
    }
}
