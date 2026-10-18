# 网盘挂载（百度网盘 / OneDrive / 115）

本项目在上游 Home KTV 的基础上增加了**网盘只读源**功能：把百度网盘、OneDrive、115
里的歌曲直接作为点歌源，不占用 NAS 本地硬盘。

## 工作原理

```
百度网盘 ┐
OneDrive  ├─→ alist（Docker）→ WebDAV /dav ─→ rclone 挂载 ─→ /cloud-music ─→ KTV 后端扫描
115      ┘                                              （只读源：复制入库）
```

- `alist` 容器把三个网盘聚合成统一的 WebDAV 服务（`http://alist:5244/dav`）。
- `cloud-mount` 容器用 rclone + FUSE 把 WebDAV 挂载到 `./cloud-music`，KTV 容器以
  `/cloud-music` 只读源的方式消费它。
- 后端行为（已打补丁，与本地源不同）：
  - 扫描时会同时扫描本地 `source-music` 和网盘 `/cloud-music`；
  - 网盘里的兼容文件**复制**进曲库（本地源是移动），网盘源文件**永远不会被移动、
    删除或清理**——「删除源文件」「自动清理」都会自动跳过网盘文件；
  - 需要转码的网盘文件照常转码，输出写到本地 `/music` 曲库；
  - 网盘挂载暂时不可用时，扫描会跳过网盘源并继续扫描本地源，不会报错中断。

## 部署步骤

> 前提：已按 README 在 NAS 上准备好本项目目录；手机、电视、NAS 在同一局域网。

### 1. 配置 alist 密码

```sh
cp .env.example .env
```

编辑 `.env`，把 `KTV_ALIST_PASSWORD` 改成你要设置的 alist 管理员密码
（先随便写一个，容器启动后以这个为准——见第 3 步说明）。

> 注意：`.env` 只被 `docker-compose.cloud.yml` 使用；`docker-compose.nas.yml`
> 本身不依赖 `.env`。

### 2. 启动全部服务

```sh
docker compose -f docker-compose.nas.yml -f docker-compose.cloud.yml up -d --build --wait
```

`--build` 会在 NAS 上编译含网盘补丁的后端镜像（`ktv-home-cloud:latest`），
首次构建需要几分钟。以后更新代码后重复这条命令即可。

确认状态：

```sh
docker compose -f docker-compose.nas.yml -f docker-compose.cloud.yml ps
curl http://127.0.0.1:8080/api/health
```

### 3. 在 alist 里添加三个网盘

1. 浏览器打开 `http://<NAS_IP>:5244`，用 `admin` 登录。
   初始密码从容器日志获取：

   ```sh
   docker logs ktv-alist 2>&1 | grep -i password
   ```

   登录后在「管理 → 用户」里把 admin 密码改成 `.env` 里写的那一个，
   保持两者一致（`cloud-mount` 靠这个密码连接 alist 的 WebDAV）。

2. 进入「管理 → 存储 → 添加」，逐个添加：
   - **百度网盘**：驱动选「百度网盘」，按页面提示填写刷新令牌
    （refresh_token，在自己的浏览器里按 alist 文档获取）；
   - **OneDrive**：驱动选「OneDrive」，需要自备 Azure 应用的
     client_id / client_secret（或使用世纪互联版驱动）；
   - **115**：驱动选「115」，填写账号 Cookie（在自己的浏览器登录 115 后复制）。

   挂载路径建议分别填 `/百度网盘`、`/OneDrive`、`/115`，备注随意。
   所有 Cookie / Token 都只填在你自己打开的 alist 网页里，不要发给任何人。

3. 添加完成后回到 alist 首页，能看到三个目录即表示成功。
   `cloud-mount` 会自动把它们挂载到 `./cloud-music`，可用以下命令验证：

   ```sh
   ls ./cloud-music
   ```

### 4. 在 KTV 后台扫描入库

1. 打开 `http://<NAS_IP>:8080/m/admin`，进入仪表盘点「扫描源路径」。
2. 网盘文件会出现在「原始音乐管理」中，来源路径以 `/cloud-music` 开头；
   兼容文件自动复制入库，不兼容的走批量转码（输出在本地曲库）。

之后网盘里新增歌曲，只需回到仪表盘再点一次「扫描源路径」即可
（网盘目录不支持自动监听）。

## 常见问题

**首次扫描很慢？**
扫描要对每个文件算 MD5 并用 ffprobe 分析，网盘文件需要完整下载一次，
大批量首次入库会比较慢，属正常现象；入库后只做增量。

**网盘里的文件会被删掉吗？**
不会。后端对 `/cloud-music` 下的文件只做读取和复制；移动、删除、
自动清理都会跳过网盘源。这是代码层面的保证，不是配置约定。

**电视/手机能直接播网盘里的歌吗？**
不能直接播。播放走的是本地 `/music` 曲库；网盘只是「素材源」，
歌曲必须先扫描入库（复制/转码）到本地曲库后才能点播。

**alist 升级 / 忘记密码？**
```sh
docker exec -it ktv-alist ./alist admin set 新密码
```
改完后同步更新 `.env` 里的 `KTV_ALIST_PASSWORD`，再
`docker compose -f docker-compose.nas.yml -f docker-compose.cloud.yml up -d`
重建 `cloud-mount` 即可。

**只想用其中一两个网盘？**
在 alist 里只添加想用的存储即可；没添加的目录不会出现在
`/cloud-music` 里，不影响使用。

**安全提醒**
alist（5244）与 KTV（8080）都只面向可信局域网，不要把端口暴露到公网。
