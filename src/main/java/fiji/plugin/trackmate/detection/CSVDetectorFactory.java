package fiji.plugin.trackmate.detection;

import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_FOLDER;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_XCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_YCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_ZCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_ACOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.DEFAULT_ICOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_FOLDER;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_XCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_YCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ZCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ACOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ICOLUMN;
import static fiji.plugin.trackmate.io.IOUtils.readDoubleAttribute;
import static fiji.plugin.trackmate.io.IOUtils.readIntegerAttribute;
import static fiji.plugin.trackmate.io.IOUtils.writeTargetChannel;
import static fiji.plugin.trackmate.io.IOUtils.writeXcolumn;
import static fiji.plugin.trackmate.io.IOUtils.writeYcolumn;
import static fiji.plugin.trackmate.io.IOUtils.writeZcolumn;
import static fiji.plugin.trackmate.io.IOUtils.writeAcolumn;
import static fiji.plugin.trackmate.io.IOUtils.writeIcolumn;
import static fiji.plugin.trackmate.io.IOUtils.writeFolder;
import static fiji.plugin.trackmate.util.TMUtils.checkMapKeys;
import static fiji.plugin.trackmate.util.TMUtils.checkParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.Settings;
import fiji.plugin.trackmate.gui.ConfigurationPanel;
import fiji.plugin.trackmate.gui.panels.detector.CSVDetectorConfigurationPanel;
import fiji.plugin.trackmate.util.TMUtils;

@Plugin( type = SpotDetectorFactory.class )
public class CSVDetectorFactory< T extends RealType< T > & NativeType< T >> implements SpotDetectorFactory< T >
{

	/*
	 * CONSTANTS
	 */

	/** A string key identifying this factory. */
	public static final String DETECTOR_KEY = "CSV_DETECTOR";

	/** The pretty name of the target detector. */
	public static final String NAME = "CSV detector";

	/** An html information text. */
	public static final String INFO_TEXT = "<html>" + "This detector reads csv tables" + "</html>";

	/*
	 * FIELDS
	 */

	/** The image to operate on. Multiple frames, single channel. */
	protected ImgPlus< T > img;

	protected Map< String, Object > settings;

	protected String errorMessage;

	/*
	 * METHODS
	 */

	@Override
	public boolean setTarget( final ImgPlus< T > img, final Map< String, Object > settings )
	{
		this.img = img;
		this.settings = settings;
		return checkSettings( settings );
	}

	@Override
	public SpotDetector< T > getDetector( final Interval interval, final int frame )
	{
		final int xcolumn = ( Integer ) settings.get( KEY_XCOLUMN );
		final int ycolumn = ( Integer ) settings.get( KEY_YCOLUMN );
		final int zcolumn = ( Integer ) settings.get( KEY_ZCOLUMN );
		final int acolumn = ( Integer ) settings.get( KEY_ACOLUMN );
		final int icolumn = ( Integer ) settings.get( KEY_ICOLUMN );
		final double[] calibration = TMUtils.getSpatialCalibration( img );
                final String folder = ( String ) settings.get( KEY_FOLDER );
                RandomAccessible< T > imFrame;
		final int cDim = TMUtils.findCAxisIndex( img );
		if ( cDim < 0 )
		{
			imFrame = img;
		}
		else
		{
			// In ImgLib2, dimensions are 0-based.
			final int channel = ( Integer ) settings.get( KEY_TARGET_CHANNEL ) - 1;
			imFrame = Views.hyperSlice( img, cDim, channel );
		}

		int timeDim = TMUtils.findTAxisIndex( img );
		if ( timeDim >= 0 )
		{
			if ( cDim >= 0 && timeDim > cDim )
			{
				timeDim--;
			}
			imFrame = Views.hyperSlice( imFrame, timeDim, frame );
		}

		// In case we have a 1D image.
		if ( img.dimension( 0 ) < 2 )
		{ // Single column image, will be rotated internally.
			calibration[ 0 ] = calibration[ 1 ]; // It gets NaN otherwise
			calibration[ 1 ] = 1;
			imFrame = Views.hyperSlice( imFrame, 0, 0 );
		}
		if ( img.dimension( 1 ) < 2 )
		{ // Single line image
			imFrame = Views.hyperSlice( imFrame, 1, 0 );
		}
                final long dimT = img.dimension( timeDim );
		final CSVDetector< T > detector = new CSVDetector< T >( imFrame, interval, calibration, frame, xcolumn, ycolumn, zcolumn, acolumn, icolumn, folder, dimT);
		detector.setNumThreads( 1 );
		return detector;
	}

