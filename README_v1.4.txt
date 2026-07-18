Emotn 开机助手 v1.4.0

实机日志确认的问题：
1. 唤醒后云 OS 先启动酷喵首页。
2. AppDaemon 会 force-stop 原来的 Emotn。
3. v1.3 随后通过助手自己的 LaunchActivity 恢复 Emotn。
4. LaunchActivity 短暂成为前台、再退到后台后，AppDaemon 会继续
   force-stop com.zvvvt.emotnboot，并同时停止 WakeWatchService。
5. 因此 v1.3 通常只能成功恢复一次；下一次唤醒时助手已经 stopped=true。

v1.4 最小修复：
1. LaunchScheduler 的 PendingIntent 直接指向：
   com.oversea.aslauncher/.ui.main.MainActivity
2. 开机和唤醒流程不再经过助手自己的 LaunchActivity。
3. 助手始终只作为 WakeWatchService 前台服务存在，不产生
   “酷喵 -> 助手 Activity -> Emotn”的前台切换。
4. 保留现有唤醒后 2 秒延迟，开机延迟仍由 BootReceiver 使用原值。
5. 保留 v1.3 的 STRCheckService 每秒看守逻辑。
6. 覆盖升级时主动取消 v1.1-v1.3 可能遗留的 LaunchActivity 定时任务。
7. LaunchActivity 文件和 Manifest 注册暂时保留作为兼容备用，但自动流程不再调用。
8. 不禁用 com.yunos.tvmgr，也不禁用 com.youku.taitan.tv。

版本：
versionCode=5
versionName=1.4.0

覆盖文件：
app/build.gradle
app/src/main/java/com/zvvvt/emotnboot/LaunchScheduler.java
README_v1.4.txt

验证重点：
- 日志应出现：scheduled direct Emotn launch in 2000ms
- 自动唤醒流程中不应再出现：
  cmp=com.zvvvt.emotnboot/.LaunchActivity
- 不应再出现：
  Force stopping com.zvvvt.emotnboot
- WakeWatchService 和助手 PID 在多轮待机唤醒后仍应存活。
