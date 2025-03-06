# 简介

[腾讯云超级应用服务（Tencent Cloud Super App as a Service，TCSAS）](https://www.tencentcloud.com/products/tcsas)，源于腾讯小程序技术框架，完全遵循微信小程序的开发规范与标准。不仅可以帮助企业实现将小程序投放到自有Super App，同时可以为企业提供自建小程序生态的闭环能力。

tcsas-demo-android 包含了TCSAS小程序容器集成的示例代码，供开发者参考。

# 使用说明

## 替换应用配置

Demo 内置的配置文件，是与官方测试应用绑定的，在体验预览您个人应用下的小程序前，需按如下步骤完成配置替换：

1. 登录腾讯云超级应用服务控制台，下载宿主应用配置文件，用来替换项目中 app/main/assets 路径下的 tcsas-android-configurations.json 文件。
2. 修改项目包名，与控制台应用配置相同，如果包名不一致，运行时无法通过校验。

完成上述步骤，即可运行体验 Demo基本功能了。
