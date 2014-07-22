/*
    Shape Filter is a plugin for ImageJ to analyse and filter segmented images 
    by shape features.
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
package de.biomedical_imaging.ij.shapefilter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Polygon;
import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.blob.Blob;
import ij.blob.ManyBlobs;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map.Entry;
/*
 * Autor: Thorsten Wagner, wagner@biomedical-imaging.de
 */
public class Shape_Filter implements ExtendedPlugInFilter {
	/*
	 * Dieses Plugin filtert binäre Objekte nach ihren Formparametern. Ob die
	 * einzelnen Objekte zu identifizieren und deren Kontur zu extrahieren,
	 * wurde folgende Algorithmus implementiert: ���F. Chang, ���A linear-time
	 * component-labeling algorithm using contour tracing technique,��� Computer
	 * Vision and Image Understanding, vol. 93, no. 2, pp. 206-220, 2004.
	 */

	private ImagePlus imp;
	private ManyBlobs[] allBlobs;
	private ImageProcessor actualIP;
	private FilterParameters para;
	private ResultsTable rt;
	boolean processStack;
	boolean previewIsActive = false;
	private static Shape_Filter instance = null;
	
	/*
	 * Die Reihenfolge, wie das 3x3 Fester um den Punkt p durchlaufen wird.
	 * Fenster:
	 * 5 * 6 * 7 
	 * 4 * p * 0 
	 * 3 * 2 * 1 
	 */
	int iterationorder[] = { 5, 4, 3, 6, 2, 7, 0, 1 };
    //Needs ImageJ 1.47h 
	
	public Shape_Filter() {
		// TODO Auto-generated constructor stub
		instance=this;
	}
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (imp == null || imp.getType() != ImagePlus.GRAY8) {
			IJ.error("Binary Image is needed!");
			return DONE;
		}
		
		//Check if binary
		ImageStatistics stats = imp.getStatistics();
		float binaryRatio = ((float)(stats.histogram[0] + stats.histogram[255]))/stats.pixelCount;
		if (binaryRatio > 0.90 && ((int)binaryRatio) != 1) {
			IJ.log("Not really binary...(lossy Image format?) but more than " + IJ.d2s(binaryRatio*100, 0)  +"% of the image are black or white pixels. Converted to Binary!");
			imp.getProcessor().threshold(127);
			
		}
		else if(binaryRatio < 0.90 ) {
			IJ.error("Binary Image is needed!");
			return DONE;
		}
		
		this.imp = imp;
		allBlobs = new ManyBlobs[imp.getStackSize()];//(imp);

		IJ.showStatus("Do Component Labeling");
		
		registerImage(imp.getTitle());
		
