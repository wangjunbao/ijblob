/*
    IJBlob is a ImageJ library for extracting connected components in binary Images
    Copyright (C) 2012  Thorsten Wagner wagner@biomedical-imaging.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MER
TABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package ij.blob;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageStatistics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/*
 * This library extracts connected components . For this purpose it uses the
 * following algorithm : F. Chang, A linear-time
 * component-labeling algorithm using contour tracing technique, Computer
 * Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
 */

/**
 * Represents the result-set of all detected blobs as ArrayList
 * @author Thorsten Wagner
 */

public class ManyBlobs extends ArrayList<Blob> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImagePlus imp = null;
	private ImagePlus labeledImage = null;
	private int BACKGROUND = 255;
	private int OBJECT = 0;

	public ManyBlobs() {
	}
	
	/**
	 * @param imp Binary Image
	 */
	public ManyBlobs(ImagePlus imp) {
		setImage(imp);
	}
	
	private void setImage(ImagePlus imp) {
		this.imp = imp;
		ImageStatistics stats = imp.getStatistics();

		if ((stats.histogram[0] + stats.histogram[255]) != stats.pixelCount) {
			throw new java.lang.IllegalArgumentException("Not a binary image");
		}
		
		if(imp.isInvertedLut()){
			//BACKGROUND = 0;
		//	OBJECT = 255;
		}
	}
	
	/**
	 * Start the Connected Component Algorithm
	 * @see  F. Chang, A linear-time component-labeling algorithm using contour tracing technique, Computer Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
	 */
	public void findConnectedComponents() {
		if(imp==null){
			throw new RuntimeException("Cannot run findConnectedComponents: No input image specified");
		}
		ConnectedComponentLabeler labeler = new ConnectedComponentLabeler(this,imp,BACKGROUND,OBJECT);
		labeler.doConnectedComponents();
		labeledImage = labeler.getLabledImage();
	}
	/**
	 * 
	 * @return Return the labeled Image.
	 */
	public ImagePlus getLabeledImage() {
		if(labeledImage == null){
			throw new RuntimeException("No input image was analysed for connected components");
		}
		return labeledImage;
	}
	
	/**
	 * Filter all blobs which feature (specified by the methodName) is lower than 
	 * the lowerLimit or higher than the upper limit.
	 * For instance: filterBlobs(Blob.GETENCLOSEDAREA,40,100) will filter all blobs between 40 and 100 pixel².
	 * @param methodName Getter method of the blob feature (double as return value).
	 * @param lowerLimit Lower limit for the feature to filter blobs.
	 * @param upperLimit Upper limit for the feature to filter blobs.
	 * @return The filtered blobs.
	 */
	public ManyBlobs filterBlobs(String methodName, double lowerLimit, double upperLimit){
		ManyBlobs blobs = new ManyBlobs();
		try {
			Method m = Blob.class.getMethod(methodName, null);
			for(int i = 0; i < this.size(); i++) {
				double value = (Double)m.invoke(this.get(i), null);
				boolean included= false;
				if (Double.isInfinite(upperLimit)) {
					included =  (value >= lowerLimit) ? true : false;
				}
				else
				{
					included = (value >= lowerLimit && value <= upperLimit) ? true : false;
				}
				if(included){
					blobs.add(this.get(i));
				}
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			IJ.log("Method not found: " + e.getMessage());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return blobs;
		
	}
	/**
	 * Filter all blobs which feature (specified by the methodName) is lower than 
	 * the limit.
	 * For instance: filterBlobs(Blob.GETENCLOSEDAREA,40) will filter all blobs with an area lower than 40 pixel²
	 * @param methodName Getter method of the blob feature (double as return value).
	 * @param limit Limit for the feature to filter blobs.
	 * @return The filtered blobs.
	 * */
	public ManyBlobs filterBlobs(String methodName, double limit){
		return filterBlobs(methodName, limit, Double.POSITIVE_INFINITY);
	}


}
