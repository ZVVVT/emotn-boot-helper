# Emotn 开机助手

适配当前设备：

- 设备：MagicBox_M_30_F
- Android：10 / API 29
- CPU ABI：armeabi-v7a
- Emotn 包名：`com.oversea.aslauncher`
- Emotn 首页：`com.oversea.aslauncher/.ui.main.MainActivity`

## 工作逻辑

1. 接收 `BOOT_COMPLETED` 等开机广播。
2. 等待 5 秒。
3. 通过 `AlarmManager + PendingIntent` 启动 Emotn。
4. 助手自身立即结束，不常驻后台。

## GitHub Actions 编译

将本项目上传到一个 GitHub 仓库的 `main` 分支。

进入：

`Actions → Build Emotn Boot Helper APK → Run workflow`

构建完成后，在该次运行页面底部下载：

`emotn_boot_helper`

解压后得到：

`emotn_boot_helper.apk`

## 安装与测试

先保持酷喵启用，防止异常时黑屏。

```powershell
$Box = "192.168.1.4:5555"

.\adb.exe connect $Box

.\adb.exe install -r `
  .\MagicBox_M_30_F\03_apk\emotn_boot_helper.apk
```

手动测试助手能否打开 Emotn：

```powershell
.\adb.exe -s $Box shell am start -n `
  com.zvvvt.emotnboot/.LaunchActivity
```

检查默认首页：

```powershell
.\adb.exe -s $Box shell cmd package resolve-activity --brief --user 0 `
  -a android.intent.action.MAIN `
  -c android.intent.category.HOME
```

应返回：

`com.oversea.aslauncher/.ui.main.MainActivity`

### 第一次重启测试：酷喵保持启用

```powershell
.\adb.exe -s $Box reboot
```

预期：系统开机后，助手在收到开机广播约 5 秒后自动进入 Emotn。

### 第二次重启测试：再禁用酷喵

第一次重启验证通过后：

```powershell
.\adb.exe -s $Box shell pm disable-user --user 0 com.youku.taitan.tv
.\adb.exe -s $Box reboot
```

预期：启动动画结束后短暂黑屏，随后自动进入 Emotn。

## 紧急恢复酷喵

```powershell
$Box = "192.168.1.4:5555"
.\adb.exe connect $Box

.\adb.exe -s $Box shell pm enable --user 0 com.youku.taitan.tv

.\adb.exe -s $Box shell cmd package set-home-activity --user 0 `
  com.youku.taitan.tv/com.youku.tv.home.activity.HomeActivity

.\adb.exe -s $Box shell am start -n `
  com.youku.taitan.tv/com.youku.tv.home.activity.HomeActivity
```

## 卸载助手

```powershell
.\adb.exe -s $Box uninstall com.zvvvt.emotnboot
```
