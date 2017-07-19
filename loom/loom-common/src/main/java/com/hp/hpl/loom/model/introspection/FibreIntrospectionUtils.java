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
package com.hp.hpl.loom.model.introspection;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessorFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.loom.exceptions.NoSuchSessionException;
import com.hp.hpl.loom.manager.query.OperationContext;
import com.hp.hpl.loom.manager.query.OperationErrorCode;
import com.hp.hpl.loom.manager.query.utils.SupportedStats;
import com.hp.hpl.loom.model.Aggregation;
import com.hp.hpl.loom.model.Fibre;
import com.hp.hpl.loom.model.Item;

/**
 * Utils for introspection of fibres.
 *
 * It might be worth migrating this code to make use of common utils (the bean-utils) {see
 * http://commons.apache.org/proper/commons-beanutils}
 *
 */
public final class FibreIntrospectionUtils {

    private static final Log LOG = LogFactory.getLog(FibreIntrospectionUtils.class);

    private FibreIntrospectionUtils() {}


    /**
     * Value returned for methods in which null values are not allowed.
     */
    public static final String PROPERTY_UNDEFINED = "l.undefined";

    // attributes used internally by loom, which values should not be available upon introspection
    @JsonIgnore
    private static final List<String> RESERVED_WORDS = Arrays.asList("aggregation", "alertCount", "class",
            "dependsOnAggregations", "dependsOnMeAggregations", "dirty", "firstLevel", "grounded",
            "groundedAggregation", "highestAlertDescription", "highestAlertLevel", "indexed", "item", "itemType",
            "iterator", "parents", "memberOf", "connectedRelationships", "topLevel", "topLevelDependsOnMeAggregations");

