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
package com.hp.hpl.loom.adapter.os.fake;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.os.BaseOsAdapter;
import com.hp.hpl.loom.adapter.os.OsInstance;
import com.hp.hpl.loom.adapter.os.OsInstanceType;
import com.hp.hpl.loom.manager.query.DefaultOperations;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.manager.query.QueryOperation;
import com.hp.hpl.loom.manager.query.utils.LoomQueryUtils;
import com.hp.hpl.loom.manager.query.utils.StatUtils;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.SeparableItem;
import com.hp.hpl.loom.model.Session;

public class FakeAdapter extends BaseOsAdapter {
    private static final Log LOG = LogFactory.getLog(FakeAdapter.class);

    protected FakeConfig fc;

    public final static String NORMALISE_RAM = "normaliseRam";

    // public FakeAdapter(final String providerType, final String providerId, final String
    // authEndpoint,
    // final String providerName) {
    // super(providerType, providerId, authEndpoint, providerName);
    // }

    public FakeConfig getConfig() {
        if (fc == null) {
            fc = new FakeConfig();
            fc.loadFromProperties(adapterConfig.getPropertiesConfiguration());
        }

        return fc;
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        return types;
    }

    @Override
    protected Provider createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new FakeProviderImpl(providerType, providerId, authEndpoint, providerName, "test",
                this.getClass().getPackage().getName());
    }

    // own methods
    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new FakeItemCollector(session, this, adapterManager, creds, provider.getAuthEndpoint(), getConfig());
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions) {

        Map<String, QuadFunctionMeta> ops = new HashMap<>(1);
        QueryOperation normaliseRam = new QueryOperation((inputs, params, errors, context) -> {
            // PACK TO GET SUMMARY STATS
            QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>> pack =
                    allFunctions.get(DefaultOperations.SUMMARY.toString());

            Aggregation agg = null;
            try {
                agg = StatUtils.getAggregateStats(context.getType(),
                        LoomQueryUtils.getFirstInput(pack.apply(inputs, new HashMap<>(0), errors, null), errors),
                        context);

            } catch (Exception e) {
                LOG.error("attempted to fetch unaccessible property data.");
                errors.put(OperationErrorCode.NotReadableField, "attempted to fetch unaccessible property data.");
                return new PipeLink<Fibre>(0, new ArrayList<>(0));
            }



            if (agg != null) {
                LOG.info(agg.getPlottableAggregateStats());
                // UPDATE FIELD IN RESULTING ITEMS ("normalise" RAM
                // value
                // using its average)

                OsInstance inst;
                for (Fibre item : agg.getContainedItems()) {
                    if (context.getType().toString().contains(OsInstanceType.TYPE_LOCAL_ID)) {
                        inst = (OsInstance) item;
                        inst.getCore().setRam(inst.getCore().getRam() / agg.getPlottableAggregateStats()
                                .get(SeparableItem.CORE_NAME + "ram_avg").doubleValue()); // normalise
                                                                                          // RAM

                    }
                }
            }
            return new PipeLink<Fibre>(0, agg.getElements());
        }, false);

        QuadFunctionMeta functionMeta = new QuadFunctionMeta("Normalise RAM", normaliseRam, false, false);
        ops.put(NORMALISE_RAM, functionMeta);

        return ops;
    }
}
