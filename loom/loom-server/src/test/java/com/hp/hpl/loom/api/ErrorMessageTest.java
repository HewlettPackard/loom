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
package com.hp.hpl.loom.api;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.junit.Test;

public class ErrorMessageTest {

    @Test
    public void testConstructNoArgs() {
        new ErrorMessage();
    }

    @Test
    public void testConstruct() {
        new ErrorMessage("", new HashMap<String, String>());
    }

    @Test
    public void testSetAndGetStatus() {
        ErrorMessage errorMsg = new ErrorMessage();
        String status = "testStatus";
        errorMsg.setStatus(status);
        assertTrue(errorMsg.getStatus().equals(status));
    }

    @Test
    public void testSetAndGetMessage() {
        ErrorMessage errorMsg = new ErrorMessage();
        String message = "testMessage";
        errorMsg.setMessage(message);
        assertTrue(errorMsg.getMessage().equals(message));
    }

    @Test
    public void testSetAndGetCausedBy() {
        ErrorMessage errorMsg = new ErrorMessage();
        String causedBy = "testCausedBy";
        errorMsg.setCausedBy(causedBy);
        assertTrue(errorMsg.getCausedBy().equals(causedBy));
    }

    @Test
    public void testSetAndGetStackTrace() {
        ErrorMessage errorMsg = new ErrorMessage();
        String stackTrace = "testStackTrace";
        errorMsg.setStackTrace(stackTrace);
        assertTrue(errorMsg.getStackTrace().equals(stackTrace));
    }

    @Test
    public void testSetAndGetErrors() {
        ErrorMessage errorMsg = new ErrorMessage();
        HashMap<String, String> errors = new HashMap<String, String>();
        String testKey = "testKey";
        String testValue = "testValue";
        errors.put(testKey, testValue);
        errorMsg.setErrors(errors);
        assertTrue(errorMsg.getErrors().get(testKey).equals(testValue));
    }

    @Test
    public void testToString() {
        ErrorMessage errorMsg = new ErrorMessage();

        String message = "testMessage";
        String status = "testStatus";
        String causedBy = "testCausedBy";
        String stackTrace = "testStackTrace";
        HashMap<String, String> errors = new HashMap<String, String>();
        errors.put("testKey", "testVal");
        errors.put("testKey2", "testVal2");

        errorMsg.setMessage(message);
        errorMsg.setStatus(status);
        errorMsg.setCausedBy(causedBy);
        errorMsg.setStackTrace(stackTrace);
        errorMsg.setErrors(errors);

        String errorsStr = "";
        Iterator<Entry<String, String>> it = errors.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> pairs = it.next();
            errorsStr += (pairs.getKey() + "=" + pairs.getValue() + ", ");
        }

        if (errorsStr.length() > 0) {
            errorsStr = errorsStr.substring(0, errorsStr.length() - 2);
        }

        String expectedString = "ErrorMessage{message='" + message + "', status='" + status + "', causedBy='" + causedBy
                + "', stackTrace='" + stackTrace + "', errors={" + errorsStr + "}}";

        assertTrue(errorMsg.toString().equals(expectedString));
    }
}
