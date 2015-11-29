/*
 * jMemorize - Learning made easy (and fun) - A Leitner flashcards tool
 * Copyright(C) 2004-2008 Riad Djemili and contributors
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package jmemorize.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;

import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

public class FileRepository {
	private static final int MAX_CACHED_IMAGES = 10;

	public static final String IMG_ID_PREFIX = "::";

	private static FileRepository m_instance;

	private Map<String, FileItem> m_imageMap = new HashMap<String, FileItem>();
	private LinkedList<ImageIcon> m_imageCache = new LinkedList<ImageIcon>();

	private static final Pattern FILE_PATTERN = Pattern.compile("(.*)_(\\d+)");

	public static class FileItem {
		private String m_sourceFile;
		private byte[] m_bytes;
		private String m_id;
		private int type;
		// private String m_desc;

		public static final int TYPE_UNKNOWN = 0;
		public static final int TYPE_SOUND = 100;
		public static final int TYPE_IMAGE = 101;
		public static final int TYPE_TEXT = 102;

		public FileItem(InputStream in, String filename) throws IOException {
			m_sourceFile = filename;
			m_id = createId(filename);
			m_bytes = readFile(in);
			// setDescription(m_id);
		}

		public FileItem(String name, File image) throws IOException {
			m_sourceFile = image.getName();
			InputStream in = new FileInputStream(image);
			m_bytes = readFile(in);
			m_id = createId(m_sourceFile);
			// setDescription(m_id);
		}

		public int getFileType() {
			return type;
		}

		public ImageIcon getImage() {
			ImageIcon image = FileRepository.getInstance().getFromCache(getId());
			if (image == null) {
				image = new ImageIcon(m_bytes);
				image.setDescription(IMG_ID_PREFIX + m_id);
				FileRepository.getInstance().setToCache(image);
			}
			return image;
		}

		public String getText() {
			return new String(m_bytes, StandardCharsets.UTF_8);
		}

		public BasicPlayer getSound() {
			BasicPlayer clp = null;
			clp = new BasicPlayer();
			try {
				clp.open(m_bytes);
			} catch (BasicPlayerException e) {
				e.printStackTrace();
			}

			return clp;
		}

		public String getId() {
			return m_id;
		}

		public String getFile() {
			return m_sourceFile;
		}

		public byte[] getBytes() {
			return m_bytes;
		}

		@Override
		public String toString() {
			return m_id;
		}

		public int getType() {
			return type;
		}

		private String createId(String filename) {
			int dotPos = filename.lastIndexOf(".");
			String extension = filename.substring(dotPos);
			String purename = filename.substring(0, dotPos);

			while (FileRepository.getInstance().getKeys().contains(purename + extension)) {
				int num = 0;

				Matcher m = FILE_PATTERN.matcher(purename);
				if (m.matches() && m.groupCount() == 2) {
					num = Integer.valueOf(m.group(2));
					num++;

					purename = m.group(1);
				}

				purename = purename + "_" + num;
			}
			type = TYPE_UNKNOWN;
			if (extension.toLowerCase().endsWith("png") || extension.toLowerCase().endsWith("jpg")
					|| extension.toLowerCase().endsWith("bmp") || extension.toLowerCase().endsWith("gif")) {
				type = TYPE_IMAGE;
			}
			if (extension.toLowerCase().endsWith("mp3") || extension.toLowerCase().endsWith("wav")
					|| extension.toLowerCase().endsWith("au")) {
				type = TYPE_SOUND;
			}
			if (extension.toLowerCase().endsWith("txt")) {
				type = TYPE_TEXT;
			}
			return purename + extension;
		}

		private byte[] readFile(InputStream in) throws IOException {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

			byte[] bytes = new byte[1024];
			int numRead = 0;

			while ((numRead = in.read(bytes, 0, bytes.length)) >= 0) {
				bytesOut.write(bytes, 0, numRead);
			}

			return bytesOut.toByteArray();
		}

		// public String getDescription() {
		// return m_desc;
		// }
		//
		// public void setDescription(String string) {
		// m_desc = string;
		// }
	}

	// TODO remove singleton pattern and make this referenced from lesson

	public static FileRepository getInstance() {
		if (m_instance == null)
			m_instance = new FileRepository();

		return m_instance;
	}

	public void setToCache(ImageIcon icon) {
		m_imageCache.addFirst(icon);
		if (m_imageCache.size() > MAX_CACHED_IMAGES)
			m_imageCache.removeLast();
	}

	public ImageIcon getFromCache(String imageId) {
		for (ImageIcon icon : m_imageCache) {
			if (equals(icon, imageId)) {
				m_imageCache.remove(icon);
				m_imageCache.addFirst(icon);
				return icon;
			}
		}
		return null;
	}

	public Set<String> getKeys() {
		return m_imageMap.keySet();
	}

	public Collection<FileItem> getFileItems() // TODO dont give imageItem to
												// outside
	{
		return m_imageMap.values();
	}

	public FileItem getItem(String imageId) {
		FileItem imageItem = m_imageMap.get(imageId);
		return imageItem;
	}

	public String addImage(InputStream in, String filename) throws IOException {
		// TOOD check if image already in our map
		// for (ImageItem item : m_imageMap.values())
		// {
		// if (item.getFile().equals(filename))
		// return item.getId();
		// }

		FileItem item = new FileItem(in, filename);
		String id = item.getId();
		m_imageMap.put(id, item);

		return id;
	}

//	public String addImage(FileItem icon) throws IOException {
//		// String description = icon.getDescription();
//
//		String id = icon.getId();
//		InputStream in;
//		String name = "";
//
//		try {
//			URL url = new URL(description);
//			name = new File(url.getPath()).getName();
//			in = url.openStream();
//		} catch (MalformedURLException ex) {
//			name = new File(description).getName();
//			in = new FileInputStream(description);
//
//			// fallthrough expected
//		}
//
//		id = addImage(in, name);
//		// icon.setDescription(IMG_ID_PREFIX + id);
//
//		return id;
//	}

	/**
	 * Converts the given list of image icons into a list of image IDs. This is
	 * done by using the description field of ImageIcon. If the image icon was
	 * already loaded from the image repository, the description will begin with
	 * IMG_ID_PREFIX, otherwise it will be a new image that wasn't added to the
	 * repository yet.
	 */
	public List<String> addImages(List<FileItem> images) {
		List<String> imageIDs = new LinkedList<String>();
		for (FileItem icon : images) {
			// imageIDs.add(addImage(icon));
			String id = icon.getId();
			m_imageMap.put(id, icon);
			imageIDs.add(id);
		}

		return imageIDs;
	}

	/**
	 * Retains all images with given IDs. All other images are removed.
	 */
	public void retain(Set<String> retainIDs) {
		Set<String> toBeRemoved = new HashSet<String>(m_imageMap.keySet());

		for (String id : retainIDs)
			toBeRemoved.remove(id);

		for (String id : toBeRemoved)
			m_imageMap.remove(id);
	}

	public static boolean equals(ImageIcon image, String id) {
		String description = image.getDescription();
		return (description.startsWith(IMG_ID_PREFIX) && description.substring(IMG_ID_PREFIX.length()).equals(id));
	}

	public static boolean equals(List<FileItem> images, List<String> ids) {
		if (images.size() != ids.size())
			return false;

		for (FileItem icon : images) {
			String id = icon.getId();
			// String description = icon.getDescription();

			// if (description.startsWith(IMG_ID_PREFIX)) {
			// id = description.substring(IMG_ID_PREFIX.length());

			if (!ids.contains(id))
				return false;
			// } else {
			// return false;
			// }
		}

		return true;
	}

	public List<FileItem> toFileItems(List<String> ids) {
		List<FileItem> images = new LinkedList<FileItem>();

		if (ids == null)
			return images;

		for (String id : ids) {
			FileItem image = getItem(id);
			if (image != null)
				images.add(image);
		}

		return images;
	}

	public void clear() {
		m_imageMap.clear();
	}

	private FileRepository() // singleton
	{
	}
}
