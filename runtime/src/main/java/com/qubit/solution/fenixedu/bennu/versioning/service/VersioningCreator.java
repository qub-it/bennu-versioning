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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

import javax.transaction.Status;
import javax.transaction.SystemException;

import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningConfiguration;

public class VersioningCreator implements CommitListener {

    //TODO configure this in versiong configuration
    private static final String DB_URL = "<DB_URL>";
    private static final String USER = "<DB_USER>";
    private static final String PASS = "<DB_PASS>";

    private static final String CONNECTION = VersioningCreator.class.getSimpleName() + ".connection";

    public void beforeCommit(final Transaction transaction) {
        final TxIntrospector txIntrospector = transaction.getTxIntrospector();
        if (txIntrospector.isWriteTransaction()) {
            final Integer txNumber = -1;

            Connection connection = null;
            try {
                connection = DriverManager.getConnection(DB_URL, USER, PASS);
                transaction.putInContext(CONNECTION, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (connection != null) {
                try {
                    VersioningHandler.startLog(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for (final Iterator<DomainObject> iterator = txIntrospector.getModifiedObjects().iterator(); iterator.hasNext();) {
                final DomainObject domainObject = (DomainObject) iterator.next();
                if (!VersionableObject.class.isAssignableFrom(domainObject.getClass())) {
                    continue;
                }

                if (isVersionedActive(domainObject.getClass())) {
                    if (txIntrospector.isDeleted(domainObject)) {
                        VersioningHandler.logDelete(connection, txNumber, (VersionableObject) domainObject);
                    } else {
                        VersioningHandler.logEdit(connection, txNumber, (VersionableObject) domainObject);
                    }
                }
            }

            for (final Iterator<DomainObject> iterator = txIntrospector.getNewObjects().iterator(); iterator.hasNext();) {
                final DomainObject domainObject = (DomainObject) iterator.next();
                if (!VersionableObject.class.isAssignableFrom(domainObject.getClass())) {
                    continue;
                }

                if (isVersionedActive(domainObject.getClass())) {
                    VersioningHandler.logCreate(connection, txNumber, (VersionableObject) domainObject);
                }
            }
        }
    }

    private static boolean isVersionedActive(Class clazz) {
        VersioningConfiguration configurationFor = VersioningConfiguration.getConfigurationFor(clazz.getName());
        return configurationFor != null && configurationFor.getActive();
    }

    @Override
    public void afterCommit(final Transaction transaction) {
        final Connection connection = transaction.getFromContext(CONNECTION);
        if (connection != null) {
            try {
                if (transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ACTIVE) {
                    VersioningHandler.endLog(connection);
                }
            } catch (SystemException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                transaction.putInContext(CONNECTION, null);
            }
        }
    }

}
