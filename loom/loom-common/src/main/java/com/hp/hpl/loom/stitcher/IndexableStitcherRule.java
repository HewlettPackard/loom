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
package com.hp.hpl.loom.stitcher;

import com.hp.hpl.loom.model.Item;

/**
 * Interface supported by an implementation of a stitch rule that can be indexed by the Stitcher.
 * The intent of indexable rules is to allow identification of a much smaller candidate set of items
 * to be tested for equivalence matching using the criteria defined by the SticherRule. An
 * IndexableStitcherRule causes all items of a specific type to be indexed by both a string key and
 * a string value, with possibly several items sharing the same {key, value}. Both the key and the
 * value are specified by the IndexableStitcherRule, and are used to index items into meaningful
 * buckets. An IndexableStitcherRule serves two purposes.
 * <p>
 * Firstly, it specifies how a source item should be indexed when an update to the source grounded
 * aggregation occurs, using the {key, value} tuple returned for the item by the rule as the index.
 * The purpose of this index is to allow the complementary IndexableStitcherRule in a
 * {@link StitcherRulePair} to identify the candidate set of items when the roles are reversed, i.e.
 * when the source items of the IndexableStitcherRule are used as the destination candidate items of
 * the complementary IndexableStitcherRule of the {@link StitcherRulePair} during an update of the
 * complementary grounded aggregation.
 * <p>
 * Secondly, it specifies the {keyOther, valueOther} index to identify the destination candidate set
 * of items for equivalence matching. The destination candidate set would have been indexed by the
 * complementary IndexableStitcherRule of the StitcherRulePair when the grounded aggregation of the
 * other set of items was updated.
 * <p>
 * The IndexableStitcherRule interface is used to index items by specified key and value and to use
 * this index to identify a candidate set of items for equivalence matching. It is optionally used
 * in combination with StitcherRule. The actual matching of an item against the candidates
 * identified by the index is done by the {@link StitcherRule#matches(Item, Item)} method of the
 * {@link StitcherRule} interface of the implementing class.
 *
 * @param <Src> Class of source items that can be stitched by this rule.
 */
public interface IndexableStitcherRule<Src extends Item> {

    /**
     * Return a unique name for the key within item attribute index, e.g. "node#name". This key will
     * be used to index source items when an update occurs to the grounded aggregation containing
     * the item.
     *
     * @return Unique key name.
     */
    String indexKey();

    /**
     * Return the indexable value of "source" Items, to allow identification of possible candidate
     * items matching with the equivalent destination. Note this may be the actual value of the
     * corresponding attribute of the Item. Alternatively, it may be a processed value, e.g. a fully
     * qualified Host Name may be truncated from "foo.hp.com" to "foo" to identify the candidate
     * set.
     *
     * @param source The source Item.
     * @return The indexable value of specified "source" Items.
     */
    String indexValue(final Src source);

    /**
     * Return the key of the item attribute index associated with destination candidate items.
     *
     * @return The key of the item attribute index associated with destination items.
     */
    String otherIndexKey();

    /**
     * Return the expected value of candidate items attribute index associated with destination
     * candidate items, used for retrieval from the item attribute index.
     *
     * @param source The source Item.
     * @return The expected value of the item attribute index associated with destination items.
     */
    String otherIndexValue(Src source);
}
