/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: paulo.abrantes@qub-it.com
 *
 * 
 * This file is part of FenixEdu bennu-versioning-core.
 *
 * FenixEdu bennu-versioning-runtime is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu bennu-versioning-runtime is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu bennu-versioning-runtime.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.solution.fenixedu.bennu.versioning.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.repository.SQLTableChangeSet;
import pt.ist.fenixframework.backend.jvstmojb.repository.SQLTableInfo;
import pt.ist.fenixframework.backend.jvstmojb.repository.ToSqlConverter;
import pt.ist.fenixframework.dml.ExternalizationElement;
import pt.ist.fenixframework.dml.ValueType;

class VersioningHandler {

    private static final String VERSIONING_UPDATE_DATE = "versioningUpdateDate";
    private static final String VERSIONING_UPDATED_BY = "versioningUpdatedBy";

    private static final Logger logger = LoggerFactory.getLogger(VersioningHandler.class);

    public static enum LogOperation {
        CREATE, UPDATE, DELETE;
    }

    private static String DEFAULT_CHARSET;

    private static Map<ValueType, Method> valueTypeSerializationMethodCache;

    static {
        Map<String, Method> toSqlConverterMap = new HashMap<String, Method>();
        for (Method method : ToSqlConverter.class.getDeclaredMethods()) {
            toSqlConverterMap.put(method.getName(), method);
        }

        valueTypeSerializationMethodCache = new HashMap<ValueType, Method>();
        for (ValueType valueType : FenixFramework.getDomainModel().getAllValueTypes()) {
            Method m = null;
            try {
                if (valueType.isBuiltin()) {
                    if (valueType.isEnum()) {
                        m = ToSqlConverter.class.getMethod("getValueForEnum", new Class[] { Enum.class });
                    } else {
                        m = toSqlConverterMap.get("getValueFor" + valueType.getDomainName());
                    }

                } else {
                    Class<?> valueTypeClass = null;
                    String valueTypeFullName = valueType.getFullname();
                    try {
                        valueTypeClass = Class.forName(valueTypeFullName);
                    } catch (Throwable t) {
                        // Might be an inner class let's give it a try
                        int lastIndexOf = valueTypeFullName.lastIndexOf('.');
                        valueTypeClass = Class.forName(
                                valueTypeFullName.substring(0, lastIndexOf) + "$" + valueTypeFullName.substring(lastIndexOf + 1));
                    }
                    List<ExternalizationElement> externalizationElements = valueType.getExternalizationElements();
                    String externalizeMethod = externalizationElements.get(0).getMethodName();
                    int lastIndexOfDot = externalizeMethod.lastIndexOf('.');
                    if (lastIndexOfDot > 0) {
                        String classname = externalizeMethod.substring(0, lastIndexOfDot);
                        String methodName = externalizeMethod.substring(lastIndexOfDot + 1);
                        Class<?> externalizer = Class.forName(classname);
                        m = findMethod(externalizer, methodName, valueTypeClass);
                    } else {
                        // In this case it's only the method name so it's from
                        // the object class and it's an instance method
                        m = valueTypeClass.getMethod(externalizeMethod, new Class[] {});
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            valueTypeSerializationMethodCache.put(valueType, m);
        }

        DEFAULT_CHARSET = getSqlcharset(Charset.defaultCharset().name());
    }

    private static String getSqlcharset(String javaCharset) {
        if (javaCharset.equals("UTF-8")) {
            return "utf8";
        } else if (javaCharset.toLowerCase().contains("iso") && javaCharset.contains("8859")) {
            return "latin1";
        } else {
            return "utf8";
        }
    }

    protected static void startLog(Connection connection) throws SQLException {
        executeQuery(connection, "START TRANSACTION;");
    }

    private static Method findMethod(Class<?> externalizer, String methodName, Class<?> valueTypeClass) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : externalizer.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 1) {
                methods.add(method);
            }
        }

        if (methods.size() == 0) {
            return null;
        }
        if (methods.size() == 1) {
            return methods.get(0);
        }

        System.out.println("More than one method found: must implement the type restriction algorithm!");
        return methods.get(0);
    }

    protected static void endLog(Connection connection, boolean success) throws SQLException {
        String commandToExecute = success ? "COMMIT" : "ROLLBACK";
        executeQuery(connection, commandToExecute);
    }

    public static void logCreate(Connection connection, int txNumber, VersionableObject domainObject) {
        log(connection, txNumber, domainObject, LogOperation.CREATE);
    }

    public static void logEdit(Connection connection, int txNumber, VersionableObject domainObject) {
        log(connection, txNumber, domainObject, LogOperation.UPDATE);
    }

    public static void logDelete(Connection connection, int txNumber, VersionableObject domainObject) {
        log(connection, txNumber, domainObject, LogOperation.DELETE);
    }

    private static Map<String, ClassDescriptor> getDescriptorTable() {
        DescriptorRepository descriptorRepository = MetadataManager.getInstance().getGlobalRepository();
        Map<String, ClassDescriptor> descriptorTable = descriptorRepository.getDescriptorTable();
        return descriptorTable;
    }

