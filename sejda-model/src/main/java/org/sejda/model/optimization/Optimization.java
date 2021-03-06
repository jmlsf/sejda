/*
 * Created on 27 gen 2016
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
package org.sejda.model.optimization;

import org.sejda.common.FriendlyNamed;

/**
 * Things that the optimization process can optimize/discad from the original document
 * 
 * @author Andrea Vacondio
 *
 */
public enum Optimization implements FriendlyNamed {
    /**
     * PDF 32000-1:2008 14.3.2
     */
    DISCARD_METADATA("discard_metadata"),
    /**
     * PDF 32000-1:2008 12.3.3
     */
    DISCARD_OUTLINE("discard_outline"),
    /**
     * PDF 32000-1:2008 12.4.3
     */
    DISCARD_THREADS("discard_threads"),
    /**
     * PDF 32000-1:2008 14.10.2
     */
    DISCARD_SPIDER_INFO("discard_spider_info"),
    /**
     * PDF 32000-1:2008 14.5
     */
    DISCARD_PIECE_INFO("discard_piece_info"),
    /**
     * PDF 32000-1:2008 14.6.2
     */
    DISCARD_MC_PROPERTIES("discard_mc_props"),
    /**
     * PDF 32000-1:2008 8.9.5.4
     */
    DISCARD_ALTERNATE_IMAGES("discard_alternate_images"),
    COMPRESS_IMAGES("compress_images"),
    @Deprecated DISCARD_UNUSED_IMAGES("discard_unused_images"),
    DISCARD_UNUSED_RESOURCES("discard_unused_resources"),
    /**
     * PDF 32000-1:2008 14.7.2
     */
    DISCARD_STRUCTURE_TREE("discard_struct_tree"),
    /**
     * PDF 32000-1:2008 12.3.4
     */
    DISCARD_THUMBNAILS("discard_thumbnails");

    private String displayName;

    private Optimization(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getFriendlyName() {
        return displayName;
    }
}
