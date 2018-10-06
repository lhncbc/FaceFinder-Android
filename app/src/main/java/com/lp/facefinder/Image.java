package com.lp.facefinder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

public class Image {
	private static int MAX_NUMBER_OF_FACES_TO_DETECT = 10; 
	private static double PIForth = Math.PI / 4.0;
	private Bitmap bitmap;
	
	public String caption, uri, digest, size;
	
	public Image(){
		this.bitmap = null;
		this.caption = "";
		this.uri = "";
		this.digest = "";
		this.size = "";
		this.bitmapWithBoundingBox = null;
		this.fileName = "";
		this.imageXml = "";
		this.imageTxt = "";
		this.imageRawTxt = "";
		this.numberOfFacesDetected = 0;
		this.faces = null;
	}
	
	public void Equals(Image img){
		this.bitmap = img.bitmap;
		this.caption = img.caption;
		this.uri = img.uri;
		this.digest = img.digest;
		this.size = img.size;
		this.bitmapWithBoundingBox = img.getBitmapWithBoundingBox();
		this.fileName = img.getFileName();
		this.imageXml = img.getImageXml();
		this.imageTxt = img.getImageTxt();
		this.imageRawTxt = img.getImageRawTxt();
		this.numberOfFacesDetected = img.getNumberOfFacesDetected();
		this.faces = img.faces;
	}
	
	private Bitmap bitmapWithBoundingBox;
	public void setBitmapWithBoundingBox(Bitmap bitmapWithBoundingBox){
		this.bitmapWithBoundingBox = bitmapWithBoundingBox;
	}
	public Bitmap getBitmapWithBoundingBox(){
		return this.bitmapWithBoundingBox;
	}

	private String fileName;
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	public String getFileName(){
		return this.fileName;
	}
	
	private String imageXml;
	public void setImageXml(String imageXml){
		this.imageXml = imageXml;
	}
	public String getImageXml() {
		return this.imageXml;
	}
	
	private String imageTxt;
	public void setImageTxt(String imageTxt){
		this.imageTxt = imageTxt;
	}
	public String getImageTxt() {
		return this.imageTxt;
	}

	private String imageRawTxt;
	public void setImageRawTxt(String imageRawTxt){
		this.imageRawTxt = imageRawTxt;
	}
	public String getImageRawTxt() {
		return this.imageRawTxt;
	}

	private int numberOfFacesDetected;
	public void setNumberOfFacesDetected(int numberOfFacesDetected){
		this.numberOfFacesDetected = numberOfFacesDetected;
	}
	public int getNumberOfFacesDetected() {
		return this.numberOfFacesDetected;
	}
	
	private FaceDetector.Face[] faces;
	public void setFaces(FaceDetector.Face[] faces){		
		this.faces = faces;
	}
	public FaceDetector.Face[] getFaces(){
		return this.faces;
	}
	
	public void createBitmap(){
		if (uri.isEmpty() == true){
			return;
		}
		BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
		bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		bitmap = null;
		bitmap = BitmapFactory.decodeFile(uri, bitmapFatoryOptions);
		Log.e("Create bitmap: ", uri);
	}
	
