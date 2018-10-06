package com.lp.facefinder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class FaceDetectionActivity extends Activity implements View.OnClickListener {
	
	final static String tagImageS = "<image>";
	final static String tagImageE = "</image>";
	final static String tagNumberOfFacesS = "<faceFN>";
	final static String tagNumberOfFacesE = "</faceFN>";
	final static String tagFaceS = "<face>";
	final static String tagFaceE = "</face>";
	final static String tagMiddlePointS = "<midPoint>";
	final static String tagMiddlePointE = "</midPoint>";
	final static String tagXS = "<x>";
	final static String tagXE = "</x>";
	final static String tagYS = "<y>";
	final static String tagYE = "</y>";
	final static String tagZS = "<z>";
	final static String tagZE = "</z>";
	final static String tagPoseS = "<pose>";
	final static String tagPoseE = "</pose>";

	String url;
	ImageView faceImage;
	TextView tvNumber;
	TextView tvDetails;
	String strDetails;
	
	Bitmap myBitmap;
	int width, height;
	FaceDetector.Face[] detectedFaces;
	int NUMBER_OF_FACES = 10; 
  	FaceDetector faceDetector;
	int NUMBER_OF_FACE_DETECTED;
	float eyeDistance;
	
	public Canvas canvas;
	public Bitmap bmpOri;
	public Bitmap bmpDrawed;
	
	Image image;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_detection);
        
        Initialize();    

//        setContentView(new MyView(this));
        
    }
       
    private void Initialize() {
    	faceImage = (ImageView) findViewById(R.id.imageFace);
    	faceImage.setOnClickListener(this);

    	tvNumber = (TextView) findViewById(R.id.textViewNumber);
    	tvDetails = (TextView) findViewById(R.id.textViewDetails);
    	strDetails = new String("");    	

		/*
    	buFindFace.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Detect faces
            	DetectFaces();
        		tvNumber.setText("Number of Faces: " + String.valueOf(NUMBER_OF_FACE_DETECTED));
        		if (NUMBER_OF_FACE_DETECTED == 0){
        			return;
        		}
        		// Draw the 
//            	DrawFaceBindingBoxes();
            }
        });
        */


    	Intent sender = getIntent();
	    url = sender.getExtras().getString("URL");	

	    if (url.isEmpty() == true){
			Toast.makeText(this, "Image file name is not defined.", Toast.LENGTH_SHORT).show();
	    	this.finish();
	    	return;
	    }
	    
	    image = new Image(url, null, null, null);
	    
		BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
		bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		bmpOri = BitmapFactory.decodeFile(url, bitmapFatoryOptions);
		faceImage.setImageBitmap(bmpOri);
    }
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageFace:
//			DetectFaces();
//			WriteDetectedFaces();
			image.createBitmap();
			image.DetectFaces();
			image.WriteRawText();
			image.WriteTxt();

			/**
			 * enable the following 2 lines for draw the face frames
			 */
			if (image.getNumberOfFacesDetected() > 0) {
				image.createBitmapWithBoundingBox();
				faceImage.setImageDrawable(new BitmapDrawable(getResources(), image.getBitmapWithBoundingBox()));
			}

			tvNumber.setText(image.getImageTxt() + "\n" + image.getImageXml());
