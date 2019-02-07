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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningTargetConfiguration;

public class HistoryRetriever {

    private VersionableObject versionedObject;

    public HistoryRetriever(VersionableObject object) {
        this.versionedObject = object;
        VersioningTargetConfiguration instance = VersioningTargetConfiguration.getInstance();
        if (instance == null) {
            throw new IllegalStateException(
                    "Can only use HistoryRetrieve after configure a versioningTargetConfiguration. Please use the interface to configure it");
        }
    }

    private String getTableForObject() {
        DomainClass domainClass = FenixFramework.getDomainModel().findClass(versionedObject.getClass().getName());
        while (domainClass.getSuperclass() != null) {
            domainClass = FenixFramework.getDomainModel().findClass(domainClass.getSuperclass().getFullName());
        }
        return "FF$QUB$" + domainClass.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase() + "_VERSION";
    }

    public List<Map<String, Object>> retrieveVersions() {
        Connection connection = null;
        Statement statement = null;
        ArrayList<Map<String, Object>> resultSet = new ArrayList<Map<String, Object>>();
        try {
            connection = VersioningTargetConfiguration.createConnection();
            statement = connection.createStatement();
            ResultSet executeQuery = statement
                    .executeQuery("SELECT * FROM " + getTableForObject() + " where OID=" + this.versionedObject.getExternalId());
            ResultSetMetaData metaData = executeQuery.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (executeQuery.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                resultSet.add(map);
                for (int i = 1; i <= columnCount; i++) {
                    map.put(metaData.getColumnName(i).toLowerCase(), executeQuery.getString(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return resultSet;
    }

}
