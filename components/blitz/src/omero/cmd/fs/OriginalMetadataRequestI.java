/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.fs;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import loci.formats.IFormatReader;
import ome.io.nio.PixelsService;
import ome.model.annotations.FileAnnotation;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.fs.Fileset;
import ome.parameters.Parameters;
import omero.RType;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OriginalMetadataRequest;
import omero.cmd.OriginalMetadataResponse;
import omero.cmd.Response;
import omero.constants.annotation.file.ORIGINALMETADATA;
import omero.constants.namespaces.NSCOMPANIONFILE;
import omero.util.IceMapper;

/**
 * Original metadata loader, handling both pre-FS and post-FS data.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.0.0
 */
public class OriginalMetadataRequestI extends OriginalMetadataRequest implements
		IRequest {

	private static final long serialVersionUID = -1L;

	private final OriginalMetadataResponse rsp = new OriginalMetadataResponse();

	private final PixelsService service;

	private Helper helper;

	public OriginalMetadataRequestI(PixelsService service) {
		this.service = service;
	}

	//
	// CMD API
	//

	public Map<String, String> getCallContext() {
		Map<String, String> all = new HashMap<String, String>();
		all.put("omero.group", "-1");
		return all;
	}

	public void init(Helper helper) {
		this.helper = helper;
		this.helper.setSteps(1);
	}

	public Object step(int step) {
		helper.assertStep(step);
		loadFileset();
		if (rsp.filesetId == null) {
			loadFileAnnotation();
		}
		return null;
	}

	public void buildResponse(int step, Object object) {
		helper.assertResponse(step);
		if (helper.isLast(step)) {
			helper.setResponseIfNull(rsp);
		}
	}

	public Response getResponse() {
		return helper.getResponse();
	}

	//
	// LOADING & PARSING
	//

	/**
	 * Searches for a {@link Fileset} attached to this {@link Image}, and if present,
	 * uses Bio-Formats to parse the metadata into the {@link OriginalMetadataResponse}
	 * instance. If no {@link Fileset} is present, then there <em>may</em> be a
	 * {@link FileAnnotation} present which has a static version of the metadata.
	 */
	@SuppressWarnings("unchecked")
	protected void loadFileset() {
		rsp.filesetId = firstIdOrNull("select i.fileset.id from Image i where i.id = :id");
		if (rsp.filesetId != null) {
			final Image image = helper.getServiceFactory().getQueryService().get(Image.class, imageId);
			final Pixels pixels = image.getPrimaryPixels();
			final IFormatReader reader = service.getBfReader(pixels);
			final int count = reader.getSeriesCount();

			final Hashtable<String, Object> global = reader.getGlobalMetadata();
			rsp.globalMetadata = wrap(global);
			rsp.seriesMetadata = new Map[count];
			for (int i = 0; i < count; i++) {
				reader.setSeries(i);
				final Hashtable<String, Object> series = reader.getSeriesMetadata();
				rsp.seriesMetadata[i] = wrap(series);
			}

		}
	}

	/**
	 * Only called if {@link #loadFileset()} finds no {@link Fileset}. If any {@link FileAnnotation}
	 * instances with the appropriate namespace and name are found, the first one is taken and
	 * parsed into the {@link OriginalMetadataResponse}.
	 */
	protected void loadFileAnnotation() {
		rsp.fileAnnotationId = firstIdOrNull(
				"select a.id from Image i join i.annotationLinks l join l.child a " +
				"where i.id = :id and a.ns = '" + ORIGINALMETADATA.value + "' " +
				"and a.file.name = '" + NSCOMPANIONFILE.value + "'");
		if (rsp.fileAnnotationId != null) {
			// loadFile
			// parseFile (from Python)
		}
	}

	//
	// HELPERS
	//

	/**
	 * Use {@link IQuery#projection(String, Parameters)} to load the first
	 * long which matches the given query. This means that the first return
	 * value in the select statement should likely be the id of an object.
	 */
	protected omero.RLong firstIdOrNull(String query) {
		List<Object[]> ids = helper.getServiceFactory().getQueryService()
				.projection(query, new Parameters().addId(imageId).page(0, 1));
		if (ids != null && ids.size() > 0) {
			return omero.rtypes.rlong((Long) ids.get(0)[0]);
		}
		return null;
	}

	/**
	 * Use {@link IceMapper} to convert from {@link Object} instances in
	 * the given {@link Hashtable} to {@link RType} instances. This may
	 * throw an exception on unknown types.
	 */
	protected Map<String, RType> wrap(Hashtable<String, Object> table) {
		final IceMapper mapper = new IceMapper();
		final Map<String, RType> rv = new HashMap<String, RType>();
		for (Entry<String, Object> entry : table.entrySet()) {
			try {
				rsp.globalMetadata.put(entry.getKey(), mapper.toRType(entry.getValue()));
			} catch (Exception e) {
				helper.warn("Could not convert to rtype: " + entry.getValue());
			}
		}
		return rv;
	}
}
