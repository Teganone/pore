import frida  # 导入frida模块
import sys  # 导入sys模块

hook_js = """
console.log("Wechat Frida js start!")
var hook_js = function () {
 var test3 = Java.use("com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView");
 test3.setMoney.overload('java.lang.String').implementation = function (){
    var ret = this.setMoney("2309290.23");
    return ret;
 };


 var test4 = Java.use("com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView");
 test4.setFirstMoney.overload('java.lang.String').implementation = function (){
    var ret = this.setFirstMoney("2309290.23");
    return ret;
 };

  var test5 = Java.use("com.tencent.mm.plugin.wallet_core.ui.view.WcPayMoneyLoadingView");
 test5.setNewMoney.overload('java.lang.String').implementation = function (){
    var ret = this.setNewMoney("2309290.23");
    return ret;
 };
 }
Java.perform(hook_js);
"""


def on_message(message, data):  # js中执行send函数后要回调的函数
    print(message)


# 得到设备并劫持进程
process = frida.get_remote_device().attach("微信")
script = process.create_script(hook_js)  # 创建js脚本
script.on('message', on_message)  # 加载回调函数，也就是js中执行send函数规定要执行的python函数
script.load()  # 加载脚本
sys.stdin.read()

