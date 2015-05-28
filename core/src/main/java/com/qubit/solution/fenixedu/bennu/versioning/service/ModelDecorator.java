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

import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Slot;
import pt.ist.fenixframework.dml.ValueType;

public class ModelDecorator {

    public static DomainModel decorateModel(DomainModel domainModel) {
        ValueType stringValueType = domainModel.findValueType("String");
        ValueType dateTimeValueType = domainModel.findValueType("DateTime");
        Slot creator = new Slot("versioningCreator", stringValueType);
        Slot creationDate = new Slot("versioningCreationDate", dateTimeValueType);
        Slot updatedBy = new Slot("versioningUpdatedBy", stringValueType);
        Slot updateDate = new Slot("versioningUpdateDate", dateTimeValueType);

        for (DomainClass domainClass : domainModel.getDomainClasses()) {
            if (domainClass.getSuperclass() != null) {
                continue;
            }
            domainClass.getInterfacesNames().add(VersionableObject.class.getName());
            domainClass.addSlot(creator);
            domainClass.addSlot(creationDate);
            domainClass.addSlot(updatedBy);
            domainClass.addSlot(updateDate);
        }

        return domainModel;

    }
}