    public static void log(Connection connection, int txNumber, VersionableObject domainObject, LogOperation operation) {
        Map<String, Object> valueMap = domainObject.getVersionInfo();

        // let's check if update entity and update timestamp are available
        // if not let's add them, so even the delete contains this information
        //
        // 24 February 2018 - Paulo Abrantes

        if (!valueMap.containsKey(VERSIONING_UPDATED_BY)) {
            valueMap.put(VERSIONING_UPDATED_BY, new com.qubit.solution.fenixedu.bennu.versioning.domain.UpdateEntity());
        }
        if (!valueMap.containsKey(VERSIONING_UPDATE_DATE)) {
            valueMap.put(VERSIONING_UPDATE_DATE, new com.qubit.solution.fenixedu.bennu.versioning.domain.UpdateTimestamp());
        }

        ClassDescriptor descriptor = getDescriptorTable().get(domainObject.getClass().getName());
        try {
            process(connection, txNumber, domainObject.getExternalId(), operation, descriptor, valueMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void process(Connection connection, int txNumber, String externalId, LogOperation operation,
            ClassDescriptor descriptor, Map<String, Object> map) throws SQLException {

        if (connection == null) {
            logger.error("Connection is null! Not versioning information");
        }
        String versionedTable = "FF$QUB$" + descriptor.getFullTableName() + "_VERSION";
        SQLTableInfo sqlTableInfo = new SQLTableInfo(versionedTable, connection);
        SQLTableChangeSet sqlTableChangeSet = new SQLTableChangeSet(sqlTableInfo);
        sqlTableChangeSet.addClassDescriptor(descriptor);

        String generateSqlUpdates = sqlTableChangeSet.generateSqlUpdates(false, DEFAULT_CHARSET);
        generateSqlUpdates = generateSqlUpdates.replaceAll("alter table `[^`]*` add primary key \\(OID\\);\n", "");
        generateSqlUpdates = generateSqlUpdates.replaceAll(", primary key \\(OID\\)", "");
        generateSqlUpdates = generateSqlUpdates.replaceAll(", add primary key \\(OID\\)", "");

        if (!StringUtils.isEmpty(generateSqlUpdates)) {
            executeQuery(connection, generateSqlUpdates);
            if (generateSqlUpdates.startsWith("create")) {
                executeQuery(connection, "alter table " + versionedTable + " add column FF$QUB$OPERATION_KIND varchar(255);");
                executeQuery(connection, "alter table " + versionedTable + " add column FF$QUB$TX_NUMBER varchar(255);");
            }
        }

        HashMap<String, String> valuesMap = new HashMap<String, String>();
        for (Entry<String, Object> entry : map.entrySet()) {
            Object object = entry.getValue();
            String field = entry.getKey();
            String fieldName = getColumnName(
                    (object instanceof DomainObject) ? "oid" + field.substring(0, 1).toUpperCase() + field.substring(1) : field);

            String valueAsString = null;
            ValueType otherValueType = getValueType(object);
            if (otherValueType != null) {
                valueAsString = getValueForValueType(object, otherValueType);
            } else if (object instanceof DomainObject) {
                valueAsString = ((DomainObject) object).getExternalId();
            } else {
                valueAsString = object.toString();
            }
            valuesMap.put(fieldName, valueAsString);
        }

        valuesMap.put("OID", externalId);
        valuesMap.put("FF$QUB$TX_NUMBER", String.valueOf(txNumber));
        valuesMap.put("FF$QUB$OPERATION_KIND", operation.toString());

        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();
        Set<Entry<String, String>> entrySet = valuesMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            if (fields.length() > 0) {
                fields.append(",");
                values.append(",");
            }
            fields.append(entry.getKey());
            values.append("?");
        }

        String query = "insert into " + versionedTable + " (" + fields.toString() + ") values (" + values.toString() + ");";
        executePreparedStatement(connection, query, valuesMap.values());
    }

    private static String getValueForValueType(Object object, ValueType valueType) {
        // hack due to true Vs '1' and false Vs '0'
        if (object.getClass().equals(boolean.class) || object.getClass().equals(Boolean.class)) {
            return Boolean.TRUE.equals(object) ? "1" : "0";
        }

        Method serialization = VersioningHandler.valueTypeSerializationMethodCache.get(valueType);
        if (serialization != null) {
            Object otherObject = null;
            try {
                if (Modifier.isStatic(serialization.getModifiers())) {
                    otherObject = serialization.invoke(null, new Object[] { object });
                } else {
                    otherObject = serialization.invoke(object, new Object[] {});
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            if (otherObject == null) {
                return StringUtils.EMPTY;
            }

            if (otherObject.getClass().equals(object.getClass())) {
                // the converter is identity, hence we'll return
                // the .toString();
                return otherObject.toString();
            }

            ValueType otherValueType = getValueType(otherObject);
            if (otherValueType != null) {
                return getValueForValueType(otherObject, otherValueType);
            } else {
                return otherObject.toString();
            }
        } else {
            return "no method found";
        }
    }

    private static ValueType getValueType(Object object) {
        ValueType findValueType = FenixFramework.getDomainModel().findValueType(object.getClass().getSimpleName());
        // findValueType received a simple class name. But there may be valuetypes with the same simple name
        // as a domain object (or any other object type). So before we return the valueType we check if 
        // the full name matches the classes FQDN. If it does not match then it's a case where we found a 
        // value type  with the same simple name as the class we are checking but it's not, in fact, the 
        // same class.
        // 
        // 10 August 2018 - Paulo Abrantes
        if (findValueType != null && object.getClass().getName().equals(findValueType.getFullname())) {
            return findValueType;
        } else {
            return null;
        }

    }

    private static String getColumnName(String name) {
        return name.replaceAll("([A-Z])", "_$1").toUpperCase();
    }

    private static void executePreparedStatement(Connection connection, String baseSQL, Collection<String> values)
            throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(baseSQL);
            int i = 1;
            for (String value : values) {
                statement.setString(i++, value);
            }
            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private static void executeQuery(Connection connection, String generateSqlUpdates) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(generateSqlUpdates);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }
}
