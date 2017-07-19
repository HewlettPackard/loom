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
package com.hp.hpl.loom.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hp.hpl.loom.api.util.SessionManager;
import com.hp.hpl.loom.model.Session;
import com.hp.hpl.loom.model.SessionImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext-test-full.xml")
public class SessionAlreadyExistsExceptionTest {
    @Autowired
    private SessionManager sessionManager;

    @Test
    public void testSessionAlreadyExistsExceptionTestString() {
        Session session = new SessionImpl("anid", sessionManager.getInterval());
        SessionAlreadyExistsException e = new SessionAlreadyExistsException(session);

        assertEquals(session, e.getSession());
        assertNotNull(e.getMessage());
    }

    @Test
    public void testSessionAlreadyExistsExceptionTestStringThrowable() {
        Session session = new SessionImpl("anid", sessionManager.getInterval());
        String message = "my message";
        SessionAlreadyExistsException e = new SessionAlreadyExistsException(session, new Throwable(message));

        assertEquals(session, e.getSession());
        assertNotNull(e.getMessage());
        assertEquals(message, e.getCause().getMessage());
    }
}
