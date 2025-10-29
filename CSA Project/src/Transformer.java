import java.util.Arrays;

public class Transformer {
    public char[] DecimalToBinary(short decimal, char[] bin, int count) {
    	Arrays.fill(bin, '0');
        int temp = decimal;
        for (int i = count - 1; i >= 0 && temp > 0; i--) {
            bin[i] = (char) (temp % 2 + '0');
            temp /= 2;
        }
//        System.out.println("transformer:"+new String(bin)+"\n");
        return bin;
    }

    public short BinaryToDecimal(char[] bin, int count) throws BlankCharArrayException{
    	boolean allBlank = true;
    	for (char c : bin) {
    	    if (c != ' ' && c != '\u0000') {
    	        allBlank = false;
    	        break;
    	    }
    	}
    	if (allBlank) {
    	    throw new BlankCharArrayException("The char array is blank!");
    	}
    	
        short out = 0;
        for (int i = 0; i < count; i++) {
        	out += (short) (bin[count-1-i] - '0')*Math.pow(2, i);
        }
//        System.out.println("binary:"+new String(bin));
//        System.out.println("decimal:"+out);
        return out;
    }
        

    public short OctToDecimal(String oct) {
        return Short.parseShort(oct, 8);
    }

    public void SrcToDes(char[] src, char[] dest, int len) {
        System.arraycopy(src, 0, dest, 0, len);
    }
}