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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDefinitionAlreadyExistsException;

public class ApiExceptionHandlerTest {
    private static final Log LOG = LogFactory.getLog(ApiExceptionHandlerTest.class);

    private ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    private ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    public void testHandleBadRequestException() {
        LOG.info("testHandleBadRequestException start");
        ResponseEntity<Object> response = handler.handleBadRequestException(new BadRequestException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        LOG.info("testHandleBadRequestException end");
    }


    @Test
    public void testHandleGeneralException() {
        LOG.info("testHandleGeneralException start");
        ResponseEntity<Object> response = handler.handleGeneralException(new CheckedLoomException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
        response = handler.handleGeneralException(new CheckedLoomException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
        LOG.info("testHandleGeneralException end");
    }

    @Test
    public void testHandleNoSuchProviderException() {
        LOG.info("testHandleNoSuchProviderException start");
        ResponseEntity<Object> response =
                handler.handleNotFoundException(new NoSuchProviderException("just a test"), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND));
        LOG.info("testHandleNoSuchProviderException end");
    }

    @Test
    public void testHandleNoSuchSessionException() {
        LOG.info("testHandleNoSuchSessionException start");
        ResponseEntity<Object> response = handler.handleNoSuchSessionException(new NoSuchSessionException(), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        LOG.info("testHandleNoSuchSessionException end");
    }

    @Test
    public void testHandleSessionAlreadyExistException() {
        LOG.info("testHandleSessionAlreadyExistException start");
        ResponseEntity<Object> response =
                handler.handleCheckedLoomException(new SessionAlreadyExistsException(null), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        LOG.info("testHandleSessionAlreadyExistException end");
    }

    @Test
    public void testHandleThreadDefinitionAlreadyExistException() {
        LOG.info("testHandleThreadDefinitionAlreadyExistException start");
        ResponseEntity<Object> response = handler.handleCheckedLoomException(
                new ThreadDefinitionAlreadyExistsException("testTapestryId", "testThreadId"), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.CONFLICT));
        LOG.info("testHandleSessionAlreadyExistException end");
    }

    @Test
    public void testHandleInvalidQueryInputException() {
        LOG.info("testHandleInvalidQueryInputException start");
        ResponseEntity<Object> response =
                handler.handleBadRequestException(new InvalidQueryInputException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        LOG.info("testHandleInvalidQueryInputException end");
    }

    @Test
    public void testHandleNoSuchQueryDefinitionException() {
        LOG.info("testHandleNoSuchQueryDefinitionException start");
        ResponseEntity<Object> response =
                handler.handleBadRequestException(new NoSuchQueryDefinitionException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        LOG.info("testHandleNoSuchQueryDefinitionException end");
    }

    @Test
    public void testHandleNoSuchTapestryDefinitionException() {
        LOG.info("testHandleNoSuchTapestryDefinitionException start");
        ResponseEntity<Object> response =
                handler.handleNotFoundException(new NoSuchTapestryDefinitionException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.NOT_FOUND));
        LOG.info("testHandleNoSuchTapestryDefinitionException end");
    }


    @Test
    public void testHandleNoSuchItemTypeException() {
        LOG.info("testHandleNoSuchItemTypeException start");
        ResponseEntity<Object> response = handler.handleBadRequestException(new NoSuchItemTypeException(""), request);
        assertTrue(response.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        LOG.info("testHandleNoSuchItemTypeException end");
    }
}
