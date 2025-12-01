import java.util.*;
import java.io.File;
import javafx.stage.FileChooser;

public class CPU_1_Simple extends Transformer {

    private static final int EA_MASK_12 = 0x0FFF;
    private short linkRegister = 0;

    // Opcodes used in Program 2 ROM
    static final short HALT = 0;
    static final short LDR  = 1;
    static final short STR  = 2;
    static final short LDA  = 3;
    static final short AMR  = 4;
    static final short SMR  = 5;
    static final short AIR  = 6;
    static final short SIR  = 7;

    static final short JZ   = 8;
    static final short JNE  = 9;
    static final short JCC  = 10;
    static final short JMA  = 11;
    static final short JSR  = 12;
    static final short RFS  = 13;
    static final short SOB  = 14;
    static final short JGE  = 15;

    static final short SRC  = 25;
    static final short RRC  = 26;

    static final short IN   = 49;  // dev=0,1,2
    static final short OUT  = 50;  // dev=1 → printer
    static final short CHK  = 51;

    static final short MLT  = 56;
    static final short DVD  = 57;
    static final short TRR  = 58;
    static final short AND  = 59;
    static final short ORR  = 60;
    static final short NOT  = 61;
    static final short TRAP = 24;

    static final short LDX = 33;
    static final short STX = 34;

    // ================== Devices =====================

    public static class KeyboardDevice {
        private final StringBuilder buf = new StringBuilder();
        public synchronized void pushString(String s){ if(s!=null) buf.append(s); }
        public synchronized int readChar(){ if(buf.length()==0) return -1; int c=buf.charAt(0); buf.deleteCharAt(0); return c; }
        public synchronized int status(){ return buf.length()>0?1:0; }
    }

    public static class PrinterDevice {
        private java.util.function.Consumer<String> sink=null;
        public synchronized void write(String s){ if(sink!=null) sink.accept(s); }
        public synchronized void setListener(java.util.function.Consumer<String> c){ sink=c; }
        public synchronized int status(){ return 1; }
    }

    public static class CardReaderDevice {
        private final StringBuilder buf=new StringBuilder();
        public synchronized void loadText(String s){ buf.setLength(0); if(s!=null) buf.append(s); }
        public synchronized int readChar(){ if(buf.length()==0) return -1; int c=buf.charAt(0); buf.deleteCharAt(0); return c; }
        public synchronized int status(){ return buf.length()>0?1:0; }
    }

    public static class ConsoleRegisterDevice {
        private int v=0;
        public synchronized int readChar(){ return v; }
        public synchronized void write(String s){ try{ v=Integer.parseInt(s.trim()); }catch(Exception ignored){} }
        public synchronized int status(){ return 1; }
    }

    // ================== Registers =====================

    public GeneralRegister generalRegister = new GeneralRegister();
    public IndexRegister indexRegister = new IndexRegister();
    public ConditionRegister conditionRegister = new ConditionRegister();

    public KeyboardDevice keyboard = new KeyboardDevice();
    public PrinterDevice printer   = new PrinterDevice();
    public CardReaderDevice cardReader = new CardReaderDevice();
    public ConsoleRegisterDevice console = new ConsoleRegisterDevice();

    public char[] IR  = new char[16];
    public char[] MFR = new char[4];
    public char[] MBR = new char[16];
    public char[] MAR = new char[12];
    public char[] PC  = new char[12];

    // ================== Reset =====================

    public CPU_1_Simple(){ ResetRegisters(); }

    private void ResetRegisters(){
        generalRegister.data.clear();
        indexRegister.data.clear();
        conditionRegister.data.clear();
        Arrays.fill(IR,(char)0);
        Arrays.fill(MBR,(char)0);
        Arrays.fill(MAR,(char)0);
        Arrays.fill(PC,(char)0);
        Arrays.fill(MFR,(char)0);
        linkRegister = 0;
    }

    public void Reset(Memory mem){ ResetRegisters(); }

    // ================== ROM Loader =====================

