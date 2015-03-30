package fiji.plugin.trackmate.gui.panels.detector;

import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_FOLDER;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_TARGET_CHANNEL;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_XCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_YCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ZCOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ACOLUMN;
import static fiji.plugin.trackmate.detection.DetectorKeys.KEY_ICOLUMN;
import static fiji.plugin.trackmate.gui.TrackMateWizard.BIG_FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.FONT;
import static fiji.plugin.trackmate.gui.TrackMateWizard.SMALL_FONT;
import static fiji.plugin.trackmate.io.IOUtils.askForFolder;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.detection.CSVDetectorFactory;
import fiji.plugin.trackmate.detection.SpotDetectorFactory;
import fiji.plugin.trackmate.gui.ConfigurationPanel;
import fiji.plugin.trackmate.gui.panels.components.JNumericTextField;
import fiji.plugin.trackmate.util.JLabelLogger;
import fiji.util.NumberParser;
import java.awt.Frame;
import java.io.File;

/**
 * Configuration panel for spot detectors based on LoG detector.
 * 
 * @author Jean-Yves Tinevez <jeanyves.tinevez@gmail.com> 2010 - 2014
 */
public class CSVDetectorConfigurationPanel extends ConfigurationPanel
{

	private static final long serialVersionUID = 1L;

	private JLabel jLabel1;

	protected JLabel jLabelSegmenterName;

	protected JButton btnSelect;

	protected JLabel jLabelHelpText;

	protected JLabel jLabelXcolumn;

	protected JLabel jLabelYcolumn;

	protected JLabel jLabelZcolumn;

	protected JLabel jLabelAcolumn;

	protected JLabel jLabelIcolumn;

	protected JTextField jTextFieldXcolumn;

	protected JTextField jTextFieldYcolumn;

	protected JTextField jTextFieldZcolumn;

	protected JTextField jTextFieldAcolumn;

	protected JTextField jTextFieldIcolumn;

	/** The HTML text that will be displayed as a help. */
	protected JLabel lblSegmentInChannel;

	protected JSlider sliderChannel;

	protected JLabel labelChannel;

	protected final String infoText;

        protected JLabel labelFolder;

	protected JTextField infoFolder;

	protected final String detectorName;

	protected final ImagePlus imp;

	protected final Model model;

	private Logger localLogger;

	/** The layout in charge of laying out this panel content. */
	protected SpringLayout layout;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Creates a new {@link CSVDetectorConfigurationPanel}, a GUI able to
	 * configure settings suitable to {@link CSVDetectorFactory} and derived
	 * implementations.
	 * 
	 * @param imp
	 *            the {@link ImagePlus} to read the image content from as well
	 *            as other metadata.
	 * @param infoText
	 *            the detector info text, will be displayed on the panel.
	 * @param detectorName
	 *            the detector name, will be displayed on the panel.
	 * @param model
	 *            the {@link Model} that will be fed with the preview results.
	 *            It is the responsibility of the views registered to listen to
	 *            model change to display the preview results.
	 */
	public CSVDetectorConfigurationPanel( final ImagePlus imp, final String infoText, final String detectorName, final Model model )
	{
		this.imp = imp;
		this.infoText = infoText;
		this.detectorName = detectorName;
		this.model = model;
		initGUI();
	}

	/*
	 * METHODS
	 */

	@Override
	public Map< String, Object > getSettings()
	{
		final HashMap< String, Object > settings = new HashMap< String, Object >( 5 );
		final int targetChannel = sliderChannel.getValue();
		final int xcolumn = NumberParser.parseInteger( jTextFieldXcolumn.getText() );
		final int ycolumn = NumberParser.parseInteger( jTextFieldYcolumn.getText() );
		final int zcolumn = NumberParser.parseInteger( jTextFieldZcolumn.getText() );
		final int acolumn = NumberParser.parseInteger( jTextFieldAcolumn.getText() );
		final int icolumn = NumberParser.parseInteger( jTextFieldIcolumn.getText() );
                final String folder = infoFolder.getText();
		settings.put( KEY_TARGET_CHANNEL, targetChannel );
		settings.put( KEY_XCOLUMN, xcolumn );
		settings.put( KEY_YCOLUMN, ycolumn );
		settings.put( KEY_ZCOLUMN, zcolumn );
		settings.put( KEY_ACOLUMN, acolumn );
		settings.put( KEY_ICOLUMN, icolumn );
                settings.put( KEY_FOLDER, folder );
		return settings;
	}

