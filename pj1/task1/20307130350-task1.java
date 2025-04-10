package com.example.wechat_autosend_xposed;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public final class delayed_Xposed implements IXposedHookLoadPackage{

    private static final String WECHAT_CHATTING_CLASS_NAME="com.tencent.mm.ui.chatting.r";
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.processName.equals("com.tencent.mm")){
            return;
        }
        XposedBridge.log("当前包名："+lpparam.packageName+",进程名："+lpparam.processName);
        hook_delayed_chatsend(lpparam);
    }


    public static void hook_delayed_chatsend(final XC_LoadPackage.LoadPackageParam loadPackageParam){
        Class<?> classChat = XposedHelpers.findClassIfExists(WECHAT_CHATTING_CLASS_NAME, loadPackageParam.classLoader);
        if(classChat==null){
            XposedBridge.log("hook聊天发送操作：未找到类"+WECHAT_CHATTING_CLASS_NAME);
            return;
        }
        XposedHelpers.findAndHookMethod(classChat, "aKl", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String msgsent=(String)param.args[0];
                XposedBridge.log("hook聊天发送，修改前："+msgsent);
                String[] newmsg=(msgsent).split(":");
                int time=0;
                if(newmsg.length==3 && newmsg[0].equals("@Timer")){
                    if(newmsg[1].endsWith("s")){
                        try{
                            time=Integer.parseInt(newmsg[1].substring(0, newmsg[1].length()-1));
                        }catch(Exception e){
                            e.printStackTrace();
                        }
//                        XposedBridge.log("Sleep:"+time);
//                        msgsent=newmsg[2];
                    }
                    else if(newmsg[1].contains("min")){
                        try{
                            time=Integer.parseInt(newmsg[0].substring(0,newmsg[1].length()-3));
                            time*=60;
                        }catch(Exception e){
                            e.printStackTrace();
                        }
//                        XposedBridge.log("Sleep:"+time);
//                        msgsent=newmsg[2];
                    }
                    if(time>0){
                        int sleeptime=time*1000;
//                        param.args[0]=newmsg[2];
                        new Thread(){
                            public void run(){
                                try {
                                    Thread.sleep(sleeptime);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                XposedHelpers.callMethod(param.thisObject,"aKl",newmsg[2]);
                            }
                        }.start();
                        param.args[0]=null;

                    }
                }


            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String sendContent=(String) param.args[0];
                XposedBridge.log("hook聊天发送操作:"+sendContent);
            }
        });

    }


}
