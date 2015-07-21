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
 * This file is part of FenixEdu bennu-versioning.
 *
 * FenixEdu bennu-versioning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu bennu-versioning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu bennu-versioning.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.qubit.solution.fenixedu.bennu.versioning.service;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.Role;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningTargetConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.util.VersioningConstants;

public class HistoryRetriever {

    public static final String NULL_STRING = "NULL";
    public static final String LABEL = "label.";
    private VersionableObject versionedObject;

    public HistoryRetriever(VersionableObject object) {
        this.versionedObject = object;
        VersioningTargetConfiguration instance = VersioningTargetConfiguration.getInstance();
        if (instance == null) {
            throw new IllegalStateException(
                    "Can only use HistoryRetrieve after configure a versioningTargetConfiguration. Please use the interface to configure it");
        }
    }

    private String getTableForObject(String versionedClass) {
        DomainClass domainClass = FenixFramework.getDomainModel().findClass(versionedClass);
        while (domainClass.getSuperclass() != null) {
            domainClass = FenixFramework.getDomainModel().findClass(domainClass.getSuperclass().getFullName());
        }
        return "FF$QUB$" + domainClass.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase() + "_VERSION";
    }

    public List<Map<String, Object>> retrieveVersionsAllInfo() {
        Connection connection = null;
        Statement statement = null;
        ArrayList<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        String updateDateColumn = LOWER_CAMEL.to(UPPER_UNDERSCORE, VersioningConstants.VERSIONING_UPDATE_DATE);
        try {
            connection = VersioningTargetConfiguration.createConnection();
            statement = connection.createStatement();
            ResultSet executeQuery =
                    statement.executeQuery("SELECT * FROM " + getTableForObject(versionedObject.getClass().getName())
                            + " where OID=" + this.versionedObject.getExternalId() + " order by " + updateDateColumn);
            ResultSetMetaData metaData = executeQuery.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (executeQuery.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                resultSet.add(map);
                for (int i = 1; i <= columnCount; i++) {
                    map.put(metaData.getColumnName(i), executeQuery.getString(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultSet;
    }

    public List<Map<String, Object>> retrieveVersionsOnlyModifications() {
        Connection connection = null;
        Statement statement = null;
        ArrayList<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        Map<String, Object> lastValueMap = new LinkedHashMap<String, Object>();
        String updateDateColumn = LOWER_CAMEL.to(UPPER_UNDERSCORE, VersioningConstants.VERSIONING_UPDATE_DATE);
        String prefixLabel = LABEL + versionedObject.getClass().getSimpleName() + ".";
        try {
            connection = VersioningTargetConfiguration.createConnection();
            statement = connection.createStatement();
            ResultSet executeQuery =
                    statement.executeQuery("SELECT * FROM " + getTableForObject(versionedObject.getClass().getName())
                            + " where OID=" + this.versionedObject.getExternalId() + " order by " + updateDateColumn);
            ResultSetMetaData metaData = executeQuery.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (executeQuery.next()) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                resultSet.add(map);
                for (int i = 1; i <= columnCount; i++) {
                    Object value;
                    if (executeQuery.getString(i) == null) {
                        value = NULL_STRING;
                    } else if (metaData.getColumnName(i).startsWith("OID_")) {
                        value = Long.parseLong(executeQuery.getString(i));
                    } else {
                        value = executeQuery.getString(i);
                    }
                    String key = convertColumnName(metaData.getColumnName(i), prefixLabel);
                    if (lastValueMap.get(key) == null || !lastValueMap.get(key).equals(value)) {
                        lastValueMap.put(key, value);
                        map.put(key, value);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultSet;
    }

    public List<String> retrieveVersionsTransactions() {
        Connection connection = null;
        Statement statement = null;
        List<String> resultSet = new ArrayList<String>();
        try {
            connection = VersioningTargetConfiguration.createConnection();
            statement = connection.createStatement();
            ResultSet executeQuery =
                    statement.executeQuery("SELECT " + VersioningConstants.FF_QUB_TX_NUMBER + " FROM "
                            + getTableForObject(versionedObject.getClass().getName()) + " where OID="
                            + this.versionedObject.getExternalId());
            while (executeQuery.next()) {
                resultSet.add(executeQuery.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultSet;
    }

    public Map<String, Map<String, List<Map<String, Object>>>> retrieveVersionsOfRelatedObjects() {
        Map<String, Map<String, List<Map<String, Object>>>> resultSet =
                new LinkedHashMap<String, Map<String, List<Map<String, Object>>>>();
        List<String> objectVersionsTransactions = retrieveVersionsTransactions();
        for (String transactionId : objectVersionsTransactions) {
            resultSet.put(transactionId, retrieveVersionsOfRelatedObjects(transactionId));
        }
        return resultSet;
    }

    public Map<String, List<Map<String, Object>>> retrieveVersionsOfRelatedObjects(String transactionId) {
        Connection connection = null;
        DomainClass domainClass = FenixFramework.getDomainModel().findClass(versionedObject.getClass().getName());
        // -1 correponds to multiplicity zero or more (*)
        List<Role> allRolesSlotesLists =
                getAllRolesSlotsListInClass(domainClass).stream()
                        .filter(role -> role.getMultiplicityUpper() == -1 || role.getOtherRole().getMultiplicityUpper() == -1)
                        .collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> queryResults = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (Role role : allRolesSlotesLists) {
            String relatedClassName = role.getType().getFullName();
            String relatedRoleName = role.getName();
            String relatedExternalIdColumnName = convertRoleRelationNameToColumn(role.getOtherRole().getName());
            String prefixLabel = LABEL + role.getType().getName() + ".";
            //Relation N to N - allRolesSlotesLists only has roles where at least one side is multiplicity -1
            if (role.getOtherRole().getMultiplicityUpper() == role.getMultiplicityUpper()) {
                //TODOJN - check my role in a N to N relation of the same class
                //TODOJN
                continue;
            }
            //Relation 1 (relatedObject) to N (versionableObject) 
            if (role.getType() != role.getOtherRole().getType()
                    && ((matchesRoleTypeDomainClass(role, domainClass) && role.getMultiplicityUpper() == -1) || (matchesRoleTypeDomainClass(
                            role.getOtherRole(), domainClass) && role.getOtherRole().getMultiplicityUpper() == -1))) {
                continue;
            }
            //Last alternative is Relation N (relatedObject) to 1 (versionableObject)
            if (matchesRoleTypeDomainClass(role, domainClass) && role.getOtherRole().getMultiplicityUpper() == -1) {
                relatedClassName = role.getOtherRole().getType().getFullName();
                relatedRoleName = role.getOtherRole().getName();
                relatedExternalIdColumnName = convertRoleRelationNameToColumn(role.getName());
                prefixLabel = LABEL + role.getOtherRole().getType().getName() + ".";
            }

            Statement statement = null;
            String tableForObject = getTableForObject(relatedClassName);
            try {
                if (connection == null || statement == null) {
                    connection = VersioningTargetConfiguration.createConnection();
                    statement = connection.createStatement();
                }

                ResultSet executeQuery =
                        statement.executeQuery("SELECT * FROM " + tableForObject + " where "
                                + VersioningConstants.FF_QUB_TX_NUMBER + "=" + transactionId + " and "
                                + relatedExternalIdColumnName + "=" + versionedObject.getExternalId());
                ResultSetMetaData metaData = executeQuery.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (executeQuery.next()) {
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    if (queryResults.get(relatedRoleName) == null) {
                        queryResults.put(relatedRoleName, new ArrayList<Map<String, Object>>());
                    }
                    queryResults.get(relatedRoleName).add(map);
                    map.put(VersioningConstants.FF_QUB_CLASS_NAME, relatedClassName);
                    map.put(VersioningConstants.FF_QUB_ROLE_NAME, relatedRoleName);
                    for (int i = 1; i <= columnCount; i++) {
                        Object value;
                        if (executeQuery.getString(i) == null) {
                            value = NULL_STRING;
                        } else if (metaData.getColumnName(i).startsWith("OID_")) {
                            value = Long.parseLong(executeQuery.getString(i));
                        } else {
                            value = executeQuery.getString(i);
                        }
                        String key = convertColumnName(metaData.getColumnName(i), prefixLabel);
                        map.put(key, value);
                    }
                    if (map.get(convertColumnName(relatedExternalIdColumnName, prefixLabel)) == NULL_STRING) {
                        map.put(VersioningConstants.FF_QUB_ROLE_OPERATION_KIND, VersioningConstants.ROLE_REMOVED_OBJECT);
                    } else {
                        map.put(VersioningConstants.FF_QUB_ROLE_OPERATION_KIND, VersioningConstants.ROLE_ADDED_OBJECT);
                    }

                }
                //Deleted Objects have all attributes undefined, like the entry column that refers the related object
                //Therefore a different approach is used to find the deleted objects
                executeQuery =
                        statement.executeQuery("SELECT OID FROM " + tableForObject + " WHERE "
                                + VersioningConstants.FF_QUB_TX_NUMBER + "=" + transactionId
                                + " AND OID IN (SELECT distinct(OID) FROM " + tableForObject + " WHERE "
                                + relatedExternalIdColumnName + "=" + versionedObject.getExternalId() + ") AND "
                                + VersioningConstants.FF_QUB_OPERATION_KIND + "='DELETE'");
                metaData = executeQuery.getMetaData();
                columnCount = metaData.getColumnCount();
                while (executeQuery.next()) {
                    Map<String, Object> map = new LinkedHashMap<String, Object>();
                    if (queryResults.get(relatedRoleName) == null) {
                        queryResults.put(relatedRoleName, new ArrayList<Map<String, Object>>());
                    }
                    queryResults.get(relatedRoleName).add(map);
                    map.put(VersioningConstants.FF_QUB_CLASS_NAME, relatedClassName);
                    map.put(VersioningConstants.FF_QUB_ROLE_NAME, relatedRoleName);
                    map.put(VersioningConstants.FF_QUB_OPERATION_KIND, "DELETE");
                    map.put(VersioningConstants.FF_QUB_TX_NUMBER, transactionId);
                    map.put(VersioningConstants.FF_QUB_ROLE_OPERATION_KIND, VersioningConstants.ROLE_REMOVED_OBJECT);
                    for (int i = 1; i <= columnCount; i++) {
                        map.put(convertColumnName(metaData.getColumnName(i), prefixLabel), executeQuery.getString(i));
                    }
                }
            } catch (SQLException e) {
                //There is no guarantee that the related object has or had versioning active,
                // therefore all exceptions thrown because of the missing table are ignored.
                //The table may exists since its name is the superclass' name, therefore is also
                // ignored all exceptions thrown because missing the column.
                String message = e.getMessage().toUpperCase();
                if (!message.contains(relatedExternalIdColumnName) && !message.contains(tableForObject)) {
                    e.printStackTrace();
                }
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return queryResults;
    }

    private boolean matchesRoleTypeDomainClass(Role role, DomainClass domainClass) {
        if (role.getType().equals(domainClass)) {
            return true;
        }
        while (domainClass.getSuperclass() != null) {
            domainClass = FenixFramework.getDomainModel().findClass(domainClass.getSuperclass().getFullName());
            if (role.getType().equals(domainClass)) {
                return true;
            }
        }
        return false;
    }

    private String convertRoleRelationNameToColumn(String name) {
        return "OID_" + LOWER_CAMEL.to(UPPER_UNDERSCORE, name);
    }

    private String convertColumnName(String s, String labelPrefix) {
        if (s.startsWith("FF$QUB$") || s.equals("OID")) {
            return s;
        }
        int index = s.indexOf("OID_");
        if (index != -1) {
            s = s.substring(index + 4);
        }
        s = toCamelCase(s);
        if (s.equals(VersioningConstants.VERSIONING_CREATION_DATE)) {
            return "label.Versioning.creationDate";
        }
        if (s.equals(VersioningConstants.VERSIONING_CREATOR)) {
            return "label.Versioning.creator";
        }
        if (s.equals(VersioningConstants.VERSIONING_UPDATE_DATE)) {
            return "label.Versioning.updateDate";
        }
        if (s.equals(VersioningConstants.VERSIONING_UPDATED_BY)) {
            return "label.Versioning.updatedby";
        }
        return labelPrefix + toCamelCase(s);
    }

    private String toCamelCase(String s) {
        return UPPER_UNDERSCORE.to(LOWER_CAMEL, s);
    }

    private List<Role> getAllRolesSlotsListInClass(DomainClass domainClass) {
        List<Role> result = new ArrayList<Role>();
        if (domainClass == null) {
            return result;
        }
        DomainClass iterator = domainClass;
        result.addAll(iterator.getRoleSlotsList());
        while (iterator.getSuperclass() != null) {
            iterator = FenixFramework.getDomainModel().findClass(iterator.getSuperclass().getFullName());
            result.addAll(iterator.getRoleSlotsList());
        }
        return result;
    }
}
