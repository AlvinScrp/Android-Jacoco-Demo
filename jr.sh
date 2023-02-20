#!/bin/bash
echo "======step1:从SD卡拷贝原始文件"
projectDir=$(cd `dirname $0`; pwd)
toDir="${projectDir}/build/ec"
if [ ! -d "$toDir" ];then
mkdir "$toDir"
echo "创建文件夹成功"
else
echo "文件夹已经存在"
fi
reportSdCardPath="storage/emulated/0/Android/data/com.a.jacocotest/cache/aa.ec"
adb pull $reportSdCardPath "$toDir"

echo ""
echo "======step2:执行gradle task:jacocoTestReport生成html (jacoco.gradle)"
rm -rf "$projectDir/app/build/reports/jacoco"
./gradlew jacocoTestReport
reportPath="$projectDir/app/build/reports/jacoco/jacocoTestReport/html/index.html"
echo "jacoco报告地址:${reportPath}"

echo ""
echo "======step3:使用chrome浏览器 打开html"
open -a "/Applications/Google Chrome.app" "${reportPath}"
