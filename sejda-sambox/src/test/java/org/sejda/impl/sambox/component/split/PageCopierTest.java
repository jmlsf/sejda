/*
 * Created on 27 giu 2016
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
package org.sejda.impl.sambox.component.split;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Test;
import org.sejda.io.SeekableSources;
import org.sejda.sambox.cos.COSName;
import org.sejda.sambox.input.PDFParser;
import org.sejda.sambox.pdmodel.PDDocument;
import org.sejda.sambox.pdmodel.PDPage;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotation;

/**
 * @author Andrea Vacondio
 *
 */
public class PageCopierTest {

    @Test
    public void existingPage() throws IOException {
        try (PDDocument document = PDFParser.parse(SeekableSources.inMemorySeekableSourceFrom(
                getClass().getClassLoader().getResourceAsStream("pdf/shared_resource_dic_w_images.pdf")))) {
            PDPage page = document.getPage(0);
            PDPage copy = new PageCopier(false).copyOf(page);
            PDPage optimizedCopy = new PageCopier(true).copyOf(page);
            assertEquals(page.getMediaBox(), copy.getMediaBox());
            assertEquals(page.getCropBox(), copy.getCropBox());
            assertEquals(page.getRotation(), copy.getRotation());
            assertEquals(page.getResources(), copy.getResources());
            assertNotEquals(page.getResources(), optimizedCopy.getResources());
        }
    }

    @Test
    public void pageWithAnnots() throws IOException {
        try (PDDocument document = PDFParser.parse(SeekableSources.inMemorySeekableSourceFrom(
                getClass().getClassLoader().getResourceAsStream("pdf/forms/simple_form_with_full_dic.pdf")))) {
            PDPage page = document.getPage(0);
            PDPage copy = new PageCopier(false).copyOf(page);
            assertEquals(page.getCOSObject().getDictionaryObject(COSName.ANNOTS),
                    page.getCOSObject().getDictionaryObject(COSName.ANNOTS));
            assertNotEquals(page.getCOSObject().getDictionaryObject(COSName.ANNOTS),
                    copy.getCOSObject().getDictionaryObject(COSName.ANNOTS));
            copy.getAnnotations().stream().map(PDAnnotation::getCOSObject).forEach(d -> {
                assertFalse(d.containsKey(COSName.P));
                assertFalse(d.containsKey(COSName.DEST));
            });

        }
    }

}
