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
package com.hp.hpl.loom.manager.query.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.introspection.FibreIntrospectionUtils;

/**
 * Loom Query util class it contains a number of useful methods to interact with LoomQueries.
 */
public final class LoomQueryUtils {

    private static final double REBALANCE_FACTOR = 0.2;

    private LoomQueryUtils() {}

    // private static int cores = Runtime.getRuntime().availableProcessors();

    // ///////////////////////////////////////////////////////////////////
    //
    // BRAID UTILS
    //
    // ///////////////////////////////////////////////////////////////////

    /**
     * Braids a list of fibres into the maxFibres.
     *
     * @param input list of fibres to braid
     * @param maxFibres max fibres in total
     * @return Map of braided fibres
     */
    public static Map<Object, List<Fibre>> braid(final List<Fibre> input, final Integer maxFibres) {
        return LoomQueryUtils.simpleBraid(input, maxFibres);
    }

    /**
     * Braids a list of fibres using the FunctonSpec provide. Errors are returned in the errors map.
     *
     * @param input list of fibres to braid
     * @param spec function to braid with
     * @param errors map to return errors
     * @param context context to run the operation within
     * @return Map of braided fibres
     */
    public static Map<Object, List<Fibre>> braid(final List<Fibre> input, final FunctionSpec spec,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        return spec.getLoomFunction().apply(input, spec.getParams(), errors, context);
    }

    /**
     * Holds the dimensions of the the thread - number of fibres and levels.
     */
    public static class ThreadDimension {

        private int numFibres; // Minimum number of top-level fibres
        private int numLevels; // Minimum number of levels of braiding

        /**
         * @param numFibres Minimum number of top-level fibres
         * @param numLevels Minimum number of levels of braiding
         */
        public ThreadDimension(final int numFibres, final int numLevels) {
            this.numFibres = numFibres;
            this.numLevels = numLevels;
        }

        /**
         * Get the Minimum number of top-level fibres.
         *
         * @return Minimum number of top-level fibres
         */
        public int getNumFibres() {
            return numFibres;
        }

        /**
         * Get the Minimum number of levels of braiding.
         *
         * @return Minimum number of levels of braiding
         */
        public int getNumLevels() {
            return numLevels;
        }
    }

    /**
     * Calculate the minimum number of levels of nesting of groups, assuming maximal grouping.
     *
     * @param input List of fibres to calculate on
     * @param maxFibres maxFibres to include
     * @return The new dimensions
     */
    public static ThreadDimension calcThreadDimension(final List<Fibre> input, final int maxFibres) {
        int numFibres = (int) Math.ceil((float) input.size() / maxFibres);
        int numLevels = 1;
        while (numFibres > maxFibres) {
            numFibres = (int) Math.ceil((float) numFibres / maxFibres);
            numLevels++;
        }
        return new ThreadDimension(numFibres, numLevels);
    }

    /**
     * Calculate a simple spread of items into bucket separators.
     *
     * @param input List of fibres to calculate on
     * @param numFibres maxFibres to include
     * @param elemsPerFibre how many elements per fibre to include
     * @return list of the bucket seperators
     */
    public static List<Integer> calcSeparators(final List<Fibre> input, final int numFibres, final int elemsPerFibre) {
        List<Integer> separators = new ArrayList<Integer>();
        for (int bucketNum = 0; bucketNum < (numFibres - 1); bucketNum++) {
            separators.add((bucketNum + 1) * elemsPerFibre);
        }
        separators.add(input.size());
        return separators;
    }

    /**
     * Convert the list of fibres into buckets, using the separators provided.
     *
     * @param input list of fibres
     * @param separators list of separators
     * @return the map of buckets
     */
    public static Map<Object, List<Fibre>> convertToBuckets(final List<Fibre> input, final List<Integer> separators) {
        // Create the clusters, using the calculated indices
        Map<Object, List<Fibre>> braided = new ConcurrentHashMap<Object, List<Fibre>>(separators.size()); // Buckets
        Integer startOfBucket = 0;
        Integer bucket = 0;
        for (Integer indx : separators) {
            for (int m = startOfBucket; m < indx; m++) {
                if (braided.get(bucket) == null) {
                    braided.put(bucket, new ArrayList<Fibre>(indx - startOfBucket));
                }
                braided.get(bucket).add(input.get(m));
            }
            startOfBucket = indx;
            bucket++;
        }
        return braided;
    }

