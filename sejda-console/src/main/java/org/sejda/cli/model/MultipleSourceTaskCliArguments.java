/*
 * Created on 28 feb 2016
 * Copyright 2015 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * This file is part of Sejda.
 *
 * Sejda is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sejda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Sejda.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.cli.model;

import java.util.List;

import org.sejda.conversion.FileSourceAdapter;

import com.lexicalscope.jewel.cli.Option;

/**
 * Command line arguments for a task accepting multiple files as input
 * 
 * @author Andrea Vacondio
 */
public interface MultipleSourceTaskCliArguments extends TaskCliArguments {
    @Option(shortName = "f", description = "files to operate on. A list of existing files (EX. -f /tmp/file1.txt) (required)")
    List<FileSourceAdapter> getFiles();
}
