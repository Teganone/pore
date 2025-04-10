import java.util.Random;

public class Encoder {

    private String convertHexToString(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length() - 1; i = i + 2) {
            stringBuilder.append((char) (Integer.parseInt(str.substring(i, i + 2), 16) ^ 255));
        }
        return stringBuilder.toString();
    }

    private String convertStringToHex(String str) {
        char[] chars = str.toCharArray();
        StringBuffer stringBuffer = new StringBuffer();
        for (char ch : chars)
            stringBuffer.append(Integer.toHexString(ch ^ 255));
        return stringBuffer.toString();
    }

    private byte[] getSalt() {
        byte[] bytes = {0, 0, 0, 0, 0, 0};
        Random random = new Random();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) random.nextInt(15);
        }
        return bytes;
    }


    public String decode(String str) {
        if (str.length() == 0)
            return "";
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < str.length(); i += 5) {
            int parseInt = 4 - (Integer.parseInt(str.substring(i, i + 1), 16) % 4);
            stringBuffer.append(str.substring(i + 1 + parseInt, i + 5) + str.substring(i + 1, parseInt + i + 1));
        }
        return convertHexToString(stringBuffer.toString()).substring(0, 11);
    }

    public String encode(String str) {
        if (str.length() != 11) {
            System.out.println("input error!");
            return "";
        }
        byte[] salt = getSalt();
        String convertStringToHex = convertStringToHex(str + "a");
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < convertStringToHex.length(); i += 4) {
            byte b = salt[i / 4];
            int i2 = b % 4;
            stringBuffer.append(Integer.toHexString(b));
            stringBuffer.append(convertStringToHex.substring(i + i2, i + 4) + convertStringToHex.substring(i, i2 + i));
        }
        return stringBuffer.toString();
    }
}
