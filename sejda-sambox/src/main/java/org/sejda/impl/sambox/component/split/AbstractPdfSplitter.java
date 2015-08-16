/*
 * Copyright 2015 by Edi Weissmann (edi.weissmann@gmail.com)
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
package org.sejda.impl.sambox.component.split;

import static org.sejda.core.notification.dsl.ApplicationEventsNotifier.notifyEvent;
import static org.sejda.core.support.io.IOUtils.createTemporaryPdfBuffer;
import static org.sejda.core.support.io.model.FileOutput.file;
import static org.sejda.core.support.prefix.NameGenerator.nameGenerator;
import static org.sejda.core.support.prefix.model.NameGenerationRequest.nameRequest;

import java.io.File;

import org.sejda.core.support.io.MultipleOutputWriter;
import org.sejda.core.support.io.OutputWriters;
import org.sejda.core.support.prefix.model.NameGenerationRequest;
import org.sejda.impl.sambox.component.PagesExtractor;
import org.sejda.model.exception.TaskException;
import org.sejda.model.parameter.base.SinglePdfSourceMultipleOutputParameters;
import org.sejda.model.split.NextOutputStrategy;
import org.sejda.model.task.NotifiableTaskMetadata;
import org.sejda.sambox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPdfSplitter<T extends SinglePdfSourceMultipleOutputParameters> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPdfSplitter.class);

    private PDDocument document;
    private T parameters;
    private int totalPages;
    private MultipleOutputWriter outputWriter;

    public AbstractPdfSplitter(PDDocument document, T parameters) {
        this.document = document;
        this.parameters = parameters;
        this.totalPages = document.getNumberOfPages();
        this.outputWriter = OutputWriters.newMultipleOutputWriter(parameters.isOverwrite());
    }

    public void split(NotifiableTaskMetadata taskMetadata) throws TaskException {
        nextOutputStrategy().ensureIsValid();

        try (PagesExtractor extractor = new PagesExtractor(document)) {
            int outputDocumentsCounter = 0;
            File tmpFile = null;
            for (int page = 1; page <= totalPages; page++) {
                if (nextOutputStrategy().isOpening(page)) {
                    LOG.debug("Starting split at page {} of the original document", page);
                    outputDocumentsCounter++;
                    tmpFile = createTemporaryPdfBuffer();
                    String outName = nameGenerator(parameters.getOutputPrefix())
                            .generate(
                                    enrichNameGenerationRequest(nameRequest().page(page)
                                            .originalName(parameters.getSource().getName())
                                            .fileNumber(outputDocumentsCounter)));
                    outputWriter.addOutput(file(tmpFile).name(outName));
                }
                LOG.trace("Retaining page {} of the original document", page);
                extractor.retain(page);
                notifyEvent(taskMetadata).stepsCompleted(page).outOf(totalPages);
                if (nextOutputStrategy().isClosing(page) || page == totalPages) {
                    LOG.debug("Created output temporary buffer {}", tmpFile);
                    extractor.setVersion(parameters.getVersion());
                    extractor.setCompress(parameters.isCompress());
                    extractor.save(tmpFile);
                    extractor.reset();

                    LOG.debug("Ending split at page {} of the original document", page);
                }
            }
        }
        parameters.getOutput().accept(outputWriter);
    }

    abstract NameGenerationRequest enrichNameGenerationRequest(NameGenerationRequest request);

    /**
     * @return the strategy to use to know if it's time to open a new document or close the current one.
     */
    abstract NextOutputStrategy nextOutputStrategy();
}