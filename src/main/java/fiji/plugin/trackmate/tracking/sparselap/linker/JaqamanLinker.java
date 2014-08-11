package fiji.plugin.trackmate.tracking.sparselap.linker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.imglib2.algorithm.BenchmarkAlgorithm;
import net.imglib2.algorithm.OutputAlgorithm;
import net.imglib2.util.Util;
import fiji.plugin.trackmate.Logger;
import fiji.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;
import fiji.plugin.trackmate.tracking.sparselap.costmatrix.CostMatrixCreator;

/**
 * Links two lists of objects based on the LAP framework described in Jaqaman
 * <i>et al.</i>, Nature Methods, <b>2008</b>.
 * 
 * @author Jean-Yves Tinevez - 2014
 * 
 * @param <K>
 *            the type of the source objects to link.
 * @param <J>
 *            the type of the target objects to link.
 */
public class JaqamanLinker< K extends Comparable< K >, J extends Comparable< J > > extends BenchmarkAlgorithm implements OutputAlgorithm< Map< K, J > >
{
	private Map< K, J > assignments;

	private Map< K, Double > costs;

	private final CostMatrixCreator< K, J > costMatrixCreator;

	private final Logger logger;

	/**
	 * Creates a new linker for the two specified object lists.
	 * 
	 * @param sources
	 *            the source objects.
	 * @param targets
	 *            the target objects.
	 * @param costFunction
	 *            a {@link CostFunction} that can compute a cost to link any
	 *            source to any target.
	 * @param costThreshold
	 *            the cost threshold above which linking will be forbidden.
	 * @param alternativeCostFactor
	 *            the Jaqaman et al. 2008 alternative cost factor, required to
	 *            build the cost for a link <b>not to happen</b>.
	 * @see {Jaqaman <i>et al.</i>, Nature Methods, <b>2008</b>, Figure 1b.}
	 */
	public JaqamanLinker( final CostMatrixCreator< K, J > costMatrixCreator, final Logger logger )
	{
		this.costMatrixCreator = costMatrixCreator;
		this.logger = logger;
	}

	public JaqamanLinker( final CostMatrixCreator< K, J > costMatrixCreator )
	{
		this( costMatrixCreator, Logger.VOID_LOGGER );
	}

	/**
	 * Returns the resulting assignments from this algorithm.
	 * <p>
	 * It takes the shape of a map, such that if <code>source</code> is a key of
	 * the map, it is assigned to <code>target = map.get(source)</code>.
	 * 
	 * @return the assignment map.
	 * @see #getAssignmentCosts()
	 */
	@Override
	public Map< K, J > getResult()
	{
		return assignments;
	}

