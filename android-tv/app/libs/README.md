# Media3 FFmpeg 扩展

`media3-decoder-ffmpeg.aar` 是 TV 播放器对 MPEG-PS、MPEG-2、MP2 及部分 AVI/TS
格式的解码依赖，需要随仓库一同保存，CI 和新环境不再依赖本地 `third_party/` 源码目录。

- Media3 版本：`1.4.1`
- FFmpeg 版本：`6.0.1`
- ABI：`arm64-v8a`、`armeabi-v7a`、`x86_64`
- SHA-256：`9feed2a29f7717955f579eff0373503ef3f8681734b053797fbda3cf62b3a5ab`

更新 AAR 后必须同步修改本文件中的版本、ABI 和 SHA-256，并执行：

```bash
shasum -a 256 app/libs/media3-decoder-ffmpeg.aar
./gradlew testDebugUnitTest assembleDebug
```
