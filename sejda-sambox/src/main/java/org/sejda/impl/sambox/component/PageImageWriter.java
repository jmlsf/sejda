/*
 * Copyright 2016 by Eduard Weissmann (edi.weissmann@gmail.com).
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
package org.sejda.impl.sambox.component;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.sejda.core.support.io.IOUtils;
import org.sejda.model.exception.TaskIOException;
import org.sejda.model.input.FileSource;
import org.sejda.model.input.Source;
import org.sejda.model.input.SourceDispatcher;
import org.sejda.model.input.StreamSource;
import org.sejda.sambox.pdmodel.PDDocument;
import org.sejda.sambox.pdmodel.PDPage;
import org.sejda.sambox.pdmodel.PDPageContentStream;
import org.sejda.sambox.pdmodel.graphics.image.PDImageXObject;
import org.sejda.sambox.pdmodel.graphics.image.UnsupportedTiffImageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageImageWriter {
    private static final Logger LOG = LoggerFactory.getLogger(PageImageWriter.class);

    private PDDocument document;

    public PageImageWriter(PDDocument document) {
        this.document = document;
    }

    public void write(PDPage page, PDImageXObject image, Point2D position, float width, float height) throws TaskIOException {
        try {
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                contentStream.drawImage(image, (float) position.getX(), (float) position.getY(), width, height);
                contentStream.close();
            }
        } catch (IOException e) {
            throw new TaskIOException("An error occurred writing image to the page.", e);
        }
    }

    public static PDImageXObject toPDXImageObject(Source<?> imageSource) throws TaskIOException {
        return imageSource.dispatch(new SourceDispatcher<PDImageXObject>() {
            @Override
            public PDImageXObject dispatch(FileSource source) throws TaskIOException {
                try {
                    return createFromFile(source.getSource().getPath());
                } catch (IOException e) {
                    throw new TaskIOException("An error occurred creating PDImageXObject from file source", e);
                }
            }

            @Override
            public PDImageXObject dispatch(StreamSource source) throws TaskIOException {
                try {
                    String extension = FilenameUtils.getExtension(source.getName());
                    File tmp = IOUtils.createTemporaryBuffer("." + extension);
                    try (FileOutputStream fos = new FileOutputStream(tmp)) {
                        org.apache.commons.io.IOUtils.copyLarge(source.getSource(), fos);
                    }
                    return createFromFile(tmp.getPath());
                } catch (IOException e) {
                    throw new TaskIOException("An error occurred creating PDImageXObject from file source", e);
                }
            }
        });
    }

    public static PDImageXObject createFromFile(String filePath) throws TaskIOException, IOException {
        try {
            return PDImageXObject.createFromFile(filePath);
        } catch (UnsupportedTiffImageException e) {
            LOG.warn("Found unsupported TIFF compression, converting TIFF to JPEG: " + e.getMessage());
            return PDImageXObject.createFromFile(convertTiffToJpg(filePath));
        }
    }

    public static String convertTiffToJpg(String filePath)
            throws IOException, TaskIOException {
        LOG.debug(filePath);
        BufferedImage image = ImageIO.read(new File(filePath));
        File tmpFile = IOUtils.createTemporaryBuffer(".jpg");
        ImageOutputStream outputStream = new FileImageOutputStream(tmpFile);

        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            writer.setOutput(outputStream);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(1.0F);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(outputStream);
        }

        return tmpFile.getPath();
    }
}
