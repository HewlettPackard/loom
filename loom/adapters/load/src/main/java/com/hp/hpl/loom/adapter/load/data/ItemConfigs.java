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
package com.hp.hpl.loom.adapter.load.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

public class ItemConfigs {

    private List<String> users;
    private ItemConfig[] config;

    public static ItemConfigs[] build(String filename) throws FileNotFoundException {
        final ItemConfigs[] loadedConfigSettigs = load(new FileInputStream(new File(filename)), ItemConfigs[].class);
        return loadedConfigSettigs;
    }

    public static <T> T load(final InputStream inputStream, final Class<T> clazz) {
        if (inputStream != null) {
            final Gson gson = new Gson();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return gson.fromJson(reader, clazz);
        }

        return null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        ItemConfigs[] loadedConfigSettigs = ItemConfigs.build("./config.json");
        for (ItemConfigs itemConfigs : loadedConfigSettigs) {
            for (String username : itemConfigs.getUsers()) {
                System.out.println("Username: " + username);
            }
            for (ItemConfig itemConfig : itemConfigs.getConfig()) {
                System.out.println(itemConfig.getName() + " " + itemConfig.getCount());
                for (Relationship relationship : itemConfig.getRelationships()) {
                    System.out.println(relationship.getName());
                    for (Percent percents : relationship.getPercent()) {
                        System.out.println(percents.getVertex() + " / " + percents.getPercentage());
                    }
                }
            }
        }
    }

    /**
     * @return the users
     */
    public List<String> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<String> users) {
        this.users = users;
    }

    /**
     * @return the config
     */
    public ItemConfig[] getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(ItemConfig[] config) {
        this.config = config;
    }

}
