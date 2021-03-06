/*
 * Created on Jun 30, 2011
 * Copyright 2011 by Eduard Weissmann (edi.weissmann@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.sejda.cli.transformer.CliCommand;
import org.sejda.core.Sejda;

/**
 * Tests for the command line's general options
 * 
 * @author Eduard Weissmann
 * 
 */
public class GeneralConsoleOptionsTest extends AbstractTestSuite {

    @Test
    public void testExecuteWithoutArgs() {
        List<String> expectedStrings = getExpectedStringForGeneralHelp();

        assertConsoleOutputContains("", expectedStrings.toArray(new String[] {}));
    }

    @Test
    public void testExecuteHelp() {
        List<String> expectedStrings = getExpectedStringForGeneralHelp();

        assertConsoleOutputContains("-h", expectedStrings.toArray(new String[] {}));
    }

    private List<String> getExpectedStringForGeneralHelp() {
        List<String> expectedStrings = new ArrayList<String>();
        expectedStrings.add("Basic commands:");

        for (CliCommand eachCommand : CliCommand.values()) {
            // each command should be mentioned
            expectedStrings.add(eachCommand.getDisplayName());
            // together with its description
            // TODO: add a test verifying that descriptions are included. What to do with formatting, test should be formatting-unaware? Should formatting also be tested?
            // expectedStrings.add(eachCommand.getDescription());
        }
        return expectedStrings;
    }

    @Test
    public void testExecuteVersion() {
        assertConsoleOutputContains("--version", "Sejda Console (Version " + Sejda.VERSION + ")");
    }

    @Test
    public void testExecuteLicense() throws IOException {
        assertConsoleOutputContains("--license", IOUtils.toString(getClass().getResourceAsStream("/sejda-console/LICENSE.txt")));
    }

    @Test
    public void testExecuteUnknownCommandHelp() {
        assertConsoleOutputContains("-h unknownCommand", "Basic commands:");
        assertConsoleOutputContains("unknownCommand -h", "Basic commands:");
    }

    @Test
    public void testExecuteUnknownCommand() {
        assertConsoleOutputContains("unknownCommand", "Basic commands:");
    }
}
