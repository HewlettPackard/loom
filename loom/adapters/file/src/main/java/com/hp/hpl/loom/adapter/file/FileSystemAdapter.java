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
package com.hp.hpl.loom.adapter.file;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.manager.query.utils.StatUtils;
import com.hp.hpl.loom.manager.query.utils.SupportedStats;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;

/**
 * The main implementation class for the FileSystemAdapter - it extends the BaseAdatper to provide
 * data on the file system.
 *
 * Types are registered via the getItemTypes and getAnnotatedItemsClasses.
 *
 */
public class FileSystemAdapter extends BaseAdapter {

    /**
     * Example provider provided operation.
     */
    public static final String EXTRACT_OP = "EXTRACT_OP";

    /**
     * Test pattern for the FileItem type.
     */
    public static final String TESTING_PATTERN = "File Pattern";

    /**
     * Test pattern for the DirItem type.
     */
    public static final String TESTING_PATTERN2 = "Directory Pattern";

    @Override
    public Collection<ItemType> getItemTypes() {
        Collection<ItemType> types = new ArrayList<ItemType>();
        return types;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        types.add(FileItem.class);
        types.add(DirItem.class);
        return types;
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        List<ItemType> itemTypes = getItemTypesFromLocalIds(Arrays.asList(Types.FILE_TYPE_LOCAL_ID));
        PatternDefinition patternDef = createPatternDefinitionWithSingleInputPerThread(TESTING_PATTERN, itemTypes,
                TESTING_PATTERN, null, true, null);

        List<ItemType> itemTypes2 = getItemTypesFromLocalIds(Arrays.asList(Types.DIR_TYPE_LOCAL_ID));
        PatternDefinition patternDef2 = createPatternDefinitionWithSingleInputPerThread(TESTING_PATTERN2, itemTypes2,
                TESTING_PATTERN2, null, true, null);

        Collection<PatternDefinition> list = new ArrayList<PatternDefinition>();
        list.add(patternDef);
        list.add(patternDef2);
        return list;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new FileSystemCollector(session, this, adapterManager);
    }

    @SuppressWarnings("checkstyle:linelength")
    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new com.hp.hpl.loom.model.ProviderImpl(providerType, providerId, authEndpoint, providerName,
                this.getClass().getPackage().getName().toString());
    }

    @Override
    @SuppressWarnings("checkstyle:linelength")
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        // We declare the new operation as a lambda to be stored by Loom's OperationManager, so that
        // our weavers can use it at any time in the future
        // The arguments to this lambda will give us all the context we need so that Loom knows what
        // ItemType, provider, etc. it has to deal with
        // when storing the operation
        QueryOperation extractValue = new QueryOperation((inputs, params, errors, context) -> {

            // conver map of inputs to a list containing the items on the first
            // entry only
            List<Fibre> input = LoomQueryUtils.getFirstInput(inputs, errors);

            // From all the files, we need to know what the maximum size
            // and length are
            // For that, we need to get stats on the attributes of the
            // files
            // Loom offers a pre-defined utility for adapter writers to
            // do exactly that
            Map<String, Number> statMap;
            try {
                statMap = StatUtils.getStatMap(context.getType(), input, context);
            } catch (Exception e) {
                errors.put(OperationErrorCode.NotReadableField, "attempted to fetch unaccessible property data.");
                return new PipeLink<Fibre>(0, new ArrayList<>(0));
            }

            // the keys of this map are the name of the attributes in
            // the ItemType
            // followed by:
            // _avg, _count, _max, _min, _sum, _geoMean, _sumSq
            // _std, _var, _skew, _kurt, _median, _mode
            // From the list of stats, we now need to fetch the ones we
            // are looking for:
            // size_max
            final float maxSize =
                    statMap.get(SeparableItem.CORE_NAME + "size" + SupportedStats.MAX.toString()).longValue() * 1F; // size_max

            // for each item in our input, we now need to update the
            // value variable
            FileItem fileItem;
            List<Fibre> output = new ArrayList<>(input.size());
            // LOG.info("# of files is "+input.size());
            float value;
            for (Fibre file : input) {
                fileItem = (FileItem) file;
                value = (fileItem.getCore().getSize() / maxSize);
                fileItem.getCore().setValue(value);
                output.add(fileItem);
            }

            // return list of items with updated values in a format that
            // Loom can
            // understand
            return new PipeLink<Fibre>(0, output);
        }, false);

        // we declare a map to contain a key with the name of the new operation and the actual
        // operation itself
        // in this example we will declare just one operation
        Map<String, QuadFunctionMeta> ops = new HashMap<>(1);

        // note that the name of the operation (key of the map) will be automatically "nameschemed"
        // by Loom (i.e. preceeded by the provider type and id to avoid name clashes)
        // prov.getProviderTypeAndId() + Provider.prov_separator + opId and it will be converted to
        // lower case (OP names ade caes INSENSITIVE).
        QuadFunctionMeta functionMeta = new QuadFunctionMeta(FileSystemAdapter.EXTRACT_OP, extractValue, false, false);
        ops.put(FileSystemAdapter.EXTRACT_OP, functionMeta);

        return ops;
    }

}
