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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import javax.transaction.Status;
import javax.transaction.SystemException;

import org.apache.ojb.broker.accesslayer.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningConfiguration;
import com.qubit.solution.fenixedu.bennu.versioning.domain.VersioningTargetConfiguration;

import pt.ist.dap.util.Log;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBTransaction;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TopLevelTransaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class VersioningCreator implements CommitListener {

    private static final String CONNECTION = VersioningCreator.class.getSimpleName() + ".connection";
    private static final Logger logger = LoggerFactory.getLogger(VersioningCreator.class);

    private static Field UNDERLYING_TRANSACTION;
    static {
        try {
            UNDERLYING_TRANSACTION = JvstmOJBTransaction.class.getDeclaredField("underlyingTransaction");
            UNDERLYING_TRANSACTION.setAccessible(true);
        } catch (Exception e) {
            UNDERLYING_TRANSACTION = null;
            e.printStackTrace();
        }
    }

    // WARNING: This is backend specific. Currently this is not a problem because we'll be only using
    // JVSTM OJB backend, nevertheless this should be refactored as soon TxIntrospector allows us to 
    // access some kind of Transaction Identifier.
    //
    // 29 May 2015 - Paulo Abrantes
    static private TopLevelTransaction getHackedUnderlyingTransaction(final Transaction input) {
        TopLevelTransaction result = null;

        if (UNDERLYING_TRANSACTION != null) {
            try {
                result = (TopLevelTransaction) UNDERLYING_TRANSACTION.get(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void beforeCommit(final Transaction transaction) {
        final TxIntrospector txIntrospector = transaction.getTxIntrospector();
        if (txIntrospector.isWriteTransaction()) {
            TopLevelTransaction hackedUnderlyingTransaction = getHackedUnderlyingTransaction(transaction);
            final Integer txNumber = hackedUnderlyingTransaction.getNumber();

            Connection connection = null;
            try {
                connection = VersioningTargetConfiguration.createConnection();
                transaction.putInContext(CONNECTION, connection);
            } catch (SQLException e) {
                // We were unable to create a connection, maybe it's because the system booting
                // for the 1st time and it's not created it. In order to still have a connection 
                // We'll piggy back the FenixFramework connection, this will like the access to
                // TxNumber will be backend specific.
                //
                // We don't add Transaction to context here because since we're doing a piggy
                // back in the current transaction it will be FF taking care of the commit.
                try {
                    connection = hackedUnderlyingTransaction.getOJBBroker().serviceConnectionManager().getConnection();
                } catch (LookupException e1) {
                    e1.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    logger.debug("Starting connection log for connection: " + connection.hashCode());
                    VersioningHandler.startLog(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (final Iterator<DomainObject> iterator = txIntrospector.getModifiedObjects().iterator(); iterator
                        .hasNext();) {
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
                logger.debug("Finishing connection log for connection: " + connection.hashCode());
                VersioningHandler.endLog(connection,
                        (transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ACTIVE));
            } catch (SystemException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    logger.debug("Releasing connection log for connection: " + connection.hashCode());
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                transaction.putInContext(CONNECTION, null);
            }
        }
    }

}
