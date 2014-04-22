// Copyright (c) 2002-2005 Canoo Engineering AG, Switzerland. All Rights Reserved.
package com.canoo.webtest.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Denis N. Antonioli
 */
public class MimeType {
	public static final MimeType ALL_MEDIA;

	static {
		ALL_MEDIA = new MimeType();
	}

	private final List<MediaRange> fMediaRanges = new ArrayList<MediaRange>();

	private MimeType() {
		fMediaRanges.add(MediaRange.ALL_MEDIA_RANGES);
	}

	public MimeType(final String mimeType) {
		final String mts[] = mimeType.split("\\s*;\\s*");
		for (int i = 0; i < mts.length; i++) {
			fMediaRanges.add(MediaRange.create(mts[i]));
		}

		if (!fMediaRanges.isEmpty()) {
			Collections.sort(fMediaRanges);

			if (fMediaRanges.get(0) instanceof AllMediaRanges) {
				fMediaRanges.clear();
				fMediaRanges.add(MediaRange.ALL_MEDIA_RANGES);
			} else {
				final List<MediaRange> original = new ArrayList<MediaRange>(fMediaRanges);
				fMediaRanges.clear();

				final Iterator it = original.iterator();
				MediaRange lastMR = (MediaRange) it.next();
				fMediaRanges.add(lastMR);
				while (it.hasNext()) {
					final MediaRange mediaRange = (MediaRange) it.next();
					if (lastMR instanceof AllSubtypesMediaRanges && lastMR.getType().equals(mediaRange.getType())) {
						continue;
					}
					fMediaRanges.add(mediaRange);
					lastMR = mediaRange;
				}
			}
		}
	}

	public boolean match(final String contentType) {
		for (Iterator it = fMediaRanges.iterator(); it.hasNext();) {
			if (((MediaRange) it.next()).match(contentType)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Iterator it = fMediaRanges.iterator(); it.hasNext();) {
			sb.append(it.next().toString()).append(";");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	protected static class MediaRange implements Comparable<MediaRange> {
		private final String fType;
		private final String fSubtype;
		static final AllMediaRanges ALL_MEDIA_RANGES;

		static {
			ALL_MEDIA_RANGES = new AllMediaRanges();
		}

		public static MediaRange create(final String mymetype) {
			if ("*/*".equals(mymetype)) {
				return ALL_MEDIA_RANGES;
			}
			if (mymetype.startsWith("*/")) {
				throw new IllegalArgumentException("'" + mymetype + "' is not a valid syntax.");
			}
			if (mymetype.endsWith("/*")) {
				return new AllSubtypesMediaRanges(mymetype);
			}
			return new MediaRange(mymetype);
		}

        protected MediaRange(final String mymetype) {
			int idx = mymetype.indexOf("/");
			if (idx == -1) {
				throw new IllegalArgumentException("'" + mymetype + "' is not a valid syntax.");
			}
			fType = mymetype.substring(0, idx + 1);
			fSubtype = mymetype.substring(idx);
		}

		public boolean match(final String contentType) {
			return matchType(contentType) && matchSubtype(contentType);
		}

		boolean matchSubtype(final String contentType) {
			return contentType.endsWith(fSubtype);
		}

		boolean matchType(final String contentType) {
			return contentType.startsWith(fType);
		}

		public String getType() {
			return fType;
		}

		public String getSubtype() {
			return fSubtype;
		}

		public String toString() {
			return fType + fSubtype.substring(1);
		}

		public int compareTo(final MediaRange other) {
			if (other instanceof AllMediaRanges) {
				return 1;
			}

			int typeCmp = getType().compareTo(other.getType());
			if (typeCmp != 0) {
				return typeCmp;
			}
			if (other instanceof AllSubtypesMediaRanges) {
				return 1;
			}
			return getSubtype().compareTo(other.getSubtype());
		}
	}

	static class AllMediaRanges extends MediaRange {

		AllMediaRanges() {
			super("*/*");
		}

		public boolean match(final String contentType) {
			return true;
		}

		public int compareTo(final MediaRange other) {
			if (other instanceof AllMediaRanges) {
				return 0;
			}
			return -1;
		}
	}

	static class AllSubtypesMediaRanges extends MediaRange {

		AllSubtypesMediaRanges(final String mimetype) {
			super(mimetype);
		}

		public boolean match(final String contentType) {
			return matchType(contentType);
		}

		public int compareTo(final MediaRange other) {
			if (other instanceof AllMediaRanges) {
				return 1;
			}
			int typeCmp = getType().compareTo(other.getType());
			if (typeCmp != 0) {
				return typeCmp;
			}
			if (other instanceof AllSubtypesMediaRanges) {
				return 0;
			}
			return -1;
		}
	}
}

