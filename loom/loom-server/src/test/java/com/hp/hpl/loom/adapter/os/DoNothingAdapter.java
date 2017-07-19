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
package com.hp.hpl.loom.adapter.os;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.hp.hpl.loom.adapter.Adapter;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;
import com.hp.hpl.loom.manager.adapter.AdapterConfig;
import com.hp.hpl.loom.manager.adapter.AdapterManager;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.PipeLink;
import com.hp.hpl.loom.manager.query.QuadFunction;
import com.hp.hpl.loom.manager.query.QuadFunctionMeta;
import com.hp.hpl.loom.model.Action;
import com.hp.hpl.loom.model.ActionResult;
import com.hp.hpl.loom.model.Credentials;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;
import com.hp.hpl.loom.model.ItemType;
import com.hp.hpl.loom.model.Provider;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.stitcher.StitcherRulePair;
import com.hp.hpl.loom.tapestry.PatternDefinition;

public class DoNothingAdapter implements Adapter {

    private Provider provider;

    private AdapterManager adapterManager;
    private ItemType instanceType;
    private Session session;
    private Collection<Fibre> newItems;
    private String instanceAggLogicalId;
    private PatternDefinition patternDef;

    public DoNothingAdapter(final Provider provider) {
        this.provider = provider;
        instanceAggLogicalId = "/os/donothing/instances";
    }

    @Override
    public void onLoad() {}

    @Override
    public Collection<Class> getAnnotatedItemsClasses() {
        Collection<Class> types = new ArrayList<Class>();
        return types;
    }

    @Override
    public void onUnload() {
        if (adapterManager != null) {
            try {
                adapterManager.deregisterAdapter(this, new HashSet<>(0));
            } catch (NoSuchProviderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void activate() {}


    @Override
    public Provider getProvider() {
        return provider;
    }

    @Override
    public void userConnected(final Session session, final Credentials creds) throws UserAlreadyConnectedException {
        this.session = session;
        activate();
    }

    @Override
    public void userDisconnected(final Session session, final Credentials creds) {
        // TODO Auto-generated method stub
    }

    @Override
    public ActionResult doAction(final Session session, final Action action, final Collection<Item> items) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ActionResult doAction(final Session session, final Action action, final List<String> items) {
        // TODO Auto-generated method stub
        return null;
    }

    // ////////////// specific to FakeAdapter
    public Collection<Fibre> getNewItems() {
        return newItems;
    }

    public Session getSession() {
        return session;
    }

    public String getAggregationLogicalId() {
        return instanceAggLogicalId;
    }

    public PatternDefinition getPatternDefinition() {
        return patternDef;
    }

    public ItemType getInstanceType() {
        return instanceType;
    }

    @Override
    public Map<String, QuadFunctionMeta> registerQueryOperations(
            final Map<String, QuadFunction<PipeLink<Fibre>, Map<String, Object>, Map<OperationErrorCode, String>, OperationContext, PipeLink<Fibre>>> allFunctions) {

        return new HashMap<>(0);
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterManager, final PropertiesConfiguration adapterConfig) {
        this.adapterManager = adapterManager;
    }

    //
    // @Override
    // public void setAdapterProperties() {
    //
    // }

    @Override
    public Collection<StitcherRulePair<?, ?>> getStitchingRules() {
        return new ArrayList<StitcherRulePair<?, ?>>();
    }

    @Override
    public Collection<ItemType> getItemTypes() {
        return new ArrayList<ItemType>();
    }

    @Override
    public Collection<PatternDefinition> getPatternDefinitions() {
        return new ArrayList<PatternDefinition>();
    }

    @Override
    public Set<Session> getSessions() {
        return new HashSet<Session>();
    }

    @Override
    public AdapterConfig getAdapterConfig() {
        return null;
    }

    @Override
    public ActionResult doAction(Session session, Action action, String itemTypeId) throws NoSuchSessionException {
        return null;
    }
}
