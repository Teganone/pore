import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;


import com.pnfsoftware.jeb.client.S;
import com.pnfsoftware.jeb.core.AbstractEnginesPlugin;
import com.pnfsoftware.jeb.core.IEnginesContext;
import com.pnfsoftware.jeb.core.IPlugin;
import com.pnfsoftware.jeb.core.IPluginInformation;
import com.pnfsoftware.jeb.core.IRuntimeProject;
import com.pnfsoftware.jeb.core.PluginInformation;
import com.pnfsoftware.jeb.core.RuntimeProjectUtil;
import com.pnfsoftware.jeb.core.Version;
import com.pnfsoftware.jeb.core.actions.ActionContext;
import com.pnfsoftware.jeb.core.actions.ActionRenameData;
import com.pnfsoftware.jeb.core.actions.Actions;
import com.pnfsoftware.jeb.core.units.code.android.IDexUnit;
import com.pnfsoftware.jeb.core.units.code.android.dex.IDexClass;
import com.pnfsoftware.jeb.core.units.code.android.dex.IDexField;
import com.pnfsoftware.jeb.core.units.code.android.dex.IDexMethod;
import com.pnfsoftware.jeb.core.units.code.android.dex.IDexPackage;
import com.pnfsoftware.jeb.util.logging.GlobalLog;
import org.yaml.snakeyaml.events.Event;

/**
 * @version 5.0 (已经可以修改类名、包名、字段名、方法名 + 内部类名，其中字段名和方法名统一用java2smali格式转换）
 * @author Tegan_
 * @time 2022-06-04
 */

public class SamplePlugin extends AbstractEnginesPlugin {
    // ^ change that for another plugin type

    @Override
    public void load(IEnginesContext context) {
        GlobalLog.getLogger().info("Loading autorename5.0 plugin");
    }

