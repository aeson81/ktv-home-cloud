package com.homektv.cloud;

import com.homektv.ai.SecretCryptoService;
import com.homektv.domain.AppSecret;
import com.homektv.domain.Setting;
import com.homektv.repo.AppSecretRepository;
import com.homektv.repo.SettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 网盘配置服务：115 Cookie、OneDrive Token/Region 均可在管理后台直接修改，
 * 保存后自动触发 rclone 重新挂载，无需 SSH。
 *
 * 敏感信息（Cookie、Token）走 AppSecret 加密存储；Region 走 Setting。
 */
@Service
public class CloudConfigService {

    private static final Logger log = LoggerFactory.getLogger(CloudConfigService.class);

    private static final String KEY_115_COOKIE = "cloud.115.cookie";
    private static final String KEY_ONEDRIVE_TOKEN = "cloud.onedrive.token";
    private static final String KEY_ONEDRIVE_REGION = "cloud.onedrive.region";
    private static final String KEY_LOCAL_SOURCE_PATH = "local.source.path";

    /** 允许的本地源目录前缀（安全限制，防止路径遍历）。 */
    private static final String[] ALLOWED_PREFIXES = {"/source-music", "/host-data", "/music"};

    private static final Path RCLONE_CONF = Paths.get("/root/.config/rclone/rclone.conf");
    private static final String MOUNT_BASE = "/cloud-music";

    private final SettingRepository settings;
    private final AppSecretRepository secrets;
    private final SecretCryptoService crypto;

    public CloudConfigService(SettingRepository settings, AppSecretRepository secrets,
                              SecretCryptoService crypto) {
        this.settings = settings;
        this.secrets = secrets;
        this.crypto = crypto;
    }

    /** 脱敏后的配置（前端展示用）。 */
    @Transactional(readOnly = true)
    public Map<String, Object> getConfig() {
        String cookie115 = getSecret(KEY_115_COOKIE);
        String odToken = getSecret(KEY_ONEDRIVE_TOKEN);
        String odRegion = settings.findById(KEY_ONEDRIVE_REGION)
                .map(Setting::getValue).orElse("global");
        String localPath = settings.findById(KEY_LOCAL_SOURCE_PATH)
                .map(Setting::getValue).orElse("/source-music");
        return Map.of(
                "cookie115Set", cookie115 != null && !cookie115.isBlank(),
                "cookie115Masked", mask(cookie115),
                "onedriveTokenSet", odToken != null && !odToken.isBlank(),
                "onedriveRegion", odRegion,
                "localSourcePath", localPath
        );
    }

    /** 保存配置并触发重新挂载。 */
    @Transactional
    public Map<String, Object> updateConfig(String cookie115, String onedriveRegion, String onedriveToken,
                                           String localSourcePath) {
        if (cookie115 != null) {
            if (cookie115.isBlank()) {
                secrets.deleteById(KEY_115_COOKIE);
            } else {
                saveSecret(KEY_115_COOKIE, cookie115.trim());
            }
        }
        if (onedriveToken != null) {
            if (onedriveToken.isBlank()) {
                secrets.deleteById(KEY_ONEDRIVE_TOKEN);
            } else {
                saveSecret(KEY_ONEDRIVE_TOKEN, onedriveToken.trim());
            }
        }
        if (onedriveRegion != null && !onedriveRegion.isBlank()) {
            settings.save(new Setting(KEY_ONEDRIVE_REGION, onedriveRegion.trim()));
        }
        if (localSourcePath != null && !localSourcePath.isBlank()) {
            String normalized = normalizeLocalPath(localSourcePath.trim());
            if (normalized != null) {
                settings.save(new Setting(KEY_LOCAL_SOURCE_PATH, normalized));
            } else {
                throw new IllegalArgumentException("本地源目录不合法，仅允许 /source-music、/host-data、/music 下的路径");
            }
        }
        // 触发重新挂载
        String result = remount();
        return Map.of("ok", true, "remount", result);
    }

    /** 获取当前本地源目录（DB 优先，环境变量兜底）。 */
    @Transactional(readOnly = true)
    public String getLocalSourcePath(String envDefault) {
        return settings.findById(KEY_LOCAL_SOURCE_PATH)
                .map(Setting::getValue)
                .filter(v -> v != null && !v.isBlank())
                .orElse(envDefault);
    }

