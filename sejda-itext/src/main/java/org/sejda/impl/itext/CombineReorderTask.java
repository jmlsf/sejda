/*
 * Copyright 2015 by Edi Weissmann (edi.weissmann@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sejda.impl.itext;

import com.lowagie.text.pdf.PdfReader;
import org.sejda.core.support.io.OutputWriters;
import org.sejda.core.support.io.SingleOutputWriter;
import org.sejda.impl.itext.component.DefaultPdfCopier;
import org.sejda.impl.itext.component.FormFieldsAwarePdfCopier;
import org.sejda.impl.itext.component.PdfCopier;
import org.sejda.impl.itext.component.input.PdfSourceOpeners;
import org.sejda.model.exception.TaskException;
import org.sejda.model.input.FileIndexAndPage;
import org.sejda.model.input.PdfSource;
import org.sejda.model.input.PdfSourceOpener;
import org.sejda.model.parameter.CombineReorderParameters;
import org.sejda.model.task.BaseTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.sejda.common.ComponentsUtility.nullSafeCloseQuietly;
import static org.sejda.core.notification.dsl.ApplicationEventsNotifier.notifyEvent;
import static org.sejda.core.support.io.IOUtils.createTemporaryPdfBuffer;
import static org.sejda.core.support.io.model.FileOutput.file;
import static org.sejda.impl.itext.util.ITextUtils.nullSafeClosePdfReader;

/**
 * IText implementation of a task that allows combining multiple pdf sources, allowing reordering of the pdf pages
 * regardless of the ordering in the original sources.
 */
public class CombineReorderTask extends BaseTask<CombineReorderParameters> {

    private static final Logger LOG = LoggerFactory.getLogger(CombineReorderTask.class);

    private SingleOutputWriter outputWriter;
    private PdfSourceOpener<PdfReader> sourceOpener;
    private PdfCopier copier = null;
    private List<PdfReader> readers = new ArrayList<PdfReader>();
    private int totalSteps;

    public void before(CombineReorderParameters parameters) {
        totalSteps = parameters.getPages().size();
        sourceOpener = PdfSourceOpeners.newFullReadOpener();
        outputWriter = OutputWriters.newSingleOutputWriter(parameters.isOverwrite());
    }

    public void execute(CombineReorderParameters parameters) throws TaskException {
        int currentStep = 0;
        File tmpFile = createTemporaryPdfBuffer();
        LOG.debug("Created output temporary buffer {} ", tmpFile);

        for (PdfSource<?> input : parameters.getSourceList()) {
            LOG.debug("Opening input {} ", input);
            PdfReader reader = input.open(sourceOpener);
            readers.add(reader);

            createCopierIfNeeded(parameters, tmpFile, reader);
        }

        for (int i = 0; i < parameters.getPages().size(); i++) {
            FileIndexAndPage filePage = parameters.getPages().get(i);
            PdfReader reader = readers.get(filePage.getFileIndex());
            copier.addPage(reader, filePage.getPage());

            if(!isReaderStillNeeded(parameters.getPages(), i, filePage.getFileIndex())) {
                copier.freeReader(reader);
            }

            notifyEvent(getNotifiableTaskMetadata()).stepsCompleted(++currentStep).outOf(totalSteps);
        }
        closeResources();
        outputWriter.setOutput(file(tmpFile).name(parameters.getOutputName()));
        parameters.getOutput().accept(outputWriter);
        LOG.debug("Input documents merged correctly and written to {}", parameters.getOutput());
    }

    /**
     * Is there any other page, after current one, that references the fileIndex file? If not, then we can safely free the reader, as it won't be required anymore.
     */
    private boolean isReaderStillNeeded(List<FileIndexAndPage> filePages, int current, int fileIndex) {
        int i = current;
        while(i < filePages.size()) {
            if(filePages.get(i).getFileIndex() == fileIndex) {
                return true;
            }
            i++;
        }

        return false;
    }

    public void after() {
        closeResources();
    }

    private void closeResources() {
        nullSafeCloseQuietly(copier);
        for (PdfReader reader : readers) {
            nullSafeClosePdfReader(reader);
        }
    }

    private void createCopierIfNeeded(CombineReorderParameters parameters, File tmpFile, PdfReader reader) throws TaskException {
        if (copier == null) {
            if (parameters.isCopyFormFields()) {
                copier = new FormFieldsAwarePdfCopier(tmpFile, parameters.getVersion());
                LOG.debug("Created FormFieldsAwarePdfCopier");
            } else {
                copier = new DefaultPdfCopier(reader, tmpFile, parameters.getVersion());
                LOG.debug("Created DefaultPdfCopier");
            }
            copier.setCompression(parameters.isCompress());
        }
    }
}