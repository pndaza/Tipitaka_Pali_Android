package mm.pndaza.tipitakapali.utils;

public class NumberUtil {

    public static String toMyanmar(int engNum){

        String engNumber = String.valueOf(engNum);
        StringBuilder myanmarNumber = new StringBuilder();
        for(char ch : engNumber.toCharArray()){
            myanmarNumber.append((char) ((int) ch + 4112));
        }
        return myanmarNumber.toString();
    }

    public static boolean isMyanmarNumber(String myanmarText){
        return  myanmarText.matches("[၀-၉]+");
    }
}
