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
 * This file is part of FenixEdu bennu-versioning-code-generator.
 *
 * FenixEdu bennu-versioning-code-generator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu bennu-versioning-code-generator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu bennu-versioning-code-generator.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qubit.extension.fenixframework;

import java.io.PrintWriter;

import com.qubit.solution.fenixedu.bennu.versioning.service.ModelDecorator;

import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DefaultCodeGenerator;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;

public class VersioningDefaultGenerator extends DefaultCodeGenerator {

    public VersioningDefaultGenerator(final CompilerArgs compArgs, final DomainModel domainModel) {
        super(compArgs, ModelDecorator.decorateModel(domainModel));
    }

    @Override
    protected void generateBaseClassBody(final DomainClass domClass, final PrintWriter out) {
        super.generateBaseClassBody(domClass, out);
        onNewline(out);
        print(out, "public java.util.Map<String, Object> getVersionInfo() { return null; }");
    }

    @Override
    public boolean isDefaultCodeGenerator() {
        // 22 Mar 2018
        // Fenix Framework has in CodeGenerator this method has:
        // getClass().equals(DefaultCodeGenerator.class), which does not allow this to be default
        return true;
    }
}
