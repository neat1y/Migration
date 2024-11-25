
Getting Started


If you wanna to test lib without your app, you can install this rep 
and write this command 
```bash
git clone https://github.com/neat1y/Migration.git

gradle build

gradle run
```
After this all values in application.yaml 
witch was in this repository will be activated to migration or rollback
In application.yaml you can change some parametrs
Url name password rollback file path in future you can change sort type

If you wanna to add this lib to your project you need to write one command

```bash
gradle jar
```
thanks to this command you can add jar archive to your app
For example we have target.jar, if we wanna to add jar to new application
we need to know only dir this jar
```gradle
dependencies {
implementation files('/home/name/target.jar')
}
```

