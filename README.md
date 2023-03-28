# Android-Jacoco-Demo
使用Jacoco查看Android项目代码覆盖率

操作步骤：
1. 编译运行APP，点击页面按钮`Ok`或者`Bad`
2. 点击页面按钮 `生成Jacoco结果文件`
3. 运行项目脚本文件 `sh jr.sh`


<img src="/页面.png" width = "200"  />

plugin：
CocoBackupPlugin 
    备份插件，备份打包后的源码和class。用了git操作，每备份一次都会建一个分支
    ./gradlew dailyDebugCocoBackup  --toDir=/xx/Android-Jacoco-Demo-Backups  --buildNum=100
    最终生成的文件在 /xx/Android-Jacoco-Demo-Backups/b100
    
CocoInstrumentPlugin 插桩插件，编译时插桩

report：
打包jar，结合备份源码和class以及运行日志，生成报告(纯java功能)

rt：
运行日志上传功能，所需要的jacoco源码

版本更新说明
0.0.1 基础版本
0.0.2 backup和report 去掉git操作

