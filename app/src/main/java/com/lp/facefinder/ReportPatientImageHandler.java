package com.lp.facefinder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Informational Notice:
 *
 * This software, the ”TBD,” was developed under contract funded by the National Library of Medicine, which is part of the National Institutes of Health, an agency of the Department of Health and Human Services, United States Government.
 *
 * The license of this software is an open-source BSD-like license.  It allows use in both commercial and non-commercial products.
 *
 * The license does not supersede any applicable United States law.
 *
 * The license does not indemnify you from any claims brought by third parties whose proprietary rights may be infringed by your usage of this software.
 *
 * Government usage rights for this software are established by Federal law, which includes, but may not be limited to, Federal Acquisition Regulation (FAR) 48 C.F.R. Part 52.227-14, Rights in Data—General.
 * The license for this software is intended to be expansive, rather than restrictive, in encouraging the use of this software in both commercial and non-commercial products.
 *
 * LICENSE:
 *
 * Government Usage Rights Notice:  The U.S. Government retains unlimited, royalty-free usage rights to this software, but not ownership, as provided by Federal law.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above Government Usage Rights Notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above Government Usage Rights Notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *  The names, trademarks, and service marks of the National Library of Medicine, the National Institutes of Health, and the names of any of the software developers shall not be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE U.S. GOVERNMENT AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITEDTO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE U.S. GOVERNMENT
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ReportPatientImageHandler {
	// identifier for launching camera and getting result.
	public static int IMAGE_CAPTURE = 1313;
	public static int IMAGE_FIND_EX = 1414;
	public static int IMAGE_PRIV_GAL = 1515;
	public static int CAMERA = 1516;
	public static int FACES = 1517;
	public static int GALLERY = 1518;
	public static int FACES1 = 1519;
	public static int FACES2 = 1520;
	public static int ALLLIST2 = 1521;
	private Uri imageUri;
	private Context c;

	ReportPatientImageHandler(Context c) {
		this.c = c;
	}

	/**
	 * prepare an inten to start the camera
	 * 
	 * @return
	 */
	public Intent startCamera() {
		// Get a unique file name for uri.
		// Prepare information for uri.
		ContentValues values = new ContentValues();
		values.put(ImageColumns.IS_PRIVATE, 1);
		values.put(MediaColumns.TITLE, System.currentTimeMillis());
		values.put(ImageColumns.DESCRIPTION, "Image capture by camera");
		values.put(MediaColumns.MIME_TYPE, "image/jpeg");
		this.imageUri = c.getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		// Prepare intent.
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, this.imageUri);
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		return intent;
	}

	/**
	 * Takes the onActivityResult parameters of the ReportPatient class and uses
	 * them to resolve the either selected or captured image. Images are first
	 * resized to an appropriate size. After resizing the images, if the image
	 * was captured from the camera any instances of the image are removed from
	 * the sd card. Then the image is stored privately on the app's memory.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return Image image uri, size and digest
	 */
	protected Image onActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (resultCode == Activity.RESULT_CANCELED)
			Toast.makeText(c, "Add Photo cancelled.", Toast.LENGTH_LONG).show();
		if (requestCode == IMAGE_FIND_EX || requestCode == IMAGE_PRIV_GAL)
			this.imageUri = data.getData();
		String uri = this.imageUri.toString();
		String size = getSize(uri, c);
		String digest = getDigest(uri, c);
		if (requestCode == IMAGE_CAPTURE)
			uri = save(uri);

		return new Image(uri, digest, size, null);
	}

	/**
	 * obtain the image digest
	 * 
	 * @param uri
	 * @param c
	 * @return
	 */
	protected static String getDigest(String uri, Context c) {
		Bitmap picture = resizedBitmap(uri, 1000, 1000, c, false);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] arr = stream.toByteArray();
		picture.recycle();

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		byte[] res = md.digest(arr);

		StringBuilder sb = new StringBuilder();
		for (byte b : res)
			sb.append(String.format("%02X", b));
		System.out.println(sb);
		return sb.toString();
	}

	/**
	 * calculate the size of the image
	 * 
	 * @param path
	 * @param c
	 * @return
	 */
	protected static String getSize(String path, Context c) {
		InputStream in = null;
		try {
			if (path.contains("content"))
				in = c.getContentResolver().openInputStream(Uri.parse(path));
			else
				in = new FileInputStream(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		/* Decode image size */
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, o);
		int size = o.outWidth * o.outHeight;
		return String.valueOf(size);
	}

	/**
	 * This takes an image's bitmap and returns its absolute path as a string.
	 * 
	 * @throws IOException
	 */
	public String save(String uri) {

		File dir = c.getDir(Environment.DIRECTORY_PICTURES,
				Context.MODE_PRIVATE);
		File internalFile = new File(dir, System.currentTimeMillis() + ".jpg");

		InputStream in;
		try {
			in = c.getContentResolver().openInputStream(Uri.parse(uri));
			OutputStream out = new FileOutputStream(internalFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
				out.write(buffer, 0, read);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			c.getContentResolver().delete(Uri.parse(uri), null, null);
			return internalFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uri;
	}

	/**
	 * calculate an images scale size. If for some reason I enter higher than
	 * 500, enfore the max size as 500*500 pixels since few devices will ever
	 * need more than that for this
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				if (reqHeight > 500)
					reqHeight = 500;
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				if (reqWidth > 500)
					reqWidth = 500;
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	/**
	 * parses the given string into a uri and then decodes the image at this
	 * uri. If the image is to be used for face detection, the image is created
	 * with the limit Config.RGB_565 per requisite of android FaceDetector
	 * class.
	 * 
	 * @param path
	 *            String: location in memory of image
	 * @param reqWidth
	 *            int: max width of image
	 * @param reqHeight
	 *            int: max height of image
	 * @param c
	 *            context: to access images
	 * @param face
	 *            boolean: image is for detecting faces
	 * @return the desired bitmap
	 */
	public static Bitmap resizedBitmap(String path, int reqWidth,
			int reqHeight, Context c, boolean face) {
		Uri uri;
		ContentResolver mContentResolver = c.getContentResolver();
		if (!path.contains("content:"))
			uri = Uri.parse("file://" + path);
		else
			uri = Uri.parse(path);

		try {
			InputStream in;
			in = mContentResolver.openInputStream(uri);
			// First decode with inJustDecodeBounds=true to check dimensions
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			in.close();

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth,
					reqHeight);
			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			if (face) {
				options.inPreferredConfig = Config.RGB_565;
			}
			in = mContentResolver.openInputStream(uri);
			Bitmap b = BitmapFactory.decodeStream(in, null, options);
			if (face && b.getWidth() % 2 != 0) {
				b = Bitmap.createBitmap(b, 0, 0, b.getWidth() - 1,
						b.getHeight());
			}

			return b;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
