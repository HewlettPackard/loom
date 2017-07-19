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
package com.hp.hpl.loom.manager.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.hp.hpl.loom.model.Fibre;

/**
 * Encapsulate runtime context for executing operations, which is basically the function that needs
 * to be executed and any other features affecting the query processing.
 *
 */
public class QueryOperation {

    /** the order key. */
    public static final String ORDER = "order";
    /** the property key. */
    public static final String PROPERTY = "property";
    /** the attribute key. */
    public static final String ATTRIBUTE = "attribute";
    /** the attributes key. */
    public static final String ATTRIBUTES = "attributes";
    /** the complement key. */
    public static final String COMPLEMENT = "complement";
    /** the maximums key. */
    public static final String MAXIMUMS = "maximums";
    /** the minimums key. */
    public static final String MINIMUMS = "minimums";
    /** the asc_order key. */
    public static final String ASC_ORDER = "ASC";
    /** the dsc_order key. */
    public static final String DSC_ORDER = "DSC";
    /** the pattern key. */
    public static final String PATTERN = "pattern";
    /** the id key. */
    public static final String ID = "id";
    /** the n key. */
    public static final String N = "N";
    /** the max_fibres key. */
    public static final String MAX_FIBRES = "maxFibres";
    /** the buckets key. */
    public static final String BUCKETS = "numBuckets";
    /** the tight_packing key. */
    public static final String TIGHT_PACKING = "tightPacking";
    /** the deltas key. */
    public static final String DELTAS = "deltas";
    /** the translations key. */
    public static final String TRANSLATIONS = "translations";
    /** the polygons key. */
    public static final String POLYGONS = "polygons";
    /** the default polygon key. */
    public static final String DEFAULT_POLYGON = "defaultPolygon";
    /** The k key (in k-means). */
    public static final String K = "k";

    private final List<Predicate<PipeLink<Fibre>>> falsePredicateList;
    @SuppressWarnings("checkstyle:linelength")
    private QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> function;
    private boolean interItemType;
    private List<Predicate<PipeLink<Fibre>>> createDAIfOutputLengthIsOne;

    /**
     * @param function function to run
     * @param interItemType is it an item that interplays with others
     */
    @SuppressWarnings("checkstyle:linelength")
    public QueryOperation(
            final QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> function,
            final boolean interItemType) {
        this.function = function;
        this.interItemType = interItemType;
        falsePredicateList = new ArrayList<>(1);
        falsePredicateList.add(x -> false);
    }

    /**
     * @param function function to run
     * @param interItemType is it an item that interplays with others
     * @param createDAIfOutputLengthIsOne createDAIfOutputLengthIsOne
     */
    @SuppressWarnings("checkstyle:linelength")
    public QueryOperation(
            final QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> function,
            final boolean interItemType, final List<Predicate<PipeLink<Fibre>>> createDAIfOutputLengthIsOne) {
        this.function = function;
        this.interItemType = interItemType;
        this.createDAIfOutputLengthIsOne = createDAIfOutputLengthIsOne;
        falsePredicateList = new ArrayList<>(1);
        falsePredicateList.add(x -> false);
    }

    /**
     * {@link QueryOperation#interItemType} Indicates whether or not changes in the execution of
     * this operation may trigger updates in other Threads in the Tapestry. The Query executor will
     * red this property to force that update
     *
     * @return true if changes in the execution of this operation will trigger updates to other
     *         Threads
     */
    public boolean isInterItemType() {
        return interItemType;
    }

    /**
     * @return Function to be executed by the query manager
     */
    @SuppressWarnings("checkstyle:linelength")
    public QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> getFunction() {
        return function;
    }

    /**
     * Tell the query manager how to process the resulting PipeLink of an operation in case it
     * contains a single Entry. The QM would normally create DAs only if the output PipeLink has
     * more than a single Entry. If DAs are needed when the number of entries is one then the exact
     * conditions should be stored on the output pipelink for runtime evaluation by the QM
     *
     * @return List of Predicates to be applied to the PipeLink output
     */
    public List<Predicate<PipeLink<Fibre>>> getOneLengthOutputPredicate() {
        if (createDAIfOutputLengthIsOne == null) {
            return falsePredicateList;
        } else {
            return createDAIfOutputLengthIsOne;
        }
    }

}
