package fiji.plugin.spottracker.features;

import mpicbg.imglib.cursor.LocalizableByDimCursor;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.type.numeric.RealType;
import fiji.plugin.spottracker.Feature;
import fiji.plugin.spottracker.Featurable;

public class LoGValue <T extends RealType<T>> extends IndependentFeatureAnalyzer {

	/*
	 * FIELDS
	 */
	
	private static final Feature FEATURE = Feature.LOG_VALUE;
	private Image<T> img;
	private LocalizableByDimCursor<T> cursor;
	private float[] downsampleFactors;
	private float[] calibration;
	/** Utility holder. */
	private float[] coords;
	
	/*
	 * CONSTRUCTORS
	 */
	
	/**
	 * Instantiate this feature analyzer assuming the filtered image was <b>not</b>
	 * down-sampled, and using the spatial calibration stored in the image in argument.
	 */
	public LoGValue(Image<T> filteredImage) {
		this.img = filteredImage;
		this.cursor = img.createLocalizableByDimCursor();
		this.calibration = filteredImage.getCalibration();
		this.downsampleFactors = new float[filteredImage.getNumDimensions()];
		coords = new float[img.getNumDimensions()];
		for (int i = 0; i < downsampleFactors.length; i++) {
			downsampleFactors[i] = 1;
		}
	}
	
	/**
	 * Instantiate this feature analyzer using the given down-sampling factors, 
	 * and the spatial calibration stored in the image in argument.
 	 * <p>
	 * <u>Warning:</u> the physical calibration must be the one of the image  
	 * before down-sampling.

	 */
	public LoGValue(Image<T> filteredImage, float[] downsampleFactors) {
		this.img = filteredImage;
		this.cursor = img.createLocalizableByDimCursor();
		this.downsampleFactors = downsampleFactors;
		this.calibration = filteredImage.getCalibration();
		coords = new float[img.getNumDimensions()];
	}
	
	/**
	 * Instantiate this feature analyzer using the given down-sampling factors, 
	 * and the given spatial calibration.
	 * <p>
	 * <u>Warning:</u> the physical calibration must be the one of the image  
	 * before down-sampling.
	 */
	public LoGValue(Image<T> filteredImage, float[] downsampleFactors, float[] calibration) {
		this.img = filteredImage;
		this.downsampleFactors = downsampleFactors;
		this.cursor = img.createLocalizableByDimCursor();
		this.calibration = calibration;
		coords = new float[img.getNumDimensions()];
	}

	/*
	 * PUBLIC METHODS 
	 */
	
	@Override
	public Feature getFeature() {
		return FEATURE;
	}

	@Override
	public void process(Featurable spot) {
		for (int i = 0; i < coords.length; i++) 
			coords[i] = spot.getFeature(Featurable.POSITION_FEATURES[i]);
		
		// 1 - Convert physical coords to pixel coords
		for (int i = 0; i < downsampleFactors.length; i++) {
			coords[i] = coords[i] / calibration[i];
		}
		
		// 2 - Downsample pixel coords, since we are using the downsampled image.
		for (int i = 0; i < downsampleFactors.length; i++) {
			coords[i] = coords[i] / downsampleFactors[i];
		}
		
		// 3 - Store the float[] coords as a int[] to set the cursor with
		final int[] intCoords = new int[downsampleFactors.length];
		for (int i = 0; i < downsampleFactors.length; i++) {
			intCoords[i] = (int) coords[i];
		}
		
		// 4 - Get the intensity at the spot's coordinates
		cursor.setPosition(intCoords);
//		System.out.println("LoG: " + cursor.getType().getRealFloat()); // DEBUG
		spot.putFeature(FEATURE, cursor.getType().getRealFloat());
	}
	
}