    // ///////////////////////////////////////////////////////////////////////////////////////////
    // Property introspection
    // ///////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Return a list of named property values for the list of fibres passed as input. Note that this
     * method assumes that all fibres in the input are of the same class. This method is not strict
     * about the validity of the named property, and will return a null value if the property is
     * unknown. It will only throw an exception if an error is encountered or if the named property
     * is reserved.
     *
     * @param propertyName Name of property to return.
     * @param fibres List of fibres to return values for.
     * @param context query context
     * @return List of values. Note that values will be reported as null if they are unknown.
     * @throws InvocationTargetException An introspection error has occurred.
     * @throws IllegalAccessException Specified property cannot be accessed.
     */
    public static List<Object> introspectPropertyForFibres(final String propertyName, final List<Fibre> fibres,
            final OperationContext context) throws IllegalAccessException, InvocationTargetException {
        List<Object> properties = new ArrayList<>(fibres.size());
        if (RESERVED_WORDS.contains(propertyName)) {
            throw new IllegalAccessException(propertyName + " is not a valid property");
        }
        Method getter = null;
        boolean useIntrospection = false;
        boolean decidedAccessMechanism = false;


        Class currentClass = null;
        Class oldClass = null;
        for (Fibre fibre : fibres) {

            Object readValue = null;
            if (!decidedAccessMechanism || !useIntrospection) {
                // Try getEntityProperty mechanism first
                PropertyResult<Object> entityPropertyResult = getEntityProperty(propertyName, fibre, null, context);
                if (entityPropertyResult.isFound()) {
                    readValue = entityPropertyResult.getReadValue();
                    decidedAccessMechanism = true;
                }
            }
            if (!decidedAccessMechanism || useIntrospection) {
                // Try introspection mechanism
                IntrospectionContext iContext = fibre.getIntrospectionContextForProperty(propertyName);
                currentClass = iContext.getObject().getClass();
                if (getter == null) {
                    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(iContext.getObject());
                    if (wrapper.isReadableProperty(iContext.getProperty())) {
                        PropertyDescriptor des = wrapper.getPropertyDescriptor(iContext.getProperty());
                        getter = des.getReadMethod();
                        useIntrospection = true;
                        decidedAccessMechanism = true;
                    }
                }
                if (getter != null) {
                    if (currentClass.equals(oldClass)) {
                        readValue = getter.invoke(iContext.getObject());
                    } else {
                        // Deal with situation in which the list of Fibres comes from two different
                        // grounded aggregations (ref to getter Method is from a different object)
                        // try to get a reference to the current class of fibres
                        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(iContext.getObject());
                        PropertyDescriptor des = wrapper.getPropertyDescriptor(iContext.getProperty());
                        getter = des.getReadMethod();
                        readValue = getter.invoke(iContext.getObject());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Getter " + getter.getName() + " for Fibre: " + currentClass.getCanonicalName()
                                    + " replaced with new getter in mapped aggregation");
                        }
                    }
                }
            }
            properties.add(readValue);
            oldClass = currentClass;
        }
        return properties;
    }



    /**
     * Return a named property value for the fibre. This method is not strict about the validity of
     * the named property, and will return a null value if the property is unknown.
     *
     * @param <T> Type of the returned property.
     * @param propertyName Name of property to return.
     * @param fibre fibre to introspect
     * @param context query context
     * @return Value of property. Note that the value will be reported as null if it is unknown.
     */
    @JsonIgnore
    public static <T> T introspectProperty(final String propertyName, final Fibre fibre,
            final OperationContext context) {
        try {
            return introspectPropertyStrict(propertyName, fibre, context);
        } catch (InvocationTargetException | InvalidPropertyException | IllegalAccessException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find property " + propertyName);
            }
            return null;
        }
    }

    /**
     * Return String representations of the values of all of the properties of the fibre, known from
     * Java introspection. Note that ConnectedTo relationships are not included in the returned set
     * of properties.
     *
     * @param errors Map of error codes and error messages if a problem is encountered.
     * @param fibre fibre to introspect
     * @param context query context
     * @return String representations of the values of all of the known properties of the fibre,
     *         known from Java introspection.
     */
    @JsonIgnore
    public static Map<String, Object> introspectProperties(final Map<OperationErrorCode, String> errors,
            final Fibre fibre, final OperationContext context) {

        IntrospectionContext iContext = fibre.getIntrospectionContextForAllProperties();
        Map<String, Object> values = new HashMap<>();
        for (Object object : iContext.getAllPropertyObjects()) {
            values.putAll(introspectPropertiesOfObject(errors, object));
        }
        return values;
    }

    /**
     * Return String representations of the values of all of the properties of the object.
     *
     * @param errors Map of error codes and error messages if a problem is encountered.
     * @param object object to introspect
     * @return String representations of the values of all of the object
     */

    public static Map<String, Object> introspectPropertiesOfObject(final Map<OperationErrorCode, String> errors,
            final Object object) {
        Map<String, Object> values = new HashMap<>();
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
        for (PropertyDescriptor des : wrapper.getPropertyDescriptors()) {
            if (RESERVED_WORDS.contains(des.getName())) {
                continue;
            }
            try {
                Method readMethod = des.getReadMethod();
                if (readMethod != null) {
                    Object readObject = readMethod.invoke(object);
                    if (readObject != null) {
                        readObject.toString();
                        values.put(des.getName(), readObject);
                    } else {
                        errors.put(OperationErrorCode.ReadNullObject, readMethod.toString());
                        continue;
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                errors.put(OperationErrorCode.NotReadableField, e.toString());
            }
        }
        return values;
    }

    /**
     * Return the value of the specified property.
     *
     * @param <T> Type of the returned property.
     * @param propertyName Name of the property.
     * @param errors Map of error codes and error messages if a problem is encountered.
     * @param fibre fibre to introspect
     * @param context query context
     * @return Value of the specified property.
     */
    @JsonIgnore
    public static <T> T introspectProperty(final String propertyName, final Fibre fibre,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        if (RESERVED_WORDS.contains(propertyName)) {
            errors.put(OperationErrorCode.NotReadableField, propertyName + " is not a valid property");
            return null;
        }
        PropertyResult<T> entityPropertyResult = getEntityProperty(propertyName, fibre, errors, context);
        if (entityPropertyResult.isFound()) {
            return entityPropertyResult.getReadValue();
        } else {
            // Try introspection
            IntrospectionContext iContext = fibre.getIntrospectionContextForProperty(propertyName);
            T readValue = null;
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(iContext.getObject());
            for (PropertyDescriptor des : wrapper.getPropertyDescriptors()) {
                if (des.getName().equalsIgnoreCase(iContext.getProperty())) {
                    try {
                        Method readMethod = des.getReadMethod();
                        if (readMethod != null) {
                            readValue = (T) readMethod.invoke(iContext.getObject());
                        } else {
                            errors.put(OperationErrorCode.NotReadableField, propertyName + " is not readable");
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        errors.put(OperationErrorCode.NotReadableField, e.toString());
                    }
                    break;
                }
            }
            return readValue;
        }
    }

    /**
     * Return the value of the specified property. If the property is null or unknown, the value
     * {@value #PROPERTY_UNDEFINED} is returned
     *
     * @param propertyName Name of the property.
     * @param errors Map of error codes and error messages if a problem is encountered.
     * @param fibre fibre to introspect
     * @param context query context
     * @return Value of the specified property, or {@value #PROPERTY_UNDEFINED} if the property is
     *         null or unknown.
     */
    @JsonIgnore
    public static Object introspectPropertyNoNull(final String propertyName, final Fibre fibre,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Object value = introspectProperty(propertyName, fibre, errors, context);
        return value == null ? PROPERTY_UNDEFINED : value;
    }

    /**
     * Return the value of the specified property, or throw an exception if the name of the property
     * is not valid.
     *
     * @param <T> Type of the returned property.
     * @param propertyName Name of the property.
     * @param fibre fibre to introspect
     * @param context query context
     * @return Value of the named property.
     * @throws InvocationTargetException An introspection error has occurred.
     * @throws IllegalAccessException Specified property cannot be accessed.
     */
    @JsonIgnore
    public static <T> T introspectPropertyStrict(final String propertyName, final Fibre fibre,
            final OperationContext context) throws InvocationTargetException, IllegalAccessException {
        if (RESERVED_WORDS.contains(propertyName)) {
            throw new IllegalAccessException(propertyName + " is not a valid property");
        }
        PropertyResult<T> entityPropertyResult = getEntityProperty(propertyName, fibre, null, context);
        if (entityPropertyResult.isFound()) {
            return entityPropertyResult.getReadValue();
        } else {
            // Try introspection
            IntrospectionContext iContext = fibre.getIntrospectionContextForProperty(propertyName);
            return introspectPropertyOfObjectStrict(iContext.getProperty(), iContext.getObject());
        }
    }

    /**
     * Return String representations of the values of all of the properties of the object. If there
     * is an error it will throw an error.
     *
     * @param <T> Type of the returned property.
     * @param propertyName properyName to look for
     * @param object object to introspect
     * @return Item to return
     * @throws InvocationTargetException thrown by the invoke reflection on the object
     * @throws IllegalAccessException thrown if the property doesn't exist / not reabable
     */
    public static <T> T introspectPropertyOfObjectStrict(final String propertyName, final Object object)
            throws InvocationTargetException, IllegalAccessException {
        T readValue = null;
        BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);
        PropertyDescriptor des = wrapper.getPropertyDescriptor(propertyName);
        Method readMethod = des.getReadMethod();
        if (readMethod != null) {
            readValue = (T) readMethod.invoke(object);
        } else {
            throw new IllegalAccessException(propertyName + " is not a readable property");
        }
        return readValue;
    }

    /**
     * Used to return a a property value, indicating whether the property was actually found.
     */
    protected static final class PropertyResult<T> {
        private boolean found;
        private T readValue;

        public PropertyResult(final boolean found, final T readValue) {
            this.found = found;
            this.readValue = readValue;
        }

        public boolean isFound() {
            return found;
        }

        public T getReadValue() {
            return readValue;
        }
    }

    /**
     * Get the propertyResults for a given propertyName, fibre. Any errors that occur will be set on
     * the errors map.
     *
     * @param <T> Type of the returned property.
     * @param propertyName propertyName to lookup
     * @param fibre object to introspect
     * @param context query context
     * @param errors map to record error in
     * @return return property
     */
    public static <T> PropertyResult<T> getEntityProperty(final String propertyName, final Fibre fibre,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        if (fibre.isAggregation()) {
            return getAggregationProperty(propertyName, fibre, context);
        } else {
            PropertyResult<T> result = getItemProperty(propertyName, fibre, context);
            if (!result.isFound()) {
                if (context != null) {
                    result = pullValueFromEquivalentItems(propertyName, fibre, errors, context);
                }
            }
            return result;
        }
    }


    private static <T> PropertyResult<T> pullValueFromEquivalentItems(final String propertyName, final Fibre fibre,
            final Map<OperationErrorCode, String> errors, final OperationContext context) {
        Collection<Item> equivalentItems = null;
        try {
            equivalentItems = context.getEquivalentItems(((Item) fibre));
        } catch (NoSuchSessionException e) {
            errors.put(OperationErrorCode.NoSuchSession, "Could not find session");
        }

        if (equivalentItems == null || equivalentItems.size() == 0) {
            return new PropertyResult<T>(false, null);
        } else {
            for (Item equivalentItem : equivalentItems) {
                // if (propertyName.equals("core.service")) {
                if (!propertyName.equals("fullyQualifiedName")) {
                    LOG.debug(equivalentItem.getLogicalId() + " is Equ to " + ((Item) fibre).getLogicalId());
                }

                PropertyResult<T> property = getItemPropertyFuzzy(propertyName, equivalentItem, context);
                if (property.isFound()) { // TODO decide how to report several equivalent items
                                          // back. LOOM-1599

                    // if (propertyName.equals("core.service")) {
                    if (!propertyName.equals("fullyQualifiedName")) {
                        LOG.debug(((Item) fibre).getLogicalId() + " Equ2: " + property.getReadValue());
                    }
                    return property;
                } else {

                    // if (propertyName.equals("core.service")) {
                    if (!propertyName.equals("fullyQualifiedName")) {
                        LOG.debug(propertyName + " not found in Equ3 " + equivalentItem.getLogicalId());
                    }
                }
            }

            return new PropertyResult<T>(false, null);
        }
    }

    protected static <T> PropertyResult<T> getItemProperty(final String propertyName, final Fibre fibre,
            final OperationContext context) {

        T mappedValue = (T) ((Item) fibre).getPropertyValueForName(propertyName);
        if (mappedValue != null) {
            return new PropertyResult<T>(true, mappedValue);
        }

        Map<String, Item> relatedItemsMap = ((Item) fibre).getConnectedRelationships().get(propertyName);

        if (relatedItemsMap == null) {
            return new PropertyResult<T>(false, null); // Not a known relationship

        }
        if (relatedItemsMap.isEmpty()) {
            return new PropertyResult<T>(true, null); // No relationships
        }

        T readValue = (relatedItemsMap.size() == 1) ? (T) relatedItemsMap.values().iterator().next() : // return
                                                                                                       // the
                                                                                                       // related
                                                                                                       // Item
                (T) relatedItemsMap.values(); // return the collection of related Items

        return new PropertyResult<T>(true, readValue);
    }

    protected static <T> PropertyResult<T> getItemPropertyFuzzy(final String propertyName, final Fibre fibre,
            final OperationContext context) {
        Map<String, Map<String, Item>> relatedItemsMaps = ((Item) fibre).getConnectedRelationships();

        if (propertyName.contains("service")) {
            LOG.debug(propertyName + " not found in Equ4 " + relatedItemsMaps.keySet());
        }

        String clearedPropName = cleanSplit(propertyName);

        for (String name : relatedItemsMaps.keySet()) {

            String clearedName = cleanSplit(name);

            if (propertyName.contains("service")) {
                LOG.debug(clearedPropName + " not found in Equ5 " + clearedName);
            }

            // could add some Jaccard similarity too
            if (clearedName.equalsIgnoreCase(clearedPropName)) {
                if (relatedItemsMaps.get(name) == null) {
                    return new PropertyResult<T>(false, null); // Not a known relationship

                }
                if (relatedItemsMaps.get(name).isEmpty()) {
                    return new PropertyResult<T>(true, null); // No relationships
                }

                T readValue = (relatedItemsMaps.get(name).size() == 1)
                        ? (T) relatedItemsMaps.get(name).values().iterator().next()
                        : // return
                        // the
                        // related
                        // Item
                        (T) relatedItemsMaps.get(name).values(); // return
                                                                 // the
                                                                 // collection
                                                                 // of
                                                                 // related
                                                                 // Items

                return new PropertyResult<T>(true, readValue);
            }
        }
        return new PropertyResult<T>(false, null);
    }

    private static String cleanSplit(final String propertyName) {
        String clearedPropName = propertyName;
        try {
            if (propertyName.contains(".")) {
                String[] dotSplit = propertyName.split("\\.");
                clearedPropName = dotSplit[1];
            } else {
                if (propertyName.contains(":")) {
                    clearedPropName = propertyName.split(":")[1];
                }
            }
            if (clearedPropName.contains("-")) {
                String[] dashSplit = clearedPropName.split("-");
                clearedPropName = dashSplit[1];
            }
        } catch (ArrayIndexOutOfBoundsException ae) {
            LOG.info("Error processing " + propertyName);
        } finally {
            return clearedPropName;
        }
    }

    private static double calculateSimilarity(final String stringOne, final String stringTwo) {

        Set<Character> one = new HashSet<>();
        Set<Character> two = new HashSet<>();

        for (char c : stringOne.toCharArray()) {
            one.add(c);
        }

        for (char c : stringTwo.toCharArray()) {
            two.add(c);
        }

        Set<Character> intersection = new HashSet<>(one);
        intersection.retainAll(two);

        Set<Character> union = new HashSet<>(one);
        union.addAll(two);

        return (double) intersection.size() / (double) union.size();
    }



    protected static <T> PropertyResult<T> getAggregationProperty(final String propertyName, final Fibre fibre,
            final OperationContext context) {
        List<String> statNames = Arrays.asList(SupportedStats.values()).stream()
                .map(stat -> stat.toString().toLowerCase()).collect(Collectors.toList());


        if (contains(statNames, propertyName.toLowerCase())) {
            Number readValue = ((Aggregation) fibre).getPlottableAggregateStats().get(propertyName);
            if (readValue != null) {
                return new PropertyResult(true, readValue);
            } else {
                readValue = ((Aggregation) fibre).getPlottableAggregateStats().get(propertyName.toLowerCase());
                if (readValue != null) {
                    return new PropertyResult(true, readValue);
                }
            }
        }

        return new PropertyResult<T>(false, null);
    }

    private static boolean contains(final List<String> stats, final String propertyName) {
        for (String stat : stats) {
            if (propertyName.contains(stat)) {
                return true;
            }
        }
        return false;
    }

}
