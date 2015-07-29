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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainEntity;
import pt.ist.fenixframework.dml.Role;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningTargetConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.util.VersioningConstants;

public class HistoryRetriever {

    public static final String NULL_STRING = "NULL";
    public static final String LABEL = "label.";

    private static interface ResultSetProcessor {
        public void process(ResultSet resultSet) throws SQLException;
    }

    private VersionableObject versionedObject;

    public HistoryRetriever(VersionableObject object) {
        this.versionedObject = object;
        VersioningTargetConfiguration instance = VersioningTargetConfiguration.getInstance();
        if (instance == null) {
            throw new IllegalStateException(
                    "Can only use HistoryRetrieve after configure a versioningTargetConfiguration. Please use the interface to configure it");
        }
    }

    public Map<String, Object> retrieveObject(String txNumber) {
        Map<String, Object> objectValues = new LinkedHashMap<String, Object>();
        String prefixLabel = LABEL + versionedObject.getClass().getSimpleName() + ".";
        String tableName = getTableNameForObject(versionedObject.getClass().getName());
        executeQuery("SELECT * FROM #tableName0 where OID=? and " + VersioningConstants.VERSIONED_COLUMN_FF_QUB_TX_NUMBER + "=?",
                new String[] { tableName }, new String[] { tableName }, new ResultSetProcessor() {

                    @Override
                    public void process(ResultSet resultSet) throws SQLException {
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        resultSet.next();
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = resultSet.getString(i);
                            String key = getPresentationNameForObject(metaData.getColumnName(i), prefixLabel);
                            objectValues.put(key, value);
                        }
                    }
                }, new String[] { this.versionedObject.getExternalId(), txNumber });
        return objectValues;
    }

    public List<Map<String, Object>> retrieveObject() {
        return retrieveObject(false);
    }

    public List<Map<String, Object>> retrieveObject(boolean onlyModifications) {
        ArrayList<Map<String, Object>> objectVersions = new ArrayList<Map<String, Object>>();
        Map<String, Object> objectRecentValues = new LinkedHashMap<String, Object>();
        String prefixLabel = LABEL + versionedObject.getClass().getSimpleName() + ".";
        String tableName = getTableNameForObject(versionedObject.getClass().getName());
        executeQuery("SELECT * FROM #tableName0 where OID=?;", new String[] { tableName }, new String[] { tableName },
                new ResultSetProcessor() {

                    @Override
                    public void process(ResultSet resultSet) throws SQLException {
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();
                        while (resultSet.next()) {
                            Map<String, Object> map = new LinkedHashMap<String, Object>();
                            objectVersions.add(map);
                            for (int i = 1; i <= columnCount; i++) {
                                Object value;
                                if (resultSet.getString(i) == null) {
                                    value = NULL_STRING;
                                } else if (metaData.getColumnName(i).startsWith("OID_")) {
                                    value = Long.parseLong(resultSet.getString(i));
                                } else {
                                    value = resultSet.getString(i);
                                }
                                String key = getPresentationNameForObject(metaData.getColumnName(i), prefixLabel);
                                if (!onlyModifications
                                        || (objectRecentValues.get(key) == null || !objectRecentValues.get(key).equals(value))) {
                                    map.put(key, value);
                                    objectRecentValues.put(key, value);
                                }
                            }
                        }
                    }
                }, new String[] { this.versionedObject.getExternalId() });
        return objectVersions;
    }

    public List<String> retrieveTransactions() {
        List<String> txIds = new ArrayList<String>();
        String tableName = getTableNameForObject(versionedObject.getClass().getName());
        executeQuery("SELECT " + VersioningConstants.VERSIONED_COLUMN_FF_QUB_TX_NUMBER + " FROM #tableName0 where OID = ?;",
                new String[] { tableName }, new String[] { tableName }, new ResultSetProcessor() {

                    @Override
                    public void process(ResultSet resultSet) throws SQLException {
                        while (resultSet.next()) {
                            txIds.add(resultSet.getString(1));
                        }
                    }
                }, new String[] { this.versionedObject.getExternalId() });
        return txIds;
    }

    public Map<String, Map<String, List<Map<String, Object>>>> retrieveRelations() {
        Map<String, Map<String, List<Map<String, Object>>>> resultSet =
                new LinkedHashMap<String, Map<String, List<Map<String, Object>>>>();
        List<String> objectVersionsTransactions = retrieveTransactions();
        for (String transactionId : objectVersionsTransactions) {
            resultSet.put(transactionId, retrieveRelations(transactionId));
        }
        return resultSet;
    }

    public Map<String, List<Map<String, Object>>> retrieveRelations(String transactionId) {
        DomainClass domainClass = FenixFramework.getDomainModel().findClass(versionedObject.getClass().getName());
        // -1 correponds to multiplicity zero or more (*)
        List<Role> allRolesSlotsLists =
                getRoleSet(domainClass).stream()
                        //Capturing 2 relations 
                        //   -> 1 (versionableObject) to N (relatedObject);
                        //   -> N (versionableObject) to N (relatedObject);
                        .filter(role -> (isRoleTypeEqualsToDomainClass(role, domainClass) && role.getOtherRole()
                                .getMultiplicityUpper() == -1)
                                || (isRoleTypeEqualsToDomainClass(role.getOtherRole(), domainClass) && role
                                        .getMultiplicityUpper() == -1)).collect(Collectors.toList());
        Map<String, List<Map<String, Object>>> modificationsInRelations = new LinkedHashMap<String, List<Map<String, Object>>>();
        for (Role role : allRolesSlotsLists) {
            //TODO - process relations N to N
            //Relation N to N - allRolesSlotsLists only has roles where at least one side is multiplicity -1
            if (role.getOtherRole().getMultiplicityUpper() == role.getMultiplicityUpper()) {
                //TODO
                continue;
            }
            final String relatedClassName;
            final String relatedRoleName;
            final String versionableOidColumnName;
            final String prefixLabel;
            //Relation 1 (versionableObject) to N (relatedObject)
            if (isRoleTypeEqualsToDomainClass(role, domainClass) && role.getOtherRole().getMultiplicityUpper() == -1) {
                relatedClassName = role.getOtherRole().getType().getFullName();
                relatedRoleName = role.getOtherRole().getName();
                versionableOidColumnName = getColumnNameForObject(role.getName());
                prefixLabel = LABEL + role.getOtherRole().getType().getName() + ".";
            } else {
                relatedClassName = role.getType().getFullName();
                relatedRoleName = role.getName();
                versionableOidColumnName = getColumnNameForObject(role.getOtherRole().getName());
                prefixLabel = LABEL + role.getType().getName() + ".";
            }
            executeQuery("SELECT * FROM #tableName0 WHERE OID IN (SELECT distinct(OID) FROM #tableName0 WHERE "
                    + versionableOidColumnName + "=?) AND " + VersioningConstants.VERSIONED_COLUMN_FF_QUB_TX_NUMBER + "=?;",
                    new String[] { getTableNameForObject(relatedClassName) }, new String[] { versionableOidColumnName,
                            getTableNameForObject(relatedClassName) }, new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet resultSet) throws SQLException {
                            while (resultSet.next()) {
                                if (resultSet.getString(VersioningConstants.VERSIONED_COLUMN_FF_QUB_OPERATION_KIND).equals(
                                        "DELETE")) {
                                    processDeleteRow(resultSet);
                                } else {
                                    processCreateUpdateRow(resultSet);
                                }
                            }
                        }

                        private void processCreateUpdateRow(ResultSet resultSet) throws SQLException {
                            ResultSetMetaData metaData = resultSet.getMetaData();
                            int columnCount = metaData.getColumnCount();
                            Map<String, Object> map = new LinkedHashMap<String, Object>();
                            if (modificationsInRelations.get(relatedRoleName) == null) {
                                modificationsInRelations.put(relatedRoleName, new ArrayList<Map<String, Object>>());
                            }
                            modificationsInRelations.get(relatedRoleName).add(map);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_CLASS_NAME, relatedClassName);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_ROLE_NAME, relatedRoleName);
                            for (int i = 1; i <= columnCount; i++) {
                                Object value;
                                if (resultSet.getString(i) == null) {
                                    value = NULL_STRING;
                                } else if (metaData.getColumnName(i).startsWith("OID_")) {
                                    value = Long.parseLong(resultSet.getString(i));
                                } else {
                                    value = resultSet.getString(i);
                                }
                                String key = getPresentationNameForObject(metaData.getColumnName(i), prefixLabel);
                                map.put(key, value);
                            }
                            if (map.get(getPresentationNameForObject(versionableOidColumnName, prefixLabel)) == NULL_STRING) {
                                map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_ROLE_OPERATION_KIND,
                                        VersioningConstants.ROLE_REMOVED_OBJECT);
                            } else {
                                map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_ROLE_OPERATION_KIND,
                                        VersioningConstants.ROLE_ADDED_OBJECT);
                            }

                        }

                        private void processDeleteRow(ResultSet resultSet) throws SQLException {
                            Map<String, Object> map = new LinkedHashMap<String, Object>();
                            if (modificationsInRelations.get(relatedRoleName) == null) {
                                modificationsInRelations.put(relatedRoleName, new ArrayList<Map<String, Object>>());
                            }
                            modificationsInRelations.get(relatedRoleName).add(map);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_CLASS_NAME, relatedClassName);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_ROLE_NAME, relatedRoleName);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_OPERATION_KIND, "DELETE");
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_TX_NUMBER, transactionId);
                            map.put(VersioningConstants.VERSIONED_COLUMN_FF_QUB_ROLE_OPERATION_KIND,
                                    VersioningConstants.ROLE_REMOVED_OBJECT);
                            map.put(getPresentationNameForObject("OID", prefixLabel), resultSet.getString("OID"));
                        }
                    }, new String[] { versionedObject.getExternalId(), transactionId });
        }
        return modificationsInRelations;
    }

    //The variables that hold the tables names use the following convention: #tableName<i> where i is the number of the variable
    //example SELECT * FROM #tableName1,#tableName2 WHERE ?=? AND ? in (SELECT ? FROM #tableName3 WHERE ?=?)
    private void executeQuery(String sqlQuery, String[] tableNames, String[] ignoreErrorMessageList,
            ResultSetProcessor processor, String... arguments) {
        ResultSet executeQuery = null;
        for (int i = 0; i < tableNames.length; i++) {
            sqlQuery = sqlQuery.replaceAll("#tableName" + i, Matcher.quoteReplacement(tableNames[i]));
        }
        try (Connection connection = VersioningTargetConfiguration.createConnection();
                PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            for (int i = 0; i < arguments.length; i++) {
                statement.setString(i + 1, arguments[i]);
            }
            executeQuery = statement.executeQuery();
            processor.process(executeQuery);
        } catch (SQLException e) {
            //There is no guarantee that the related object has or had versioning active,
            // therefore all exceptions thrown because of the missing table are ignored.
            //The table may exists since its name is the superclass' name, therefore is also
            // ignored all exceptions thrown because missing the column.
            String message = e.getMessage().toUpperCase();
            for (String ignoreMessage : ignoreErrorMessageList) {
                if (message.contains(ignoreMessage)) {
                    return;
                }
            }
            e.printStackTrace();
        } finally {
            if (executeQuery != null) {
                try {
                    executeQuery.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isRoleTypeEqualsToDomainClass(Role role, DomainClass domainClass) {
        DomainEntity domainEntity = role.getType();
        if (domainEntity.equals(domainClass)) {
            return true;
        }
        while (domainClass.getSuperclass() != null) {
            domainClass = FenixFramework.getDomainModel().findClass(domainClass.getSuperclass().getFullName());
            if (domainEntity.equals(domainClass)) {
                return true;
            }
        }
        return false;
    }

    private String getTableNameForObject(String versionedClassName) {
        DomainClass domainClass = FenixFramework.getDomainModel().findClass(versionedClassName);
        while (domainClass.getSuperclass() != null) {
            domainClass = FenixFramework.getDomainModel().findClass(domainClass.getSuperclass().getFullName());
        }
        return "FF$QUB$" + LOWER_CAMEL.to(UPPER_UNDERSCORE, domainClass.getName()) + "_VERSION";
    }

    private String getColumnNameForObject(String className) {
        return "OID_" + LOWER_CAMEL.to(UPPER_UNDERSCORE, className);
    }

    private String getPresentationNameForObject(String columnName, String labelPrefix) {
        if (columnName.startsWith("FF$QUB$") || columnName.equals("OID")) {
            return columnName;
        }
        int index = columnName.indexOf("OID_");
        if (index != -1) {
            columnName = columnName.substring(index + 4);
        }
        columnName = UPPER_UNDERSCORE.to(LOWER_CAMEL, columnName);
        if (columnName.equals(VersioningConstants.SLOT_VERSIONING_CREATION_DATE)) {
            return "label.Versioning.creationDate";
        }
        if (columnName.equals(VersioningConstants.SLOT_VERSIONING_CREATOR)) {
            return "label.Versioning.creator";
        }
        if (columnName.equals(VersioningConstants.SLOT_VERSIONING_UPDATE_DATE)) {
            return "label.Versioning.updateDate";
        }
        if (columnName.equals(VersioningConstants.SLOT_VERSIONING_UPDATED_BY)) {
            return "label.Versioning.updatedby";
        }
        return labelPrefix + columnName;
    }

    private List<Role> getRoleSet(DomainClass domainClass) {
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
