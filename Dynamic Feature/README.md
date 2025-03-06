# Introduction

[Tencent Cloud Super App as a Service (TCSAS)](https://www.tencentcloud.com/products/tcsas)

Built on the Tencent Mini Program technical framework and fully following the development specifications and standards of Weixin mini programs, Tencent Cloud Super App as a Service（TCSAS） empowers enterprises to integrate mini programs into their own super apps and establish their own mini program ecosystem.

The `tcsas-demo-android` includes example code for integrating TCSAS mini program containers, making it convenient for developers to reference.

# Usage Instructions

## Replace Application Configuration

The configuration file built into the demo is bound to the official test application. Before you can preview your personal application's mini programs, you need to follow these steps to replace the configuration:

1. Log in to the Tencent Cloud Super App as a Service console and download the host application configuration file to replace the `tcsas-android-configurations.json` file in the `app/main/assets` path of the project.
2. Modify the project package name to match the configuration in the console. If the package name is inconsistent, it will not pass the verification when running.

After completing these steps, you can run and experience the basic functions of the container.