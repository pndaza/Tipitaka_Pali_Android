package mm.pndaza.tipitakapali.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Word;

public class SearchFactory {

    // explicit index because tipitaka have exact books and pages
    private static final int START_INDEX_PALI = 1;
    private static final int END_INDEX_PALI = 16688;
    private static final int START_INDEX_ATTHA = END_INDEX_PALI + 1;
    private static final int END_INDEX_ATTHA = 34457;
    private static final int START_INDEX_TIKA = END_INDEX_ATTHA + 1;
    private static final int END_INDEX_TIKA = 44182;
    private static final int START_INDEX_ANNYA = END_INDEX_TIKA + 1;
    private static final int END_INDEX_ANNYA = 56150;

    private static final int PALI = 0;
    private static final int ATTHA = 1;
    private static final int TIKA = 2;
    private static final int ANNYA = 3;


    private static final String TAG = "SearchFactory";

    public static ArrayList<Word> Search(Context context, String queryword, Boolean[] searchFilter) {

        DBOpenHelper dbOpenHelper = DBOpenHelper.getInstance(context);


        ArrayList<Word> results = null;

        String[] words = queryword.trim().split(" +");
        for (String word : words) {
            if (results == null) {
                // one word search or first word of phrase
                results = dbOpenHelper.getPageIdListOfWord(word);
                if (!results.isEmpty()) {
                    doFilter(results, searchFilter);
                }
            } else {
                // second or other words of phrase
                if (!results.isEmpty()) {
                    ArrayList<Word> idListOfNextWord = dbOpenHelper.getPageIdListOfWord(word);

                    if (idListOfNextWord.isEmpty()) {

                        results.clear();

                    } else {

                        List<Word> match = findMatch(results, idListOfNextWord);
                        results.clear();
                        results.addAll(match);
                    }
                }
            }
        }

        return results;
    }


    private static void doFilter(ArrayList<Word> result, Boolean[] searchFilter) {

        for (int i = 0; i < searchFilter.length; i++) {
            if (!searchFilter[i]) {
                removeFromResult(result, i);
            }
        }
    }

    private static List<Word> findMatch(List<Word> previous, List<Word> next){

        Comparator<Word> wordComparator = new Comparator<Word>() {
            @Override
            public int compare(Word word, Word t1) {
                return ((Integer) word.getRowid()).compareTo(t1.getRowid());
            }
        };

        List<Word> match = new ArrayList<>();

        int lengthOfPrevious = previous.size();
        int lengthOfNext = next.size();

        if ( lengthOfNext < lengthOfPrevious) {
            for (Word nextWord : next) {
                int index = Collections.binarySearch(previous, nextWord, wordComparator);
                if (index > -1) {
                    int leftMostMatch = findLeftMostMatch(previous, index, nextWord.getRowid());
                    int rightMostMatch = findRightMostMatch(previous, index, nextWord.getRowid());
                    for (int i = leftMostMatch; i <= rightMostMatch; i++) {
                        if (previous.get(i).getLocation() + 1 == nextWord.getLocation()) {
                            match.add(nextWord);
                        }
                    }
                }
            }
        } else {
            for (Word previousWord : previous ){
                int index = Collections.binarySearch(next, previousWord, wordComparator);
                if (index > -1) {
                    int leftMostMatch = findLeftMostMatch(next, index, previousWord.getRowid());
                    int rightMostMatch = findRightMostMatch(next, index, previousWord.getRowid());
                    for (int i = leftMostMatch; i <= rightMostMatch; i++) {
                        if (next.get(i).getLocation() - 1 == previousWord.getLocation()) {
                            match.add(previousWord);
                        }
                    }
                }
            }
        }

        return match;

    }

    private static int findLeftMostMatch(List<Word> list, int index, int rowId){

        int left = index;
        while (left - 1 >= 0 && list.get(left - 1).getRowid() ==rowId) {
            left--;
        }
        return left;
    }

    private static int findRightMostMatch(List<Word> list, int index, int rowId){

        int right = index;
        int length = list.size();
        while (right + 1 < length && list.get(right + 1).getRowid() ==rowId) {
            right++;
        }
        return right;
    }

    private static void removeFromResult(ArrayList<Word> result, int category) {

        int start = 0;
        int end = 0;
        switch (category) {
            case PALI:
                start = START_INDEX_PALI;
                end = END_INDEX_PALI;
                break;
            case ATTHA:
                start = START_INDEX_ATTHA;
                end = END_INDEX_ATTHA;
                break;
            case TIKA:
                start = START_INDEX_TIKA;
                end = END_INDEX_TIKA;
                break;
            case ANNYA:
                start = START_INDEX_ANNYA;
                end = END_INDEX_ANNYA;
                break;
        }

        List<Word> found = new ArrayList<>();
        for (Word word : result) {
            if (word.getRowid() >= start && word.getRowid() <= end) {
                found.add(word);
            }
        }
        result.removeAll(found);
    }

}