		return DOES_8G;
	}
	
	public void setIsPreview(boolean ispreview){
		this.previewIsActive = ispreview;
	}
	
	public void setParameters(FilterParameters para){
		this.para = para;
	}
	@Override
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		BlobFilterDialog dialog = new BlobFilterDialog();
		
		if (dialog.showDialog(pfr)==-1){ 
			return DONE;
		}else{
			previewIsActive=false;
		}
		para = dialog.getParams();
		
		int flags = IJ.setupDialog(imp, DOES_8G);
		processStack = (flags&DOES_STACKS)!=0;
		if(previewIsActive==false){
			IJ.getTextPanel().addMouseListener(new ResultsTableSelectionDrawer(imp));
		}
		
		return flags;
	}

	@Override
	public void setNPasses(int nPasses) {
		// TODO Auto-generated method stub
		
	}

	
	public void registerImage(String title){
		Window window = WindowManager.getWindow(title);
		ImagePlus image = WindowManager.getImage(title);
		boolean windowIsVisible = (window!=null);
		if(windowIsVisible){
			window.getComponent(0).addMouseListener(new ImageResultsTableSelector(image));
		}
	}
	
	public static Shape_Filter getInstance(){
		return instance;
	}
 
	public ManyBlobs[] getAllBlobs(){
		return allBlobs;
	}
	
	public Blob getBlobByFrameAndLabel(int frame, int label){
		return allBlobs[frame].getBlobByLabel(label);
	}

	@Override
	public void run(ImageProcessor ip) {
		actualIP = ip;
		
		ImagePlus helpimp = new ImagePlus("", ip);
		helpimp.setCalibration(imp.getCalibration());
		allBlobs[actualIP.getSliceNumber()-1] = new ManyBlobs(helpimp);
		if(para.isBlackBackground()){
			allBlobs[actualIP.getSliceNumber()-1].setBackground(0);
		}
		else{
			allBlobs[actualIP.getSliceNumber()-1].setBackground(1);
		}
		allBlobs[actualIP.getSliceNumber()-1].findConnectedComponents();
		IJ.showStatus("Component Labeling Done");
		
		addResultImage(para, ip);
		if (para.isAddToManager() && previewIsActive==false) {
			addToManager(para);
		}
		
		if(para.isFillResultsTable()&& previewIsActive==false){
			fillResultTable(para);
		}
		
		if(para.isShowLabeledImage()&& previewIsActive==false){
			ManyBlobs fb = getFilteredBlobs(para);
			fb.getLabeledImage().show();
		}
		
	}
	
	/**
	 * Fügt für alle Blobs, dessen Formparameter innerhalb der Schwellwerte liegen, die ermittelten Formparamter
	 * in die Result-Table ein
	 * @param params Schwellwerte der Formparamter
	 */
	private void fillResultTable(FilterParameters params) {
		// TODO Auto-generated method stub
		rt = Analyzer.getResultsTable();

		if(rt==null)
		{
			IJ.log("new");
			 rt = new ResultsTable();
			
			
			 Analyzer.setResultsTable(rt);
			 
			 
		}
		if( IJ.getTextPanel().getMouseListeners().length == 0){
		//IJ.getTextPanel().addMouseListener(new ResultsTableMouseListener());
			
		}
	
		
		ManyBlobs fb = getFilteredBlobs(params);
		for (int i = 0; i < fb.size(); i++) {
				rt.incrementCounter();
				rt.addValue("Frame", processStack?actualIP.getSliceNumber():imp.getCurrentSlice());
				rt.addValue("Label", fb.get(i).getLabel());
				Point2D cog = fb.get(i).getCenterOfGravity();
				rt.addValue("X", cog.getX());
				rt.addValue("Y", cog.getY());
				rt.addValue("Area", fb.get(i).getEnclosedArea());
				rt.addValue("Area Conv. Hull", fb.get(i).getAreaConvexHull());
				rt.addValue("Peri.", fb.get(i).getPerimeter());
				rt.addValue("Peri. Conv. Hull", fb.get(i)
						.getPerimeterConvexHull());
				rt.addValue("Feret", fb.get(i).getFeretDiameter());
				rt.addValue("Min. Feret", fb.get(i).getMinFeretDiameter());
				rt.addValue("Long Side Length MBR", fb.get(i).getLongSideMBR());
				rt.addValue("Short Side Length MBR", fb.get(i).getShortSideMBR());
				rt.addValue("Aspect Ratio", fb.get(i).getAspectRatio());
				rt.addValue("Area/Peri.", fb.get(i)
						.getAreaToPerimeterRatio());
				rt.addValue("Circ.", fb.get(i).getCircularity());
				rt.addValue("Elong.", fb.get(i).getElongation());
				rt.addValue("Convexity", fb.get(i).getConvexity());
				rt.addValue("Solidity", fb.get(i).getSolidity());
				rt.addValue("Num. of Holes", fb.get(i)
						.getNumberofHoles());
				rt.addValue("Thinnes Rt.", fb.get(i).getThinnesRatio());
				rt.addValue("Contour Temp.", fb.get(i)
						.getContourTemperature());
				rt.addValue("Fract. Dim.", fb.get(i)
						.getFractalBoxDimension(params.getFractalBoxSizes()));
				rt.addValue("Fract. Dim. Goodness", fb.get(i)
						.getFractalDimensionGoodness());
		}

		rt.show("Results");
	}
	
	/**
	 * Erstellt einen Plot, der alle Blobs enth��lt, die innerhalb der Schwellwerte params liegen.
	 * @param params Schwellwerte der Formparamter
	 */
	private void addResultImage(FilterParameters params, ImageProcessor ip) {
		ManyBlobs fb = getFilteredBlobs(params);
		if(params.isBlackBackground()){
			actualIP.setColor(Color.black);
			Blob.setDefaultColor(Color.white);
		}
		else
		{
			actualIP.setColor(Color.white);
			Blob.setDefaultColor(Color.black);
		}
		actualIP.fill();
		for (int i = 0; i < fb.size(); i++) {
			IJ.showStatus("Feature Calculation");
			IJ.showProgress(i + 1, fb.size());
			fb.get(i).draw(actualIP, params.isDrawHoles() | params.isDrawConvexHull() | params.isDrawLabel());
		}
	}
	
	private void addToManager(FilterParameters params) {
		Frame frame = WindowManager.getFrame("ROI Manager");
		if (frame == null)
			IJ.run("ROI Manager...");
		frame = WindowManager.getFrame("ROI Manager");
		RoiManager roiManager = (RoiManager) frame;

		ManyBlobs fb = getFilteredBlobs(params);
		for (int i = 0; i < fb.size(); i++) {
				Polygon p = fb.get(i).getOuterContour();
				int n = p.npoints;
				float[] x = new float[p.npoints];
				float[] y = new float[p.npoints];
				
				for (int j=0; j<n; j++) {
				     x[j] = p.xpoints[j]+0.5f;
				     y[j] = p.ypoints[j]+0.5f;
				}
				
				Roi roi = new PolygonRoi(x,y,n,Roi.TRACED_ROI);
				
				Roi.setColor(Color.green);
				roiManager.add(imp, roi, i);
				
				
		}
	}
	
	private ManyBlobs getFilteredBlobs(FilterParameters params){
		ManyBlobs fb = new ManyBlobs();
		Iterator<Entry<String, double[]>> it = params.getFeatureIterator();
		Entry<String, double[]> pairs = it.next();
	
		fb = allBlobs[actualIP.getSliceNumber()-1].filterBlobs((double[])pairs.getValue(), pairs.getKey(),params.getFilterMethodParameter(pairs.getKey()));
		while(it.hasNext()) {
			pairs = it.next();
			fb = fb.filterBlobs((double[])pairs.getValue(), pairs.getKey(),params.getFilterMethodParameter(pairs.getKey()));
		}
		return fb;
		
	}

	
}
