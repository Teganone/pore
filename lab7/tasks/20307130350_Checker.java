public class Checker {

    byte[] secret = {112, 100, 100, 68, 31, 5, 114, 120};

    public Checker() {
    }

    private static byte charToByteAscii(char ch) {
        return (byte) ch;
    }

    private boolean checkStr1(String str) {
        for (int i = 0; i < str.length(); i++) {
            if ((charToByteAscii(str.charAt(i)) ^ (i * 11)) != this.secret[i]) {
                return false;
            }
        }
        return true;
    }


    private boolean checkStr2(String str) {
        try {
            Integer value = Integer.parseInt(str);
            if (value < 1000)
                return false;
            if (value % 16 == 0 || value % 27 == 0 || value % 10 == 8)
                return true;
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean check(String str) {
        if (str.length() != 12)
            return false;
        if (checkStr1(str.substring(0, 8)) && checkStr2(str.substring(8, 12)))
            return true;
        return false;
    }

}