	@Override
	public void setSettings( final Map< String, Object > settings )
	{
		sliderChannel.setValue( ( Integer ) settings.get( KEY_TARGET_CHANNEL ) );
		jTextFieldXcolumn.setText( "" + settings.get( KEY_XCOLUMN ) );
		jTextFieldYcolumn.setText( "" + settings.get( KEY_YCOLUMN ) );
		jTextFieldZcolumn.setText( "" + settings.get( KEY_ZCOLUMN ) );
		jTextFieldAcolumn.setText( "" + settings.get( KEY_ACOLUMN ) );
		jTextFieldIcolumn.setText( "" + settings.get( KEY_ICOLUMN ) );
                infoFolder.setText( "" + settings.get( KEY_FOLDER ));
	}

	/**
	 * Returns a new instance of the {@link SpotDetectorFactory} that this
	 * configuration panels configures. The new instance will in turn be used
	 * for the preview mechanism. Therefore, classes extending this class are
	 * advised to return a suitable implementation of the factory.
	 * 
	 * @return a new {@link SpotDetectorFactory}.
	 */
	@SuppressWarnings( "rawtypes" )
	protected SpotDetectorFactory< ? > getDetectorFactory()
	{
		return new CSVDetectorFactory();
	}

	/*
	 * PRIVATE METHODS
	 */

	protected void initGUI()
	{
		try
		{
			this.setPreferredSize( new java.awt.Dimension( 300, 461 ) );
			layout = new SpringLayout();
			setLayout( layout );
			{
				jLabel1 = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabel1, 10, SpringLayout.NORTH, this );
				layout.putConstraint( SpringLayout.WEST, jLabel1, 5, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, jLabel1, -5, SpringLayout.EAST, this );
				this.add( jLabel1 );
				jLabel1.setText( "Settings for detector:" );
				jLabel1.setFont( FONT );
			}
			{
				jLabelSegmenterName = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelSegmenterName, 10, SpringLayout.SOUTH, jLabel1 );
				layout.putConstraint( SpringLayout.WEST, jLabelSegmenterName, 11, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, jLabelSegmenterName, -11, SpringLayout.EAST, this );
				this.add( jLabelSegmenterName );
				jLabelSegmenterName.setFont( BIG_FONT );
				jLabelSegmenterName.setText( detectorName );
			}
			{
				jLabelHelpText = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelHelpText, 60, SpringLayout.SOUTH, jLabelSegmenterName );
				layout.putConstraint( SpringLayout.WEST, jLabelHelpText, 10, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, jLabelHelpText, -10, SpringLayout.EAST, this );
				this.add( jLabelHelpText );
				jLabelHelpText.setFont( FONT.deriveFont( Font.ITALIC ) );
				jLabelHelpText.setText( infoText.replace( "<br>", "" ).replace( "<p>", "<p align=\"justify\">" ).replace( "<html>", "<html><p align=\"justify\">" ) );
			}
/* X column */
			{
				jLabelXcolumn = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelXcolumn, 10, SpringLayout.SOUTH, jLabelHelpText );
				layout.putConstraint( SpringLayout.WEST, jLabelXcolumn, 16, SpringLayout.WEST, this );
				this.add( jLabelXcolumn );
				jLabelXcolumn.setText( "X column:" );
				jLabelXcolumn.setFont( FONT );

			}
			{
				jTextFieldXcolumn = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldXcolumn, 10, SpringLayout.SOUTH, jLabelHelpText );
				layout.putConstraint( SpringLayout.WEST, jTextFieldXcolumn, 16, SpringLayout.EAST, jLabelXcolumn );
				jTextFieldXcolumn.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldXcolumn.setColumns( 5 );
				jTextFieldXcolumn.setText( "5" );
				this.add( jTextFieldXcolumn );
				jTextFieldXcolumn.setFont( FONT );
			}

