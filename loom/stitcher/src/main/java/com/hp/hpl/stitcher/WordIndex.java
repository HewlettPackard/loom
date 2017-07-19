/*******************************************************************************
 * (c) Copyright 2017 Hewlett Packard Enterprise Development LP Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.hp.hpl.stitcher;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class WordIndex<T extends Identifiable<Id>, Id> {

    public enum CharFilterMode {
        DEFAULT, WHITE_LIST, BLACK_LIST
    }

    // CONSTANTS -----------------------------------------------------------------------------------

    public static final String UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    public static final String LETTERS = UPPER_CASE_LETTERS + LOWER_CASE_LETTERS;
    public static final String NUMBERS = "0123456789";
    public static final String LETTERS_AND_NUMBERS = LETTERS + NUMBERS;

    private static final boolean PARALLEL_EXEC_BY_DEFAULT = false;
    private static final boolean IGNORE_CASE_BY_DEFAULT = true;
    private static final int MINIMUM_LENGHT_BY_DEFAULT = 1;

    // CONSTANTS - END -----------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    private Map<String, Collection<Id>> indexTable;
    private Collection<String> stopWords;
    private String whiteListFilterString, whiteListRegex;
    private String blackListFilterString, blackListRegex;
    private CharFilterMode charFilterMode;
    private int minimumWordLenght;

    private boolean parallelExec;
    private boolean ignoreCase;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public WordIndex() {
        indexTable = new ConcurrentHashMap<String, Collection<Id>>();
        stopWords = new TreeSet<String>();
        parallelExec = PARALLEL_EXEC_BY_DEFAULT;
        ignoreCase = IGNORE_CASE_BY_DEFAULT;
        charFilterMode = CharFilterMode.DEFAULT;
        minimumWordLenght = MINIMUM_LENGHT_BY_DEFAULT;
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    protected void index(T item, String tokens) {
        Collection<String> wordsToIndex = filterTokens(tokens);

        // Retrieve item ID
        Id itemId = item.getId();

        if (parallelExec) {
            // Index <item> from the kept words (parallel execution)
            wordsToIndex.parallelStream().forEach((String word) -> {
                indexItemByWord(itemId, word, indexTable);
            });
        } else {
            // Index <item> from the kept words (sequential execution)
            for (String word : wordsToIndex) {
                indexItemByWord(itemId, word, indexTable);
            }
        }

    }

    public void index(T item, Function<T, String> tokensRetriever) {
        index(item, tokensRetriever.apply(item));
    }

    public void index(Collection<T> items, Function<T, String> tokensRetriever) {
        if (parallelExec) {
            // Index <item> from the kept words (parallel execution)
            items.parallelStream().forEach((T item) -> {
                index(item, tokensRetriever);
            });
        } else {
            for (T item : items) {
                index(item, tokensRetriever);
            }
        }
    }

    public Collection<Id> getItems(String key) {
        Collection<Id> result;

        if (ignoreCase) {
            result = indexTable.get(key.toLowerCase());
        } else {
            result = indexTable.get(key);
        }

        if (result == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableCollection(result);
        }
    }

    public Collection<Id> removeKey(String toBeRemoved) {
        return indexTable.remove(toBeRemoved);
    }

    protected boolean remove(T item, String tokens) {
        Collection<String> keys = filterTokens(tokens);
        boolean changed = false;

        // Retrieve item ID
        Id itemId = item.getId();

        // Remove <item> from each key collection
        for (String key : keys) {
            changed |= removeItemByKey(itemId, key, indexTable);
        }

        return changed;
    }

    public boolean remove(T item, Function<T, String> tokensRetriever) {
        return remove(item, tokensRetriever.apply(item));
    }

    public boolean remove(Collection<T> items, Function<T, String> tokensRetriever) {
        boolean changed = false;

        // TODO: This execution can be optimised to remove all the item ids from from each key
        // collection at once.
        for (T item : items) {
            changed |= remove(item, tokensRetriever);
        }

        return changed;
    }

    public boolean addStopWords(Collection<String> toBeAdded) {
        return stopWords.addAll(toBeAdded);
    }

    public boolean removeStopWords(Collection<String> toBeRemoved) {
        return stopWords.removeAll(toBeRemoved);
    }

    public Collection<String> getStopWords() {
        return Collections.unmodifiableCollection(stopWords);
    }

    public int keySetSize() {
        return indexTable.keySet().size();
    }

    public int size() {
        return indexTable.size();
    }

    public CharFilterMode getCharFilterMode() {
        return charFilterMode;
    }

    public String getCharFilterModeString() {
        switch (charFilterMode) {
            // case DEFAULT: return null; // Unnecessary
            case WHITE_LIST:
                return whiteListFilterString;
            case BLACK_LIST:
                return blackListFilterString;
            default:
                return null;
        }
    }

    public void setCharFilterMode(CharFilterMode charFilterMode, String filterString) {
        throwIllegalArgExcIfNull(charFilterMode);
        switch (charFilterMode) {
            case WHITE_LIST:
                throwIllegalArgExcIfNull(filterString);
                whiteListFilterString = filterString;
                whiteListRegex = "[^" + whiteListFilterString + ']';
                break;
            case BLACK_LIST:
                throwIllegalArgExcIfNull(filterString);
                blackListFilterString = filterString;
                blackListRegex = '[' + blackListFilterString + ']';
                break;
            default:
                break;
        }

        this.charFilterMode = charFilterMode;
    }

    public int getMinimumWordLenght() {
        return minimumWordLenght;
    }

    public void setMinimumWordLenght(int minimumWordLenght) {
        this.minimumWordLenght = minimumWordLenght;
    }

    public boolean isParallelExec() {
        return parallelExec;
    }

    public void setParallelExec(boolean parallelExec) {
        this.parallelExec = parallelExec;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public Collection<String> filterTokens(String toBeFiltered) {
        Collection<String> filteredTokens = new ArrayList<String>();
        StringTokenizer st;

        if (ignoreCase) {
            st = new StringTokenizer(toBeFiltered.toLowerCase());
        } else {
            st = new StringTokenizer(toBeFiltered);
        }

        while (st.hasMoreElements()) {
            String candidate = filterString(st.nextToken());

            if (stopWords.contains(candidate)) {
                // This token is a stop word -> Go for the next token (new iteration)
                continue;
            }

            // Reached this point: This token is NOT a stop word.
            // Add it to the filtered list if its length is accepted
            if (candidate.length() >= minimumWordLenght) {
                filteredTokens.add(candidate);
            }
        }

        return filteredTokens;
    }

    // METHODS - END -------------------------------------------------------------------------------

    // HELPER METHODS ------------------------------------------------------------------------------

    static Collection<String> generateDefaultStopWords() {
        String stopWords[] = {"a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any",
                "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both",
                "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing",
                "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
                "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself",
                "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is",
                "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no",
                "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves",
                "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so",
                "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then",
                "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those",
                "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're",
                "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while",
                "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll",
                "you're", "you've", "your", "yours", "yourself", "yourselves"};

        Set<String> set = new TreeSet<String>();

        for (String s : stopWords) {
            set.add(s);
        }

        return Collections.unmodifiableSet(set);
    }

    void printWordIndexInOrder(OutputStream stream) {
        PrintStream printer = new PrintStream(stream);

        for (String key : new TreeSet<String>(indexTable.keySet())) {
            printer.printf("%s=%s\n", key, indexTable.get(key));
        }
    }

    private String filterString(String toBeFiltered) {
        if (charFilterMode == CharFilterMode.WHITE_LIST) {
            return toBeFiltered.replaceAll(whiteListRegex, "");
        } else if (charFilterMode == CharFilterMode.BLACK_LIST) {
            return toBeFiltered.replaceAll(blackListRegex, "");
        } else {
            return toBeFiltered;
        }
    }

    private void indexItemByWord(Id itemId, String word, Map<String, Collection<Id>> index) {
        Collection<Id> coll = index.get(word);
        if (coll == null) {
            coll = new ArrayList<Id>();
            index.put(word, coll);
        }
        coll.add(itemId);
    }

    private boolean removeItemByKey(Id itemId, String key, Map<String, Collection<Id>> index) {
        boolean result;

        Collection<Id> coll = index.get(key);
        result = coll.remove(itemId);

        if (coll.isEmpty()) {
            index.remove(key);
        }

        return result;
    }

    private void throwIllegalArgExcIfNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
    }

    // HELPER METHODS - END ------------------------------------------------------------------------

}