    /**
     * Perform a simple braiding operation on the fibres.
     *
     * @param input list of fibres
     * @param maxFibres maxiumum number of fibres for the braid
     * @return Map of braided fibres.
     */
    public static Map<Object, List<Fibre>> simpleBraid(final List<Fibre> input, final Integer maxFibres) {

        Map<Object, List<Fibre>> braided = new ConcurrentHashMap<Object, List<Fibre>>(maxFibres);
        int elemsPerFibre = (int) Math.floor((float) input.size() / maxFibres);

        List<Integer> separators = new ArrayList<Integer>();
        int bucketNum = 0;
        if (input.size() > maxFibres) {
            int i = elemsPerFibre - 1;
            while (i < input.size()) {
                if ((i + elemsPerFibre) > (input.size() - 1) || bucketNum == (maxFibres - 1)) {
                    separators.add(input.size() - 1);
                    break;
                } else {
                    separators.add(i);
                    i += elemsPerFibre;
                }
                bucketNum++;
            }

            List<Integer> balancedSeparators = new ArrayList<Integer>();
            // rebalance if needed
            int size1 = input.size() - 1;
            int size2 = separators.size() - 2;
            int sizeLast = size1 - separators.get(size2);
            int excessLast = sizeLast - elemsPerFibre;
            int excessLastCopy = excessLast;
            if (excessLast > (REBALANCE_FACTOR * (maxFibres - 1))) {
                // move all indices one position down
                List<Integer> shifts = IntStream.rangeClosed(1, maxFibres).boxed().collect(Collectors.toList());
                Integer j = 0;
                for (Integer sep : separators) {
                    if (excessLast > 0) {
                        sep = sep + shifts.get(j);
                        balancedSeparators.add(j, sep);
                        excessLast--;
                    } else {
                        if ((separators.get(j) + excessLastCopy) < (input.size() - 1)) {
                            balancedSeparators.add(separators.get(j) + excessLastCopy);
                        } else {
                            balancedSeparators.add(input.size() - 1);
                        }
                    }
                    j++;
                }
            } else {
                balancedSeparators = separators;
            }

            Integer k = 0;
            Integer bucket = 0;
            for (Integer indx : balancedSeparators) {
                for (int m = k; m <= indx; m++) {
                    if (braided.get(bucket) == null) {
                        braided.put(bucket, new ArrayList<Fibre>(elemsPerFibre));
                    }
                    braided.get(bucket).add(input.get(m));
                }
                k = indx + 1;
                bucket++;
            }
        } else {
            braided.put(0, input);
        }
        return braided;
    }

    // ///////////////////////////////////////////////////////////////////
    //
    // SORT UTILS
    //
    // ///////////////////////////////////////////////////////////////////

    /**
     * Build a sort comparator for from the attribute, recording errors in the errors map.
     *
     * @param attribute Attribute to introspect for sorting parameters.
     * @param errors error map
     * @param context query context
     * @return Comparator for the fibre based on the Attribute
     */
    public static Comparator<Fibre> buildSortComparator(final String attribute,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        return (final Fibre e1, final Fibre e2) -> {
            Object at1 = FibreIntrospectionUtils.introspectProperty(attribute, e1, errors, context);
            Object at2 = FibreIntrospectionUtils.introspectProperty(attribute, e2, errors, context);

            if (at1 == null && at2 != null) {
                errors.put(OperationErrorCode.NullField, "NULL " + attribute);
                return 1;
            }
            if (at2 == null && at1 != null) {
                errors.put(OperationErrorCode.NullField, "NULL " + attribute);
                return -1;
            }
            if (at1 == null && at2 == null) {
                errors.put(OperationErrorCode.NullField, "NULLs " + attribute);
                return 0;
            }
            // is an Numeric
            if (Number.class.isAssignableFrom(at1.getClass())) {
                // value
                Double n1 = ((Number) at1).doubleValue();
                Double n2 = ((Number) at2).doubleValue();
                if ((n1 - n2) < 0.0) {
                    return -1;
                } else if (n1 - n2 == 0.0) {
                    return 0;
                } else {
                    return 1;
                }
                // return (int) (n1 - n2);
            } else { // is a String
                Pair<Boolean, Boolean> num1 = LoomQueryUtils.isNumber(at1.toString().trim());
                Pair<Boolean, Boolean> num2 = LoomQueryUtils.isNumber(at2.toString().trim());
                String s1 = at1.toString().trim();
                String s2 = at2.toString().trim();
                if (LoomQueryUtils.bothAreNumbers(num1, num2)) {
                    Double n1 = Double.parseDouble(s1);
                    Double n2 = Double.parseDouble(s2);
                    if ((n1 - n2) < 0.0) {
                        return -1;
                    } else if (n1 - n2 == 0.0) {
                        return 0;
                    } else {
                        return 1;
                    }
                    // return (int) (n1 - n2);
                } else {
                    return s1.compareTo(s2);
                }
            }
        };
    }

