/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.model.reader;

import static org.junit.Assert.assertEquals;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class DependencyLevelComparatorTest {

    @Test
    public void testCalcDependencyLevelWithNonOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", Version.parseVersion("1"), "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyDepth(modDefA));
        assertEquals(1, reg.calcDependencyDepth(modDefB));
        assertEquals(2, reg.calcDependencyDepth(modDefC));
    }

    @Test
    public void testCalcDependencyLevelIgnoresUnregisteredOptionalDependencies() {
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", Version.parseVersion("1"), "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        // mod-a is not registered in this case
        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyDepth(modDefB));
        assertEquals(1, reg.calcDependencyDepth(modDefC));
    }

    @Test
    public void testCalcDependencyLevelDoesNotIgnoreRegisteredOptionalDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefB = new ModuleDefinition("mod-b", Version.parseVersion("1"), "fake.Module", null);
        final ModuleDefinition modDefC = new ModuleDefinition("mod-c", Version.parseVersion("1"), "fake.Module", null);
        final DependencyDefinition depOnA = new DependencyDefinition();
        depOnA.setName("mod-a");
        depOnA.setVersion("1");
        depOnA.setOptional(true);
        final DependencyDefinition depOnB = new DependencyDefinition();
        depOnB.setName("mod-b");
        depOnB.setVersion("1");
        modDefB.addDependency(depOnA);
        modDefC.addDependency(depOnA);
        modDefC.addDependency(depOnB);

        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);
        map.put(modDefB.getName(), modDefB);
        map.put(modDefC.getName(), modDefC);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyDepth(modDefA));
        assertEquals(1, reg.calcDependencyDepth(modDefB));
        assertEquals(2, reg.calcDependencyDepth(modDefC));
    }

    /**
     * All dependencies are optional, and none are present.
     */
    @Test
    public void testCanHandleOnlyOptionalAndMissingDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);
        final DependencyDefinition optDepOnX = new DependencyDefinition();
        optDepOnX.setName("mod-x");
        optDepOnX.setVersion("1");
        optDepOnX.setOptional(true);
        final DependencyDefinition optDepOnY = new DependencyDefinition();
        optDepOnY.setName("mod-y");
        optDepOnY.setVersion("1");
        optDepOnY.setOptional(true);

        modDefA.addDependency(optDepOnX);
        modDefA.addDependency(optDepOnY);

        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyDepth(modDefA));
    }
    
    /**
     * No dependencies.
     */
    @Test
    public void testCalcDependencyLevelWithNoDependencies() {
        final ModuleDefinition modDefA = new ModuleDefinition("mod-a", Version.parseVersion("1"), "fake.Module", null);

        final Map<String, ModuleDefinition> map = new HashMap<String, ModuleDefinition>();
        map.put(modDefA.getName(), modDefA);

        final DependencyLevelComparator reg = new DependencyLevelComparator(map);

        assertEquals(0, reg.calcDependencyDepth(modDefA));
    } 
}