/* Y column */
			{
				jLabelYcolumn = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelYcolumn, 10, SpringLayout.SOUTH, jLabelXcolumn );
				layout.putConstraint( SpringLayout.WEST, jLabelYcolumn, 16, SpringLayout.WEST, this );
				this.add( jLabelYcolumn );
				jLabelYcolumn.setText( "Y column:" );
				jLabelYcolumn.setFont( FONT );

			}
			{
				jTextFieldYcolumn = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldYcolumn, 10, SpringLayout.SOUTH, jTextFieldXcolumn );
				layout.putConstraint( SpringLayout.WEST, jTextFieldYcolumn, 16, SpringLayout.EAST, jLabelXcolumn );
				jTextFieldYcolumn.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldYcolumn.setColumns( 5 );
				jTextFieldYcolumn.setText( "5" );
				this.add( jTextFieldYcolumn );
				jTextFieldYcolumn.setFont( FONT );
			}
/* Z column */
			{
				jLabelZcolumn = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelZcolumn, 10, SpringLayout.SOUTH, jLabelYcolumn );
				layout.putConstraint( SpringLayout.WEST, jLabelZcolumn, 16, SpringLayout.WEST, this );
				this.add( jLabelZcolumn );
				jLabelZcolumn.setText( "Z column:" );
				jLabelZcolumn.setFont( FONT );

			}
			{
				jTextFieldZcolumn = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldZcolumn, 10, SpringLayout.SOUTH, jTextFieldYcolumn );
				layout.putConstraint( SpringLayout.WEST, jTextFieldZcolumn, 16, SpringLayout.EAST, jLabelZcolumn );
				jTextFieldZcolumn.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldZcolumn.setColumns( 5 );
				jTextFieldZcolumn.setText( "5" );
				this.add( jTextFieldZcolumn );
				jTextFieldZcolumn.setFont( FONT );
			}
/* A column */
			{
				jLabelAcolumn = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelAcolumn, 10, SpringLayout.SOUTH, jLabelZcolumn );
				layout.putConstraint( SpringLayout.WEST, jLabelAcolumn, 16, SpringLayout.WEST, this );
				this.add( jLabelAcolumn );
				jLabelAcolumn.setText( "Area column:" );
				jLabelAcolumn.setFont( FONT );

			}
			{
				jTextFieldAcolumn = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldAcolumn, 10, SpringLayout.SOUTH, jTextFieldZcolumn );
				layout.putConstraint( SpringLayout.WEST, jTextFieldAcolumn, 16, SpringLayout.EAST, jLabelAcolumn );
				jTextFieldAcolumn.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldAcolumn.setColumns( 5 );
				jTextFieldAcolumn.setText( "5" );
				this.add( jTextFieldAcolumn );
				jTextFieldAcolumn.setFont( FONT );
			}
/* I column */
			{
				jLabelIcolumn = new JLabel();
				layout.putConstraint( SpringLayout.NORTH, jLabelIcolumn, 10, SpringLayout.SOUTH, jLabelAcolumn );
				layout.putConstraint( SpringLayout.WEST, jLabelIcolumn, 16, SpringLayout.WEST, this );
				this.add( jLabelIcolumn );
				jLabelIcolumn.setText( "Intensity column:" );
				jLabelIcolumn.setFont( FONT );

			}
			{
				jTextFieldIcolumn = new JNumericTextField();
				layout.putConstraint( SpringLayout.NORTH, jTextFieldIcolumn, 10, SpringLayout.SOUTH, jTextFieldAcolumn );
				layout.putConstraint( SpringLayout.WEST, jTextFieldIcolumn, 16, SpringLayout.EAST, jLabelIcolumn );
				jTextFieldIcolumn.setHorizontalAlignment( SwingConstants.CENTER );
				jTextFieldIcolumn.setColumns( 5 );
				jTextFieldIcolumn.setText( "5" );
				this.add( jTextFieldIcolumn );
				jTextFieldIcolumn.setFont( FONT );
			}
