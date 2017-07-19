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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.function.Function;

import org.junit.Test;

import static com.hp.hpl.stitcher.StitcherTestsUtils.*;

public class WordIndexTest {

    // CLASSES -------------------------------------------------------------------------------------

    class TitleAndArtist implements Identifiable<Integer> {

        private int id;
        private String title, artist;

        public TitleAndArtist(int id, String title, String artist) {
            this.id = id;
            this.title = title;
            this.artist = artist;
        }

        @Override
        public Integer getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

    }

    class TitleRetriever implements Function<TitleAndArtist, String> {

        @Override
        public String apply(TitleAndArtist item) {
            return item.getTitle();
        }

    }

    class ArtistRetriever implements Function<TitleAndArtist, String> {

        @Override
        public String apply(TitleAndArtist item) {
            return item.getArtist();
        }

    }

    // CLASSES - END -------------------------------------------------------------------------------

    // VARIABLES AND CONSTANTS ---------------------------------------------------------------------

    public static final int RUNS = 20;

    private Collection<String> stopWords;

    private TitleAndArtist loveMeDo;
    private TitleAndArtist allYouNeedIsLove;
    private TitleAndArtist iNeedYou;
    private TitleAndArtist withOrWithoutYou;

    private TitleAndArtist random1;
    private String random1Title = "Just a random title";
    private String random1Artist = "Just a random artist";
    private TitleAndArtist random2;
    private String random2Title = "Just another random title";
    private String random2Artist = "Just another random artist";
    private TitleAndArtist random3;
    private String random3Title = "Some title I just made up";
    private String random3Artist = "Some artist I just made up";
    private TitleAndArtist random4;
    private String random4Title = "One more title which just came to my mind";
    private String random4Artist = "One more artist which just came to my mind";
    private TitleAndArtist random5;
    private String random5Title = "Random title just plain and simple";
    private String random5Artist = "Random artist just plain and simple";
    private TitleAndArtist random6;
    private String random6Title = "Not to forget just another title";
    private String random6Artist = "Not to forget just another artist";

    // VARIABLES AND CONSTANTS - END ---------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public WordIndexTest() {
        stopWords = WordIndex.generateDefaultStopWords();

        // Songs
        loveMeDo = new TitleAndArtist(1, "Love me do", "The Beatles");
        allYouNeedIsLove = new TitleAndArtist(2, "All you need is love", "The Beatles");
        iNeedYou = new TitleAndArtist(3, "I need you", "The Beatles");
        withOrWithoutYou = new TitleAndArtist(4, "With or without you", "U2");

        // Random stuff
        random1 = new TitleAndArtist(1, random1Title, random1Artist);
        random2 = new TitleAndArtist(2, random2Title, random2Artist);
        random3 = new TitleAndArtist(3, random3Title, random3Artist);
        random4 = new TitleAndArtist(4, random4Title, random4Artist);
        random5 = new TitleAndArtist(5, random5Title, random5Artist);
        random6 = new TitleAndArtist(6, random6Title, random6Artist);
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    @Test
    public void testNoStopWords() {
        for (int i = 0; i < RUNS; i++) {
            testNoStopWordsSub();
        }
    }

    public void testNoStopWordsSub() {
        WordIndex<TitleAndArtist, Integer> wi = new WordIndex<TitleAndArtist, Integer>();

        wi.index(Arrays.asList(loveMeDo, allYouNeedIsLove, iNeedYou, withOrWithoutYou), new TitleRetriever());

        checkSameElements(getIntegerCollection("2 3 4"), wi.getItems("you"));
        checkSameElements(getIntegerCollection("1 2"), wi.getItems("love"));
        checkSameElements(getIntegerCollection("2 3"), wi.getItems("need"));
        checkSameElements(getIntegerCollection("1"), wi.getItems("me"));
        checkSameElements(getIntegerCollection("1"), wi.getItems("do"));
        checkSameElements(getIntegerCollection("2"), wi.getItems("all"));
        checkSameElements(getIntegerCollection("2"), wi.getItems("is"));
        checkSameElements(getIntegerCollection("3"), wi.getItems("I"));
        checkSameElements(getIntegerCollection("4"), wi.getItems("with"));
        checkSameElements(getIntegerCollection("4"), wi.getItems("or"));
        checkSameElements(getIntegerCollection("4"), wi.getItems("without"));
    }

