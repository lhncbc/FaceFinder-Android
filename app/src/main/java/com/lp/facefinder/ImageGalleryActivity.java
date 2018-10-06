package com.lp.facefinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

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

public class ImageGalleryActivity extends Activity implements OnItemClickListener {
	private static String FACES_PATH = "/data/data/com.pl.facefinder/app_Pictures";

	private GridView gridGallery;
	private ArrayList<Image> images;
	private Bitmap[] bitmaps;
	private ImageAdapter adapter;
	
	private String localPath;
	
	/**
	 * Obtains all app Image files and sets them in an ArrayAdapter for this
	 * page, While a background thread loads images refreshing the adapter when
	 * it loads an image
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		
        Intent sender = getIntent();
	    int source = sender.getExtras().getInt("SOURCE");
	    if (source == ReportPatientImageHandler.CAMERA){
			localPath = "/FaceFinder/";
	    }
	    else if (source == ReportPatientImageHandler.FACES){
	    	localPath = "/512list";
	    }
	    else if (source == ReportPatientImageHandler.ALLLIST2){
	    	localPath = "/alllist";
	    }
	    else {
	    	localPath = "";
	    }

		gridGallery = (GridView) findViewById(R.id.gallery_grid);

		File rootsd = Environment.getExternalStorageDirectory();
		File dir = new File(rootsd.getAbsolutePath() + localPath);

		if (dir.isDirectory() == false){
			Toast.makeText(this, "There is no directory: " + Environment.DIRECTORY_PICTURES + localPath, Toast.LENGTH_SHORT).show();
			Log.e("Error", "No such directory: " + Environment.DIRECTORY_PICTURES + localPath);
			this.finish();
			return;
		}
		
		if (dir.listFiles().length == 0){
			Toast.makeText(this, "No file is found in " + Environment.DIRECTORY_PICTURES + localPath, Toast.LENGTH_SHORT).show();
			Log.e("Error", "No file is found in " + Environment.DIRECTORY_PICTURES + localPath);
			this.finish();
			return;
		}

		images = new ArrayList<Image>();

		for (File f : dir.listFiles()){
			if (f.isDirectory()){
				continue;
			}
			Image img = new Image(f.getAbsolutePath(), null, null, null); 
			if (img.uri.endsWith("txt")){
				continue;
			}
			if (img.uri.endsWith("xml")){
				continue;
			}
			if (img.uri.endsWith("tmp")){
				continue;
			}
			images.add(img);
		}
		
		// Add sort
		Log.e("Status", "Start to sort... ");
		for (int i = 0; i < images.size() - 1; i++){
			Image imgI = images.get(i);
			for (int j = i + 1; j < images.size(); j++){
				Image imgJ = images.get(j); 
				if (imgI.getFileName().compareTo(imgJ.getFileName()) > 0){
					Image tmp = new Image();
					tmp.Equals(imgI);
					imgI.Equals(imgJ);
					imgJ.Equals(tmp);
				}
			}
		}
		Log.e("Status", "End of sorting!");
		//
		
		bitmaps = new Bitmap[images.size()];

		/**
		 * display all the images found in current directory.
		 */
		adapter = new ImageAdapter();

