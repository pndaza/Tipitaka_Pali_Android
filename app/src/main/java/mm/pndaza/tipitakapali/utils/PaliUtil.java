package mm.pndaza.tipitakapali.utils;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PaliUtil {

    public static String getStemWord(String word){
        String stemWord = word;
        stemWord = stemWord.trim();
        // remove punctuation
        stemWord = stemWord.replaceAll("[\u104a\u104b\u2018\u2019'\",\\.\\?]","");

        // various ending of pada
        List<String> endings = new ArrayList<>();


        endings.add("\u1031\u1014$"); // ena ေန
        endings.add("\u1031[\u101f\u1018]\u102d$"); // ehi ebhi ေဟိ ေဘိ
        endings.add("\u103f$");  // ssa ဿ
        // naṃ (နံ) preceded by vowel ā or ī or ū
        // first, will find with dīgha vowel in dict
        // if not find , convert to rassa vowel and will find again
        endings.add("(?<=[\u102b\u102c\u102e\u1030])\u1014\u1036$");
        endings.add("\u101e\u1039\u1019\u102c$"); // smā သ္မာ
        endings.add("\u1019\u103e\u102c$"); // mhā မှာ
        endings.add("\u101e\u1039\u1019\u102d\u1036$"); // smiṃ သ္မိံ
        endings.add("\u1019\u103e\u102d$"); // mhi မှိ
        endings.add("\u1031\u101e\u102f$"); // esu ေသု
        // cittādigana
        endings.add("[\u102b\u102c]\u1014\u102d$"); // // āni ါနိ or ာနိ
        // kannādigana etc
        endings.add("(?<=[\u102b\u102c\u102d])\u101a\u1031\u102c$"); // āyo ါယော or ာယော
        endings.add("(?<=[\u102d])\u101a\u102c$"); // iyā ိယာ
        endings.add("(?<=[\u102b\u102c])\u101a\u1036?$"); //  āya or āyaṃ  ါယ or ါယံ or ာယ or ာယံ
        // su (သု) preceded by vowel ā or ī or ū
        // first, will find with dīgha vowel in dict
        // if not find , convert to rassa vowel and will find again
        endings.add("(?<=[\u102b\u102c\u102d\u102e\u102f\u1030])\u101e\u102f?$");

        endings.add("\u1031[\u102b\u102c]$"); // dependent vowel O ော
        endings.add("\u1031$"); // dependent vowel E ေ
        endings.add("\u1036$"); // niggahita ṃ ံ

        for ( String ending : endings){
            if(Pattern.compile(ending).matcher(stemWord).find()){
                stemWord = stemWord.replaceAll(ending, "");
                break;
            }
        }

        return stemWord;
    }

    public static boolean isEndWithRassa(String word){

        return Pattern.compile("[\u1000-\u1020\u102d\u102f]").matcher(word).find();
    }

    public static boolean isEndWithDigha(String word){

        return Pattern.compile("[\u102b\u102c\u102e\u1030]").matcher(word).find();
    }

    public static String convertToDigha(String word){

        String _word = word;
        int length = _word.length();

        if (word.endsWith("\u102d")){
            return _word.substring(0, length - 1) + "\u102e";
        }
        if (word.endsWith("\u102f")){
            return _word.substring(0, length - 1) + "\u1030";
        }
        // TODO to insert suitable shape of dependent vowel ā
        String lastChar = _word.substring(length-1);
        if(lastChar.matches("[ခဂဒပဝ]")){
            return _word + "\u102b";
        }
        return _word + "\u102c";
    }

    public static String convertToRassa(String word){

        String _word = word;
        int length = _word.length();

        if (word.endsWith("\u102e")){
            _word = _word.substring(0, length - 1) + "\u102d";
        } else if (word.endsWith("\u1030")){
            _word = _word.substring(0, length - 1) + "\u102f";
        } else {
            _word = _word.substring(0, length - 1) ;
        }

        return _word;
    }
}