    public boolean loadROM(File f, Memory mem){
        try(Scanner sc=new Scanner(f)){
            while(sc.hasNextLine()){
                String line=sc.nextLine().trim();
                if(line.isEmpty()||line.startsWith(";")) continue;
                String[] p=line.split("\\s+");
                int addr=Integer.parseInt(p[0],8);
                int val =Integer.parseInt(p[1],8);
                mem.setValue(addr,val);
            }
            return true;
        }catch(Exception e){ return false; }
    }

    public static FileChooser getROMFileChooser(){
        FileChooser fc=new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text","*.txt"));
        return fc;
    }

    // ================= Register helpers =================

    public void setGPR(short r, short v){ generalRegister.setValue(r,v); }
    public short getGPR(short r){ Integer x=generalRegister.getValue(r); return x==null?0:x.shortValue(); }

    public void setIXR(short x, short v){ indexRegister.setValue(x,v); }
    public short getIXR(short x){ Integer v=indexRegister.getValue(x); return v==null?0:v.shortValue(); }

    public void setPC(short v){ DecimalToBinary(v,PC,12); }
    public short getPC(){ return BinaryToDecimal(PC,12); }

    public void setMAR(short v){ DecimalToBinary(v,MAR,12); }
    public short getMAR(){ return BinaryToDecimal(MAR,12); }

    public void setMBR(short v){ DecimalToBinary(v,MBR,16); }
    public short getMBR(){ return BinaryToDecimal(MBR,16); }

    public void setCC(short v){ conditionRegister.setValue((short)0,v); }
    public short getCC(){ Integer v=conditionRegister.getValue((short)0); return v==null?0:v.shortValue(); }

    // ================= Memory Access =================

    public void memReadToMBR(Memory mem, short addr){
        setMAR(addr);
        int v=mem.readFromCache(addr & EA_MASK_12);
        setMBR((short)v);
    }

    public void memWriteFromMBR(Memory mem, short addr){
        mem.writeToCache(addr & EA_MASK_12, getMBR());
    }

    private short EA(int x,int d,int i,Memory mem){
        int base=(x>0&&x<=3)?(getIXR((short)x)&0x0FFF):0;
        int ea=(d&0x1F)+base;
        if(i==1) ea=mem.readFromCache((short)(ea&0x0FFF)) & 0x0FFF;
        return (short)(ea&0x0FFF);
    }

    // ================= Blocking char input =================

    private short readKeyboard(){
        while(true){
            int c=keyboard.readChar();
            if(c!=-1) return (short)c;
            try{ Thread.sleep(5);}catch(Exception ignored){}
        }
    }

    private short readCard(){
        while(true){
            int c=cardReader.readChar();
            if(c!=-1) return (short)c;
            try{ Thread.sleep(5);}catch(Exception ignored){}
        }
    }

    // ================= Execute Instruction =================