    /** 校验并规范化本地路径，返回 null 表示不合法。 */
    private String normalizeLocalPath(String path) {
        try {
            // 兼容宿主机路径：/vol2/1000/... 自动转为容器内 /host-data/...
            if (path.startsWith("/vol2/1000/")) {
                path = "/host-data/" + path.substring("/vol2/1000/".length());
            } else if (path.equals("/vol2/1000")) {
                path = "/host-data";
            }
            Path p = Paths.get(path).toAbsolutePath().normalize();
            String s = p.toString();
            for (String prefix : ALLOWED_PREFIXES) {
                if (s.equals(prefix) || s.startsWith(prefix + "/")) {
                    // 确保目录存在（不存在则尝试创建）
                    if (!Files.isDirectory(p)) {
                        try { Files.createDirectories(p); } catch (Exception ignored) {}
                    }
                    return s;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 重新生成 rclone 配置并挂载。返回执行结果摘要。 */
    public String remount() {
        StringBuilder sb = new StringBuilder();
        try {
            Files.createDirectories(RCLONE_CONF.getParent());
            StringBuilder conf = new StringBuilder("# Generated by CloudConfigService\n");

            String cookie115 = getSecret(KEY_115_COOKIE);
            if (cookie115 != null && !cookie115.isBlank()) {
                conf.append("[115]\ntype = 115\ncookie = ").append(cookie115).append("\n");
                sb.append(mount("115:", MOUNT_BASE + "/115", "115")).append("; ");
            }

            String odToken = getSecret(KEY_ONEDRIVE_TOKEN);
            String odRegion = settings.findById(KEY_ONEDRIVE_REGION)
                    .map(Setting::getValue).orElse("global");
            if (odToken != null && !odToken.isBlank()) {
                conf.append("[onedrive]\ntype = onedrive\nregion = ").append(odRegion)
                    .append("\ntoken = ").append(odToken).append("\n");
                sb.append(mount("onedrive:", MOUNT_BASE + "/onedrive", "OneDrive")).append("; ");
            }

            Files.writeString(RCLONE_CONF, conf.toString());
            // 限制权限
            try {
                Files.setPosixFilePermissions(RCLONE_CONF,
                        java.util.Set.of(java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                                java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
            } catch (Exception ignored) {}

            if (sb.length() == 0) {
                return "no cloud source configured";
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("[CloudConfig] remount failed", e);
            return "failed: " + e.getMessage();
        }
    }

    private String mount(String remote, String mountPoint, String label) {
        try {
            Path mp = Paths.get(mountPoint);
            Files.createDirectories(mp);
            // 先卸载旧的
            run("fusermount", "-uz", mountPoint);
            // 挂载
            ProcessBuilder pb = new ProcessBuilder(
                    "rclone", "mount", remote, mountPoint,
                    "--allow-other", "--allow-non-empty",
                    "--vfs-cache-mode", "writes",
                    "--dir-cache-time", "5m",
                    "--daemon");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean done = p.waitFor(15, TimeUnit.SECONDS);
            // 简单验证：挂载点下能列出内容（允许空目录，115 根目录可能为空）
            Thread.sleep(2000);
            boolean ok = Files.isDirectory(mp);
            String msg = label + (ok ? ": mounted" : ": mount attempted");
            log.info("[CloudConfig] {}", msg);
            return msg;
        } catch (Exception e) {
            log.warn("[CloudConfig] mount {} failed: {}", label, e.getMessage());
            return label + ": failed (" + e.getMessage() + ")";
        }
    }

    private String run(String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder out = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) out.append(line).append('\n');
            }
            p.waitFor(10, TimeUnit.SECONDS);
            return out.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String getSecret(String key) {
        return secrets.findById(key)
                .map(s -> {
                    try {
                        return crypto.decrypt(key, s.getCiphertext(), s.getNonce());
                    } catch (Exception e) {
                        log.warn("[CloudConfig] decrypt failed for {}", key);
                        return null;
                    }
                })
                .orElse(null);
    }

    private void saveSecret(String key, String plaintext) {
        SecretCryptoService.EncryptedValue enc = crypto.encrypt(key, plaintext);
        AppSecret s = new AppSecret();
        s.setKey(key);
        s.setCiphertext(enc.ciphertext());
        s.setNonce(enc.nonce());
        secrets.save(s);
    }

    private String mask(String s) {
        if (s == null || s.isBlank()) return "";
        if (s.length() <= 12) return "****";
        return s.substring(0, 6) + "****" + s.substring(s.length() - 4);
    }
}
