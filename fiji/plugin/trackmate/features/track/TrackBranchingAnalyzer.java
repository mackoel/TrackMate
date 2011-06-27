package fiji.plugin.trackmate.features.track;

import java.util.HashSet;
import java.util.Set;

import fiji.plugin.trackmate.Spot;
import fiji.plugin.trackmate.TrackCollection;
import fiji.plugin.trackmate.TrackFeature;
import fiji.plugin.trackmate.util.TrackSplitter;

public class TrackBranchingAnalyzer implements TrackFeatureAnalyzer {

	@Override
	public void process(final TrackCollection tracks) {
		for (int i = 0; i < tracks.size(); i++) {
			final Set<Spot> track = tracks.getTrackSpots(i);
			int nmerges = 0;
			int nsplits = 0;
			int ncomplex = 0;
			for (Spot spot : track) {
				int type = TrackSplitter.getVertexType(tracks, spot);
				switch(type) {
				case TrackSplitter.MERGING_POINT:
				case TrackSplitter.MERGING_END:
					nmerges++;
					break;
				case TrackSplitter.SPLITTING_START:
				case TrackSplitter.SPLITTING_POINT:
					nsplits++;
					break;
				case TrackSplitter.COMPLEX_POINT:
					ncomplex++;
					break;
				}
			}
			// Put feature data
			tracks.putFeature(i, TrackFeature.NUMBER_SPLITS, (float) nsplits);
			tracks.putFeature(i, TrackFeature.NUMBER_MERGES, (float) nmerges);
			tracks.putFeature(i, TrackFeature.NUMBER_COMPLEX, (float) ncomplex);
			tracks.putFeature(i, TrackFeature.NUMBER_SPOTS, (float) track.size());
		}

	}


	@Override
	public Set<TrackFeature> getFeatures() {
		Set<TrackFeature> featureList = new HashSet<TrackFeature>(3);
		featureList.add(TrackFeature.NUMBER_SPLITS);
		featureList.add(TrackFeature.NUMBER_MERGES);
		featureList.add(TrackFeature.NUMBER_COMPLEX);
		featureList.add(TrackFeature.NUMBER_SPOTS);
		return featureList ;
	}

}