    public boolean ExecuteInstruction(int code, Memory mem){

        int op = (code>>>10)&0x3F;
        int r  = (code>>>8)&0x03;
        int x  = (code>>>6)&0x03;
        int i  = (code>>>5)&0x01;
        int d  =  code      &0x1F;

        short pcNow = getPC();
        short pcNext= (short)((pcNow+1)&0x0FFF);

        switch(op){

            case HALT: return false;

            case LDR: { short ea=EA(x,d,i,mem); memReadToMBR(mem,ea); setGPR((short)r,getMBR()); break; }
            case STR: { short ea=EA(x,d,i,mem); setMBR(getGPR((short)r)); memWriteFromMBR(mem,ea); break; }
            case LDA: { short ea=EA(x,d,i,mem); setGPR((short)r,ea); break; }

            case LDX:{ int ix=(code>>>8)&3; short ea=EA(x,d,i,mem); memReadToMBR(mem,ea); setIXR((short)ix,getMBR()); break; }
            case STX:{ int ix=(code>>>8)&3; short ea=EA(x,d,i,mem); setMBR(getIXR((short)ix)); memWriteFromMBR(mem,ea); break; }

            case AIR:{ setGPR((short)r,(short)(getGPR((short)r)+(d&0x1F))); break; }
            case SIR:{ setGPR((short)r,(short)(getGPR((short)r)-(d&0x1F))); break; }

            case AMR:{ short ea=EA(x,d,i,mem); memReadToMBR(mem,ea); setGPR((short)r,(short)(getGPR((short)r)+getMBR())); break; }
            case SMR:{ short ea=EA(x,d,i,mem); memReadToMBR(mem,ea); setGPR((short)r,(short)(getGPR((short)r)-getMBR())); break; }

            case JZ:  if(getGPR((short)r)==0){ setPC(EA(x,d,i,mem)); return true;} break;
            case JNE: if(getGPR((short)r)!=0){ setPC(EA(x,d,i,mem)); return true;} break;
            case JCC: if((getCC()&r)!=0){ setPC(EA(x,d,i,mem)); return true;} break;
            case JMA: setPC(EA(x,d,i,mem)); return true;

            case JSR:{
                short ret=(short)((getPC()+1)&0x0FFF);
                setGPR((short)3,ret);
                linkRegister=ret;
                short ea=EA(x,d,i,mem);
                setPC(ea);
                return true;
            }

            case RFS:{
                setGPR((short)0,(short)(d&0x1F));
                setPC(linkRegister!=0?linkRegister:getGPR((short)3));
                return true;
            }

            case SOB:{
                short v=(short)(getGPR((short)r)-1);
                setGPR((short)r,v);
                if(v>0){ setPC(EA(x,d,i,mem)); return true; }
                break;
            }

            case JGE:{
                if(getGPR((short)r)>=0){ setPC(EA(x,d,i,mem)); return true; }
                break;
            }

            case TRR:{
                int rx=(code>>>8)&3;
                int ry=(code>>>6)&3;
                int lhs=getGPR((short)rx), rhs=getGPR((short)ry);
                short cc= (lhs==rhs?1: lhs<rhs?2:4);
                setCC(cc);
                break;
            }

            case AND:{
                int rx=(code>>>8)&3, ry=(code>>>6)&3;
                setGPR((short)rx,(short)(getGPR((short)rx)&getGPR((short)ry)));
                break;
            }

            case ORR:{
                int rx=(code>>>8)&3, ry=(code>>>6)&3;
                setGPR((short)rx,(short)(getGPR((short)rx)|getGPR((short)ry)));
                break;
            }

            case NOT:{
                int rx=(code>>>8)&3;
                setGPR((short)rx,(short)~getGPR((short)rx));
                break;
            }

            case SRC:
            case RRC:{
                short val=getGPR((short)r);
                int c=d&0x1F;
                if(op==SRC) val=(short)((val&0xFFFF)<<c);
                else{
                    int u=val&0xFFFF;
                    val=(short)(((u>>>c)|(u<<(16-c)))&0xFFFF);
                }
                setGPR((short)r,val);
                break;
            }

            case IN:{
                int dev=d&0x1F;
                short v=(dev==0?readKeyboard():dev==2?readCard(): (short)console.readChar());
                setGPR((short)r,v);
                break;
            }

            case OUT:{
                int dev=d&0x1F;
                int v=getGPR((short)r)&0xFFFF;
                if(dev==1) printer.write(String.valueOf((char)(v&0xFF)));
                else console.write(String.valueOf(v));
                break;
            }

            case CHK:{
                int dev=d&0x1F;
                short s=(dev==0? (short)keyboard.status():
                         dev==1? (short)printer.status():
                         dev==2? (short)cardReader.status():
                                 (short)console.status());
                setGPR((short)r,s);
                break;
            }

            default:
                System.out.println("Bad opcode "+op+" at PC "+getPC());
                return false;
        }

        setPC(pcNext);
        return true;
    }

    public static void main(String[] args){
        System.out.println("CPU Ready.");
    }
}