	/**
	 * Returns the costs associated to the assignment results.
	 * <p>
	 * It takes the shape of a map, such that if <code>source</code> is a key of
	 * the map, its assignment as a cost <code>cost = map.get(source)</code>.
	 * 
	 * @return the assignment costs.
	 * @see #getResult()
	 */
	public Map< K, Double > getAssignmentCosts()
	{
		return costs;
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		final long start = System.currentTimeMillis();

		/*
		 * Generate the cost matrix
		 */

		logger.setStatus( "Creating the main cost matrix..." );
		if ( !costMatrixCreator.checkInput() || !costMatrixCreator.process() )
		{
			errorMessage = costMatrixCreator.getErrorMessage();
			return false;
		}
		logger.setProgress( 0.5 );

		final SparseCostMatrix tl = costMatrixCreator.getResult();
		final List< K > matrixRows = costMatrixCreator.getSourceList();
		final List< J > matrixCols = costMatrixCreator.getTargetList();

		/*
		 * Complement the cost matrix with alternative no linking cost matrix.
		 */

		logger.setStatus( "Completing the cost matrix..." );

		final int nCols = tl.getNCols();
		final int nRows = tl.getNRows();

		/*
		 * Top right
		 */

		final double[] cctr = new double[ nRows ];
		final int[] kktr = new int[ nRows ];
		for ( int i = 0; i < nRows; i++ )
		{
			kktr[ i ] = i;
			cctr[ i ] = costMatrixCreator.getAlternativeCostForSource( matrixRows.get( i ) );
		}
		final int[] numbertr = new int[ nRows ];
		Arrays.fill( numbertr, 1 );
		final SparseCostMatrix tr = new SparseCostMatrix( cctr, kktr, numbertr, nRows );

		/*
		 * Bottom left
		 */
		final double[] ccbl = new double[ nCols ];
		final int[] kkbl = new int[ nCols ];
		for ( int i = 0; i < kkbl.length; i++ )
		{
			kkbl[ i ] = i;
			ccbl[ i ] = costMatrixCreator.getAlternativeCostForTarget( matrixCols.get( i ) );
		}
		final int[] numberbl = new int[ nCols ];
		Arrays.fill( numberbl, 1 );
		final SparseCostMatrix bl = new SparseCostMatrix( ccbl, kkbl, numberbl, nCols );

		/*
		 * Bottom right.
		 * 
		 * Alt. cost is the overall min of alternative costs. This deviate or
		 * extend a bit the u-track code.
		 */
		final double minCost = Math.min( Util.computeMin( ccbl ), Util.computeMin( cctr ) );
		final SparseCostMatrix br = tl.transpose();
		br.fillWith( minCost );

		/*
		 * Stitch them together
		 */
		final SparseCostMatrix full = ( tl.hcat( tr ) ).vcat( bl.hcat( br ) );
		logger.setProgress( 0.6 );

		/*
		 * Solve the full cost matrix.
		 */
		logger.setStatus( "Solving the cost matrix..." );
		final LAPJV solver = new LAPJV( full );
		if ( !solver.checkInput() || !solver.process() )
		{
			errorMessage = solver.getErrorMessage();
			return false;
		}

		final int[] assgn = solver.getResult();
		assignments = new HashMap< K, J >();
		costs = new HashMap< K, Double >();
		for ( int i = 0; i < assgn.length; i++ )
		{
			final int j = assgn[ i ];
			if ( i < matrixRows.size() && j < matrixCols.size() )
			{
				final K source = matrixRows.get( i );
				final J target = matrixCols.get( j );
				assignments.put( source, target );

				final double cost = full.get( i, j, Double.POSITIVE_INFINITY );
				costs.put( source, Double.valueOf( cost ) );
			}
		}

		logger.setProgress( 1 );
		logger.setStatus( "" );
		final long end = System.currentTimeMillis();
		processingTime = end - start;

		return true;
	}

	public String resultToString()
	{
		if ( null == assignments ) { return "Not solved yet. Process the algorithm prior to calling this method."; }

		final HashSet< K > unassignedSources = new HashSet< K >( costMatrixCreator.getSourceList() );
		final HashSet< J > unassignedTargets = new HashSet< J >( costMatrixCreator.getTargetList() );

		int sw = -1;
		for ( final K source : unassignedSources )
		{
			if ( source.toString().length() > sw )
			{
				sw = source.toString().length();
			}
		}
		sw = sw + 1;

		int tw = -1;
		for ( final J target : unassignedTargets )
		{
			if ( target.toString().length() > tw )
			{
				tw = target.toString().length();
			}
		}
		tw = tw + 1;

		int cw = -1;
		for ( final K source : assignments.keySet() )
		{
			final double cost = costs.get( source ).doubleValue();
			if ( Math.log10( cost ) > cw )
			{
				cw = ( int ) Math.log10( cost );
			}
		}
		cw = cw + 1;

		final StringBuilder str = new StringBuilder();
		str.append( "Found " + assignments.size() + " assignments:\n" );
		for ( final K source : assignments.keySet() )
		{
			final J target = assignments.get( source );

			unassignedSources.remove( source );
			unassignedTargets.remove( target );

			final double cost = costs.get( source ).doubleValue();
			str.append( String.format( "%1$-" + sw + "s → %2$" + tw + "s, cost = %3$" + cw + ".1f\n", source.toString(), target.toString(), cost ) );
		}

		if ( !unassignedSources.isEmpty() )
		{
			str.append( "Found " + unassignedSources.size() + " unassigned sources:\n" );
			for ( final K us : unassignedSources )
			{
				str.append( String.format( "%1$-" + sw + "s → %2$" + tw + "s\n", us.toString(), 'ø' ) );
			}
		}

		if ( !unassignedTargets.isEmpty() )
		{
			str.append( "Found " + unassignedTargets.size() + " unassigned targets:\n" );
			for ( final J ut : unassignedTargets )
			{
				str.append( String.format( "%1$-" + sw + "s → %2$" + tw + "s\n", 'ø', ut.toString() ) );
			}
		}

		return str.toString();
	}
}
