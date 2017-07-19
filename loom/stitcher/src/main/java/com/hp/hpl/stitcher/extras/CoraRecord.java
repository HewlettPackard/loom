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
package com.hp.hpl.stitcher.extras;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.stitcher.Identifiable;

public class CoraRecord implements Identifiable<String> {

    // CONSTANTS -----------------------------------------------------------------------------------

    static final String LOCKED_EXC_MESSAGE = "Record is locked and cannot be modified";
    static final String SHINGLE_SIZE_EXC_MESSAGE = "Shingle size needs to be strictly positive";

    // CONSTANTS - END -----------------------------------------------------------------------------

    // VARIABLES -----------------------------------------------------------------------------------

    // If this record is locked, changing its attributes will result in an IllegalStateException
    boolean locked;

    String id;
    String publicationId;
    String authors;
    String title;
    SortedSet<String> authorsShingles;
    SortedSet<String> titleShingles;
    int authorsShingleLenght;
    int titleShingleLenght;

    // VARIABLES - END -----------------------------------------------------------------------------

    // CONSTRUCTORS --------------------------------------------------------------------------------

    public CoraRecord(String id, String publicationId, String authors, String title, int shingleLenght) {
        if (shingleLenght <= 0) {
            throw new IllegalArgumentException(SHINGLE_SIZE_EXC_MESSAGE);
        }

        this.id = id;
        this.publicationId = publicationId;
        this.authors = authors;
        this.title = title;
        this.locked = false;
        this.authorsShingleLenght = shingleLenght;
        this.titleShingleLenght = shingleLenght;
    }

    // CONSTRUCTORS - END --------------------------------------------------------------------------

    // METHODS -------------------------------------------------------------------------------------

    public CoraRecord lock() {
        locked = true;

        return this;
    }

    public CoraRecord unlock() {
        locked = false;
        authorsShingles.clear();
        titleShingles.clear();

        return this;
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getPublicationId() {
        return publicationId;
    }

    public String getAuthors() {
        return authors;
    }

    public CoraRecord setAuthors(String authors) {
        throwExceptionIfLocked();
        this.authors = authors;

        return this;
    }

    public int getAuthorsShingleLenght() {
        return authorsShingleLenght;
    }

    public CoraRecord setAuthorsShingleLenght(int authorsShingleLenght) {
        throwExceptionIfLocked();
        this.authorsShingleLenght = authorsShingleLenght;

        return this;
    }

    public String getTitle() {
        return title;
    }

    public CoraRecord setTitle(String title) {
        throwExceptionIfLocked();
        this.title = title;

        return this;
    }

    public SortedSet<String> getAuthorsShingles() {
        if (authorsShingles == null) {
            authorsShingles = calculateShingles(authors, authorsShingleLenght);
        }

        return Collections.unmodifiableSortedSet(authorsShingles);
    }

    public SortedSet<String> getTitleShingles() {
        if (titleShingles == null) {
            titleShingles = calculateShingles(title, titleShingleLenght);
        }

        return Collections.unmodifiableSortedSet(titleShingles);
    }

    public int getTitleShingleLenght() {
        return titleShingleLenght;
    }

    public CoraRecord setTitleShingleLenght(int titleShingleLenght) {
        throwExceptionIfLocked();
        this.titleShingleLenght = titleShingleLenght;

        return this;
    }

    private void throwExceptionIfLocked() {
        if (locked) {
            throw new IllegalStateException(LOCKED_EXC_MESSAGE);
        }
    }

    private SortedSet<String> calculateShingles(String s, int shingleLength) {
        SortedSet<String> result = new TreeSet<String>();

        String sIgnoreCase = s.toLowerCase();
        int howManyShingles = sIgnoreCase.length() - shingleLength + 1;
        for (int i = 0; i < howManyShingles; i++) {
            result.add(sIgnoreCase.substring(i, i + shingleLength));
        }

        return result;
    }

    // METHODS - END -------------------------------------------------------------------------------

}