    @Test
    public void testStopWords() {
        for (int i = 0; i < RUNS; i++) {
            testStopWordsSub();
        }
    }

    public void testStopWordsSub() {

        WordIndex<TitleAndArtist, Integer> wiTitle = new WordIndex<TitleAndArtist, Integer>();
        wiTitle.addStopWords(stopWords);

        WordIndex<TitleAndArtist, Integer> wiArtist = new WordIndex<TitleAndArtist, Integer>();
        wiArtist.addStopWords(stopWords);

        wiTitle.index(Arrays.asList(random1, random2, random3, random4, random5, random6), new TitleRetriever());
        wiArtist.index(Arrays.asList(random1, random2, random3, random4, random5, random6), new ArtistRetriever());

        checkSameElements(getIntegerCollection(""), wiTitle.getItems("a")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("and")); // Stop word!
        checkSameElements(getIntegerCollection("2 6"), wiTitle.getItems("another"));
        checkSameElements(getIntegerCollection("4"), wiTitle.getItems("came"));
        checkSameElements(getIntegerCollection("6"), wiTitle.getItems("forget"));
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("i")); // Stop word!
        checkSameElements(getIntegerCollection("1 2 3 4 5 6"), wiTitle.getItems("just"));
        checkSameElements(getIntegerCollection("3"), wiTitle.getItems("made"));
        checkSameElements(getIntegerCollection("4"), wiTitle.getItems("mind"));
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("more")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("my")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("not")); // Stop word!
        checkSameElements(getIntegerCollection("4"), wiTitle.getItems("one"));
        checkSameElements(getIntegerCollection("5"), wiTitle.getItems("plain"));
        checkSameElements(getIntegerCollection("1 2 5"), wiTitle.getItems("random"));
        checkSameElements(getIntegerCollection("5"), wiTitle.getItems("simple"));
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("some")); // Stop word!
        checkSameElements(getIntegerCollection("1 2 3 4 5 6"), wiTitle.getItems("title"));
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("to")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("up")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiTitle.getItems("which")); // Stop word!

        checkSameElements(getIntegerCollection(""), wiArtist.getItems("a")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("and")); // Stop word!
        checkSameElements(getIntegerCollection("2 6"), wiArtist.getItems("another"));
        checkSameElements(getIntegerCollection("1 2 3 4 5 6"), wiArtist.getItems("artist"));
        checkSameElements(getIntegerCollection("4"), wiArtist.getItems("came"));
        checkSameElements(getIntegerCollection("6"), wiArtist.getItems("forget"));
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("i")); // Stop word!
        checkSameElements(getIntegerCollection("1 2 3 4 5 6"), wiArtist.getItems("just"));
        checkSameElements(getIntegerCollection("3"), wiArtist.getItems("made"));
        checkSameElements(getIntegerCollection("4"), wiArtist.getItems("mind"));
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("more")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("my")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("not")); // Stop word!
        checkSameElements(getIntegerCollection("4"), wiArtist.getItems("one"));
        checkSameElements(getIntegerCollection("5"), wiArtist.getItems("plain"));
        checkSameElements(getIntegerCollection("1 2 5"), wiArtist.getItems("random"));
        checkSameElements(getIntegerCollection("5"), wiArtist.getItems("simple"));
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("some")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("to")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("up")); // Stop word!
        checkSameElements(getIntegerCollection(""), wiArtist.getItems("which")); // Stop word!
    }

    // METHODS -------------------------------------------------------------------------------------

    // HELPER METHODS ------------------------------------------------------------------------------

    private Collection<Integer> getIntegerCollection(String integers) {
        Collection<Integer> coll = new ArrayList<Integer>();

        StringTokenizer st = new StringTokenizer(integers);
        while (st.hasMoreTokens()) {
            coll.add(Integer.parseInt(st.nextToken()));
        }

        return coll;
    }

    // HELPER METHODS - END ------------------------------------------------------------------------

}
