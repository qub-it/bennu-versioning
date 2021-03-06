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
package com.qubit.extension.fenixframework;

import java.util.function.Consumer;

import com.qubit.solution.fenixedu.bennu.versioning.service.ModelDecorator;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;

public class VersioningConfig extends JvstmOJBConfig {

    private String versionConfigurators;

    public void setVersionConfigurators(String versionConfigurators) {
        this.versionConfigurators = versionConfigurators;
    }

    public String getVersionConfigurators() {
        return versionConfigurators;
    }

    @Override
    protected void init() {
        if (versionConfigurators != null && !versionConfigurators.isEmpty()) {
            for (String classname : versionConfigurators.split(",")) {
                try {
                    Class<?> forName = Class.forName(classname);
                    Consumer<Config> newInstance = (Consumer<Config>) forName.newInstance();
                    newInstance.accept(this);
                } catch (Throwable t) {
                    // It may fail but we want to keep going, so let's just print the stacktrace
                    t.printStackTrace();
                }
            }
        }
        ModelDecorator.decorateModel(FenixFramework.getDomainModel());
        super.init();
    }
}
