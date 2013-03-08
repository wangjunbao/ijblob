package test;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import ij.IJ;
import ij.ImagePlus;
import ij.blob.Blob;
import ij.blob.ManyBlobs;

import org.junit.Test;
public class ManyBlobsTest {
	@Test
	public void testFilterBlobs () {
		URL url = this.getClass().getResource("3blobs.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		ManyBlobs t = mb.filterBlobs(0.9, 1, Blob.GETTHINNESRATIO);
		assertEquals(1, t.size(),0);
	}
	@Test
	public void testFilterBlobs_NoUpperLimit () {
		URL url = this.getClass().getResource("3blobs.tif");
		ImagePlus ip = new ImagePlus(url.getPath());
		ManyBlobs mb = new ManyBlobs(ip);
		mb.findConnectedComponents();
		ManyBlobs t = mb.filterBlobs(0.9, Blob.GETTHINNESRATIO);
		
		assertEquals(1, t.size(),0);
	}
	
	@Test (expected=RuntimeException.class)
	public void testNewObject_findConnectedComponents() {
		ManyBlobs t = new ManyBlobs();
		t.findConnectedComponents();
	}
	
	@Test (expected=RuntimeException.class)
	public void testNewObject_getLabeledImage() {
		ManyBlobs t = new ManyBlobs();
		t.findConnectedComponents();
	}

}