//			WriteDetectedFacesXML();
//        	DrawFaceBindingBoxes();
			break;
		default:
			break;
		}
	}
    

	private void WriteDetectedFacesXML() {
		tvNumber.setText(String.valueOf(image.getNumberOfFacesDetected()) + " face(s) found.");
		if (image.getNumberOfFacesDetected() == 0){
			return;
		}			
		
		// Get the data
		strDetails = "";
		for (int i = 0; i < image.getNumberOfFacesDetected(); i++){
			String eachFace = new String("Face " + String.valueOf(i + 1) + ". ");
//			Face f = detectedFaces[i];
			Face f = image.getFaces()[i];
			float d = f.eyesDistance();
			eachFace = eachFace + "eye d: " + String.valueOf(d) + "; ";
			PointF p = new PointF();
			f.getMidPoint(p);
			eachFace = eachFace + "mid x: " + String.valueOf(p.x) + "; ";
			eachFace = eachFace + "mid y: " + String.valueOf(p.y) + "; ";
			float c = f.confidence();
			eachFace = eachFace + "conf: " + String.valueOf(c);
			strDetails = strDetails + eachFace + "\n";
		}
		tvDetails.setText(strDetails);
	}

	protected void DrawFaceBindingBoxes() {
		bmpDrawed = Bitmap.createBitmap(bmpOri, 1, 1, bmpOri.getWidth()-1, bmpOri.getHeight()-1);
//		bmpDrawed = Bitmap.createBitmap(bmpOri);

		canvas = new Canvas(bmpDrawed);
		DrawCanvas(bmpDrawed);
		faceImage.setImageDrawable(new BitmapDrawable(getResources(), bmpDrawed));		
	}

	protected void DetectFaces() {
		detectedFaces = new FaceDetector.Face[NUMBER_OF_FACES];
		faceDetector = new FaceDetector(bmpOri.getWidth(), bmpOri.getHeight(), NUMBER_OF_FACES);
		NUMBER_OF_FACE_DETECTED = faceDetector.findFaces(bmpOri, detectedFaces);
		
		image.setNumberOfFacesDetected(NUMBER_OF_FACE_DETECTED);
		image.setFaces(detectedFaces);
	}
	
	protected void WriteDetectedFaces(){
		tvNumber.setText(String.valueOf(image.getNumberOfFacesDetected()) + " face(s) found.");
		if (image.getNumberOfFacesDetected() == 0){
			return;
		}			
		
		// Get the data
		strDetails = "";
		for (int i = 0; i < image.getNumberOfFacesDetected(); i++){
			String eachFace = new String("Face " + String.valueOf(i + 1) + ". ");
//			Face f = detectedFaces[i];
			Face f = image.getFaces()[i];
			float d = f.eyesDistance();
			eachFace = eachFace + "eye d: " + String.valueOf(d) + "; ";
			PointF p = new PointF();
			f.getMidPoint(p);
			eachFace = eachFace + "mid x: " + String.valueOf(p.x) + "; ";
			eachFace = eachFace + "mid y: " + String.valueOf(p.y) + "; ";
			float c = f.confidence();
			eachFace = eachFace + "conf: " + String.valueOf(c);
			strDetails = strDetails + eachFace + "\n";
		}
		tvDetails.setText(strDetails);
	}

	private void DrawCanvas(Bitmap bmp) {
		canvas.drawBitmap(bmp, 0,0, null);
		Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE); 
        myPaint.setStrokeWidth(3);

        for(int count=0;count<NUMBER_OF_FACE_DETECTED;count++)
        {
        	Face face = detectedFaces[count];
        	PointF midPoint=new PointF();
        	face.getMidPoint(midPoint);
        	
        	eyeDistance=face.eyesDistance();
        	
        	float a = (float) 2.0;
        	float b = (float) 2.0;
        	
        	PointF startPoint = new PointF();
        	startPoint.x = midPoint.x - eyeDistance;
        	startPoint.y = midPoint.y - eyeDistance;
        	
        	float w = a * eyeDistance; 
        	float h = b * eyeDistance; 
        	
        	PointF endPoint = new PointF();
        	endPoint.x = startPoint.x + w;
        	endPoint.y = startPoint.y + h;

        	canvas.drawRect(
        			startPoint.x, 
        			startPoint.y, 
        			endPoint.x, 
        			endPoint.y,
        			myPaint
        			);
        }
	}

	private class MyView extends View
    {
    	private Bitmap myBitmap;
    	private int width, height;
    	private FaceDetector.Face[] detectedFaces;
    	private int NUMBER_OF_FACES=4;
    	private FaceDetector faceDetector;
    	private int NUMBER_OF_FACE_DETECTED;
    	private float eyeDistance;
    	
		public MyView(Context context) 
		{
			super(context);
			BitmapFactory.Options bitmapFatoryOptions=new BitmapFactory.Options();
			bitmapFatoryOptions.inPreferredConfig=Bitmap.Config.RGB_565;
//			bitmapFatoryOptions.inSampleSize = 1;
//			myBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.faceswapping,bitmapFatoryOptions);
//			myBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.face4,bitmapFatoryOptions);
//			myBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.faces,bitmapFatoryOptions);
//			myBitmap=BitmapFactory.decodeResource(getResources(), R.drawable.two_persons,bitmapFatoryOptions);
			
			myBitmap = BitmapFactory.decodeFile(url, bitmapFatoryOptions);
			 
			width=myBitmap.getWidth();
			height=myBitmap.getHeight();
			detectedFaces=new FaceDetector.Face[NUMBER_OF_FACES];
			faceDetector=new FaceDetector(width,height,NUMBER_OF_FACES);
			NUMBER_OF_FACE_DETECTED=faceDetector.findFaces(myBitmap, detectedFaces);
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawBitmap(myBitmap, 0,0, null);
			Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE); 
            myPaint.setStrokeWidth(3);

            for(int count=0;count<NUMBER_OF_FACE_DETECTED;count++)
            {
            	Face face = detectedFaces[count];            	
            	PointF midPoint=new PointF();
            	face.getMidPoint(midPoint);
            	
            	eyeDistance = face.eyesDistance();
            	canvas.drawRect(midPoint.x-eyeDistance, midPoint.y-eyeDistance, midPoint.x+eyeDistance, midPoint.y+eyeDistance, myPaint);
            }
		}
    	
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		/*
		outState.putInt("NUMBER_OF_FACE_DETECTED", NUMBER_OF_FACE_DETECTED);
		for(int i = 0; i < NUMBER_OF_FACE_DETECTED; i++){
        	Face face = detectedFaces[i];            	
        	PointF midPoint=new PointF();
        	face.getMidPoint(midPoint);        	
        	eyeDistance=face.eyesDistance();
        	
			outState.putFloat("x" + String.valueOf(i), midPoint.x);
			outState.putFloat("y" + String.valueOf(i), midPoint.y);
			outState.putFloat("d" + String.valueOf(i), eyeDistance);
		}
		*/
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		/*
		NUMBER_OF_FACE_DETECTED = savedInstanceState.getInt("NUMBER_OF_FACE_DETECTED");
		
		if (NUMBER_OF_FACE_DETECTED == 0){
			return;
		}
		
		canvas = new Canvas(bmpDrawed);
		canvas.drawBitmap(bmpDrawed, 0,0, null);
		Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE); 
        myPaint.setStrokeWidth(3);

        for(int i = 0; i < NUMBER_OF_FACE_DETECTED; i++){
			PointF midPoint = null;
			midPoint.x = savedInstanceState.getFloat("x" + String.valueOf(i));
			midPoint.y = savedInstanceState.getFloat("y" + String.valueOf(i));
			eyeDistance = savedInstanceState.getFloat("d" + String.valueOf(i));
        	canvas.drawRect(midPoint.x-eyeDistance, midPoint.y-eyeDistance, midPoint.x+eyeDistance, midPoint.y+eyeDistance, myPaint);
		}
		faceImage.setImageDrawable(new BitmapDrawable(getResources(), bmpDrawed));
*/
		super.onRestoreInstanceState(savedInstanceState);
	}
}