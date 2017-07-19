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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SetOperations {

    private SetOperations() {}

    public static <T> Set<T> union(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }

    public static <T> Set<T> intersection(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.retainAll(setB);
        return tmp;
    }

    public static <T> Set<T> difference(Collection<T> setA, Collection<T> setB) {
        Set<T> tmp = new HashSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }

    public static <T> Set<T> symDifference(Collection<T> setA, Collection<T> setB) {
        Collection<T> tmpA;
        Collection<T> tmpB;

        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }

    // public static void main(String args[]) {
    //
    // Set<Integer> set1to5, set3to7;
    //
    // set1to5 = new HashSet<Integer>(Arrays.asList(1, 2, 3, 4, 5));
    // set3to7 = new HashSet<Integer>(Arrays.asList(3, 4, 5, 6, 7));
    //
    // System.out.printf("set1to5: %s\n", set1to5);
    // System.out.printf("set3to7: %s\n", set3to7);
    // System.out.printf("Union: %s\n", union(set1to5, set3to7));
    // System.out.printf("Union: %s\n", union(set3to7, set1to5));
    // System.out.printf("Intersection: %s\n", intersection(set1to5, set3to7));
    // System.out.printf("Intersection: %s\n", intersection(set3to7, set1to5));
    // System.out.printf("set1to5 - set3to7: %s\n", difference(set1to5, set3to7));
    // System.out.printf("set3to7 - set1to5: %s\n", difference(set3to7, set1to5));
    // System.out.printf("Symmetric difference: %s\n", symDifference(set1to5, set3to7));
    // System.out.printf("Symmetric difference: %s\n", symDifference(set3to7, set1to5));
    // }

}
