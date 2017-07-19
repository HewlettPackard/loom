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
package com.hp.hpl.loom.adapter.keystonev3;

import com.hp.hpl.loom.adapter.keystonev3.rest.resources.Auth;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.Domain;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.Identity;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.JsonAuth;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.Password;
import com.hp.hpl.loom.adapter.keystonev3.rest.resources.User;
import com.hp.hpl.loom.model.Credentials;

public class KeystoneUtils {

    public static JsonAuth getUnscopedAuth(final Credentials creds) {
        Domain domain = new Domain();
        User user = new User();
        user.setDomain(domain);
        user.setName(creds.getUsername());
        user.setPassword(creds.getPassword());
        Password password = new Password();
        password.setUser(user);
        Identity identity = new Identity();
        identity.setMethodsToPassword();
        identity.setPassword(password);
        Auth auth = new Auth();
        auth.setIdentity(identity);
        return new JsonAuth(auth);
    }

}
