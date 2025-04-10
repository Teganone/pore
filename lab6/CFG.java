package cn.edu.fudan.analysis;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.instruction.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;

import java.time.temporal.ChronoUnit;
import java.util.*;


public class CFG {

    /**
     * The method that the control flow graph is built on
     */
    private Method targetMethod = null;

    /**
     * All the basic blocks EXCEPT catch blocks.
     */
    private HashSet<BasicBlock> blocks = new HashSet<BasicBlock>();

    /**
     * Entry block of this method
     * */
    private BasicBlock entryBB;

    private CFG(){}

    public static String classType2Name(String s) {
        if (s == null) return "";

        if (s.startsWith("L"))
            s = s.substring(1);

        String res = s.replace(";", "").replace("$", "~").replace("/",".");
        return res;
    }

    //methodSignature: 方法名和形参列表
    public static String methodSignature2Name(Method m) {
        String temp = m.getName() + "("; //方法名(
        List<? extends CharSequence> parameters = m.getParameterTypes();
        List<String> params = new ArrayList<>();
        for (CharSequence p : parameters) {
            String param = p.toString();
            String suffix = "";
            if (param.startsWith("[")) //数组由smali格式转换为java格式。[ -> []
            {
                suffix = "[]";
                param = param.substring(1);
            }
            switch (param)
            {
                case "B":
                    params.add("byte" + suffix);
                    break;
                case "C":
                    params.add("char" + suffix);
                    break;
                case "D":
                    params.add("double" + suffix);
                    break;
                case "F":
                    params.add("float" + suffix);
                    break;
                case "I":
                    params.add("int" + suffix);
                    break;
                case "J":
                    params.add("long" + suffix);
                    break;
                case "S":
                    params.add("short" + suffix);
                    break;
                case "V":
                    params.add("void" + suffix);
                    break;
                case "Z":
                    params.add("boolean" + suffix);
                    break;
                default:
                    String tmp = classType2Name(param); //String类等

                    if (tmp.contains("~")) //内部类
                        tmp = tmp.substring(tmp.lastIndexOf('~') + 1); //内部类名称

                    params.add(tmp + suffix);
                    break;
            }
        }

        temp += String.join(",", params); //加上参数列表
        temp += ")";
        return temp;  //一个完整的java方法签名
    }

