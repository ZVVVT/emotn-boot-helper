Emotn 开机助手 v1.3.0

修复原因：
v1.2 仅在 0、2、8、20 秒停止 STRCheckService。
实机冷启动日志确认，云 OS 会在助手启动约 57 秒后再次启动该服务，
所以冷启动虽然能进入 Emotn，但下一次待机仍可能被清理。

v1.3 修改：
1. WakeWatchService 前台服务存活期间，每 1 秒检查一次 STRCheckService。
2. 仅在真正停止成功时记录日志，避免 result=false 刷屏。
3. 增加 SCREEN_OFF 监听，待机前立即补停。
4. 保留 SCREEN_ON 后 2 秒再返回 Emotn 的稳定时序。
5. 不禁用 com.yunos.tvmgr 整包。
6. 版本：versionCode=4，versionName=1.3.0。

使用：
将本压缩包解压并覆盖现有 05_emotn_boot_helper 项目，
提交到 GitHub 后由 Actions 构建 APK。
