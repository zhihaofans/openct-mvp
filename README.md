# OpenCT-MVP


开源图书馆与课程表应用


## 软件截图

<div align=center>
![](https://github.com/jeffreystoke/openct-mvp/blob/master/screenshots/features.JPG)
![](https://github.com/jeffreystoke/openct-mvp/blob/master/screenshots/class_table.JPG)
![](https://github.com/jeffreystoke/openct-mvp/blob/master/screenshots/borrow_info.JPG)
![](https://github.com/jeffreystoke/openct-mvp/blob/master/screenshots/search_result.JPG)
</div>



(主页还是一片空白)


## 依赖


- Jsoup 1.10.1
- android support v4, v7, design, cardview-v7
- Gson
- Guava, for null check
- Retrofit with Converter-gson and Converter-scalars
- Recyclerview-Animators
- ButterKnife


## 功能


获取教务网课程表与成绩信息, 自动更新周数 (目前支持的教务系统有 正方, 苏文)


搜索图书馆馆藏信息, 获取个人图书借阅信息 (目前支持 汇文OPACv4.5+)


## 特性


自动解析登录表单 (理论上所有`用户名-密码(-验证码)`的登录表单结构均可自动解析)


软件内教务系统以及图书馆均为自动解析登录


自动解析图书搜索表单 (汇文软件OPACv4.5+测试通过)


用户名密码本地加密存储


## 开发进度


正在向 RxJava + Dagger 迁移


正在开发 `空教室查询功能`


服务端正在开发中(提供空教室查询服务)


## 目前支持的学校

    常州工学院、淮阴工学院、金陵科技学院、南京城市职业学院、南京工程学院、南京工业大学、南京信息工程大学
    南京信息工程大学滨江学院、南京邮电大学、苏州大学、苏州科技大学、西华大学、盐城师范学院、中国矿业大学
