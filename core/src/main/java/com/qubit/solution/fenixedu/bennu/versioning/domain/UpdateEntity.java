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
package com.qubit.solution.fenixedu.bennu.versioning.domain;

import java.io.Serializable;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;

@SuppressWarnings("serial")
public class UpdateEntity implements Serializable {

    private String username;

    public UpdateEntity() {
        username = getCurrentUsername();
    }

    private String getCurrentUsername() {
        User currentUser = Authenticate.getUser();
        return currentUser != null ? currentUser.getUsername() : "unknown";
    }

    public UpdateEntity(String externalizedString) {
        this.username = externalizedString;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }

    public String externalize() {
        String currentUsername = getCurrentUsername();
        return currentUsername;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UpdateEntity) {
            UpdateEntity other = (UpdateEntity) o;
            return username.equals(other.getUsername());
        }
        return false;
    }
}
