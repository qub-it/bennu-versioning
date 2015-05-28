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
 * FenixEdu bennu-versioning-core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu bennu-versioning-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu bennu-versioning-core.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.solution.fenixedu.bennu.versioning.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.joda.time.DateTime;

import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class VersioningFieldsFiller implements CommitListener {

    @Override
    public void beforeCommit(Transaction transaction) {
        final TxIntrospector txIntrospector = transaction.getTxIntrospector();
        if (txIntrospector.isWriteTransaction()) {

            String currentUsername = getCurrentUsername();

            for (final Iterator<DomainObject> iterator = txIntrospector.getModifiedObjects().iterator(); iterator.hasNext();) {

                final DomainObject domainObject = iterator.next();
                if (!txIntrospector.isDeleted(domainObject) && VersionableObject.class.isAssignableFrom(domainObject.getClass())) {
                    VersionableObject versionableObject = (VersionableObject) domainObject;
                    versionableObject.setVersioningUpdateDate(new DateTime());
                    versionableObject.setVersioningUpdatedBy(currentUsername);
                }
            }

            List<DomainObject> newObjects = new ArrayList<DomainObject>(txIntrospector.getNewObjects());
            for (DomainObject domainObject : newObjects) {
                if (VersionableObject.class.isAssignableFrom(domainObject.getClass())) {
                    VersionableObject versionableObject = (VersionableObject) domainObject;
                    DateTime dateTime = new DateTime();
                    versionableObject.setVersioningCreationDate(dateTime);
                    versionableObject.setVersioningCreator(currentUsername);
                    versionableObject.setVersioningUpdateDate(dateTime);
                    versionableObject.setVersioningUpdatedBy(currentUsername);
                }
            }
        }

    }

    private String getCurrentUsername() {
        User currentUser = Authenticate.getUser();
        return currentUser != null ? currentUser.getUsername() : "unknown";
    }

    @Override
    public void afterCommit(Transaction arg0) {
        // TODO Auto-generated method stub

    }

}
