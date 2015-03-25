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

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

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
        
        protected final String folder;

	protected String baseErrorMessage;

	protected String errorMessage;

	/** The list of {@link Spot} that will be populated by this detector. */
	protected List< Spot > spots = new ArrayList< Spot >();

	/** The processing time in ms. */
	protected long processingTime;

	protected int numThreads;

	protected final Interval interval;

	protected final double[] calibration;
/** The frame we operate in. */
        private final int frame;
        
        private final long dimT;
 
	/*
	 * CONSTRUCTORS
	 */

	public CSVDetector( final RandomAccessible< T > img, final Interval interval, final double[] calibration, final double radius, final double threshold, final int frame, final String folder, final long dimT )
	{
		this.img = img;
		this.interval = DetectionUtils.squeeze( interval );
		this.calibration = calibration;
		this.radius = radius;
		this.threshold = threshold;
		this.baseErrorMessage = BASE_ERROR_MESSAGE;
                this.frame = frame;
                this.folder = folder;
                this.dimT = dimT;
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

            String fn1 = folder + "/frame_";
            String fn;
            if ( dimT <= 10 )
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100 && frame < 10 )
            {
                fn = fn1 + "0" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100 && frame >= 10 )
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 1000 && frame < 10 )
            {
                fn = fn1 + "00" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 1000 && frame < 100 )
            {
                fn = fn1 + "0" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 1000 && frame >= 100 )
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 10000 && frame < 10 )
            {
                fn = fn1 + "000" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 10000 && frame < 100 )
            {
                fn = fn1 + "00" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 10000 && frame < 1000 )
            {
                fn = fn1 + "0" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 10000 && frame >= 1000 )
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100000 && frame < 10 )
            {
                fn = fn1 + "0000" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100000 && frame < 100 )
            {
                fn = fn1 + "000" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100000 && frame < 1000 )
            {
                fn = fn1 + "00" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100000 && frame < 10000 )
            {
                fn = fn1 + "0" + String.valueOf(frame) + ".csv";
            } else if ( dimT <= 100000 && frame >= 10000 )
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            } else
            {
                fn = fn1 + String.valueOf(frame) + ".csv";
            }
            System.out.println("frame:" + String.valueOf(frame) + " " + fn);
            int done = 0;
            try 
            {
                BufferedReader br = new BufferedReader(new FileReader(fn));
                String line;
                line = br.readLine();// skip header
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    double spot_radius = Math.sqrt(Double.parseDouble(data[1])/Math.PI); // if it is a circle
                    double quality = Double.parseDouble(data[38]); //mean intensity
                    double x = Double.parseDouble(data[5]);
                    double y = Double.parseDouble(data[6]);
                    double z = Double.parseDouble(data[7]);
                    final Spot spot = new Spot( x * calibration[ 0 ], y * calibration[ 1 ], z * calibration[ 2 ], spot_radius * calibration[ 0 ], quality );
                    if (done == 0) {
                        System.out.println("frame:" + String.valueOf(frame) + " " + spot.echo());
                        done = 1;
                    }
                    spots.add( spot );
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
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
