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

import java.io.EOFException;
import java.security.InvalidParameterException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hp.hpl.loom.api.exceptions.ApiThrottlingException;
import com.hp.hpl.loom.api.exceptions.BadRequestException;
import com.hp.hpl.loom.exceptions.AccessExpiredException;
import com.hp.hpl.loom.exceptions.CheckedLoomException;
import com.hp.hpl.loom.exceptions.InvalidActionSpecificationException;
import com.hp.hpl.loom.exceptions.InvalidQueryInputException;
import com.hp.hpl.loom.exceptions.InvalidQueryParametersException;
import com.hp.hpl.loom.exceptions.ItemPropertyNotFound;
import com.hp.hpl.loom.exceptions.LoomException;
import com.hp.hpl.loom.exceptions.NoSuchAggregationException;
import com.hp.hpl.loom.exceptions.NoSuchItemException;
import com.hp.hpl.loom.exceptions.NoSuchItemTypeException;
import com.hp.hpl.loom.exceptions.NoSuchProviderException;
import com.hp.hpl.loom.exceptions.NoSuchQueryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.exceptions.NoSuchTapestryDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchThreadDefinitionException;
import com.hp.hpl.loom.exceptions.NoSuchUserException;
import com.hp.hpl.loom.exceptions.OperationException;
import com.hp.hpl.loom.exceptions.OperationNotSupportedException;
import com.hp.hpl.loom.exceptions.RelationPropertyNotFound;
import com.hp.hpl.loom.exceptions.SessionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDefinitionAlreadyExistsException;
import com.hp.hpl.loom.exceptions.ThreadDeletedByDynAdapterUnload;
import com.hp.hpl.loom.exceptions.UserAlreadyConnectedException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Log LOG = LogFactory.getLog(ApiExceptionHandler.class);

    @Value("${api.rate.limit}")
    private Integer maxReqsPerSecond;

    private ResponseEntity<Object> handleExceptionCommon(final LoomException ex, final HashMap<String, String> errors,
            final HttpStatus status, final WebRequest request, final String summary) {
        ErrorMessage errorMessage = new ErrorMessage(status.toString(), errors);

        if (ex != null) {
            LOG.error(summary);
            errorMessage.setMessage(ex.getMessage());

            if (ex.getCause() != null) {
                errorMessage.setCausedBy(ex.getCause().getMessage());
            }

            errorMessage.setStackTrace(ex.getStackTrace().toString());
        }
        return handleExceptionInternal((Exception) ex, errorMessage, new HttpHeaders(), status, request);
    }

    /*
     * NOT FOUND
     */
    @ExceptionHandler({NoSuchItemException.class, NoSuchProviderException.class,
            NoSuchTapestryDefinitionException.class})
    public ResponseEntity<Object> handleNotFoundException(final LoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.NOT_FOUND, request, ex.getDescription());
    }

    /*
     * CONFLICT
     */
    @ExceptionHandler({SessionAlreadyExistsException.class, RelationPropertyNotFound.class, ItemPropertyNotFound.class,
            ThreadDefinitionAlreadyExistsException.class})
    public ResponseEntity<Object> handleCheckedLoomException(final CheckedLoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.CONFLICT, request, ex.getMessage());
    }

    /*
     * BAD REQUEST
     */
    @ExceptionHandler({BadRequestException.class, InvalidParameterException.class, IllegalArgumentException.class,
            OperationNotSupportedException.class, NoSuchAggregationException.class, InvalidQueryInputException.class,
            InvalidQueryParametersException.class, NoSuchQueryDefinitionException.class, NoSuchItemTypeException.class,
            InvalidActionSpecificationException.class})
    public ResponseEntity<Object> handleBadRequestException(final LoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.BAD_REQUEST, request, ex.getDescription());
    }

    /*
     * TOO MANY
     */
    @ExceptionHandler({ApiThrottlingException.class})
    public ResponseEntity<Object> handleApiThrottlingException(final LoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.TOO_MANY_REQUESTS, request,
                "Too Many Requests. Max supported is: " + maxReqsPerSecond + " reqs/s");
    }

    /*
     * PANIC
     */
    @ExceptionHandler({Exception.class, OperationException.class})
    public ResponseEntity<Object> handleGeneralException(final LoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.INTERNAL_SERVER_ERROR, request,
                "Unexpected exception - cause unknown");
    }

    /*
     * END OF FILE // Note: there is no test for this method
     */
    @ExceptionHandler({EOFException.class})
    public void handleEOFException(final EOFException ex, final WebRequest request) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(ex.toString() + " caused by: " + ex.getCause());
        }
    }

    /*
     * UNAUTHORIZED
     */
    @ExceptionHandler({NoSuchSessionException.class, NoSuchUserException.class, UserAlreadyConnectedException.class})
    public ResponseEntity<Object> handleNoSuchSessionException(final LoomException ex, final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.UNAUTHORIZED, request, ex.getDescription());
    }

    /*
     * GONE
     */
    @ExceptionHandler({NoSuchThreadDefinitionException.class, ThreadDeletedByDynAdapterUnload.class})
    public ResponseEntity<Object> handleNoSuchThreadDefinitionException(final LoomException ex,
            final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.GONE, request, ex.getDescription());
    }

    /*
     * LOCKED
     */
    @ExceptionHandler({AccessExpiredException.class})
    public ResponseEntity<Object> handleSessionExpiredException(final AccessExpiredException ex,
            final WebRequest request) {
        return handleExceptionCommon(ex, null, HttpStatus.LOCKED, request, ex.getMessage());
    }
}
