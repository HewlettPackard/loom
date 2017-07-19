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
package com.hp.hpl.loom.openstack.keystonev3;

import com.hp.hpl.loom.openstack.keystonev3.model.Auth;
import com.hp.hpl.loom.openstack.keystonev3.model.Domain;
import com.hp.hpl.loom.openstack.keystonev3.model.Identity;
import com.hp.hpl.loom.openstack.keystonev3.model.JsonAuth;
import com.hp.hpl.loom.openstack.keystonev3.model.Password;
import com.hp.hpl.loom.openstack.keystonev3.model.Project;
import com.hp.hpl.loom.openstack.keystonev3.model.Scope;
import com.hp.hpl.loom.openstack.keystonev3.model.Token;
import com.hp.hpl.loom.openstack.keystonev3.model.User;

/**
 * Helper class to create the JsonAuth's.
 */
public final class KeystoneUtils {

    private KeystoneUtils() {}

    /**
     * Create an unscoped JsonAuth.
     *
     * @param username username for the JsonAuth
     * @param password password for the JsonAuth
     * @return the json auth
     */
    public static JsonAuth getUnscopedAuth(final String username, final String password) {
        Domain domain = new Domain();
        User user = new User();
        user.setDomain(domain);
        user.setName(username);
        user.setPassword(password);
        Password passwordObj = new Password();
        passwordObj.setUser(user);
        Identity identity = new Identity();
        identity.setMethodsToPassword();
        identity.setPassword(passwordObj);
        Auth auth = new Auth();
        auth.setIdentity(identity);
        return new JsonAuth(auth);
    }

    /**
     * Create an scoped JsonAuth.
     *
     * @param tokenStr tokenStr string to build auth from
     * @param projectId projectId for the tokenStr
     * @return the json auth
     */
    public static JsonAuth getScopedAuth(final String tokenStr, final String projectId) {
        Identity identity = new Identity();
        identity.setMethodsToToken();
        Token token = new Token();
        token.setId(tokenStr);
        identity.setToken(token);
        Auth auth = new Auth();
        auth.setIdentity(identity);
        Scope scope = new Scope();
        Project project = new Project();
        project.setId(projectId);
        scope.setProject(project);
        auth.setScope(scope);
        return new JsonAuth(auth);
    }

}