    @Override
    public void execute(IEnginesContext iEnginesContext, Map<String, String> params) {
        GlobalLog.getLogger().info("Executing AutoRename plugin");
        File file = new File("D:\\下载\\mapping (2).txt");
//        File file = new File("D:\\mapping (3).txt");
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        String readStr="";
        IRuntimeProject prj=iEnginesContext.getProject(0);
        IDexClass curClass=null;
        for (IDexUnit iDexUnit : RuntimeProjectUtil.findUnitsByType(prj,IDexUnit.class,false)) {
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                bufferedWriter = new BufferedWriter(new FileWriter("d:\\mapping(3)copy.txt"));
                while((readStr=bufferedReader.readLine())!=null){
                    String[] strings=readStr.split(" -> ");
//                    eg:IDexClass aClass = iDexUnit.getClass("Lcom/example/lab3/BuildConfig;");
                    //仅针对类名
                    if(strings[0].indexOf(" ")!=0) {
                        bufferedWriter.write("Strings[0]:L" + strings[0].replace(".","/") + ";\n");
                        IDexClass aClass = iDexUnit.getClass("L" + strings[0].replace(".","/") + ";");
                        if(aClass==null) {
                            bufferedWriter.write("null\n");
                        }
                        if(aClass!=null) {
                            bufferedWriter.write("全类名："+aClass.getSignature()+"\n");
                            bufferedWriter.write("Strings[1]:L" + strings[1].replace(".","/") + ";\n");
//                            GlobalLog.getLogger().info("原全类名："+aClass.getSignature());
                            curClass=aClass;
                            if(!strings[0].equals(strings[1])){ //需要修改
                                bufferedWriter.write("要修改+\n");
                                GlobalLog.getLogger().info("修改");
                                String[] renameStr=strings[1].split("\\.");
                                String[] srcStr=strings[0].split("\\.");
                                bufferedWriter.write("==========输出包名==========\n");
                                StringBuffer pkgName=new StringBuffer("L");
//                                for (int i = 0; i < srcStr.length-2; i++) {
//                                    pkgName.append(srcStr[i]+"/");
//                                }
//                                if(srcStr.length>=2) {
//                                    pkgName.append(srcStr[srcStr.length-2]+";");
//                                    bufferedWriter.write("原包名："+pkgName.toString()+"\n");
//                                    IDexPackage aPackage = iDexUnit.getPackage(pkgName.toString());
//                                    if(aPackage!=null)
//                                        bufferedWriter.write("原包名"+aPackage.getSignature()+"\n");
//                                }
                                bufferedWriter.write("=========改包名==========\n");
                                String curPkgName="L";
                                for (int i = 0; i < Math.min(srcStr.length, renameStr.length)-1; i++) {
                                    if(srcStr[i]!=renameStr[i]){
                                        curPkgName=curPkgName+srcStr[i]+"/";
//                                        String curPkgName="L"+strings[0].substring(0,strings[0].indexOf(srcStr[i+1])).replace(".","/");
                                        bufferedWriter.write("原包名："+curPkgName+"\n");
                                        IDexPackage aPackage = iDexUnit.getPackage(curPkgName.toString());
                                        if(aPackage!=null) {
                                            bufferedWriter.write("原包名" + aPackage.getSignature() + "\n");
                                            rename_Allkinds(iDexUnit,aPackage.getItemId(),aPackage.getAddress(),renameStr[i]);
                                        }
                                    }
                                }

                                if(!srcStr[srcStr.length-1].contains("$")) {
                                    bufferedWriter.write("要修改的名字:" + renameStr[renameStr.length - 1] + "\n");
                                    GlobalLog.getLogger().info("修改的名字:" + renameStr[renameStr.length - 1]);
                                    rename_Allkinds(iDexUnit, aClass.getItemId(), aClass.getAddress(), renameStr[renameStr.length - 1]);
                                    bufferedWriter.write("改名成功：" + aClass.getSignature() + "\n");
                                }
                                else{
                                    GlobalLog.getLogger().info("修改内部类："+aClass.getSignature());
                                    bufferedWriter.write("============修改内部类："+aClass.getSignature()+"==================\n");
                                    String[] srcInner = srcStr[srcStr.length - 1].split("\\$");
                                    String[] renameInner = renameStr[renameStr.length - 1].split("\\$");

                                    IDexPackage aPackage = iDexUnit.getPackage(pkgName.toString());
                                    if(aPackage!=null) {
                                        String outerClazzName=aPackage.getSignature()+srcInner[0];
                                        bufferedWriter.write("外部name："+outerClazzName+"\n");
                                        IDexClass outerClazz = iDexUnit.getClass(outerClazzName+ ";");
                                        if(outerClazz!=null) {
                                            bufferedWriter.write("外部类："+outerClazz.getSignature()+"\n");
                                            rename_Allkinds(iDexUnit,outerClazz.getItemId(),outerClazz.getAddress(),renameInner[0]);
                                            bufferedWriter.write("外部类修改成功:"+outerClazz.getSignature()+"\n");
                                        }
                                        String curInnerClassName=outerClazzName;
                                        String renameInnerName=renameInner[0];
                                        for (int i = 1; i < Math.min(renameInner.length,srcInner.length); i++) {
                                            curInnerClassName=curInnerClassName+"$"+srcInner[i];
                                            IDexClass curInnerClazz = iDexUnit.getClass(curInnerClassName + ";");
                                            renameInnerName=renameInnerName+"$"+renameInner[i];
                                            bufferedWriter.write("\n================修改内部类=================\n"+renameInnerName+"\n");
                                            if(curInnerClazz!=null) {
                                                bufferedWriter.write("原内部类名：" + curInnerClassName + "\n");
                                                bufferedWriter.write("原内部类名全：" + curInnerClazz.getSignature() + "\n");
//                                            rename_class(iDexUnit,curInnerClazz.getItemId(),curInnerClazz.getAddress(),renameInner[i]);
                                                rename_Allkinds(iDexUnit, curInnerClazz.getItemId(), curInnerClazz.getAddress(), renameInnerName);
                                                bufferedWriter.write("修改完内部类"+curInnerClazz.getSignature()+"\n");
                                            }
                                        }
                                    }
                                }
                            }
                            bufferedWriter.write("\n");
                        }
                    }
                    else {
                        bufferedWriter.write(strings[0]+"\n");
//                        strings[0].lastIndexOf(" ");
                        bufferedWriter.write("\ncurClass"+curClass+"\n");
                        if(curClass!=null) {
//                            GlobalLog.getLogger().info("成员所属类的全类名：" + curClass.getSignature());
                            bufferedWriter.write("成员所属类的全类名：" + curClass.getSignature()+"\n");
                            String srcSmaliSignature = java2smali(curClass, strings[0]);
                            if(srcSmaliSignature.indexOf("(")==-1) { //字段
                                String srcSmaliName = srcSmaliSignature.substring(srcSmaliSignature.indexOf(">")+1,srcSmaliSignature.indexOf(":"));
                                if(srcSmaliName!=strings[1]) {
                                    bufferedWriter.write("原字段名：" + srcSmaliName + "\n");
                                    IDexField iDexField = iDexUnit.getField(srcSmaliSignature);
                                    if (iDexField != null) {
                                        bufferedWriter.write("字段名：" + iDexField.getSignature() + "\n");
//                                        GlobalLog.getLogger().info("字段名：" + iDexField.getSignature());
                                        String destStr=strings[1];
                                        bufferedWriter.write("要改的字段名：" + destStr + "\n");
                                        rename_Allkinds(iDexUnit, iDexField.getItemId(), iDexField.getAddress(), destStr);
//                                        boolean isSetName = iDexField.setName(destStr);
//                                        GlobalLog.getLogger().info("\n用setName改字段名！"+isSetName);
                                        bufferedWriter.write("改字段名成功：" + iDexField.getSignature() + "\n");
//                                        GlobalLog.getLogger().info("改字段名：" + iDexField.getSignature());
                                    }
                                }
                            }
                            else if(srcSmaliSignature.indexOf("(")!=-1){ //method
                                String srcSmaliName = srcSmaliSignature.substring(srcSmaliSignature.indexOf(">")+1,srcSmaliSignature.indexOf("("));
                                if(srcSmaliName!=strings[1]) {
                                    IDexMethod iDexMethod = iDexUnit.getMethod(srcSmaliSignature);
                                    if (iDexMethod != null) {
                                        bufferedWriter.write("方法名：" + iDexMethod.getSignature() + "\n");
                                        String destStr=strings[1];
                                        rename_Allkinds(iDexUnit, iDexMethod.getItemId(), iDexMethod.getAddress(), destStr);
//                                        boolean isSetName = iDexMethod.setName(destStr);
//                                        GlobalLog.getLogger().info("\n用setName改方法名！"+isSetName);
                                        bufferedWriter.write("改方法名成功：" + iDexMethod.getSignature() + "\n");
//                                        GlobalLog.getLogger().info("改方法名：" + iDexMethod.getSignature());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    bufferedReader.close();
                    bufferedWriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }


    private void rename_Allkinds(IDexUnit unit, long itemId, String address, String sourceStr) {
        GlobalLog.getLogger().info("\n+"+sourceStr+"\n");
        ActionContext actionContext = new ActionContext(unit, Actions.RENAME, itemId, address);
        ActionRenameData actionRenameData = new ActionRenameData();
        actionRenameData.setNewName(sourceStr);
        if(unit.prepareExecution(actionContext,actionRenameData)){
            try{
                boolean result = unit.executeAction(actionContext, actionRenameData);
                if(result){
                    GlobalLog.getLogger().info("rename to "+sourceStr+" success!");
                }
                else{
                    GlobalLog.getLogger().info("rename to "+sourceStr+" failed!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public String java2smali(IDexClass iDexClass, String javaStr){
        String[] temp = javaStr.substring(4).split(" "); //前面都是4个空格开始的
        String smaliStr="";
        if(temp[1].indexOf("(")==-1) { //Field
            smaliStr = iDexClass.getSignature() + "->" + temp[1] + ":L" + temp[0].replace(".", "/") + ";";
            //eg:Lcn/jiguang/curve/b;->a:Landroid/content/Context;
            System.out.println(smaliStr);
        }
        else{ //Method
            String returnTypeWithNoArray=temp[0];
            if(temp[0].indexOf("[")!=-1){
                returnTypeWithNoArray=temp[0].substring(0,temp[0].indexOf("["));
            }
            returnTypeWithNoArray=java2smaliTypeTable(returnTypeWithNoArray);
            int num=(temp[0].lastIndexOf("[")-temp[0].indexOf("["))/2+1;
            if(temp[0].indexOf("[")==-1)
                num=0;
            temp[0]=String.join("", Collections.nCopies(num,"["))+returnTypeWithNoArray; //返回类型
            System.out.println("返回类型"+temp[0]);
            String[] parameters=temp[1].substring(temp[1].indexOf("(")+1,temp[1].lastIndexOf(")")).split(",");
            StringBuffer paramsBuffer=new StringBuffer("(");
            for (String param :parameters) {
                paramsBuffer.append(java2smaliTypeTable(param));
            }
            paramsBuffer.append(")");
            smaliStr=iDexClass.getSignature() + "->"+temp[1].substring(0,temp[1].indexOf("("))+paramsBuffer.toString()+temp[0];
        }
        return smaliStr;
    }

    //仅作为test的尝试
    public String java2smali(String iDexClass,String javaStr){
        String[] temp = javaStr.substring(4).split(" "); //前面都是4个空格开始的
        String smaliStr="";
        if(temp[1].indexOf("(")==-1) { //Field
            smaliStr = iDexClass + "->" + temp[1] + ":L" + temp[0].replace(".", "/") + ";";
            //eg:Lcn/jiguang/curve/b;->a:Landroid/content/Context;
            System.out.println(smaliStr);
        }
        else{ //Method
            String returnTypeWithNoArray=temp[0];
            if(temp[0].indexOf("[")!=-1){
                returnTypeWithNoArray=temp[0].substring(0,temp[0].indexOf("["));
            }
            returnTypeWithNoArray=java2smaliTypeTable(returnTypeWithNoArray);
            int num=(temp[0].lastIndexOf("[")-temp[0].indexOf("["))/2+1;
            if(temp[0].indexOf("[")==-1)
                num=0;
            temp[0]=String.join("", Collections.nCopies(num,"["))+returnTypeWithNoArray; //返回类型
            System.out.println("返回类型"+temp[0]);
            String[] parameters=temp[1].substring(temp[1].indexOf("(")+1,temp[1].lastIndexOf(")")).split(",");
            StringBuffer paramsBuffer=new StringBuffer("(");
            for (String param :parameters) {
                paramsBuffer.append(java2smaliTypeTable(param));
            }
            paramsBuffer.append(")");
            smaliStr=iDexClass + "->"+temp[1].substring(0,temp[1].indexOf("("))+paramsBuffer.toString()+temp[0];
        }
        return smaliStr;
    }

    public String java2smaliTypeTable(String str){
        if(str==null)
            return "";
        switch (str){
            case "void":
                str="V";
                break;
            case "boolean":
                str="Z";
                break;
            case "byte":
                str="B";
                break;
            case "short":
                str="S";
                break;
            case "char":
                str="C";
                break;
            case "int":
                str="I";
                break;
            case "long":
                str="J";
                break;
            case "float":
                str="F";
                break;
            case "double":
                str="D";
                break;
            default:
                str="L"+str.replace(".","/")+";";
                break;
        }
        return str;
    }

    @Override
    public IPluginInformation getPluginInformation() {
        return new PluginInformation("Jeb2AutoRenamePlugin", "A autorename plugin", "Author", Version.create(1, 0, 0));
    }
}

