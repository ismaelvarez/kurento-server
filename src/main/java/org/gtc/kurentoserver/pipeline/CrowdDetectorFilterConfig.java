package org.gtc.kurentoserver.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.kurento.client.MediaPipeline;
import org.kurento.module.crowddetector.CrowdDetectorFilter;
import org.kurento.module.crowddetector.RegionOfInterest;
import org.kurento.module.crowddetector.RegionOfInterestConfig;
import org.kurento.module.crowddetector.RelativePoint;

public class CrowdDetectorFilterConfig {
    public static CrowdDetectorFilter create(MediaPipeline pipeline, List<RegionOfInterest> rois) {
        return new CrowdDetectorFilter.Builder(pipeline, rois).build();
    }


    public static List<RegionOfInterest> getGTCCamROI() {
        List<RelativePoint> points = new ArrayList<>();
        points.add(new RelativePoint(0.57377f, 0.69397f));
        points.add(new RelativePoint(0.43115f, 0.6506f));
        points.add(new RelativePoint(0.25902f, 0.655421f));
        points.add(new RelativePoint(0.f, 0.8072289f));
        points.add(new RelativePoint(0.f, 0.874698f));
        points.add(new RelativePoint(0.f, 1.f));
        points.add(new RelativePoint(1.f, 1.f));
        points.add(new RelativePoint(1.f, 0.76385f));
        points.add(new RelativePoint(0.61803f, 0.35180f));
        points.add(new RelativePoint(0.57377f, 0.69397f));

        RegionOfInterestConfig config = new RegionOfInterestConfig();

        config.setFluidityLevelMin(0);
        config.setFluidityLevelMed(52);
        config.setFluidityLevelMax(100);
        config.setFluidityNumFramesToEvent(5);
        config.setOccupancyLevelMin(10);
        config.setOccupancyLevelMed(35);
        config.setOccupancyLevelMax(65);
        config.setOccupancyNumFramesToEvent(5);

        config.setSendOpticalFlowEvent(false);

        config.setOpticalFlowNumFramesToEvent(3);
        config.setOpticalFlowNumFramesToReset(3);
        config.setOpticalFlowAngleOffset(0);

        List<RegionOfInterest> rois = new ArrayList<>();
        rois.add(new RegionOfInterest(points, config, "dummyRoy"));

        return rois;
    }

    public static List<RegionOfInterest> getValenciaROIs() {
        List<RelativePoint> points = new ArrayList<>();
        points.add(new RelativePoint(0.52459f, 0.2321f));
        points.add(new RelativePoint(0.f, 0.53145f));
        points.add(new RelativePoint(0.f, 0.80260f));
        points.add(new RelativePoint(0.27377f, 0.87201f));
        points.add(new RelativePoint(0.5082f, 0.89587f));
        points.add(new RelativePoint(0.75902f, 0.87418f));
        points.add(new RelativePoint(1.f, 0.82429f));
        points.add(new RelativePoint(1.f, 0.42950f));
        points.add(new RelativePoint(0.75082f, 0.35357f));
        points.add(new RelativePoint(0.72623f, 0.29284f));
        points.add(new RelativePoint(0.75246f, 0.25162f));
        points.add(new RelativePoint(0.72787f, 0.24511f));
        points.add(new RelativePoint(0.67049f, 0.29718f));
        points.add(new RelativePoint(0.65246f, 0.35357f));
        points.add(new RelativePoint(0.60328f, 0.35357f));
        points.add(new RelativePoint(0.63607f, 0.438177f));
        points.add(new RelativePoint(0.6918f, 0.44468f));
        points.add(new RelativePoint(0.77705f, 0.47505f));
        points.add(new RelativePoint(0.8082f, 0.55531f));
        points.add(new RelativePoint(0.80492f, 0.68980f));
        points.add(new RelativePoint(0.68689f, 0.77223f));
        points.add(new RelativePoint(0.51475f, 0.80043f));
        points.add(new RelativePoint(0.33279f, 0.76138f));
        points.add(new RelativePoint(0.22131f, 0.65292f));
        points.add(new RelativePoint(0.23934f, 0.51193f));
        points.add(new RelativePoint(0.3f, 0.43817f));
        points.add(new RelativePoint(0.41148f, 0.40130f));
        points.add(new RelativePoint(0.50984f, 0.39045f));
        points.add(new RelativePoint(0.50984f, 0.35574f));
        points.add(new RelativePoint(0.42787f, 0.36442f));
        points.add(new RelativePoint(0.39508f, 0.33839f));
        points.add(new RelativePoint(0.54754f, 0.23427f));
        points.add(new RelativePoint(0.52459f, 0.23210f));

        RegionOfInterestConfig config = new RegionOfInterestConfig();

        config.setFluidityLevelMin(0);
        config.setFluidityLevelMed(52);
        config.setFluidityLevelMax(100);
        config.setFluidityNumFramesToEvent(1);
        config.setOccupancyLevelMin(10);
        config.setOccupancyLevelMed(35);
        config.setOccupancyLevelMax(65);
        config.setOccupancyNumFramesToEvent(5);

        config.setSendOpticalFlowEvent(false);

        config.setOpticalFlowNumFramesToEvent(3);
        config.setOpticalFlowNumFramesToReset(3);
        config.setOpticalFlowAngleOffset(0);

        List<RegionOfInterest> rois = new ArrayList<>();
        rois.add(new RegionOfInterest(points, config, "dummyRoy"));

        return rois;
    }

    public static List<RegionOfInterest> getPlazaROI() {
        List<RelativePoint> points = new ArrayList<>();
        points.add(new RelativePoint(0.f, 0.f));
        points.add(new RelativePoint(0.f, 1.f));
        points.add(new RelativePoint(1.f, 1.f));
        points.add(new RelativePoint(1.f, 0.f));
        points.add(new RelativePoint(0.f, 0.f));

        RegionOfInterestConfig config = new RegionOfInterestConfig();

        config.setFluidityLevelMin(0);
        config.setFluidityLevelMed(52);
        config.setFluidityLevelMax(100);
        config.setFluidityNumFramesToEvent(5);
        config.setOccupancyLevelMin(10);
        config.setOccupancyLevelMed(35);
        config.setOccupancyLevelMax(65);
        config.setOccupancyNumFramesToEvent(5);

        config.setSendOpticalFlowEvent(false);

        config.setOpticalFlowNumFramesToEvent(3);
        config.setOpticalFlowNumFramesToReset(3);
        config.setOpticalFlowAngleOffset(0);

        List<RegionOfInterest> rois = new ArrayList<>();
        rois.add(new RegionOfInterest(points, config, "dummyRoy"));

        return rois;
    }
}
