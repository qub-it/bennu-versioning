/**
x * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fenixedu.bennu.core.domain.Bennu;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;

public class VersioningConfiguration extends VersioningConfiguration_Base {

    private static final Map<String, VersioningConfiguration> CACHE = new HashMap<String, VersioningConfiguration>();

    public VersioningConfiguration(String classname) {
        this();
        setClassname(classname);
        setActive(false);
    }

    protected VersioningConfiguration() {
        super();
        setRootDomainObject(Bennu.getInstance());
    }

    public static void syncConfigurationWithDomain() {
        List<String> names = new ArrayList<String>();
        Collection<DomainClass> domainClasses = FenixFramework.getDomainModel().getDomainClasses();
        for (DomainClass domainClass : domainClasses) {
            if (getConfigurationFor(domainClass.getFullName()) == null) {
                names.add(domainClass.getFullName());
            }
        }

        if (!names.isEmpty()) {
            createConfigurations(names);
            rebuildCache();
        }
    }

    public static VersioningConfiguration getConfigurationFor(String fullClassName) {
        return CACHE.get(fullClassName);
    }

    public static void rebuildCache() {
        CACHE.clear();
        for (VersioningConfiguration configuration : Bennu.getInstance().getVersioningConfigurationsSet()) {
            CACHE.put(configuration.getClassname(), configuration);
        }
    }

    @Atomic
    private static void createConfigurations(List<String> names) {
        for (String name : names) {
            new VersioningConfiguration(name).setActive(false);;
        }
    }

}