    // ///////////////////////////////////////////////////////////////////
    //
    // GENERAL UTILS
    //
    // ///////////////////////////////////////////////////////////////////

    //
    /**
     * Check if the string is a number. This still doesn't cover "NaN" or hex values.
     *
     * Suggest we move to the Apache commons for this feature.
     *
     * @param value String to cover
     * @return the pair of boolean result
     */
    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    public static Pair<Boolean, Boolean> isNumber(final String value) {
        boolean seenDot = false;
        boolean seenExp = false;
        boolean justSeenExp = false;
        boolean seenDigit = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') {
                seenDigit = true;
                continue;
            }
            if ((c == '-' || c == '+') && (i == 0 || justSeenExp)) {
                continue;
            }
            if (c == '.' && !seenDot) {
                seenDot = true;
                continue;
            }
            justSeenExp = false;
            if ((c == 'e' || c == 'E') && !seenExp) {
                seenExp = true;
                justSeenExp = true;
                continue;
            }

            return new Pair(false, false);
        }
        if (!seenDigit) {
            return new Pair(false, false);
        }
        try {
            Double.parseDouble(value);
            return new Pair(true, seenDot || seenExp);
        } catch (NumberFormatException e) {
            return new Pair(false, false);
        }
    }

    private static boolean bothAreNumbers(final Pair<Boolean, Boolean> num1, final Pair<Boolean, Boolean> num2) {
        return num1.getKey() && num2.getKey();
    }


    /**
     * Get the first input from a pipelink, recording any errors in the error map.
     *
     * @param pipe pipelink to get the first input from
     * @param errors error map
     * @return first input
     */
    public static List<Fibre> getFirstInput(final PipeLink pipe, final Map<OperationErrorCode, String> errors) {
        Optional<Fibre> inputElement = pipe.values().stream().findFirst();
        List<Fibre> input = new ArrayList<>(0);
        if (!inputElement.isPresent()) {
            errors.put(OperationErrorCode.EmptyInput, "");
        } else {
            input = (List<Fibre>) inputElement.get();
        }
        return input;
    }

    // ///////////////////////////////////////////////////////////////////
    //
    // ATTRIBUTES LOOKUP UTILS
    //
    // ///////////////////////////////////////////////////////////////////

    /**
     * Convert list of attributes to numbers.
     *
     * @param attributes list of attributes
     * @param le Fibre to work on
     * @param errors error map
     * @param context query context
     * @return List of numbers from the fibre base on the attributes
     */
    public static List<Double> convertAttributesToNumbers(final List<String> attributes, final Fibre le,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        if (le.isAggregation()) {
            return attributes.stream().map(att -> att + "_avg")
                    .map(att -> LoomQueryUtils
                            .getNumRepresentation(FibreIntrospectionUtils.introspectProperty(att, le, errors, context)))
                    .collect(Collectors.toList());
        } else {
            return attributes.stream()
                    .map(att -> LoomQueryUtils
                            .getNumRepresentation(FibreIntrospectionUtils.introspectProperty(att, le, errors, context)))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Get a number representation from the object provided.
     *
     * @param att object to try and represent
     * @return number that represents
     */
    public static Double getNumRepresentation(final Object att) {
        // is an Numeric value
        if (Number.class.isAssignableFrom(att.getClass())) {
            return ((Number) att).doubleValue();
        } else { // is a String
            Pair<Boolean, Boolean> num1 = LoomQueryUtils.isNumber(att.toString().trim());
            String s1 = att.toString().trim();
            if (num1.getKey()) {
                return Double.parseDouble(s1);
            } else {
                return Double.valueOf(s1);
            }
        }
    }


}