		gridGallery.setAdapter(adapter);
		gridGallery.setOnItemClickListener(this);
		new Thread() {
			public void run() {
				for (int i = 0; i < bitmaps.length; i++) {
					String fullName = images.get(i).uri;
					bitmaps[i] = ReportPatientImageHandler.resizedBitmap(fullName, 400, 400, ImageGalleryActivity.this, true);
					ImageGalleryActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							ImageGalleryActivity.this.adapter.notifyDataSetChanged();
						}
					});
				}
			}
		}.start();
	}

	/**
	 * selects an image and returns its uri to the ReportPatient activity
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent(ImageGalleryActivity.this, FaceDetectionActivity.class);
		i.putExtra("URL", images.get(position).uri);    			
		startActivity(i);	
	}

	public class ImageAdapter extends BaseAdapter {
		/**
		 * If the bitmap has loaded it is used, if not the defualt image is used
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			System.gc();

			int width = parent.getWidth() / gridGallery.getNumColumns();
			imageView = new ImageView(parent.getContext());
			imageView.setLayoutParams(new AbsListView.LayoutParams(width, width));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			if (bitmaps[position] != null) {
				imageView.setImageBitmap(bitmaps[position]);
			}
			else {
				imageView.setImageDrawable(ImageGalleryActivity.this.getResources()
						.getDrawable(R.drawable.questionhead));
			}
			return imageView;
		}

		public int getCount() {
			return bitmaps.length;
		}

		public Object getItem(int position) {
			return bitmaps[position];
		}

		public long getItemId(int position) {
			return position;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.itemFindFace:
        	FindFace();
        	break;
        case R.id.menu_raw:
        	OutputRaw();
        	break;
        case R.id.menu_xml:
        	OutputXml();
        	break;
        case R.id.menu_txt:
        	OutputTxt();
        	break;
			case R.id.menu_delete_images:
				deleteImages();
				break;
        case R.id.menu_statistics:
        	Statistics();
        case R.id.menu_settings:
        	Toast.makeText(this, "Settings is not completed.", Toast.LENGTH_SHORT).show();
        	break;
        case R.id.menu_help:
        	Toast.makeText(this, "Help is not completed.", Toast.LENGTH_SHORT).show();
           	break;
        }
        return true;
	}

	private void deleteImages() {
		if (!images.isEmpty()){
		}
	}

	private void Statistics() {
		int totalFaces = 0;
		for (int i = 0; i < images.size(); i++){
			Image img = images.get(i);
			totalFaces += img.getNumberOfFacesDetected();
		}
    	Toast.makeText(this, String.valueOf(totalFaces) + " faces detected in " + String.valueOf(images.size() + " images."), Toast.LENGTH_SHORT).show();
	}

	final String prefix = "2003/01/17/big/";
	final String prefixF = "2003.01.17.big";

	private void OutputTxt() {
		String outputStr = new String("");
		for (int i = 0; i < images.size(); i++){
			Image img = images.get(i);
			outputStr += prefix + img.getImageTxt() + "\n";
		}
		
		File rootsd = Environment.getExternalStorageDirectory();
		File dir = new File(rootsd.getAbsolutePath() + localPath);
		File internalFile = new File(dir, prefixF + ".txt");

		InputStream in;
		try {
			FileOutputStream fileOutStream = new FileOutputStream(internalFile);
			OutputStreamWriter out = new OutputStreamWriter(fileOutStream);
			out.write(outputStr);
			out.flush();
			out.close();
			fileOutStream.flush();
			fileOutStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("Now", prefixF + " is completed.");
    	Toast.makeText(this, internalFile.getName() + " is completed.", Toast.LENGTH_SHORT).show();		
	}

	private void OutputRaw() {
		String outputStr = new String("#Android face detection raw data\n#ImageFN\tconf\tiDist\tmX\tmY\trX\trY\trZ\n");
		for (int i = 0; i < images.size(); i++){
			Image img = images.get(i);
			outputStr += img.getImageRawTxt() + "\n";
		}
		
		File rootsd = Environment.getExternalStorageDirectory();
		File dir = new File(rootsd.getAbsolutePath() + localPath);
		File internalFile = new File(dir, System.currentTimeMillis() + "_raw.txt");

		InputStream in;
		try {
			FileOutputStream fileOutStream = new FileOutputStream(internalFile);
			OutputStreamWriter out = new OutputStreamWriter(fileOutStream);
			out.write(outputStr);
			out.flush();
			out.close();
			fileOutStream.flush();
			fileOutStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Toast.makeText(this, internalFile.getName() + " is completed.", Toast.LENGTH_SHORT).show();		
	}

	private void OutputXml() {
		String outputStr = new String("");
		for (int i = 0; i < images.size(); i++){
			Image img = images.get(i);
			outputStr += img.getImageXml();
		}
		
		File rootsd = Environment.getExternalStorageDirectory();
		File dir = new File(rootsd.getAbsolutePath() + localPath);
		File internalFile = new File(dir, System.currentTimeMillis() + "_xml.txt");

		InputStream in;
		try {
			FileOutputStream fileOutStream = new FileOutputStream(internalFile);
			OutputStreamWriter out = new OutputStreamWriter(fileOutStream);
			out.write(outputStr);
			out.flush();
			out.close();
			fileOutStream.flush();
			fileOutStream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	Toast.makeText(this, internalFile.getName() + " is completed.", Toast.LENGTH_SHORT).show();		
	}

	private void FindFace() {
		for (int i = 0; i < images.size(); i++){
			Image img = images.get(i);
			img.createBitmap();
			img.DetectFaces();
			img.WriteRawText();
			img.WriteTxt();
			Log.i("Now: ", "image: " + String.valueOf(i));
	    	Toast.makeText(this, "Find face: " + img.getFileName(), Toast.LENGTH_SHORT).show();
		}	
    	Toast.makeText(this, "Finding face is completed.", Toast.LENGTH_SHORT).show();		
	}	
}
