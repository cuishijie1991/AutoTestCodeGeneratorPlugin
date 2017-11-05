# AutoTestCodeGeneratorPlugin

## 功能简介
本插件解析实现了IParseFromObject接口的model和protocol，自动生成对应的单元测试代码模板。
**注意：插件并不能真正生成单元测试，开发人员需要认真校验和修改测试代码逻辑，完成最终的单元测试**

## 更新日志
- **171101_v1.0**
  - 生成parseFromObj()相关测试代码，不保证正确性

- **171102_v2.0**
  - 优化生成功能代码功能，提高生成代码正确性

- **171103_v2.1**
  - 修复部分代码无法生成方法的bug
  - 修复了AndroidStudio生成class类没有public关键字的bug
  - 增添了自动打开Test类的功能

- **171105_v3.0**
  - 支持对model类型解析，新增对包含Array和子model类型解析支持
  - 支持对protocol类型解析，支持包含Array和子model的protocol

## 使用方法
 1. 在工程/plugins中，找到最新的插件jar包，AutoTestCodeGeneratorPlugin_xxx.jar下载到本地
 2. 打开AndroidStudio-Preferences-plugins-Install plugin from disk...
    选中AutoTestCodeGeneratorPlugin_xxx.jar-Ok-重启AndroidStudio
 3. 插件安装成功后，有两种方法生成测试文件
    - 工程目录中，选中 XX.java 文件，右键菜单中，选中"generateTestCode",即可生成test对应目录下生成 XXTest.java
    
    - 打开 XX.java 文件，选则导航栏的Code菜单，选中"generateTestCode",即可生成test对应目录下生成 XXTest.java