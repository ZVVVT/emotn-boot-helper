Emotn 开机助手 v1.5 补丁

版本：versionCode 6 / versionName 1.5.0

目标：
解决 MagicBox_M_30_F 上 com.yunos.tvmgr 的 DoneRemoteBlacklist 定时任务
把普通第三方助手 force-stop，导致 stopped=true、后续唤醒回到酷喵的问题。

核心改动：
1. APK 包名保持 com.zvvvt.emotnboot，不卸载、不更换应用。
2. BootReceiver 与 WakeWatchService 改为运行在：
   com.zvvvt.emotnwatch
3. LaunchActivity 保持默认进程；v1.4 自动路径本来就不会调用它。
4. 保留 v1.4 的直接启动 Emotn、2 秒唤醒延迟和 STRCheckService 看守。
5. 不禁用 com.yunos.tvmgr，不禁用 com.youku.taitan.tv，也不修改系统组件。

注意：
v1.5 运行后，下面命令通常不会再返回 PID：
  pidof com.zvvvt.emotnboot
应改用：
  pidof com.zvvvt.emotnwatch

安装必须覆盖升级：
  adb -s 192.168.2.24:5555 install -r emotn_boot_helper.apk
不要卸载旧版。
