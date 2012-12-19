package test;

import static org.junit.Assert.*;

import java.awt.Polygon;
import java.net.URL;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.ManyBlobs;

import org.junit.Test;

public class FeatureTest {

	@Test
	public void testGetCenterOfGravity() {
		URL url = this.getClass().getResource("circle_r30.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		int centerx = mb.get(0).getCenterOfGravity().x;
		int centery = mb.get(0).getCenterOfGravity().y;
		double diff = Math.abs(47-centerx)+Math.abs(47-centery);
		assertEquals(0, diff,0);
	}

	@Test
	public void testGetElongation() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPerimeterCircleRad30() {
		URL url = this.getClass().getResource("circle_r30.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int peri = (int)(2*Math.PI*29.5);
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(peri, mb.get(0).getPerimeter(),2);
	}

	@Test
	public void testGetPerimeterConvexHull() {
		URL url = this.getClass().getResource("square100x100_minus30x30.png");
		ImagePlus ip = new ImagePlus(url.getPath());
		int periConv = 4*100-4; //400-4(-4 Because the Edges doesnt mutiple counted 
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(periConv, mb.get(0).getPerimeterConvexHull(),2);
	}

	@Test
	public void testEnclosedAreaCircleRad30() {
		URL url = this.getClass().getResource("circle_r30.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int area = (int)(Math.PI*30*30);
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(area, mb.get(0).getEnclosedArea(),2);
		
	}

	@Test
	public void testGetCircularity() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetThinnesRatio() {
		URL url = this.getClass().getResource("circle_r30.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int circ = 1;
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(circ, mb.get(0).getThinnesRatio(),0.01);
	}

	@Test
	public void testFind4holes() {
		URL url = this.getClass().getResource("nestedObjects.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int holes = 4;
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(holes, mb.get(0).getInnerContours().size(),0);
	}
	
	@Test
	public void testFindThreeBlobs() {
		URL url = this.getClass().getResource("3blobs.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int count = 3;
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(count, mb.size(),0);
	}
	@Test
	public void testNestedFindFiveBlobs() {
		URL url = this.getClass().getResource("nestedObjects.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		int blobs = 5;
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		assertEquals(blobs, mb.size(),0);
	}
	
	@Test
	public void testGetOuterContourIsCorrect() {
		URL url = this.getClass().getResource("correctcontour.png");
		ImagePlus ip = new ImagePlus(url.getPath());
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		
		int[] xp = {3,4,5,6,7,8,9,10,11,11,11,10,9,8,7,6,5,4,3,2,2,2,3};
		int[] yp = {1,1,2,2,2,1,1,2,2,3,4,4,5,5,4,4,4,5,5,4,3,2,1};
		
		Polygon contour = mb.get(0).getOuterContour();
		
		int diff=0;
		for(int i = 0; i < contour.npoints; i++){
			diff += Math.abs(contour.xpoints[i] - xp[i]) + Math.abs(contour.ypoints[i] - yp[i]);
		}
		assertEquals(0,diff,0);
	}

}
