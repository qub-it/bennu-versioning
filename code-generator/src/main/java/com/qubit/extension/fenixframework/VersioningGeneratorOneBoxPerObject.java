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

import pt.ist.fenixframework.backend.jvstmojb.codeGenerator.FenixCodeGeneratorOneBoxPerObject;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

import com.qubit.solution.fenixedu.bennu.versioning.service.ModelDecorator;

public class VersioningGeneratorOneBoxPerObject extends FenixCodeGeneratorOneBoxPerObject {

    public VersioningGeneratorOneBoxPerObject(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, ModelDecorator.decorateModel(domainModel));
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        super.generateBaseClassBody(domClass, out);
        generateValueMapForAuditing(domClass, out);
    }

    protected void generateValueMapForAuditing(DomainClass domClass, PrintWriter out) {
        onNewline(out);
        print(out, "public java.util.Map<String, Object> getVersionInfo()");

        newBlock(out);
        onNewline(out);
        if (domClass.getSuperclass() == null) {
            print(out, "java.util.Map<String, Object> map = new java.util.HashMap<String,Object>();");
        } else {
            print(out, "java.util.Map<String, Object> map = super.getVersionInfo();");
        }

        for (Slot slot : domClass.getSlotsList()) {
            onNewline(out);
            print(out, "Object " + slot.getName() + " = ((DO_State) this.get$obj$state(false))." + slot.getName() + ";");
            onNewline(out);
            print(out, "if (" + slot.getName() + " != null)");
            newBlock(out);
            print(out, "map.put(\"" + slot.getName() + "\", " + slot.getName() + ");");
            closeBlock(out);
        }

        for (Role role : domClass.getRoleSlotsList()) {
            if ((role.getName() != null) && (role.getMultiplicityUpper() == 1)) {
                onNewline(out);
                print(out, "Object " + role.getName() + " = ((DO_State) this.get$obj$state(false))." + role.getName() + ";");
                onNewline(out);
                print(out, "if (" + role.getName() + " != null)");
                newBlock(out);
                print(out, "map.put(\"" + role.getName() + "\", " + role.getName() + ");");
                closeBlock(out);
            }
        }
        print(out, "return map;");
        closeBlock(out);
    }
}
