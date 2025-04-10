package com.example.wechat_autosend_xposed;


import android.content.ContentValues;

import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class autosend_Xposed implements IXposedHookLoadPackage {
    private static final String WECHAT_DATABASE_CLASS_NAME="com.tencent.wcdb.database.SQLiteDatabase";
    private static final String WECHAT_CHATFOOTER_CLASS_NAME="com.tencent.mm.pluginsdk.ui.chat.ChatFooter";

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.processName.equals("com.tencent.mm")){
            return;
        }
        XposedBridge.log("当前包名："+lpparam.packageName+",进程名："+lpparam.processName);
        hook_auto_chatsend(lpparam);
    }
    public static void hook_auto_chatsend(final XC_LoadPackage.LoadPackageParam loadPackageParam){
        Class<?> classauto= XposedHelpers.findClass(WECHAT_CHATFOOTER_CLASS_NAME, loadPackageParam.classLoader);
        Class<?> clazz=XposedHelpers.findClass("com.tencent.mm.pluginsdk.ui.chat.b",loadPackageParam.classLoader);
        final Object[] INTERFACE_CHATTING={null};
        XposedHelpers.findAndHookMethod(classauto, "setFooterEventListener", clazz, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                INTERFACE_CHATTING[0] = XposedHelpers.getObjectField(param.thisObject, "MgP");
                super.afterHookedMethod(param);
            }
        });


        Class<?> classDb = XposedHelpers.findClassIfExists(WECHAT_DATABASE_CLASS_NAME, loadPackageParam.classLoader);
//        if(classDb==null){
//            XposedBridge.log("hook数据库insert操作：未找到类"+WECHAT_DATABASE_CLASS_NAME);
//            return;
//        }
        XposedHelpers.findAndHookMethod(classDb, "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("hook微信数据库insert操作");
                String tableName = (String) param.args[0];
                ContentValues contentValues=(ContentValues) param.args[2];
                if(tableName==null || tableName.length()==0||contentValues==null){
                    return;
                }
                if(!tableName.equals("message")){
                    return;
                }
                //提取消息内容
                //1：表示是自己发送的消息
                int isSend = contentValues.getAsInteger("isSend");
                //消息内容
                String strContent = contentValues.getAsString("content");
                //说话人ID
                String strTalker = contentValues.getAsString("talker");
                XposedBridge.log("isend="+isSend+"\tcontent="+strContent+"\ttalker="+strTalker);
                //收到消息，进行回复（要判断不是自己发送的、不是群消息、不是公众号消息，才回复）
                String sendmsg="";
                if (isSend != 1 && !strTalker.endsWith("@chatroom") && !strTalker.startsWith("gh_")&& INTERFACE_CHATTING[0]!=null) {
                    int num=(int)(Math.random()*6);
                    switch(num){
                        case 0:
                            sendmsg=strContent;
                            break;
                        case 1:
                            sendmsg="哈哈哈哈哈哈哈哈哈哈笑拉了";
                            break;
                        case 2:
                            sendmsg="感动天感动地，怎么感动不了你";
                            break;
                        case 3:
                            sendmsg="哟，这不是我的傻儿子吗？";
                            break;
                        case 4:
                            sendmsg="这是一条自动回复，稍后联系";
                            break;
                        case 5:
                            sendmsg="我要吃火锅啊啊啊啊啊";
                            break;
                    }
                    String finalsendmsg="自动回复："+sendmsg;

                    // 可发送 但是要缓冲很久
                    new Thread(){
                        public void run() {
                            Class<?> classiVar = XposedHelpers.findClassIfExists("com.tencent.mm.modelmulti.i", loadPackageParam.classLoader);
                            Object objectiVar = XposedHelpers.newInstance(classiVar,
                                    new Class[]{String.class, String.class, int.class, int.class, Object.class},
                                    strTalker, finalsendmsg, 1, 1, new HashMap<String, String>() {
                                        {
                                            put(strTalker, strTalker);
                                        }
                                    });
                        }
                    }.start();

//                    Object object=XposedHelpers.getObjectField("com.tencent.mm.ui.chatting.e.a","RGw");
//                    XposedHelpers.callMethod(object,"post");
//                    XposedHelpers.callMethod(object,"post");
                    XposedHelpers.callMethod(INTERFACE_CHATTING[0],"aKl","自动回复");
                    super.beforeHookedMethod(param);
                }
            }
        });
    }
}