/* Folder */
			{
				infoFolder = new JTextField();
				layout.putConstraint( SpringLayout.NORTH, infoFolder, 10, SpringLayout.SOUTH, jLabelIcolumn );
				layout.putConstraint( SpringLayout.WEST, infoFolder, 16, SpringLayout.WEST, this );
				layout.putConstraint( SpringLayout.EAST, infoFolder, 241, SpringLayout.WEST, this );
				this.add( infoFolder );
				infoFolder.setText( "Folder " );
				infoFolder.setFont( FONT );
			}
			{
				lblSegmentInChannel = new JLabel( "Segment in channel:" );
				layout.putConstraint( SpringLayout.NORTH, lblSegmentInChannel, 10, SpringLayout.SOUTH, infoFolder );
				layout.putConstraint( SpringLayout.WEST, lblSegmentInChannel, 16, SpringLayout.WEST, this );
				lblSegmentInChannel.setFont( SMALL_FONT );
				add( lblSegmentInChannel );

				sliderChannel = new JSlider();
				layout.putConstraint( SpringLayout.NORTH, sliderChannel, 10, SpringLayout.SOUTH, infoFolder );
				layout.putConstraint( SpringLayout.WEST, sliderChannel, 16, SpringLayout.EAST, lblSegmentInChannel );
				sliderChannel.addChangeListener( new ChangeListener()
				{
					@Override
					public void stateChanged( final ChangeEvent e )
					{
						labelChannel.setText( "" + sliderChannel.getValue() );
					}
				} );
				add( sliderChannel );

				labelChannel = new JLabel( "1" );
				layout.putConstraint( SpringLayout.NORTH, labelChannel, 10, SpringLayout.SOUTH, sliderChannel );
				layout.putConstraint( SpringLayout.WEST, labelChannel, 16, SpringLayout.WEST, this );
				labelChannel.setHorizontalAlignment( SwingConstants.CENTER );
				labelChannel.setFont( SMALL_FONT );
				add( labelChannel );
			}
			{
				btnSelect = new JButton( "Select directory with frames" );
				layout.putConstraint( SpringLayout.NORTH, btnSelect, 10, SpringLayout.SOUTH, labelChannel );
				layout.putConstraint( SpringLayout.WEST, btnSelect, 16, SpringLayout.WEST, this );
				this.add( btnSelect );
				btnSelect.setFont( SMALL_FONT );
				btnSelect.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						select();
					}
				} );
			}
			{

				// Deal with channels: the slider and channel labels are only
				// visible if we find more than one channel.
				final int n_channels = imp.getNChannels();
				sliderChannel.setMaximum( n_channels );
				sliderChannel.setMinimum( 1 );
				sliderChannel.setValue( imp.getChannel() );

				if ( n_channels <= 1 )
				{
					labelChannel.setVisible( false );
					lblSegmentInChannel.setVisible( false );
					sliderChannel.setVisible( false );
				}
				else
				{
					labelChannel.setVisible( true );
					lblSegmentInChannel.setVisible( true );
					sliderChannel.setVisible( true );
				}
			}
			{
				final JLabelLogger labelLogger = new JLabelLogger();
				layout.putConstraint( SpringLayout.NORTH, labelLogger, 10, SpringLayout.SOUTH, btnSelect );
				layout.putConstraint( SpringLayout.WEST, labelLogger, 16, SpringLayout.WEST, this );
				add( labelLogger );
				localLogger = labelLogger.getLogger();
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}
	}

        /*
	 * PRIVATE METHODS
	 */

        /**
	 * Fill the text field for directory.
	 */
	private void select()
	{
                File file;
                File selectedFolder = null;
                try {
                    file = new File(System.getProperty("user.dir"));
                    selectedFolder = askForFolder( file, "Select directory with frames", null, localLogger );
                    if ( selectedFolder != null )
                    {
                        final String folder = selectedFolder.getCanonicalPath();
                        infoFolder.setText( folder );
                    }
                } catch (Exception e) { 
                    e.printStackTrace(); 
                }
	}
}