    public static CFG createCFG(Method method) {
        CFG cfg = new CFG();
        cfg.targetMethod = method;
        BasicBlock currentBB = null;
        int switchAddress = 0; //便于记录原来的switch地址
        int switchFlag = 0;
        int isReturn=0;  //1:上一句是return语句
        int offset = 0;  //!0:上一句是GOTO/IF/SWITCH语句
        int notNew=0;    //1:对于上一句是GOTO/IF/SWITCH语句的情况，是否创建新的bb
        int isThrow=0;
        Iterable<? extends Instruction> instructions = cfg.targetMethod.getImplementation().getInstructions();
        Iterator<BasicBlock> iterator = cfg.blocks.iterator();
        if (instructions.iterator().hasNext()) {
            Instruction first = instructions.iterator().next();
            cfg.entryBB = new BasicBlock(method, ((DexBackedInstruction) first).instructionStart);
        }
        cfg.blocks.add(cfg.entryBB);
        currentBB = cfg.entryBB;

        //遍历每条指令，确定每条指令的offset——同时if语句也有两个offset
        for (Instruction i : instructions) {
            DexBackedInstruction dbi = (DexBackedInstruction) i; //向下转型 //DexBackedInstruction:解析后的dex的所有指令存放的类(smali类？)

            if(dbi.opcode == Opcode.SPARSE_SWITCH_PAYLOAD || dbi.opcode==Opcode.PACKED_SWITCH_PAYLOAD
                    || dbi.opcode == Opcode.MOVE_EXCEPTION || isThrow==1 || isReturn==1){
                BasicBlock newBB = new BasicBlock(method, dbi.instructionStart);
                newBB.addInstruction(i);
                cfg.blocks.add(newBB);
                currentBB = newBB;
                isReturn=0;
                isThrow=0;
            }

            else if (offset == 0) {
                iterator = cfg.blocks.iterator();
                while (iterator.hasNext()) {
                    BasicBlock BB = iterator.next();
                    if (BB.getStartAddress() == dbi.instructionStart) {
                        currentBB = BB;
                        break;
                    }
                }
                currentBB.addInstruction(i);
            } else {
                iterator = cfg.blocks.iterator();
                while (iterator.hasNext()) {
                    BasicBlock BB = iterator.next();
                    if (BB.getStartAddress() == dbi.instructionStart) {
                        currentBB = BB;
                        notNew=1;
                        break;
                    }
                }
                if(notNew==0){
                    BasicBlock newBB = new BasicBlock(method, dbi.instructionStart);
                    if(switchFlag==1) {
                        currentBB.addSuccessor(newBB);
                        switchFlag = 0;
                    }
                    currentBB = newBB;
                    cfg.blocks.add(currentBB);
                }
                currentBB.addInstruction(i);
            }
//            System.out.println("\n");
//            System.out.println(dbi.instructionStart+"\t"+dbi.opcode);
//            for (BasicBlock basicBlock : cfg.blocks) {
//                System.out.println(basicBlock);
//            }
            offset = 0;
            switch (dbi.opcode) {
                case GOTO:
                    offset = ((DexBackedInstruction10t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    addBlock(cfg, currentBB, offset);
                    break;
                case GOTO_16:
                    offset = ((DexBackedInstruction20t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    addBlock(cfg, currentBB, offset);
                    break;
                case GOTO_32:
                    offset = ((DexBackedInstruction30t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                    addBlock(cfg, currentBB, offset);
                    break;

                //DexBackedInstruction22t——2 registers
                case IF_EQ:
                case IF_NE:
                case IF_LT:
                case IF_GE:
                case IF_GT:
                case IF_LE:
                    offset = dbi.instructionStart + dbi.getOpcode().format.size;
                    System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                    addBlock(cfg, currentBB, offset);
                    offset = ((DexBackedInstruction22t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                    addBlock(cfg, currentBB, offset);
                    break;

                //DexBackedInstruction21t——1 register (compared to 0)
                case IF_EQZ:
                case IF_NEZ:
                case IF_LTZ:
                case IF_GEZ:
                case IF_GTZ:
                case IF_LEZ:
                    offset = dbi.instructionStart + dbi.getOpcode().format.size;
                    System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                    addBlock(cfg, currentBB, offset);
                    offset = ((DexBackedInstruction21t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                    addBlock(cfg, currentBB, offset);
                    break;
                case THROW:
                case THROW_VERIFICATION_ERROR:
                    isThrow = 1;
                    break;

                case RETURN_OBJECT:
                case RETURN:
                case RETURN_VOID:
                case RETURN_VOID_BARRIER:
                case RETURN_VOID_NO_BARRIER:
                case RETURN_WIDE:
                    isReturn=1;
                    break;

                case PACKED_SWITCH:
                case SPARSE_SWITCH:
                    switchFlag=1;
                    switchAddress = dbi.instructionStart;
                    offset = ((DexBackedInstruction31t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                    System.out.println(dbi.getOpcode() + ", switch payload offset: " + offset);
                    break;

                case PACKED_SWITCH_PAYLOAD:
                case SPARSE_SWITCH_PAYLOAD:
                    // Since switch-payloads actually are just data, not an instruction.
                    // Though dexlib treat them as special instructions
                    // (sparse-switch-payload & packed-switch-payload),
                    // we should not include them in our CFG.

                    // Take the following switch instruction for example.
                    // 0xAA switch : switch_payload_0
                    // ...
                    // 0xBB switch_payload_0:
                    // 0x1-> 20(offset)
                    // 0x6-> 50(offset)
                    // The offset in a payload instruction points to the instruction
                    // whose address is relative to the address of the switch opcode(0xAA),
                    // not of this table(0xBB).


                    List<? extends SwitchElement> switchElements = null;
                    if (dbi instanceof DexBackedPackedSwitchPayload)
                        switchElements = ((DexBackedPackedSwitchPayload) dbi).getSwitchElements();
                    else
                        switchElements = ((DexBackedSparseSwitchPayload) dbi).getSwitchElements();

                    for (SwitchElement s : switchElements) {
                        /*
                         * !!! Important:
                         * According to sparse-switch-payload Format :
                         * The targets are relative to the address of the switch opcode, not of this table.
                         */

                        offset = s.getOffset() * 2 + switchAddress;
                        System.out.println(dbi.getOpcode() + ", offset: " + offset);
                        addBlock(cfg, currentBB, offset);
                        }
                    break;
                    }
                }


        for (BasicBlock preBB : cfg.blocks) {
            ArrayList<Instruction> Instuctions = preBB.getInstructions();
            for (Instruction instruction : Instuctions) {
                DexBackedInstruction dbi = (DexBackedInstruction) instruction;
                switch (dbi.opcode) {
                    case GOTO:
                        offset = ((DexBackedInstruction10t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", offset: " + offset);
                        linkBlock(cfg, preBB, offset);
                        break;
                    case GOTO_16:
                        offset = ((DexBackedInstruction20t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", offset: " + offset);
                        linkBlock(cfg, preBB, offset);
                        break;
                    case GOTO_32:
                        offset = ((DexBackedInstruction30t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", offset: " + offset);
                        linkBlock(cfg, preBB, offset);
                        break;

                    //DexBackedInstruction22t——2 registers
                    case IF_EQ:
                    case IF_NE:
                    case IF_LT:
                    case IF_GE:
                    case IF_GT:
                    case IF_LE:
                        offset = dbi.instructionStart + dbi.getOpcode().format.size;
                        System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                        linkBlock(cfg, preBB, offset);
                        offset = ((DexBackedInstruction22t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                        linkBlock(cfg, preBB, offset);
                        break;

                    //DexBackedInstruction21t——1 register (compared to 0)
                    case IF_EQZ:
                    case IF_NEZ:
                    case IF_LTZ:
                    case IF_GEZ:
                    case IF_GTZ:
                    case IF_LEZ:
                        offset = dbi.instructionStart + dbi.getOpcode().format.size;
                        System.out.println(dbi.getOpcode() + ", offset1: " + offset);
                        linkBlock(cfg, preBB, offset);
                        offset = ((DexBackedInstruction21t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", offset2: " + offset);
                        linkBlock(cfg, preBB, offset);
                        break;

                    case PACKED_SWITCH:
                    case SPARSE_SWITCH:
                        switchAddress = dbi.instructionStart;
                        offset = ((DexBackedInstruction31t) dbi).getCodeOffset() * 2 + dbi.instructionStart;
                        System.out.println(dbi.getOpcode() + ", switch payload offset: " + offset);
                        iterator=cfg.blocks.iterator();
                        while (iterator.hasNext()) {
                            BasicBlock payloadBB = iterator.next();
                            if(payloadBB.getStartAddress()==offset){
                                Iterator plIterator=payloadBB.getInstructions().iterator();
                                Instruction plInstr=null;
                                while (plIterator.hasNext()) {
                                    plInstr = (org.jf.dexlib2.iface.instruction.Instruction) plIterator.next();
                                    break;
                                }
                                DexBackedInstruction DBI=(DexBackedInstruction)plInstr;
                                List<? extends SwitchElement> switchElements = null;
                                if (DBI instanceof DexBackedPackedSwitchPayload)
                                    switchElements = ((DexBackedPackedSwitchPayload) DBI).getSwitchElements();
                                else
                                    switchElements = ((DexBackedSparseSwitchPayload) DBI).getSwitchElements();

                                for (SwitchElement s : switchElements) {

                                    offset = s.getOffset() * 2 + switchAddress;
                                    System.out.println(dbi.getOpcode() + ", offset: " + offset);
                                    linkBlock(cfg,preBB,offset);
                                }
                                break;
                            }
                        }
                        break;

                }

            }
        }
        return cfg;
    }



    private static void addBlock(CFG cfg,BasicBlock currentBB,int offset){
        Iterator<BasicBlock> iterator=cfg.blocks.iterator();
        while (iterator.hasNext()) {
            BasicBlock BB =  iterator.next();
            if(BB.getStartAddress()==offset) {  //已经有了，不新创
//                currentBB.addSuccessor(BB);
                return;
            }
        }
        BasicBlock newBB = new BasicBlock(cfg.targetMethod, offset);
        cfg.blocks.add(newBB);
//        currentBB.addSuccessor(newBB);
        iterator=cfg.blocks.iterator();
        while (iterator.hasNext()) {
            int tag=0;
            BasicBlock nextBB =  iterator.next();
            Iterator<Instruction> iteratorInstr=nextBB.getInstructions().iterator();
            while (iteratorInstr.hasNext()) {
                Instruction nextInstr=iteratorInstr.next();
                DexBackedInstruction dbi = (DexBackedInstruction)nextInstr;
                if(dbi.instructionStart==offset) {
                    tag = 1;
                }
                if(tag==1 && dbi.instructionStart>=offset){
                    newBB.addInstruction(nextInstr);
                    nextBB.getInstructions().remove(nextInstr);
                    iteratorInstr=nextBB.getInstructions().iterator();
                }
            }
            if(tag==1){
                nextBB.addSuccessor(newBB);
                return;
            }

        }

    }



    /**
     * link an edge from BasicBlock (bb) to a BasicBlock started at offset
     * */
    private static void linkBlock(CFG cfg, BasicBlock bb, int offset) {
        for (BasicBlock basicBlock : cfg.blocks) {
            if (basicBlock.getStartAddress() == offset) {
                bb.addSuccessor(basicBlock);
                return;
            }
        }
         //Typically, no exception will be thrown.
        throw new RuntimeException("no basic block found at offset: " + offset);
    }

    public BasicBlock getEntryBB() {return entryBB;}

    public Method getTargetMethod(){
        return this.targetMethod;
    }

    public HashSet<BasicBlock> getBasicBlocks() {
        return blocks;
    }
}
