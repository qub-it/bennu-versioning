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

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import pt.ist.fenixframework.dml.DomainModel;

public class ModelDecorator {

    public static String LOOKUP_PATH = "pt/ist/fenixframework/dml/decorator";

    public static interface FenixFrameworkDomainModelDecorator {

        public void decorateModel(DomainModel domainModel);
    }

    public static DomainModel decorateModel(DomainModel domainModel) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(FenixFrameworkDomainModelDecorator.class));

        Set<BeanDefinition> components = provider.findCandidateComponents(LOOKUP_PATH);
        for (BeanDefinition component : components) {
            try {
                Class<? extends FenixFrameworkDomainModelDecorator> cls =
                        (Class<? extends FenixFrameworkDomainModelDecorator>) Class.forName(component.getBeanClassName());
                System.out.println("Applying decorator: " + cls.getName());
                cls.newInstance().decorateModel(domainModel);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return domainModel;

    }
}
