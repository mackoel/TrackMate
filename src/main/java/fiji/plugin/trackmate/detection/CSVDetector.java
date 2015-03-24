package fiji.plugin.trackmate.detection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.algorithm.MultiThreaded;
import net.imglib2.algorithm.fft2.FFTConvolution;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import fiji.plugin.trackmate.Spot;

public class CSVDetector< T extends RealType< T > & NativeType< T >> implements SpotDetector< T >, MultiThreaded
{

	/*
	 * FIELDS
	 */

	private final static String BASE_ERROR_MESSAGE = "CSVDetector: ";

	/** The image to segment. Will not modified. */
	protected RandomAccessible< T > img;

	protected double radius;

	protected double threshold;

	protected String baseErrorMessage;

	protected String errorMessage;

	/** The list of {@link Spot} that will be populated by this detector. */
	protected List< Spot > spots = new ArrayList< Spot >();

	/** The processing time in ms. */
	protected long processingTime;

	protected int numThreads;

	protected final Interval interval;

	protected final double[] calibration;

	/*
	 * CONSTRUCTORS
	 */

	public CSVDetector( final RandomAccessible< T > img, final Interval interval, final double[] calibration, final double radius, final double threshold )
	{
		this.img = img;
		this.interval = DetectionUtils.squeeze( interval );
		this.calibration = calibration;
		this.radius = radius;
		this.threshold = threshold;
		this.baseErrorMessage = BASE_ERROR_MESSAGE;
		setNumThreads();
	}

	/*
	 * METHODS
	 */

	@Override
	public boolean checkInput()
	{
		if ( null == img )
		{
			errorMessage = baseErrorMessage + "Image is null.";
			return false;
		}
		if ( img.numDimensions() > 3 )
		{
			errorMessage = baseErrorMessage + "Image must be 1D, 2D or 3D, got " + img.numDimensions() + "D.";
			return false;
		}
		return true;
	};

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();

//                spots = DetectionUtils.findLocalMaxima( to, threshold, calibration, radius, doSubPixelLocalization, numThreads );

		final long end = System.currentTimeMillis();
		this.processingTime = end - start;

		return true;
	}

	@Override
	public List< Spot > getResult()
	{
		return spots;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public long getProcessingTime()
	{
		return processingTime;
	}

	@Override
	public void setNumThreads()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public void setNumThreads( final int numThreads )
	{
		this.numThreads = numThreads;
	}

	@Override
	public int getNumThreads()
	{
		return numThreads;
	}
}
