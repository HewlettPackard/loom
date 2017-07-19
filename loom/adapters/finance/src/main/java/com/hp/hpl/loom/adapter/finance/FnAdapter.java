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
package com.hp.hpl.loom.adapter.finance;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.loom.adapter.BaseAdapter;
import com.hp.hpl.loom.adapter.ItemCollector;
import com.hp.hpl.loom.adapter.finance.items.FnMarket;
import com.hp.hpl.loom.adapter.finance.items.FnQuote;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.ProviderImpl;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.tapestry.PatternDefinition;

public class FnAdapter extends BaseAdapter {
    // private static final Log log = LogFactory.getLog(FnAdapter.class);

    private static final String PATTERN_ALL = "patternAll";

    // ------------------------------------------------------------------ //
    // PRIVATE FIELDS //
    // ------------------------------------------------------------------ //

    protected Collection<ItemType> itemTypes;
    protected Collection<PatternDefinition> patterns;
    protected String[] symbols;
    protected Proxy proxy = null;

    // ------------------------------------------------------------------ //
    // PUBLIC INTERFACE //
    // ------------------------------------------------------------------ //

    public String[] getSymbols() {
        if (symbols == null) {
            symbols = adapterConfig.getPropertiesConfiguration().getStringArray("symbols");
        }
        return symbols;
    }

    public Proxy getProxy() {
        if (proxy == null) {
            String proxyHost = adapterConfig.getPropertiesConfiguration().getString("proxyHost");
            Integer proxyPort = adapterConfig.getPropertiesConfiguration().getInt("proxyPort");
            if (!StringUtils.isBlank(proxyHost) && proxyPort != null) {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            }
        }

        return proxy;
    }

    @Override
    protected ItemCollector getNewItemCollectorInstance(final Session session, final Credentials creds) {
        return new FnItemCollector(session, this, adapterManager, getProxy());
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        if (patterns == null) {
            patterns = this.createPatterns().values();
        }
        return patterns;
    }

    protected Map<String, PatternDefinition> createPatterns() {
        Map<String, PatternDefinition> newMap = new HashMap<>(1);
        newMap.put(FnAdapter.PATTERN_ALL, this.createAllPattern());
        return newMap;
    }

    private PatternDefinition createAllPattern() {
        List<ItemType> itemTypes =
                this.getItemTypesFromLocalIds(Arrays.asList(FnQuote.TYPE_LOCAL_ID, FnMarket.TYPE_LOCAL_ID));
        return this.createPatternDefinitionWithSingleInputPerThread(FnAdapter.PATTERN_ALL, itemTypes, "Global Pattern",
                null, true, null);
    }

    @Override
    protected ProviderImpl createProvider(final String providerType, final String providerId, final String authEndpoint,
            final String providerName) {
        return new FnProviderImpl(providerType, providerId, authEndpoint, providerName,
                this.getClass().getPackage().getName());
    }

    @Override
    protected String createHumanReadableThreadName(final ItemType type) {
        if (type.getLocalId().equals(FnQuote.TYPE_LOCAL_ID)) {
            return "Quotes";
        }
        if (type.getLocalId().equals(FnMarket.TYPE_LOCAL_ID)) {
            return "Markets";
        }
        // Otherwise returns a bad looking string that doesn't look like a
        // human readable one -> people will likely notice it.
        return "={#ERROR!@/+[com.reflect.the.void.invoke(/@0xDEADBEEF1984)";
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> map) {
        // TODO Auto-generated method stub
        return new HashMap<String, QuadFunctionMeta>();
    }

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        // TODO Auto-generated method stub
        return Arrays.asList(FnQuote.class, FnMarket.class);
    }
}
