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
 * This file is part of FenixEdu bennu-versioning-runtime.
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
package com.qubit.solution.fenixedu.bennu.versioning.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import pt.ist.fenixframework.FenixFramework;

import com.qubit.solution.fenixedu.bennu.versioning.service.VersioningCreator;
import com.qubit.solution.fenixedu.bennu.versioning.service.VersioningFieldsFiller;

@WebListener
public class BennuVersioningRuntimeInitializer implements ServletContextListener {

    public static final VersioningFieldsFiller FIELDS_FILLER_COMMIT_LISTENER = new VersioningFieldsFiller();
    public static final VersioningCreator VERSION_CREATOR_COMMIT_LISTENER = new VersioningCreator();

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        FenixFramework.getTransactionManager().removeCommitListener(VERSION_CREATOR_COMMIT_LISTENER);
        FenixFramework.getTransactionManager().removeCommitListener(FIELDS_FILLER_COMMIT_LISTENER);
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        FenixFramework.getTransactionManager().addCommitListener(FIELDS_FILLER_COMMIT_LISTENER);
        FenixFramework.getTransactionManager().addCommitListener(VERSION_CREATOR_COMMIT_LISTENER);
    }

}
