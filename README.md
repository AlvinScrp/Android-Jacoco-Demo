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
0.0.2 
    backup和report 去掉git操作
    支持不同build的运行日志，合并生成报告
0.0.3 
    修改日志文件、备份文件、报告文件名格式及存放路径
    日志文件：
        fxj_Android_200/fxj_200_Redmi22041211AC_230316-1412078.ec
    --> Android/FXJ/200/FXJ-200-Redmi22041211AC-2303131412082.ec
    备份文件：
        /backup/b100
    --> /Android/backup/FXJ/100
    报告文件
        /report/fxj/fxj_200_8_20230322161838
        /Android/report/FXJ/200-8-20230322161838
    

