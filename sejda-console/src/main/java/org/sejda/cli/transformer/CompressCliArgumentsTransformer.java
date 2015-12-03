/*
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
package org.sejda.cli.transformer;

import org.sejda.cli.model.CompressTaskCliArguments;
import org.sejda.model.input.FileIndexAndPage;
import org.sejda.model.parameter.OptimizeParameters;

import java.util.Optional;

public class CompressCliArgumentsTransformer extends BaseCliArgumentsTransformer implements
        CommandCliArgumentsTransformer<CompressTaskCliArguments, OptimizeParameters> {

    @Override
    public OptimizeParameters toTaskParameters(CompressTaskCliArguments taskCliArguments) {
        OptimizeParameters parameters = new OptimizeParameters();
        populateAbstractParameters(parameters, taskCliArguments);
        populateSourceParameters(parameters, taskCliArguments);
        populateOutputTaskParameters(parameters, taskCliArguments);
        populateOutputPrefix(parameters, taskCliArguments);

        parameters.setCompressImages(true);
        parameters.setImageDpi(taskCliArguments.getImageDpi());
        parameters.setImageMaxWidthOrHeight(taskCliArguments.getImageMaxWidthOrHeight());
        parameters.setImageQuality(taskCliArguments.getImageQuality());

        return parameters;
    }

    private Optional<FileIndexAndPage> parseIndexAndPage(String in) {
        String[] parts = in.split(":");
        try {
            return Optional.of(new FileIndexAndPage(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}