	@Override
	public String getKey()
	{
		return DETECTOR_KEY;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public boolean checkSettings( final Map< String, Object > settings )
	{
		boolean ok = true;
		final StringBuilder errorHolder = new StringBuilder();
		ok = ok & checkParameter( settings, KEY_TARGET_CHANNEL, Integer.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_XCOLUMN, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_YCOLUMN, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_ZCOLUMN, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_ACOLUMN, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_ICOLUMN, Double.class, errorHolder );
		ok = ok & checkParameter( settings, KEY_FOLDER, String.class, errorHolder );
		final List< String > mandatoryKeys = new ArrayList< String >();
		mandatoryKeys.add( KEY_TARGET_CHANNEL );
		mandatoryKeys.add( KEY_XCOLUMN );
		mandatoryKeys.add( KEY_YCOLUMN );
		mandatoryKeys.add( KEY_ZCOLUMN );
		mandatoryKeys.add( KEY_ACOLUMN );
		mandatoryKeys.add( KEY_ICOLUMN );
                mandatoryKeys.add( KEY_FOLDER );
		ok = ok & checkMapKeys( settings, mandatoryKeys, null, errorHolder );
		if ( !ok )
		{
			errorMessage = errorHolder.toString();
		}
		return ok;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		final StringBuilder errorHolder = new StringBuilder();
		final boolean ok = writeTargetChannel( settings, element, errorHolder ) && writeXcolumn( settings, element, errorHolder ) && writeYcolumn( settings, element, errorHolder ) && writeZcolumn( settings, element, errorHolder ) && writeAcolumn( settings, element, errorHolder ) && writeIcolumn( settings, element, errorHolder ) && writeFolder( settings, element, errorHolder );
		if ( !ok )
		{
			errorMessage = errorHolder.toString();
		}
		return ok;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		settings.clear();
		final StringBuilder errorHolder = new StringBuilder();
		boolean ok = true;
		ok = ok & readIntegerAttribute( element, settings, KEY_XCOLUMN, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_YCOLUMN, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_ZCOLUMN, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_ACOLUMN, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_ICOLUMN, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_TARGET_CHANNEL, errorHolder );
		ok = ok & readIntegerAttribute( element, settings, KEY_FOLDER, errorHolder );
		if ( !ok )
		{
			errorMessage = errorHolder.toString();
			return false;
		}
		return checkSettings( settings );
	}

	@Override
	public ConfigurationPanel getDetectorConfigurationPanel( final Settings settings, final Model model )
	{
		return new CSVDetectorConfigurationPanel( settings.imp, INFO_TEXT, NAME, model );
	}

	@Override
	public String getInfoText()
	{
		return INFO_TEXT;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		final Map< String, Object > settings = new HashMap< String, Object >();
		settings.put( KEY_TARGET_CHANNEL, DEFAULT_TARGET_CHANNEL );
		settings.put( KEY_XCOLUMN, DEFAULT_XCOLUMN );
		settings.put( KEY_YCOLUMN, DEFAULT_YCOLUMN );
		settings.put( KEY_ZCOLUMN, DEFAULT_ZCOLUMN );
		settings.put( KEY_ACOLUMN, DEFAULT_ACOLUMN );
		settings.put( KEY_ICOLUMN, DEFAULT_ICOLUMN );
		settings.put( KEY_FOLDER, DEFAULT_FOLDER );
		return settings;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

}
