/*
 * Created on 10/set/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
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
package org.sejda.core.manipulation.model.task.itext;

import static org.sejda.core.manipulation.model.task.itext.component.PdfCopiers.nullSafeClosePdfCopy;
import static org.sejda.core.manipulation.model.task.itext.util.ITextUtils.nullSafeClosePdfReader;
import static org.sejda.core.notification.dsl.ApplicationEventsNotifier.notifyEvent;
import static org.sejda.core.support.io.model.FileOutput.file;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.sejda.core.exception.TaskException;
import org.sejda.core.manipulation.model.RectangularBox;
import org.sejda.core.manipulation.model.input.PdfSource;
import org.sejda.core.manipulation.model.input.PdfSourceOpener;
import org.sejda.core.manipulation.model.parameter.CropParameters;
import org.sejda.core.manipulation.model.task.Task;
import org.sejda.core.manipulation.model.task.itext.component.DefaultPdfCopier;
import org.sejda.core.manipulation.model.task.itext.component.input.PdfSourceOpeners;
import org.sejda.core.support.io.SingleOutputWriterSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfRectangle;

/**
 * iText implementation of the Crop task to set MEDIABOX and CROPBOX on an input document. This task allow multiple boxes on the same page, generating an output document that
 * contains duplicated pages with different boxes applied.
 * 
 * @author Andrea Vacondio
 * 
 */
public class CropTask implements Task<CropParameters> {

    private static final Logger LOG = LoggerFactory.getLogger(CropTask.class);

    private PdfReader reader = null;
    private DefaultPdfCopier copier = null;
    private SingleOutputWriterSupport outputWriter;
    private PdfSourceOpener<PdfReader> sourceOpener;

    public void before(CropParameters parameters) {
        outputWriter = new SingleOutputWriterSupport();
        sourceOpener = PdfSourceOpeners.newFullReadOpener();
    }

    public void execute(CropParameters parameters) throws TaskException {
        PdfSource source = parameters.getSource();
        LOG.debug("Opening {} ", source);
        reader = source.open(sourceOpener);
        int totalPages = reader.getNumberOfPages();

        File tmpFile = outputWriter.createTemporaryPdfBuffer();
        LOG.debug("Created output temporary buffer {} ", tmpFile);

        copier = new DefaultPdfCopier(reader, tmpFile, parameters.getVersion());
        copier.setCompression(parameters.isCompressXref());

        Set<PdfRectangle> cropAreas = getPdfRectangles(parameters.getCropAreas());
        for (int page = 1; page <= totalPages; page++) {
            PdfDictionary dictionary = reader.getPageN(page);
            for (PdfRectangle cropBox : cropAreas) {
                LOG.trace("Applying crop box {} to page {}", cropBox, page);
                dictionary.put(PdfName.MEDIABOX, cropBox);
                dictionary.put(PdfName.CROPBOX, cropBox);
                copier.addPage(reader, page);
            }
            notifyEvent().stepsCompleted(page).outOf(totalPages);
        }
        nullSafeClosePdfCopy(copier);
        nullSafeClosePdfReader(reader);

        outputWriter.flushSingleOutput(file(tmpFile).name(parameters.getOutputName()), parameters.getOutput(),
                parameters.isOverwrite());
        LOG.debug("Crop areas applied to {}", parameters.getOutput());
    }

    public void after() {
        nullSafeClosePdfCopy(copier);
        nullSafeClosePdfReader(reader);
    }

    private Set<PdfRectangle> getPdfRectangles(Set<RectangularBox> areas) {
        Set<PdfRectangle> retVal = new LinkedHashSet<PdfRectangle>();
        for (RectangularBox box : areas) {
            retVal.add(new PdfRectangle(box.getLeft(), box.getBottom(), box.getRight(), box.getTop()));
        }
        return retVal;
    }
}