	public void DetectFaces() {
		System.gc();
		faces = new FaceDetector.Face[MAX_NUMBER_OF_FACES_TO_DETECT];
        FaceDetector fd = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), MAX_NUMBER_OF_FACES_TO_DETECT);

		numberOfFacesDetected = fd.findFaces(bitmap, faces);
		Log.e("Detectd faces: ", String.valueOf(numberOfFacesDetected));
	}
	
	public void createBitmapWithBoundingBox(){
		bitmapWithBoundingBox = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth()-1, bitmap.getHeight()-1);
		DrawCanvas(bitmapWithBoundingBox);	
	}
	
	private void DrawCanvas(Bitmap bmp) {
		Canvas canvas = new Canvas(bitmapWithBoundingBox);
		canvas.drawBitmap(bmp, 0,0, null);
		Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE); 
        myPaint.setStrokeWidth(3);

        for(int i = 0; i < numberOfFacesDetected; i++)
        {
        	Face f = faces[i];
        	Rect rect = FaceToRect(f);
        	canvas.drawRect(
        			rect.x, 
        			rect.y, 
        			rect.x + rect.w, 
        			rect.y + rect.h,
        			myPaint
        			);
        }
	}
		
	public void WriteXml(){
	    // build request object 
        try {
            //Creating an personXML Document
            //We need a Document
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            //Creating the XML tree            
            //create the root element and add it to the document
            Node image = doc.createElement("image");
            doc.appendChild(image);

            // file name
            Node fileNameNode = doc.createElement("fileName");
            fileNameNode.setTextContent(fileName);
            image.appendChild(fileNameNode);

            // Node face number
            Node faceNumber = doc.createElement("faceNumber");
            faceNumber.setTextContent(String.valueOf(numberOfFacesDetected));
            image.appendChild(faceNumber);
            
            for (int i = 0; i < numberOfFacesDetected; i++){
            	Face curFace = faces[i];
            	float curConfidence = curFace.confidence();
            	float curEyeDistance = curFace.eyesDistance();
                PointF curMidPoint = new PointF();
                curFace.getMidPoint(curMidPoint);
                int curPose = 0;
                curFace.pose(curPose);
                
                // Node face
                Node faceNode = doc.createElement("face");
                
                // confidence
                Node confidenceNode = doc.createElement("confidence");
                confidenceNode.setTextContent(String.valueOf(curConfidence));
                faceNode.appendChild(confidenceNode);
                
                // eye distance
                Node eyeDistanceNode = doc.createElement("eyeDistance");
                eyeDistanceNode.setTextContent(String.valueOf(curEyeDistance));
                faceNode.appendChild(eyeDistanceNode);
                
                // Start midpoint
                Node midPointNode = doc.createElement("midPoint");
                
                // Node x
                Node xNode = doc.createElement("x");
                xNode.setTextContent(String.valueOf(curMidPoint.x));
                midPointNode.appendChild(xNode);
                
                // Node y
                Node yNode = doc.createElement("y");
                yNode.setTextContent(String.valueOf(curMidPoint.y));
                midPointNode.appendChild(yNode);

                faceNode.appendChild(midPointNode);
                // end midpoint
                
                // pose
                Node poseNode = doc.createElement("pose");
                
                // Node nodeposeEulerX
                Node nodeEulerX = doc.createElement("eulerX");
                nodeEulerX.setTextContent(String.valueOf(curFace.pose(FaceDetector.Face.EULER_X)));
                poseNode.appendChild(nodeEulerX);
                
                // Node nodeposeEulerY
                Node nodeEulerY = doc.createElement("eulerY");
                nodeEulerY.setTextContent(String.valueOf(curFace.pose(FaceDetector.Face.EULER_Y)));
                poseNode.appendChild(nodeEulerY);
                
                // Node nodeposeEulerZ
                Node nodeEulerZ = doc.createElement("eulerZ");
                nodeEulerZ.setTextContent(String.valueOf(curFace.pose(FaceDetector.Face.EULER_Z)));
                poseNode.appendChild(nodeEulerZ);
                
                faceNode.appendChild(poseNode);                
                // end of pose
                
                image.appendChild(faceNode);
                // end of face            	
            }
      
            //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            //create string from xml tree
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            imageXml = sw.toString();
            
        } catch (Exception e) {
            System.out.println(e);
        }
	}
	
	public void WriteRawText(){
	    // build request object 
        try {
        	if (numberOfFacesDetected == 0){
            	imageRawTxt = fileName;       	        	
        	}
        	else {
            	imageRawTxt = fileName + "\t";       	
        	}
            for (int i = 0; i < numberOfFacesDetected; i++){
            	Face curFace = faces[i];
            	float curConf = curFace.confidence();
                float eyeDis = curFace.eyesDistance();
                PointF curMidPoint = new PointF();
                curFace.getMidPoint(curMidPoint);
                float curPoseX = curFace.pose(FaceDetector.Face.EULER_X);
                float curPoseY = curFace.pose(FaceDetector.Face.EULER_Y);
                float curPoseZ = curFace.pose(FaceDetector.Face.EULER_Z);
                
                if (i == numberOfFacesDetected - 1){
                	imageRawTxt += String.valueOf(curConf) + "\t" + 
           					String.valueOf(eyeDis) + "\t" + 
           					String.valueOf(curMidPoint.x) + "\t" + 
           					String.valueOf(curMidPoint.y) + "\t" +
           					String.valueOf(curPoseX) + "\t" +
           					String.valueOf(curPoseY) + "\t" +
           					String.valueOf(curPoseZ); 
                }
                else {
                	imageRawTxt += String.valueOf(curConf) + "\t" + 
           					String.valueOf(eyeDis) + "\t" + 
           					String.valueOf(curMidPoint.x) + "\t" + 
           					String.valueOf(curMidPoint.y) + "\t" +
           					String.valueOf(curPoseX) + "\t" +
           					String.valueOf(curPoseY) + "\t" +
           					String.valueOf(curPoseZ) + "\t";                 	
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }		
	}
	
	public void WriteTxt(){
	    // build request object 
        try {
        	if (numberOfFacesDetected == 0){
            	imageTxt = fileName;       	
        	}
        	else {
            	imageTxt = fileName + "\t";       	
        	}
            for (int i = 0; i < numberOfFacesDetected; i++){
            	Face curFace = faces[i];
            	
            	Rect curRect = new Rect();
            	Rect leftEyeRect = new Rect();
            	Rect rightEyeRect = new Rect();
            	
            	curRect = FaceToRect(curFace);
            	leftEyeRect = LeftEyeToRect(curFace, curRect);
            	rightEyeRect = RightEyeToRect(curFace, curRect);

            	float f = curFace.pose(curFace.EULER_Y);
            	if (f >= PIForth) {
            		// Profile
            		if (i == numberOfFacesDetected - 1){
                		imageTxt += "p[" + 0 + "," + 0 + ";" + 0 + "," + 0 + "]";
            		}
            		else {
                		imageTxt += "p[" + 0 + "," + 0 + ";" + 0 + "," + 0 + "]\t";
            		}
            	}
            	else {
            		if (i == numberOfFacesDetected - 1){
                		imageTxt += "f{" + 
                					"[" + String.valueOf((int)curRect.x) + "," + String.valueOf((int)curRect.y) + ";" + String.valueOf((int)curRect.w) + "," + String.valueOf((int)curRect.h) + "]\t" + 
                					"i[" + String.valueOf((int)leftEyeRect.x) + "," + String.valueOf((int)leftEyeRect.y) + ";" + String.valueOf((int)leftEyeRect.w) + "," + String.valueOf((int)leftEyeRect.h) + "]\t" + 
                					"i[" + String.valueOf((int)rightEyeRect.x) + "," + String.valueOf((int)rightEyeRect.y) + ";" + String.valueOf((int)rightEyeRect.w) + "," + String.valueOf((int)rightEyeRect.h) + "]}";
            		}
            		else {
                		imageTxt += "f{" + 
            					"[" + String.valueOf((int)curRect.x) + "," + String.valueOf((int)curRect.y) + ";" + String.valueOf((int)curRect.w) + "," + String.valueOf((int)curRect.h) + "]\t" + 
            					"i[" + String.valueOf((int)leftEyeRect.x) + "," + String.valueOf((int)leftEyeRect.y) + ";" + String.valueOf((int)leftEyeRect.w) + "," + String.valueOf((int)leftEyeRect.h) + "]\t" + 
            					"i[" + String.valueOf((int)rightEyeRect.x) + "," + String.valueOf((int)rightEyeRect.y) + ";" + String.valueOf((int)rightEyeRect.w) + "," + String.valueOf((int)rightEyeRect.h) + "]}\t";
            		}
            	}
            }
        } catch (Exception e) {
            System.out.println(e);
        }
	}

	
	/**
	 * 
	 * @param uri
	 *            location of image in TPoT memory. Required for image to exist
	 * @param digest
	 *            SHA1 of image implicitly required
	 * @param size
	 *            Pixel width*pixel height, implicitly required
	 * @param caption
	 *            comment on image optional
	 */
	Image(String uri, String digest, String size, String caption) {
		this.uri = uri;
		this.digest = digest;
		this.size = size;
		this.caption = caption;
		
        if (uri.isEmpty() == true){
        	fileName = "";
        }
        else {
        	int lastSlash = uri.lastIndexOf("/");
        	fileName = uri.substring(lastSlash + 1);
        	fileName.trim();
        }
        
		faces = new FaceDetector.Face[MAX_NUMBER_OF_FACES_TO_DETECT];
	}

	/**
	 * 
	 * @param images
	 *            ArrayList of Images
	 * @return csv of image uris from the ArrayList
	 */
	static protected String setUri(ArrayList<Image> images) {
		if (images != null && images.size() > 0) {
			String uris = "";
			for (Image i : images)
				if (i.uri != null) {
					uris += "," + i.uri;
				}
			uris.replaceFirst(",", "");
			return uris;
		}
		return null;

	}

	/**
	 * 
	 * @param images
	 *            : ArrayList of Images
	 * @return csv of of image captions from the ArrayList
	 */
	static protected String setCaptions(ArrayList<Image> images) {
		if (images != null && images.size() > 0) {
			String captions = "";
			for (Image i : images)
				if (i.uri != null)
					captions += "," + (i.caption != null ? i.caption : " ");
			captions.replaceFirst(",", "");
			return captions;
		}
		return null;
	}

	/**
	 * 
	 * @param images
	 *            : ArrayList of Images
	 * @return csv of image sizes from the ArrayList
	 */
	static protected String setSizes(ArrayList<Image> images) {
		if (images != null && images.size() > 0) {
			String sizes = "";
			for (Image i : images)
				if (i.uri != null)
					sizes += "," + (i.size != null ? i.size : " ");
			sizes.replaceFirst(",", "");
			return sizes;
		}
		return null;
	}

	/**
	 * 
	 * @param images
	 *            : ArrayList of Images
	 * @return csv of of image digests from the ArrayList
	 */
	static protected String setDigests(ArrayList<Image> images) {
		if (images != null && images.size() > 0) {
			String digests = "";
			for (Image i : images)
				if (i.uri != null)
					digests += "," + (i.digest != null ? i.digest : " ");
			digests.replaceFirst(",", "");
			return digests;
		}
		return null;
	}

	/**
	 * /** Creates an arraylist of images based on the given string inputs.
	 * Returns an empty list if it couldn't parse any images
	 * 
	 * @param uris
	 *            : String csv of uris
	 * @param captions
	 *            : String csv of captions
	 * @param sizes
	 *            : String csv of sizes
	 * @param digests
	 *            : String csv of digests
	 * @return : ArrayList of Images
	 */
	static protected ArrayList<Image> setImages(String uris, String digests,
			String sizes, String captions) {
		ArrayList<Image> images = new ArrayList<Image>();
		if (uris != null) {
			String[] uri = uris.split(","), caption = captions.split(","), size = sizes
					.split(","), digest = digests.split(",");
			for (int i = 0; i < uri.length; i++) {
				if (uri[i].contains(".jpg") || uri[i].contains("content"))
					images.add(new Image(uri[i], digest[i], size[i], caption[i]));
			}

		}
		return images;
	}

	@Override
	public String toString() {
		return "Uri: " + (uri != null ? uri : "NULL") + "\nCaption: "
				+ (caption != null ? caption : "NULL") + "\nSize: "
				+ (size != null ? size : "NULL") + "\nDigest: "
				+ (digest != null ? digest : "NULL" + "\n");
	}
	
	class Rect {
		public float x;
		public float y;
		public float w;
		public float h;
		
		Rect(){
			x = y = w = h = 0;
		}
		
		Rect(Rect r){
			this.x = r.x;
			this.y = r.y;
			this.w = r.w;
			this.h = r.h;
		}
	}
	
	public Rect FaceToRect(Face f){
		Rect r = new Rect();
    	PointF midPoint = new PointF();
    	f.getMidPoint(midPoint);
    	
    	float a = (float) 2.3;
    	float b = (float) 2.3;
    	
    	r.w = a * f.eyesDistance(); 
    	r.h = b * f.eyesDistance(); 
    	
    	PointF startPoint = new PointF();
    	r.x = (float) (midPoint.x - r.w/2.0);
    	r.y = (float) (midPoint.y - r.h/3.0);    
    	
    	return r;
	}
	
	public Rect LeftEyeToRect(Face f, Rect faceR){
		Rect r = new Rect();
    	PointF midPoint = new PointF();
    	f.getMidPoint(midPoint);
    	
    	PointF eye = new PointF();
    	eye.x = (float) (midPoint.x - 0.5 * f.eyesDistance());
    	eye.y = midPoint.y;
    	
    	r.w = f.eyesDistance() / 4;
    	r.h = f.eyesDistance() / 8;
    	r.x = (float) (eye.x - 0.5 * r.w);
    	r.y = (float) (eye.y - 0.5 * r.h);
    	
    	r.x = r.x - faceR.x;
    	r.y = r.y - faceR.y;
    	
    	return r;
	}
	
	public Rect RightEyeToRect(Face f, Rect faceR){
		Rect r = new Rect();
    	PointF midPoint = new PointF();
    	f.getMidPoint(midPoint);
    	
    	PointF eye = new PointF();
    	eye.x = (float) (midPoint.x + 0.5 * f.eyesDistance());
    	eye.y = midPoint.y;
    	
    	r.w = f.eyesDistance() / 4;
    	r.h = f.eyesDistance() / 8;
    	r.x = (float) (eye.x - 0.5 * r.w);
    	r.y = (float) (eye.y - 0.5 * r.h);
    	
    	r.x = r.x - faceR.x;
    	r.y = r.y - faceR.y;
    	
    	return r;
	}
}
