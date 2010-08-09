package fiji.plugin.nperry.scoring;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.cursor.special.RegionOfInterestCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.outofbounds.OutOfBoundsStrategyValueFactory;
import mpicbg.imglib.type.numeric.RealType;
import fiji.plugin.nperry.Spot;

public class BlobBrightnessScorer <T extends RealType<T>> extends IndependentScorer {

	private static final String SCORING_METHOD_NAME = "BlobBrightnessScorer";
	private Image<T> img;
	private double diam;
	private double[] calibration;
	
	public BlobBrightnessScorer(Image<T> originalImage, double diam, double[] calibration) {
		this.img = originalImage;
		this.diam = diam;
		this.calibration = calibration;
	}
	
	@Override
	public String getName() {
		return SCORING_METHOD_NAME;
	}

	@Override
	public boolean isNormalized() {
		return false;
	}

	@Override
	public void score(Spot spot) {
		final LocalizableByDimCursor<T> cursor = img.createLocalizableByDimCursor(new OutOfBoundsStrategyValueFactory<T>());
		final double[] origin = spot.getCoordinates();

		// Create the size array for the ROI cursor
		int size[] = new int[img.getNumDimensions()];
		for (int i = 0; i < size.length; i++) {
			size[i] = (int) (diam / calibration[i]);
		}

		// Adjust the integer coordinates of the spot to set the ROI correctly
		int[] roiCoords = new int[img.getNumDimensions()];
		for (int i = 0; i < origin.length; i++) {
			roiCoords[i] = (int) (origin[i] - (size[i] / 2));  
		}
		
		// Use ROI cursor to search a sphere around the spot's coordinates
		/* need to handle case where ROI is not in image anymore!! */
		double sum = 0;
		RegionOfInterestCursor<T> roi = cursor.createRegionOfInterestCursor(roiCoords, size);
		//System.out.println();
		//System.out.println("Maximum: " + origin[0] + ", " + origin[1] + ", " + origin[2] + "; ");
		//System.out.println();
		while (roi.hasNext()) {
			roi.next();
			if (inSphere(origin, cursor.getPosition(), diam / 2)) {
				sum += roi.getType().getRealDouble();
				//System.out.print(cursor.getPosition()[0] + ", " + cursor.getPosition()[1] + ", " + cursor.getPosition()[2] + "; ");
			}
		}

		// Close cursors
		roi.close();
		cursor.close();
		
		// Add total intensity.
		spot.addScore(SCORING_METHOD_NAME, sum);
	}
	
	/**
	 * Determines if the coordinate coords is at least min distance away from the
	 * origin, but within max distance. The distance metric used is Euclidean
	 * distance.
	 * 
	 * @param origin 
	 * @param coords
	 * @param max
	 * @param min
	 * @return
	 */
	private boolean inSphere(double[] origin, int[] coords, double rad) {
		double euclDist = 0;
		for (int i = 0; i < coords.length; i++) {
			euclDist += Math.pow((origin[i] - (double) coords[i]) * calibration[i], 2);
		}
		euclDist = Math.sqrt(euclDist);
		return euclDist <= rad;
	}
}